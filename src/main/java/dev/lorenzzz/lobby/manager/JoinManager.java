package dev.lorenzzz.lobby.manager;

import com.cryptomorin.xseries.XMaterial;
import dev.lorenzzz.lobby.Lobby;
import dev.lorenzzz.lobby.util.C;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Optional;

public class JoinManager implements Manager, Listener {

    @Getter
    private static JoinManager instance;
    private final Lobby plugin;
    private NamespacedKey itemKey;

    public JoinManager(Lobby plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        instance = this;
        itemKey = new NamespacedKey(plugin, "join-item");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void stop() {
        instance = null;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null);

        teleportToSpawn(player);
        clearAndGiveItems(player);
        applyPotionEffects(player);
        applySpeed(player);
        playJoinSound(player);
        sendJoinMessages(player);
        clearEntities();

        if (FlyManager.getInstance() != null) {
            FlyManager.getInstance().applyOnJoin(player);
        }
        if (PlayerVisibilityManager.getInstance() != null) {
            PlayerVisibilityManager.getInstance().handleJoin(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
        Player player = event.getPlayer();
        if (BuildModeManager.getInstance() != null) {
            BuildModeManager.getInstance().remove(player.getUniqueId());
        }
        if (PlayerVisibilityManager.getInstance() != null) {
            PlayerVisibilityManager.getInstance().handleQuit(player.getUniqueId());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT")) return;
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        String id = item.getItemMeta().getPersistentDataContainer()
                .get(itemKey, PersistentDataType.STRING);
        if (id == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        ConfigurationSection itemSection = plugin.getSettings()
                .getConfigurationSection("join-items." + id);
        if (itemSection == null) return;

        String action = itemSection.getString("action", "");

        if (action.startsWith("{openmenu:")) {
            String menuName = action.substring(10, action.length() - 1);
            if (MenuManager.getInstance() != null) {
                MenuManager.getInstance().openMenu(player, menuName);
            }
        } else if ("enderbutt".equals(action)) {
            if (EnderButtManager.getInstance() != null) {
                EnderButtManager.getInstance().launch(player);
            }
        } else if ("players_off".equals(action)) {
            if (PlayerVisibilityManager.getInstance() != null) {
                boolean nowHidden = PlayerVisibilityManager.getInstance().toggle(player);
                updateVisibilityItem(player, nowHidden);
            }
        } else if ("players_on".equals(action)) {
            if (PlayerVisibilityManager.getInstance() != null) {
                boolean nowHidden = PlayerVisibilityManager.getInstance().toggle(player);
                updateVisibilityItem(player, nowHidden);
            }
        }

        if (itemSection.getBoolean("command.enabled", false)) {
            String cmd = itemSection.getString("command.execute", "");
            if (!cmd.isEmpty()) {
                player.performCommand(cmd);
            }
        }
    }

    private void teleportToSpawn(Player player) {
        if (!plugin.getSettings().getBoolean("TP-TO-SPAWN-ON-JOIN", true)) return;

        String worldName = plugin.getSettings().getString("spawn.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        double x = plugin.getSettings().getDouble("spawn.x");
        double y = plugin.getSettings().getDouble("spawn.y");
        double z = plugin.getSettings().getDouble("spawn.z");
        float yaw = (float) plugin.getSettings().getDouble("spawn.yaw");
        float pitch = (float) plugin.getSettings().getDouble("spawn.pitch");

        player.teleport(new Location(world, x, y, z, yaw, pitch));
    }

    private void clearAndGiveItems(Player player) {
        if (plugin.getSettings().getBoolean("CLEARINV.ENABLED", true)) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
        }

        ConfigurationSection items = plugin.getSettings().getConfigurationSection("join-items");
        if (items == null) return;

        for (String key : items.getKeys(false)) {
            ConfigurationSection section = items.getConfigurationSection(key);
            if (section == null) continue;

            String materialName = section.getString("material", "STONE");
            Optional<XMaterial> xmat = XMaterial.matchXMaterial(materialName);
            if (xmat.isEmpty()) continue;

            ItemStack item = xmat.get().parseItem();
            if (item == null) continue;

            item.setAmount(section.getInt("amount", 1));

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String name = section.getString("name", "");
                meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));

                String loreStr = section.getString("lore", "");
                if (!loreStr.isEmpty()) {
                    meta.lore(List.of(LegacyComponentSerializer.legacyAmpersand().deserialize(loreStr)));
                }

                meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, key);
                item.setItemMeta(meta);
            }

            int slot = section.getInt("slot", 0) - 1;
            if (slot >= 0 && slot < 36) {
                player.getInventory().setItem(slot, item);
            }
        }
    }

