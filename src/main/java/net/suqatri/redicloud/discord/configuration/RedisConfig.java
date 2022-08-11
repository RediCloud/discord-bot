package net.suqatri.redicloud.discord.configuration;

import lombok.Data;

@Data
public class RedisConfig {

    private String hostname;
    private int port;
    private String password;
    private int databaseId;

}
