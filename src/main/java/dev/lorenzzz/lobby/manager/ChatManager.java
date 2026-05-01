package dev.lorenzzz.lobby.manager;

import dev.lorenzzz.lobby.Lobby;
import dev.lorenzzz.lobby.util.C;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatManager implements Manager, Listener {

    @Getter
    private static ChatManager instance;
    private final Lobby plugin;

    public ChatManager(Lobby plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void stop() {
        instance = null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        if (!plugin.getSettings().getBoolean("chat-format.enabled", false)) return;

        Player player = event.getPlayer();

        if (!plugin.getSettings().getBoolean("options.chat-use", true)
                && !player.hasPermission("lobby.chat.bypass")) {
            event.setCancelled(true);
            player.sendMessage(C.translate(
                    plugin.getMessages().getString("prefix", "") + " "
                            + plugin.getMessages().getString("CANT-USE-CHAT", "")));
            return;
        }

        String format = plugin.getSettings().getString("chat-format.format", "%player_name%: <message>");
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        format = format.replace("%player_name%", player.getName())
                .replace("<message>", message);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            format = PlaceholderAPI.setPlaceholders(player, format);
        }

        event.setCancelled(true);
        Bukkit.broadcast(LegacyComponentSerializer.legacySection()
                .deserialize(C.translate(format)));
    }
}