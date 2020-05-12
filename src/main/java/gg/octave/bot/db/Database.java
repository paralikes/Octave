package gg.octave.bot.db;

import com.rethinkdb.gen.exc.ReqlDriverError;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;
import gg.octave.bot.Launcher;
import gg.octave.bot.db.guilds.GuildData;
import gg.octave.bot.db.guilds.UserData;
import gg.octave.bot.db.premium.PremiumGuild;
import gg.octave.bot.db.premium.PremiumUser;
import gg.octave.bot.entities.BotCredentials;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

import javax.annotation.Nullable;
import java.util.List;

import static com.rethinkdb.RethinkDB.r;

public class Database {
    private static final Logger LOG = LoggerFactory.getLogger("Database");
    private Connection conn;
    private static JedisPool defaultJedisPool;
    private Config config = new Config();
    private RedissonClient redisson;

    public Database(String name) {
        BotCredentials creds = Launcher.INSTANCE.getCredentials();

        String redisHost = creds.getRedisHost();
        int redisPort = creds.getRedisPort();
        String redisAuth = creds.getRedisAuth();

        SingleServerConfig ssc = config.useSingleServer();
        ssc.setAddress("redis://" + redisHost + ":" + redisPort);

        if (redisAuth != null && !redisAuth.isEmpty()) {
            defaultJedisPool = new JedisPool(new GenericObjectPoolConfig(), redisHost, redisPort, 2000, redisAuth);
            ssc.setPassword(redisAuth);
        } else {
            defaultJedisPool = new JedisPool(redisHost, redisPort);
        }

        redisson = Redisson.create(config);

        try {
            Connection.Builder builder = r.connection()
                    .hostname(creds.getRethinkHost())
                    .port(creds.getRethinkPort());

            String rethinkUser = creds.getRethinkUsername();
            String rethinkAuth = creds.getRethinkAuth();

            if (rethinkAuth != null && !rethinkAuth.isEmpty()) {
                if (rethinkUser != null && !rethinkUser.isEmpty()) {
                    builder.user(creds.getRethinkUsername(), rethinkAuth);
                } else {
                    builder.authKey(rethinkAuth);
                }
            }

            conn = builder.connect();

            if (!r.dbList().<List<String>>run(conn).contains(name)) {
                r.dbCreate(name).run(conn);
            }

            LOG.info("Connected to database.");
            conn.use(name);
        } catch (ReqlDriverError e) {
            conn = null;
            LOG.error("Rethink Database connection failed.", e);
            System.exit(0);
        }
    }

    public Connection getConn() {
        return conn;
    }

    public boolean isOpen() {
        return conn != null && conn.isOpen();
    }

    public void close() {
        conn.close();
    }

    @Nullable
    public GuildData getGuildData(String id) {
        return get("guilds_v2", id, GuildData.class);
    }

    @Nullable
    public PremiumKey getPremiumKey(String id) {
        return get("keys", id, PremiumKey.class);
    }

    public UserData getUserData(String id) {
        return get("users", id, UserData.class);
    }

    @Nullable
    public PatreonEntry getPatreonEntry(String id) {
        return get("patreon", id, PatreonEntry.class);
    }

    public boolean hasPremiumUser(String id) {
        return isOpen() ? r.table("premiumusers").get(id).coerceTo("bool").run(conn) : false;
    }

    public PremiumUser getPremiumUser(String id) {
        return !isOpen() ? null : r.table("premiumusers")
                .get(id)
                .default_(r.hashMap("id", id).with("pledgeAmount", "0.0"))
                .run(conn, PremiumUser.class);
    }

    public List<PremiumUser> getPremiumUsers() {
        if (!isOpen()) {
            return List.of();
        }

        Cursor<PremiumUser> cursor = r.table("premiumusers").run(conn, PremiumUser.class);
        return cursor.toList();
    }

    @Nullable
    public PremiumGuild getPremiumGuild(String id) {
        return get("premiumguilds", id, PremiumGuild.class);
    }

    @Nullable
    public Cursor<PremiumGuild> getPremiumGuilds(String id) {
        return isOpen()
                ? r.table("premiumguilds").filter(guild -> guild.g("redeemer").eq(id)).run(conn, PremiumGuild.class)
                : null;
    }

    @Nullable
    public <T> T get(String table, String id, Class<T> cls) {
        return isOpen() ? r.table(table).get(id).run(conn, cls) : null;
    }

    public static JedisPool getDefaultJedisPool() {
        return defaultJedisPool;
    }

    public RedissonClient getRedisson() {
        return redisson;
    }
}
