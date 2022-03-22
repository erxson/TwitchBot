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

import static java.util.logging.Level.INFO;

public class Utils {
    private static final Plugin plugin = TwitchMinecraftPlugin.getPlugin(TwitchMinecraftPlugin.class);
    public static void webhookSend() {
        try {
            String webhook = plugin.getConfig().getString("webhook_url");
            JsonNode content = new ObjectMapper().readValue(FileUtils.readFileToString(new File(plugin.getDataFolder(), "webhook.json"), Charsets.UTF_8), JsonNode.class);
            String command = "curl -H \"Content-Type: application/json\" -X POST -d " + StringEscapeUtils.escapeJson(content.toString()) + " " + webhook;
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            Bukkit.getLogger().log(INFO, "Content " + StringEscapeUtils.escapeJson(content.toString()));
            Bukkit.getLogger().log(INFO, "Command " + command);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
    public static int getNearMinute (int minutes, int value) {
        int mod = minutes %  value;
        int res = 0 ;
        if ((mod) >= value % 2) {
            res = minutes + (value - mod);
        }else {
            res = minutes - mod;
        }
        return res;
    }
    public static void download(String link, String file) {
        URL asd = null;
        try {
            asd = new URL(link);
            FileUtils.copyURLToFile(asd, new File(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
