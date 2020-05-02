package xyz.gnarbot.gnar;


//public class Bot {
//    private final DiscordFM discordFM;
//    private final PatreonAPI patreon;
//    final StatsPoster statsPoster;
//
//    public Bot() throws LoginException {
//        LOG.info("Name  :\t" + configuration.getName());
//        LOG.info("Shards:\t" + this.credentials.getTotalShards());
//        LOG.info("Prefix:\t" + configuration.getPrefix());
//        LOG.info("Admins:\t" + configuration.getAdmins());
//        LOG.info("JDA v.:\t" + JDAInfo.VERSION);
//
//        LOG.info("The bot is now connecting to Discord. Bucket factor: {}", configuration.getBucketFactor());
//
//        // SETUP APIs
//        discordFM = new DiscordFM();
//
//        patreon = new PatreonAPI(credentials.getPatreonAccessToken());
//        LOG.info("Patreon Established.");
//
//        statsPoster = new StatsPoster("201503408652419073"); // Config option? @Kodehawa
//        statsPoster.postEvery(30, TimeUnit.MINUTES);
//    }
//}
