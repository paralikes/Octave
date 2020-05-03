package xyz.gnarbot.gnar.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup;
import com.sedmelluq.lava.extensions.youtuberotator.planner.AbstractRoutePlanner;
import com.sedmelluq.lava.extensions.youtuberotator.planner.RotatingNanoIpRoutePlanner;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.IpBlock;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block;
import io.sentry.Sentry;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.gnarbot.gnar.Launcher;
import xyz.gnarbot.gnar.db.OptionsRegistry;
import xyz.gnarbot.gnar.entities.BotCredentials;
import xyz.gnarbot.gnar.entities.Configuration;
import xyz.gnarbot.gnar.music.sources.caching.CachingSourceManager;
import xyz.gnarbot.gnar.music.sources.spotify.SpotifyAudioSourceManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerRegistry {
    private final Logger LOG = LoggerFactory.getLogger("PlayerRegistry");

    private final Map<Long, MusicManager> registry;

    private final Launcher bot;
    private final ScheduledExecutorService executor;
    private final AudioPlayerManager playerManager;

    public PlayerRegistry(Launcher bot, ScheduledExecutorService executor) {
        this.bot = bot;
        this.executor = executor;

        registry = new ConcurrentHashMap<>(bot.getConfiguration().getMusicLimit());
        executor.scheduleAtFixedRate(() -> clear(false), 20, 10, TimeUnit.MINUTES);

        this.playerManager = new DefaultAudioPlayerManager();
        playerManager.setFrameBufferDuration(5000);
        playerManager.getConfiguration().setFilterHotSwapEnabled(true);
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);

        YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager(true);

        Configuration config = bot.getConfiguration();
        BotCredentials credentials = bot.getCredentials();
        if (!config.getIpv6Block().isEmpty()) {
            AbstractRoutePlanner planner;
            String block = config.getIpv6Block();
            List<IpBlock> blocks = Collections.singletonList(new Ipv6Block(block));

            if (config.getIpv6Exclude().isEmpty())
                planner = new RotatingNanoIpRoutePlanner(blocks);
            else {
                try {
                    InetAddress blacklistedGW = InetAddress.getByName(config.getIpv6Exclude());
                    planner = new RotatingNanoIpRoutePlanner(blocks, inetAddress -> !inetAddress.equals(blacklistedGW));
                } catch (Exception ex) {
                    planner = new RotatingNanoIpRoutePlanner(blocks);
                    Sentry.capture(ex);
                    ex.printStackTrace();
                }
            }

            new YoutubeIpRotatorSetup(planner)
                    .forSource(youtubeAudioSourceManager)
                    .setup();
        }

        SpotifyAudioSourceManager spotifyAudioSourceManager = new SpotifyAudioSourceManager(
                credentials.getSpotifyClientId(),
                credentials.getSpotifyClientSecret(),
                youtubeAudioSourceManager
        );

        playerManager.registerSourceManager(new CachingSourceManager());
        playerManager.registerSourceManager(spotifyAudioSourceManager);
        playerManager.registerSourceManager(youtubeAudioSourceManager);
        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        playerManager.registerSourceManager(new GetyarnAudioSourceManager());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new BeamAudioSourceManager());
    }

    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    @Nonnull
    public MusicManager get(Guild guild) throws MusicLimitException {
        MusicManager manager = registry.get(guild.getIdLong());

        if (manager == null) {
            if (size() >= bot.getConfiguration().getMusicLimit() && !OptionsRegistry.INSTANCE.ofGuild(guild).isPremium()) {
                throw new MusicLimitException();
            }

            manager = new MusicManager(bot, guild.getId(), this, playerManager);
            registry.put(guild.getIdLong(), manager);
        }

        return manager;
    }

    @Nullable
    public MusicManager getExisting(long id) {
        return registry.get(id);
    }

    @Nullable
    public MusicManager getExisting(Guild guild) {
        return getExisting(guild.getIdLong());
    }

    public void destroy(long id) {
        MusicManager manager = registry.get(id);
        if (manager != null) {
            manager.destroy();
            registry.remove(id);
        }
    }

    public void destroy(Guild guild) {
        destroy(guild.getIdLong());
    }

    public boolean contains(long id) {
        return registry.containsKey(id);
    }

    public boolean contains(Guild guild) {
        return registry.containsKey(guild.getIdLong());
    }

    public void shutdown() {
        clear(true);
    }

    public void clear(boolean force) {
        LOG.info("Cleaning up players (forceful: " + force + ")");
        Iterator<Map.Entry<Long, MusicManager>> iterator = registry.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Long, MusicManager> entry = iterator.next();
            try {
                //Guild was long gone, dangling manager,
                MusicManager musicManager = entry.getValue();
                if(Launcher.INSTANCE.getShardManager().getGuildById(musicManager.getGuildId()) == null) {
                    iterator.remove();
                    return;
                }

                if (force || !musicManager.getGuild().getSelfMember().getVoiceState().inVoiceChannel()
                        || musicManager.getPlayer().getPlayingTrack() == null) {
                    LOG.debug("Cleaning player {}", musicManager.getGuild().getId());

                    musicManager.getScheduler().getQueue().clear();
                    musicManager.destroy();
                    iterator.remove();
                }
            } catch (Exception e) {
                LOG.warn("Exception occured while trying to clean up id " + entry.getKey(), e);
            }
        }

        LOG.info("Finished cleaning up players.");
    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public Map<Long, MusicManager> getRegistry() {
        return registry;
    }

    public int size() {
        return registry.size();
    }
}
