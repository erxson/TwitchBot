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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Objects;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.github.twitch4j.minecraft.Utils.*;


public class TwitchEventHandler {

    private final TwitchMinecraftPlugin plugin;
    //private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public TwitchEventHandler(TwitchMinecraftPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
    }

    @EventSubscriber
    public void onStreamUp(ChannelGoLiveEvent event) {
        Stream stream = event.getStream();
        String stream_up = plugin.getConfig().getString("stream_up");

        // Broadcast stream up message
        // broadcast(String.format(plugin.getConfig().getString("stream_up"), stream.getUserName(), stream.getTitle()));
        broadcast(stream_up
                .replace("{streamer}", stream.getUserName())
                .replace("{game}", stream.getGameName())
                .replace("{title}", stream.getTitle())
        );

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
        /*if (Boolean.parseBoolean(plugin.getConfig().getString("duration"))) {
            int delay = plugin.getConfig().getInt("message_delay") * 1000;
            int roundTo = plugin.getConfig().getInt("round_to");

            executor.scheduleAtFixedRate(() -> {
                if (stream.getType().equals("live")) {

                    String stream_duration = plugin.getConfig().getString("stream_duration");

                    Instant now = Instant.now();
                    Instant startedAt = stream.getStartedAtInstant();
                    Duration duration = Duration.between(startedAt, now);
                    long durationInMinutes = duration.toMinutes();
                    long durationInHours = durationInMinutes / 60;
                    long remainingMinutes = durationInMinutes % 60;
                    String durationString;
                    if (remainingMinutes == 0) {
                        durationString = String.format("%d hours", durationInHours);
                    } else {
                        long roundedMinutes = getNearMinute((int) remainingMinutes, roundTo);
                        durationString = String.format("%d hours and %d minutes", durationInHours, roundedMinutes);
                    }


                    // Broadcast stream duration message
                    // broadcast(String.format(plugin.getConfig().getString("stream_duration"), durationString, stream.getUserName()));

                    broadcast(stream_duration
                            .replace("{streamer}", stream.getUserName())
                            .replace("{game}", stream.getGameName())
                            .replace("{title}", stream.getTitle())
                            .replace("{duration}", durationString)
                    );
                }
            }, delay, delay, TimeUnit.MILLISECONDS);
        }*/
    }

    @EventSubscriber
    public void onStreamDown(ChannelGoOfflineEvent event) {
        String stream_down = plugin.getConfig().getString("stream_down");

        // Broadcast stream down message
        // broadcast(String.format(plugin.getConfig().getString("stream_down"), event.getChannel().getName()));
        broadcast(stream_down
                .replace("{streamer}", event.getChannel().getName())
        );

        // Shutdown the stream duration message executor
        /*if (Boolean.parseBoolean(plugin.getConfig().getString("duration"))) {
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Bukkit.getLogger().log(Level.WARNING, "Interrupted while waiting for stream duration message executor to terminate.", e);
            }
            executor.shutdownNow();
        }*/
    }

    @EventSubscriber
    public void onFollow(FollowEvent event) {
        String on_follow = plugin.getConfig().getString("on_follow");

        // Broadcast follow message if the feature is enabled
        if (Boolean.parseBoolean(plugin.getConfig().getString("follow"))) {
            // broadcast(String.format(plugin.getConfig().getString("on_follow"), event.getUser().getName()));
            broadcast(on_follow
                    .replace("{user}", event.getUser().getName())
                    .replace("{streamer}", event.getChannel().getName())
            );
        }
    }

    @EventSubscriber
    public void onCheer(CheerEvent event) {
        String on_cheer = plugin.getConfig().getString("on_cheer");

        // Broadcast cheer message if the feature is enabled and the cheer amount is greater than or equal to 100
        int cheerThreshold = 100;
        if (Boolean.parseBoolean(plugin.getConfig().getString("cheer")) && event.getBits() >= cheerThreshold) {
            // broadcast(String.format(plugin.getConfig().getString("on_cheer"), event.getUser().getName(), event.getBits()));
            broadcast(on_cheer
                    .replace("{streamer}", event.getChannel().getName())
                    .replace("{user}", event.getUser().getName())
                    .replace("{bits}", String.valueOf(event.getBits()))
                    .replace("{message}", event.getMessage())
                    .replace("{sub_months}", String.valueOf(event.getSubscriberMonths()))
                    .replace("{sub_tier}", String.valueOf(event.getSubscriptionTier()))
            );
        }
    }

    @EventSubscriber
    public void onSub(SubscriptionEvent event) {
        String on_sub = plugin.getConfig().getString("on_sub");
        String on_sub_gift = plugin.getConfig().getString("on_sub_gift");

        // Broadcast subscription message if the feature is enabled and the subscription is not a gift
        if (Boolean.parseBoolean(plugin.getConfig().getString("sub"))) {
            if (!event.getGifted()) {
                // broadcast(String.format(plugin.getConfig().getString("on_sub"), event.getUser().getName(), event.getMonths()));
                broadcast(on_sub
                        .replace("{streamer}", event.getChannel().getName())
                        .replace("{user}", event.getUser().getName())
                        .replace("{sub_plan}", event.getSubscriptionPlan())
                        .replace("{months}", event.getMonths().toString())
                        .replace("{sub_streak}", event.getSubStreak().toString())
                );
            } else {
                broadcast(on_sub_gift
                        .replace("{streamer}", event.getChannel().getName())
                        .replace("{user}", event.getGiftedBy().getName())
                        .replace("{gifted}", event.getUser().getName())
                        .replace("{sub_plan}", event.getSubscriptionPlan())
                        .replace("{gift_months}", event.getGiftMonths().toString())
                        .replace("{months}", event.getMonths().toString())
                        .replace("{sub_streak}", event.getSubStreak().toString())
                );
            }
        }
    }

    @EventSubscriber
    public void onSubMysteryGift(GiftSubscriptionsEvent event) {
        String on_sub_gift = plugin.getConfig().getString("on_sub_mystery");

        // Broadcast subscription gift message if the feature is enabled
        if (Boolean.parseBoolean(plugin.getConfig().getString("sub_mystery"))) {
            // broadcast(String.format(plugin.getConfig().getString("on_sub_gift"), event.getUser().getName(), event.getCount(), event.getChannel().getName()));
            broadcast(on_sub_gift
                    .replace("{streamer}", event.getChannel().getName())
                    .replace("{user}", event.getUser().getName())
                    .replace("{sub_plan}", event.getSubscriptionPlan())
                    .replace("{subs_gifted}", String.valueOf(event.getCount()))
                    .replace("{subs_gifted_total}", String.valueOf(event.getTotalCount()))
            );
        }
    }

    private void broadcast(String message) {
        Bukkit.broadcastMessage(Objects.requireNonNull(message, "message must not be null"));
    }
}
