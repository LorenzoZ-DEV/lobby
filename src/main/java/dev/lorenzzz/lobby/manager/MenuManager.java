package dev.lorenzzz.lobby.manager;

import dev.lorenzzz.lobby.Lobby;
import dev.lorenzzz.lobby.menu.ConfigMenu;
import lombok.Getter;
import net.j4c0b3y.api.menu.MenuHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MenuManager implements Manager {

    @Getter
    private static MenuManager instance;
    private final Lobby plugin;
    @Getter
    private MenuHandler menuHandler;
    private final Map<String, ConfigurationSection> menuSections = new HashMap<>();

    public MenuManager(Lobby plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        instance = this;
        menuHandler = new MenuHandler(plugin);
        loadMenus();
    }

    @Override
    public void stop() {
        menuSections.clear();
        instance = null;
    }

    private void loadMenus() {
        menuSections.clear();
        ConfigurationSection menusRoot = plugin.getMenus().getConfigurationSection("menus");
        if (menusRoot == null) return;

        for (String key : menusRoot.getKeys(false)) {
            ConfigurationSection section = menusRoot.getConfigurationSection(key);
            if (section == null) continue;

            String name = section.getString("name", key);
            menuSections.put(name, section);
        }
    }

    public void openMenu(Player player, String name) {
        ConfigurationSection section = menuSections.get(name);
        if (section == null) return;

        new ConfigMenu(player, section, plugin).open();
    }

    public void reload() {
        loadMenus();
    }
}