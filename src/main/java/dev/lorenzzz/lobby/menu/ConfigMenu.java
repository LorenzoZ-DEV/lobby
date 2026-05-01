package dev.lorenzzz.lobby.menu;

import com.cryptomorin.xseries.XMaterial;
import dev.lorenzzz.lobby.Lobby;
import dev.lorenzzz.lobby.manager.MenuManager;
import dev.lorenzzz.lobby.ui.BorderButton;
import dev.lorenzzz.lobby.util.C;
import me.clip.placeholderapi.PlaceholderAPI;
import net.j4c0b3y.api.menu.Menu;
import net.j4c0b3y.api.menu.MenuSize;
import net.j4c0b3y.api.menu.button.Button;
import net.j4c0b3y.api.menu.button.ButtonClick;
import net.j4c0b3y.api.menu.layer.impl.BackgroundLayer;
import net.j4c0b3y.api.menu.layer.impl.ForegroundLayer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Optional;

public class ConfigMenu extends Menu {

    private final ConfigurationSection section;
    private final Lobby plugin;

    public ConfigMenu(Player player, ConfigurationSection section, Lobby plugin) {
        super(C.translate(section.getString("menu-title", "Menu")),
                MenuSize.of(section.getInt("menu-size", 27) / 9),
                player);
        this.section = section;
        this.plugin = plugin;
    }

    @Override
    public void setup(BackgroundLayer background, ForegroundLayer foreground) {
        if (section.getBoolean("fill-menu.enabled", false)) {
            background.fill(new BorderButton());
        }

        ConfigurationSection items = section.getConfigurationSection("items");
        if (items == null) return;

        for (String key : items.getKeys(false)) {
            ConfigurationSection itemSection = items.getConfigurationSection(key);
            if (itemSection == null) continue;

            int slot = itemSection.getInt("slot", 0);
            foreground.set(slot, new ConfigMenuButton(itemSection, this));
        }
    }

    private static boolean hasPapi() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    private static String applyPapi(Player player, String text) {
        if (hasPapi() && text.contains("%")) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    private static class ConfigMenuButton extends Button {
        private final ConfigurationSection section;
        private final ConfigMenu menu;

        ConfigMenuButton(ConfigurationSection section, ConfigMenu menu) {
            this.section = section;
            this.menu = menu;
        }

        @Override
        public ItemStack getIcon() {
            Player player = menu.getPlayer();

            String materialName = section.getString("material", "STONE");
            Optional<XMaterial> xmat = XMaterial.matchXMaterial(materialName);
            ItemStack item = xmat.map(XMaterial::parseItem).orElse(null);
            if (item == null) item = new ItemStack(org.bukkit.Material.STONE);

            item.setAmount(section.getInt("amount", 1));
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String name = applyPapi(player, section.getString("name", ""));
                meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(C.translate(name)));

                List<String> loreLines = section.getStringList("lore");
                if (!loreLines.isEmpty()) {
                    meta.lore(loreLines.stream()
                            .map(line -> applyPapi(player, line))
                            .map(line -> LegacyComponentSerializer.legacyAmpersand().deserialize(C.translate(line)))
                            .toList());
                }
                item.setItemMeta(meta);
            }

            return item;
        }

        @Override
        public void onClick(ButtonClick click) {
            Player player = click.getMenu().getPlayer();

            if (section.getBoolean("message.enabled", false)) {
                String msg = section.getString("message.text", "");
                msg = msg.replace("<player>", player.getName());
                player.sendMessage(C.translate(applyPapi(player, msg)));
            }

            if (section.getBoolean("command.enabled", false)) {
                String cmd = section.getString("command.execute", "");
                if (!cmd.isEmpty()) {
                    player.performCommand(cmd);
                }
            }

            String action = section.getString("action", "");
            if (action.startsWith("{openmenu:")) {
                String menuName = action.substring(10, action.length() - 1);
                if (MenuManager.getInstance() != null) {
                    MenuManager.getInstance().openMenu(player, menuName);
                }
            }

            if (section.getBoolean("close-inventory", false)) {
                player.closeInventory();
            }
        }
    }
}