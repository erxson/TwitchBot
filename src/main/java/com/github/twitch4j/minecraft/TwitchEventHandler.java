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

import static com.github.twitch4j.minecraft.Utils.*;
import static java.util.logging.Level.SEVERE;


public class TwitchEventHandler {

    private final TwitchMinecraftPlugin plugin;

    public TwitchEventHandler(TwitchMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @EventSubscriber
    public void onStreamUp(ChannelGoLiveEvent event) {
        Stream stream = event.getStream();
        broadcast(
            String.format(
                Objects.requireNonNull(plugin.getConfig().getString("stream_up")),
                stream.getUserName(),
                stream.getTitle()
                )
            );
        if (Objects.equals(plugin.getConfig().getString("discord"), "true")) {
            if (!Objects.equals(plugin.getConfig().getString("webhook_url"), "")) {
                webhookSend();
            } else {
                Bukkit.getLogger().log(SEVERE, "Wtf, bro, you can't use discord notifications without webhook. Get webhook url and put it in config.yml");
            }
        }

        if (Objects.equals(plugin.getConfig().getString("duration"), "true")) {
            Runnable task = () -> {
                if (stream.getType().equals("live")) {
                    broadcast(String.format(Objects.requireNonNull(plugin.getConfig().getString("stream_duration")),
                        DurationFormatUtils.formatDuration(TimeUnit.MINUTES.toMillis(
                                getNearMinute(
                                    Instant.now().atZone(ZoneOffset.UTC).getMinute() - stream.getStartedAtInstant().atZone(ZoneOffset.UTC).getMinute(),
                                    plugin.getConfig().getInt("round_to"))),
                            "HH' hours and 'mm' minutes'", false), stream.getUserName()));
                }
            };
            executor.scheduleAtFixedRate(task, plugin.getConfig().getInt("message_delay"), plugin.getConfig().getInt("message_delay"), TimeUnit.MILLISECONDS);
        }
    }

    @EventSubscriber
    public void onStreamDown(ChannelGoOfflineEvent event) throws InterruptedException {
        broadcast(String.format(Objects.requireNonNull(plugin.getConfig().getString("stream_down")), event.getChannel().getName()));
        if (Objects.equals(plugin.getConfig().getString("duration"), "true")) {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            executor.shutdownNow();
        }
    }

    @EventSubscriber
    public void onFollow(FollowEvent event) {
        if (Objects.equals(plugin.getConfig().getString("follow"), "true")) {
            broadcast(String.format(Objects.requireNonNull(plugin.getConfig().getString("on_follow")), event.getUser().getName()));
        }
    }

    @EventSubscriber
    public void onCheer(CheerEvent event) {
        if (Objects.equals(plugin.getConfig().getString("cheer"), "true")) {
            if (event.getBits() >= 100)
                broadcast(String.format(Objects.requireNonNull(plugin.getConfig().getString("on_cheer")), event.getUser().getName(), event.getBits()));
        }
    }

    @EventSubscriber
    public void onSub(SubscriptionEvent event) {
        if (Objects.equals(plugin.getConfig().getString("sub"), "true")) {
            if (!event.getGifted())
                broadcast(String.format(Objects.requireNonNull(plugin.getConfig().getString("on_sub")), event.getUser().getName(), event.getMonths()));
        }
    }

    @EventSubscriber
    public void onSubMysteryGift(GiftSubscriptionsEvent event) {
        if (Objects.equals(plugin.getConfig().getString("sub_gift"), "true")) {
            broadcast(String.format(Objects.requireNonNull(plugin.getConfig().getString("on_sub_gift")), event.getUser().getName(), event.getCount(), event.getChannel().getName()));
        }
    }

    private void broadcast(String message) {
        this.plugin.getServer().broadcastMessage(message);
    }

}
