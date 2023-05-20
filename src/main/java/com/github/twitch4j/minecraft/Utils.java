package com.github.twitch4j.minecraft;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

public class Utils {
    private static final Plugin plugin = TwitchMinecraftPlugin.getPlugin(TwitchMinecraftPlugin.class);

    public static void webhookSend() {
        try {
            String webhook = plugin.getConfig().getString("webhook_url");
            JsonNode content = new ObjectMapper().readValue(FileUtils.readFileToString(new File(plugin.getDataFolder(), "webhook.json"), Charsets.UTF_8), JsonNode.class);
            String command = "curl -H \"Content-Type: application/json\" -X POST -d " + StringEscapeUtils.escapeJson(content.toString()) + " " + webhook;
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            Bukkit.getLogger().log(Level.INFO, "Content: {0}\nCommand: {1}", new Object[] {StringEscapeUtils.escapeJson(content.toString()), command});
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public static int getNearMinute(int minutes, int value) {
        int mod = minutes % value;
        return mod > value / 2 ? minutes + (value - mod) : minutes - mod;
    }

    public static void download(String link, String file) {
        try {
            URL url = new URL(link);
            FileUtils.copyURLToFile(url, new File(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
