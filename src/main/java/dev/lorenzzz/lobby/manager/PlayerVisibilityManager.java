package dev.lorenzzz.lobby.manager;

import dev.lorenzzz.lobby.Lobby;
import dev.lorenzzz.lobby.util.C;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerVisibilityManager implements Manager {

    @Getter
    private static PlayerVisibilityManager instance;
    private final Lobby plugin;
    private final Set<UUID> hiddenPlayers = new HashSet<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public PlayerVisibilityManager(Lobby plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        instance = this;
    }

    @Override
    public void stop() {
        hiddenPlayers.clear();
        cooldowns.clear();
        instance = null;
    }

    public boolean isHidden(UUID uuid) {
        return hiddenPlayers.contains(uuid);
    }

    public boolean toggle(Player player) {
        long now = System.currentTimeMillis();
        Long lastUse = cooldowns.get(player.getUniqueId());
        if (lastUse != null && now - lastUse < 3000) {
            long remaining = (3000 - (now - lastUse)) / 1000 + 1;
            String msg = plugin.getMessages().getString("PLAYER-VISIBILITY-COOLDOWN", "");
            msg = msg.replace("<time>", remaining + "s");
            player.sendMessage(C.translate(plugin.getMessages().getString("prefix", "") + " " + msg));
            return isHidden(player.getUniqueId());
        }
        cooldowns.put(player.getUniqueId(), now);

        if (hiddenPlayers.contains(player.getUniqueId())) {
            hiddenPlayers.remove(player.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                player.showPlayer(plugin, online);
            }
            player.sendMessage(C.translate(plugin.getMessages().getString("prefix", "") + " "
                    + plugin.getMessages().getString("PLAYER-VISIBILITY-TOGGLED-ON", "")));
            return false;
        } else {
            hiddenPlayers.add(player.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) {
                    player.hidePlayer(plugin, online);
                }
            }
            player.sendMessage(C.translate(plugin.getMessages().getString("prefix", "") + " "
                    + plugin.getMessages().getString("PLAYER-VISIBILITY-TOGGLED-OFF", "")));
            return true;
        }
    }

    public void handleJoin(Player joiner) {
        for (UUID hiddenUuid : hiddenPlayers) {
            Player hiddenPlayer = Bukkit.getPlayer(hiddenUuid);
            if (hiddenPlayer != null && hiddenPlayer.isOnline()) {
                hiddenPlayer.hidePlayer(plugin, joiner);
            }
        }
    }

    public void handleQuit(UUID uuid) {
        hiddenPlayers.remove(uuid);
        cooldowns.remove(uuid);
    }
}