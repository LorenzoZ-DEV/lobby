package dev.lorenzzz.lobby.manager;

import dev.lorenzzz.lobby.Lobby;
import dev.lorenzzz.lobby.util.BungeeUtil;
import dev.lorenzzz.lobby.util.C;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class QueueManager implements Manager, Listener {

    @Getter
    private static QueueManager instance;
    private final Lobby plugin;
    private final Map<String, LinkedList<UUID>> queues = new HashMap<>();
    private final Map<UUID, String> playerQueue = new HashMap<>();
    private final Set<String> pausedServers = new HashSet<>();
    private BukkitTask sendTask;
    private BukkitTask actionBarTask;

    public QueueManager(Lobby plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        instance = this;
        if (!plugin.getSettings().getBoolean("queue.enabled", false)) return;

        ConfigurationSection servers = plugin.getSettings().getConfigurationSection("servers");
        if (servers != null) {
            for (String key : servers.getKeys(false)) {
                String bungeeName = servers.getString(key + ".bungee-name", key.toLowerCase());
                queues.put(bungeeName, new LinkedList<>());
            }
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);

        int sendInterval = plugin.getSettings().getInt("queue.send-every-seconds", 2) * 20;
        sendTask = Bukkit.getScheduler().runTaskTimer(plugin, this::processSend, sendInterval, sendInterval);
        actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, this::processActionBars, 20L, 20L);
    }

    @Override
    public void stop() {
        if (sendTask != null) sendTask.cancel();
        if (actionBarTask != null) actionBarTask.cancel();
        queues.clear();
        playerQueue.clear();
        pausedServers.clear();
        instance = null;
    }

    public void joinQueue(Player player, String server) {
        String lower = server.toLowerCase();
        if (!queues.containsKey(lower)) {
            queues.put(lower, new LinkedList<>());
        }

        if (playerQueue.containsKey(player.getUniqueId())) {
            player.sendMessage(C.translate(prefix() + " " + plugin.getMessages().getString("QUEUE.ALREADY-QUEUED", "")));
            return;
        }

        LinkedList<UUID> queue = queues.get(lower);

        if (player.hasPermission("lobby.queue.priority")) {
            queue.addFirst(player.getUniqueId());
        } else {
            queue.addLast(player.getUniqueId());
        }
        playerQueue.put(player.getUniqueId(), lower);

        String msg = plugin.getMessages().getString("QUEUE.JOINED", "")
                .replace("<server>", server);
        player.sendMessage(C.translate(prefix() + " " + msg));
    }

    public void leaveQueue(Player player) {
        String server = playerQueue.remove(player.getUniqueId());
        if (server == null) {
            player.sendMessage(C.translate(prefix() + " " + plugin.getMessages().getString("QUEUE.NOT-QUEUED", "")));
            return;
        }
        LinkedList<UUID> queue = queues.get(server);
        if (queue != null) queue.remove(player.getUniqueId());

        String msg = plugin.getMessages().getString("QUEUE.LEFT", "")
                .replace("<server>", server);
        player.sendMessage(C.translate(prefix() + " " + msg));
    }

    public void togglePause(String server, Player admin) {
        String lower = server.toLowerCase();
        boolean paused;
        if (pausedServers.contains(lower)) {
            pausedServers.remove(lower);
            paused = false;
        } else {
            pausedServers.add(lower);
            paused = true;
        }
        String option = paused ? "messo in pausa" : "ripreso";
        String msg = plugin.getMessages().getString("QUEUE.PAUSED-BY-PLAYER", "")
                .replace("<option>", option)
                .replace("<server>", server);
        admin.sendMessage(C.translate(prefix() + " " + msg));
    }

    private void processSend() {
        for (Map.Entry<String, LinkedList<UUID>> entry : queues.entrySet()) {
            String server = entry.getKey();
            LinkedList<UUID> queue = entry.getValue();

            if (pausedServers.contains(server) || queue.isEmpty()) continue;

            UUID uuid = queue.pollFirst();
            playerQueue.remove(uuid);
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            String sendMsg = plugin.getMessages().getString("SENDING-TO-SERVER", "")
                    .replace("<server>", server);
            player.sendMessage(C.translate(prefix() + " " + sendMsg));
            BungeeUtil.connect(player, server, plugin);
        }
    }

    private void processActionBars() {
        for (Map.Entry<UUID, String> entry : playerQueue.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) continue;

            String server = entry.getValue();
            LinkedList<UUID> queue = queues.get(server);
            if (queue == null) continue;

            int pos = queue.indexOf(entry.getKey()) + 1;
            int total = queue.size();

            boolean paused = pausedServers.contains(server);
            ConfigurationSection abSection = paused
                    ? plugin.getActionbar().getConfigurationSection("PausedQueueActionBar")
                    : plugin.getActionbar().getConfigurationSection("QueueActionBar");

            if (abSection == null || !abSection.getBoolean("enabled", false)) continue;

            List<String> lines = abSection.getStringList("message");
            String combined = String.join("", lines)
                    .replace("<pos>", String.valueOf(pos))
                    .replace("<in_queue>", String.valueOf(total))
                    .replace("<server>", server);

            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                combined = PlaceholderAPI.setPlaceholders(player, combined);
            }

            player.sendActionBar(LegacyComponentSerializer.legacySection()
                    .deserialize(C.translate(combined)));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        String server = playerQueue.remove(uuid);
        if (server != null) {
            LinkedList<UUID> queue = queues.get(server);
            if (queue != null) queue.remove(uuid);
        }
    }

    private String prefix() {
        return plugin.getMessages().getString("prefix", "");
    }

    public boolean isQueued(UUID uuid) {
        return playerQueue.containsKey(uuid);
    }

    public String getQueuedServer(UUID uuid) {
        return playerQueue.get(uuid);
    }
}