package dev.lorenzzz.lobby;

import co.aikar.commands.PaperCommandManager;
import dev.lorenzzz.lobby.command.LeaveQueueCommand;
import dev.lorenzzz.lobby.command.LobbyCommand;
import dev.lorenzzz.lobby.command.PauseQueueCommand;
import dev.lorenzzz.lobby.command.QueueCommand;
import dev.lorenzzz.lobby.manager.ManagerService;
import dev.lorenzzz.lobby.util.C;
import dev.lorenzzz.lobby.util.ConfigFIle;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class Lobby extends JavaPlugin {

    @Getter
    private ConfigFIle messages, menus, core, actionbar, settings;
    @Getter
    private static Lobby instance;
    private ManagerService managerService;

    @Override
    public void onEnable() {
        instance = this;
        try {
            registerConfig();
            registerBungeeChannels();
            managerService = new ManagerService(this);
            managerService.init();
            registerCmds();
            C.logstart();
        } catch (Exception ex) {
            C.error("Error starting plugin: " + ex.getMessage());
            ex.printStackTrace();
            Bukkit.getServer().shutdown();
        }
    }

    @Override
    public void onDisable() {
        if (managerService != null) {
            managerService.shutdown();
        }
        instance = null;
        C.logstop();
    }

    private void registerCmds() {
        PaperCommandManager cmdManager = new PaperCommandManager(this);

        cmdManager.getCommandCompletions().registerCompletion("servers", c -> {
            List<String> servers = new ArrayList<>();
            ConfigurationSection section = settings.getConfigurationSection("servers");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    servers.add(section.getString(key + ".bungee-name", key.toLowerCase()));
                }
            }
            return servers;
        });

        cmdManager.registerCommand(new LobbyCommand(this));
        cmdManager.registerCommand(new QueueCommand(this));
        cmdManager.registerCommand(new LeaveQueueCommand(this));
        cmdManager.registerCommand(new PauseQueueCommand(this));
    }

    private void registerBungeeChannels() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    private void registerConfig() {
        long start = System.currentTimeMillis();
        this.messages = new ConfigFIle(this, "messages.yml", true, false);
        this.core = new ConfigFIle(this, "core.yml", true, false);
        this.actionbar = new ConfigFIle(this, "actionbars.yml", true, false);
        this.menus = new ConfigFIle(this, "menus.yml", true, false);
        this.settings = new ConfigFIle(this, "settings.yml", true, false);
        long elapsed = System.currentTimeMillis() - start;
        C.info("Loaded configs in " + elapsed + "ms");
    }
}