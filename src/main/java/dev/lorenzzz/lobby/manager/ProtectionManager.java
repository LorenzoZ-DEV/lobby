package dev.lorenzzz.lobby.manager;

import dev.lorenzzz.lobby.Lobby;
import dev.lorenzzz.lobby.util.C;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class ProtectionManager implements Manager, Listener {

    @Getter
    private static ProtectionManager instance;
    private final Lobby plugin;

    public ProtectionManager(Lobby plugin) {
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

    private boolean option(String key) {
        return plugin.getSettings().getBoolean("options." + key, false);
    }

    private boolean inBuildMode(Player player) {
        return BuildModeManager.getInstance() != null
                && BuildModeManager.getInstance().isInBuildMode(player.getUniqueId());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!option("break-blocks") && !inBuildMode(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(C.translate(
                    plugin.getMessages().getString("prefix", "") + " "
                            + plugin.getMessages().getString("CANT-BREAK", "")));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!option("place-blocks") && !inBuildMode(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(C.translate(
                    plugin.getMessages().getString("prefix", "") + " "
                            + plugin.getMessages().getString("CANT-PLACE", "")));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        if (!option("damage")) {
            event.setCancelled(true);
            return;
        }

        if (!option("fall") && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;

        if (!option("pvp-damage")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (!option("item-drop") && !inBuildMode(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!option("item-pickup") && !inBuildMode(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        if (!option("explosions")) {
            event.setCancelled(true);
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.COMMAND) return;

        boolean isMonster = event.getEntity() instanceof org.bukkit.entity.Monster;
        boolean isAnimal = event.getEntity() instanceof org.bukkit.entity.Animals;

        if (isMonster && !option("monster-spawn")) {
            event.setCancelled(true);
        } else if (isAnimal && !option("animal-spawn")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!option("food-change")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (!option("weather-change") && event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (option("no-death-tp") && player.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true);
            String worldName = plugin.getSettings().getString("spawn.world", "world");
            org.bukkit.World world = Bukkit.getWorld(worldName);
            if (world == null) return;
            double x = plugin.getSettings().getDouble("spawn.x");
            double y = plugin.getSettings().getDouble("spawn.y");
            double z = plugin.getSettings().getDouble("spawn.z");
            float yaw = (float) plugin.getSettings().getDouble("spawn.yaw");
            float pitch = (float) plugin.getSettings().getDouble("spawn.pitch");
            player.teleport(new org.bukkit.Location(world, x, y, z, yaw, pitch));
        }
    }
}