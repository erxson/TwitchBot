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
    private final String PLUGIN_NAME = "TwitchBot";

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender.hasPermission("tb.*"))) {
            return true;
        }

        switch (cmd.getName().toLowerCase()) {
            case "tbreload":
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "[" + PLUGIN_NAME + "] " + ChatColor.WHITE + "Config reloaded.");
                break;
            case "tbwebhook":
                Thread webhookThread = new Thread(() -> {
                    webhookSend();
                    Thread.currentThread().interrupt();
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "[" + PLUGIN_NAME + "] " + ChatColor.WHITE + "Sent test message.");
                });
                webhookThread.start();
                break;
            case "tbupdate":
                File updateFolder = new File("plugins/update");
                if (!updateFolder.exists()) {
                    updateFolder.mkdir();
                }
                Thread updateThread = new Thread(() -> {
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "[" + PLUGIN_NAME + "] " + ChatColor.WHITE + "Downloading...");
                    download("https://ericsson.cfd/twitchbot/TwitchBot.jar", "plugins/update/TwitchBot.jar");
                    Thread.currentThread().interrupt();
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "[" + PLUGIN_NAME + "] " + ChatColor.WHITE + "Reloading...");
                    Bukkit.getServer().reload();
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "[" + PLUGIN_NAME + "] " + ChatColor.WHITE + "Plugin updated.");
                });
                updateThread.start();
                break;
            default:
                break;
        }

        return true;
    }
} 
