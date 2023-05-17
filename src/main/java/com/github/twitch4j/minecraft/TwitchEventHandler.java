package com.github.twitch4j.minecraft;

import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.chat.events.channel.CheerEvent;
import com.github.twitch4j.chat.events.channel.FollowEvent;
import com.github.twitch4j.chat.events.channel.GiftSubscriptionsEvent;
import com.github.twitch4j.chat.events.channel.SubscriptionEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.helix.domain.Stream;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.github.twitch4j.minecraft.Utils.*;
import static java.util.logging.Level.SEVERE;


public class TwitchEventHandler {
    
    private final TwitchMinecraftPlugin plugin;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public TwitchEventHandler(TwitchMinecraftPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
    }

    @EventSubscriber
    public void onStreamUp(ChannelGoLiveEvent event) {
        Stream stream = event.getStream();

        // Broadcast stream up message
        broadcast(String.format(plugin.getConfig().getString("stream_up"), stream.getUserName(), stream.getTitle())); 
        
        // Send Discord notification if the integration is enabled
        if (Boolean.parseBoolean(plugin.getConfig().getString("discord"))) {
            String webhookUrl = plugin.getConfig().getString("webhook_url");
            if (!webhookUrl.isEmpty()) {
                webhookSend();
            } else {
                Bukkit.getLogger().log(Level.SEVERE, "Discord notifications are enabled, but webhook_url is not set!");
            }
        }

        // Schedule periodic stream duration messages if the duration feature is enabled
        if (Boolean.parseBoolean(plugin.getConfig().getString("duration"))) {
            int delay = plugin.getConfig().getInt("message_delay");
            int roundTo = plugin.getConfig().getInt("round_to");
            
            executor.scheduleAtFixedRate(() -> {
                if (stream.getType().equals("live")) {
                    int timeDiff = Instant.now().atZone(ZoneOffset.UTC).getMinute() - stream.getStartedAtInstant().atZone(ZoneOffset.UTC).getMinute();
                    int durationInMinutes = getNearMinute(timeDiff, roundTo);
                    String durationString = DurationFormatUtils.formatDuration(
                        TimeUnit.MINUTES.toMillis(durationInMinutes),
                        "HH' hours and 'mm' minutes'", 
                        false
                    );
                    // Broadcast stream duration message
                    broadcast(String.format(plugin.getConfig().getString("stream_duration"), durationString, stream.getUserName()));
                }
            }, delay, delay, TimeUnit.MILLISECONDS);
        }
    }

    @EventSubscriber
    public void onStreamDown(ChannelGoOfflineEvent event) {
        // Broadcast stream down message
        broadcast(String.format(plugin.getConfig().getString("stream_down"), event.getChannel().getName()));
        
        // Shutdown the stream duration message executor
        if (Boolean.parseBoolean(plugin.getConfig().getString("duration"))) {
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Bukkit.getLogger().log(Level.WARNING, "Interrupted while waiting for stream duration message executor to terminate.", e);
            }
            executor.shutdownNow();
        }
    }

    @EventSubscriber
    public void onFollow(FollowEvent event) {
        // Broadcast follow message if the feature is enabled
        if (Boolean.parseBoolean(plugin.getConfig().getString("follow"))) {
            broadcast(String.format(plugin.getConfig().getString("on_follow"), event.getUser().getName()));
        }
    }

    @EventSubscriber
    public void onCheer(CheerEvent event) {
        // Broadcast cheer message if the feature is enabled and the cheer amount is greater than or equal to 100
        int cheerThreshold = 100;
        if (Boolean.parseBoolean(plugin.getConfig().getString("cheer")) && event.getBits() >= cheerThreshold) {
            broadcast(String.format(plugin.getConfig().getString("on_cheer"), event.getUser().getName(), event.getBits()));
        }
    }

    @EventSubscriber
    public void onSub(SubscriptionEvent event) {
        // Broadcast subscription message if the feature is enabled and the subscription is not a gift
        if (Boolean.parseBoolean(plugin.getConfig().getString("sub")) && !event.getGifted()) {
            broadcast(String.format(plugin.getConfig().getString("on_sub"), event.getUser().getName(), event.getMonths()));
        }
    }

    @EventSubscriber
    public void onSubMysteryGift(GiftSubscriptionsEvent event) {
        // Broadcast subscription gift message if the feature is enabled
        if (Boolean.parseBoolean(plugin.getConfig().getString("sub_gift"))) {
            broadcast(String.format(plugin.getConfig().getString("on_sub_gift"), event.getUser().getName(), event.getCount(), event.getChannel().getName()));
        }
    }

    private void broadcast(String message) {
        Bukkit.broadcastMessage(Objects.requireNonNull(message, "message must not be null"));
    }

}
