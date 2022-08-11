package net.suqatri.redicloud.discord;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.suqatri.redicloud.discord.configuration.BotConfig;
import net.suqatri.redicloud.discord.configuration.RedisConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.Arrays;

@Getter
public class DiscordBot {

    public static void main(String[] args) {
        instance = new DiscordBot(args);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(instance == null) return;
            instance.shutdownBot(true);
        }, "DiscordBotShutdownHook"));
    }

    @Getter
    private static DiscordBot instance;

    private boolean isShutdownInitiated = false;
    private JDA jda;
    private final String[] args;
    private BotConfig botConfig;
    private final RedisConfig redisConfig;
    private RedissonClient redissonClient;

    private DiscordBot(String[] args){
        this.args = args;
        this.redisConfig = new RedisConfig();
        this.handleArguments();
        this.initRedis();
        startBot();
    }

    private void handleArguments(){
        for(String arg : args){
            switch (arg.toLowerCase().split("=")[0].replaceAll("--", "")){
                case "password": {
                    this.redisConfig.setPassword(arg.split("=")[1]);
                    break;
                }
                case "port": {
                    this.redisConfig.setPort(Integer.parseInt(arg.split("=")[1]));
                    break;
                }
                case "host":
                case "hostname": {
                    this.redisConfig.setHostname(arg.split("=")[1]);
                    break;
                }
                case "dbid":
                case "databaseid": {
                    this.redisConfig.setDatabaseId(Integer.parseInt(arg.split("=")[1]));
                    break;
                }
            }
        }
    }

    private void startBot(){
        JDABuilder builder = JDABuilder.create(this.botConfig.getToken(), Arrays.asList(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_INVITES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS));
        builder.setActivity(Activity.watching("https://github.com/RediCloud/"));
        builder.setAutoReconnect(true);
        builder.setStatus(OnlineStatus.ONLINE);

        try {
            this.jda = builder.build();
            this.jda.awaitReady();

            this.registerCommands();
        } catch (Exception e) {
            e.printStackTrace();
            shutdownBot(false);
        }
    }

    private void initRedis(){
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + this.redisConfig.getHostname() + ":" + this.redisConfig.getPort())
                .setDatabase(this.redisConfig.getDatabaseId())
                .setPassword(this.redisConfig.getPassword());
        this.redissonClient = Redisson.create(config);
    }

    private void registerCommands(){

    }

    private void shutdownBot(boolean fromHook){
        if(this.isShutdownInitiated) return;
        this.isShutdownInitiated = true;

        if(this.jda != null) this.jda.shutdownNow();

        System.exit(0);
    }

}
