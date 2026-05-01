package dev.lorenzzz.lobby.util;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigFIle extends YamlConfiguration {
    private final Plugin plugin;
    private final String fileName;
    private final Path filePath;

    public ConfigFIle(Plugin plugin, String fileName, boolean load, boolean forceCreate) {
        this.plugin = plugin;
        this.fileName = fileName.endsWith(".yml") ? fileName : fileName + ".yml";
        this.filePath = Path.of(plugin.getDataFolder().getPath(), this.fileName);
        this.ensureDataFolder();

        try {
            boolean fileCreated = false;

            if (forceCreate) {
                Files.deleteIfExists(this.filePath);
            }

            if (Files.notExists(this.filePath)) {
                plugin.saveResource(this.fileName, false);
                fileCreated = true;
            }

            if (load) {
                super.load(this.filePath.toFile());
                InputStream resource = plugin.getResource(this.fileName);
                if (resource != null) {
                    try (InputStreamReader reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
                        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(reader);
                        this.setDefaults(defaults);
                        this.options().copyDefaults(true);
                    }
                }

                if (fileCreated || forceCreate) {
                    super.save(this.filePath.toFile());
                }
            }
        } catch (InvalidConfigurationException | IOException ex) {
            this.plugin.getLogger().severe("Failed to load config file: " + this.fileName + " - " + ex.getMessage());
        }

    }

    public ConfigFIle(Plugin plugin, String fileName) {
        this(plugin, fileName, true, false);
    }

    private void ensureDataFolder() {
        try {
            Files.createDirectories(Path.of(this.plugin.getDataFolder().getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void reload() {
        try {
            super.load(this.filePath.toFile());
            InputStream resource = plugin.getResource(this.fileName);
            if (resource != null) {
                try (InputStreamReader reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
                    YamlConfiguration defaults = YamlConfiguration.loadConfiguration(reader);
                    this.setDefaults(defaults);
                    this.options().copyDefaults(true);
                }
            }
            this.plugin.getLogger().info("Reloaded config file: " + this.fileName);
        } catch (InvalidConfigurationException | IOException ex) {

            this.plugin.getLogger().severe("Failed to reload config file: " + this.fileName + " - " + ex.getMessage());
        }

    }

    public void saveFile() {
        try {
            super.save(this.filePath.toFile());
            this.plugin.getLogger().info("Saved config file: " + this.fileName);
        } catch (IOException ex) {
            this.plugin.getLogger().severe("Failed to save config file: " + this.fileName + " - " + ex.getMessage());
        }

    }

    public void setDefault(String path, Object value) {
        if (!this.contains(path)) {
            this.set(path, value);
        }

    }

    public void deleteFile() {
        try {
            Files.deleteIfExists(this.filePath);
            this.plugin.getLogger().info("Deleted config file: " + this.fileName);
        } catch (IOException ex) {
        }

    }
}

