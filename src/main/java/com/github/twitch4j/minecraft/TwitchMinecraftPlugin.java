package com.github.twitch4j.minecraft;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.ITwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TwitchMinecraftPlugin extends JavaPlugin {

    private ITwitchClient client;
    public static File webhookcontent;
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        FileConfiguration config = getConfig();
        webhookcontent = new File(getDataFolder(), "webhook.json");
        if(!webhookcontent.exists()) {
            try {
                webhookcontent.createNewFile();
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(webhookcontent), StandardCharsets.UTF_8))) {
                    writer.write("Create your message on https://discohook.org/, click \"JSON Data Editor\", copy everything and paste here.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.getCommand("tbreload").setExecutor(new TwitchCommandManager());
        this.getCommand("tbwebhook").setExecutor(new TwitchCommandManager());
        this.getCommand("tbupdate").setExecutor(new TwitchCommandManager());

        String token = config.getString("oauth_token");
        OAuth2Credential credential = StringUtils.isNotBlank(token) ? new OAuth2Credential("twitch", token) : null;
        client = TwitchClientBuilder.builder()
            .withClientId(config.getString("client_id"))
            .withClientSecret(config.getString("client_secret"))
            .withEnableChat(true)
            .withChatAccount(credential)
            .withEnableHelix(true)
            .withDefaultAuthToken(credential)
            .build();
        List<String> channels = config.getStringList("channels");
        if (!channels.isEmpty()) {
            channels.forEach(name -> client.getChat().joinChannel(name));
            client.getClientHelper().enableStreamEventListener(channels);
            client.getClientHelper().enableFollowEventListener(channels);
        }
        client.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(new TwitchEventHandler(this));
    }

    @Override
    public void onDisable() {
        if (client != null)
            client.close();
    }

    public ITwitchClient getTwitchClient() {
        return this.client;
    }

}