    private void applyPotionEffects(Player player) {
        if (!plugin.getSettings().getBoolean("joinpotions.enabled", false)) return;

        boolean hidden = plugin.getSettings().getBoolean("joinpotions.hidden", false);
        ConfigurationSection effects = plugin.getSettings().getConfigurationSection("joinpotions.effects");
        if (effects == null) return;

        for (String effectName : effects.getKeys(false)) {
            int amplifier = effects.getInt(effectName, 0) - 1;
            PotionEffectType type = PotionEffectType.getByName(effectName);
            if (type == null) continue;

            player.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, amplifier, false, !hidden));
        }
    }

    private void applySpeed(Player player) {
        if (!plugin.getSettings().getBoolean("JOINSPEED-LEGACY.ENABLED", false)) return;
        int level = plugin.getSettings().getInt("JOINSPEED-LEGACY.LEVEL", 1);
        float speed = Math.min(0.2f * level, 1.0f);
        player.setWalkSpeed(speed);
    }

    private void playJoinSound(Player player) {
        String soundName = plugin.getSettings().getString("join-sound", "");
        if (soundName.isEmpty()) return;

        Sound sound = C.resolveSound(soundName);
        if (sound == null) return;

        float volume = (float) plugin.getSettings().getDouble("join-sound-volume", 1.0);
        float pitch = (float) plugin.getSettings().getDouble("join-sound-pitch", 1.0);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private void sendJoinMessages(Player player) {
        ConfigurationSection joinSection = plugin.getSettings().getConfigurationSection("join");
        if (joinSection == null) return;

        if (joinSection.getBoolean("message.enabled", false)) {
            broadcastVipMessages(player, joinSection);
        }

        if (joinSection.getBoolean("send-message.enabled", false)) {
            List<String> messages = joinSection.getStringList("send-message.message");
            for (String msg : messages) {
                msg = msg.replace("%player_name%", player.getName());
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                    msg = PlaceholderAPI.setPlaceholders(player, msg);
                }
                player.sendMessage(LegacyComponentSerializer.legacySection()
                        .deserialize(C.translate(msg)));
            }
        }
    }

    private void broadcastVipMessages(Player player, ConfigurationSection joinSection) {
        if (!joinSection.getBoolean("vip_globale_lobbys.enabled", false)) return;

        List<?> groups = joinSection.getList("vip_globale_lobbys.groups");
        if (groups == null) return;

        ConfigurationSection groupsSection = joinSection.getConfigurationSection("vip_globale_lobbys");
        if (groupsSection == null) return;

        for (String key : groupsSection.getConfigurationSection("groups") != null
                ? groupsSection.getConfigurationSection("groups").getKeys(false)
                : List.<String>of()) {
            // groups is a list, handle as list
        }

        List<java.util.Map<?, ?>> groupList = joinSection.getMapList("vip_globale_lobbys.groups");
        for (java.util.Map<?, ?> group : groupList) {
            String permission = String.valueOf(group.get("permission"));
            if (!player.hasPermission(permission)) continue;

            Object messagesObj = group.get("messages");
            if (messagesObj instanceof List<?> msgList) {
                for (Object msgObj : msgList) {
                    String msg = String.valueOf(msgObj)
                            .replace("%player%", player.getName())
                            .replace("%player_name%", player.getName());
                    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                        msg = PlaceholderAPI.setPlaceholders(player, msg);
                    }
                    Bukkit.broadcast(LegacyComponentSerializer.legacySection()
                            .deserialize(C.translate(msg)));
                }
                break;
            }
        }
    }

    private void clearEntities() {
        if (!plugin.getSettings().getBoolean("CLEARENTITIES.ENABLED", false)) return;

        String worldName = plugin.getSettings().getString("spawn.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        for (Entity entity : world.getEntities()) {
            if (entity instanceof Player) continue;
            if (entity instanceof Item) {
                entity.remove();
            }
        }
    }

    private void updateVisibilityItem(Player player, boolean nowHidden) {
        String giveKey = nowHidden ? "player-invisibility-off" : "player-invisibility-on";
        ConfigurationSection section = plugin.getSettings().getConfigurationSection("join-items." + giveKey);
        if (section == null) return;

        String materialName = section.getString("material", "GRAY_DYE");
        Optional<XMaterial> xmat = XMaterial.matchXMaterial(materialName);
        if (xmat.isEmpty()) return;

        ItemStack item = xmat.get().parseItem();
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(section.getString("name", "")));
            meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, giveKey);
            item.setItemMeta(meta);
        }

        int slot = section.getInt("slot", 9) - 1;
        if (slot >= 0 && slot < 36) {
            player.getInventory().setItem(slot, item);
        }
    }

    public NamespacedKey getItemKey() {
        return itemKey;
    }
}