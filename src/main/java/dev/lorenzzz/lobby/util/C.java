package dev.lorenzzz.lobby.util;

import dev.lorenzzz.lobby.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.plugin.PluginManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface C {

    Pattern HEX_COLOR_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    Pattern HEX_TAG_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    Map<String, String> TRANSLATE_CACHE = new ConcurrentHashMap<>(128);

    static String translate(String message) {
        if (message == null) return "";
        return TRANSLATE_CACHE.computeIfAbsent(message, C::translateRaw);
    }

    private static String translateRaw(String message) {
        String msg = message;

        Lobby instance = Lobby.getInstance();
        if (instance != null && instance.getMessages() != null && msg.contains("%prefix%")) {
            String prefix = instance.getMessages().getString("prefix", "");
            msg = msg.replace("%prefix%", prefix);
        }

        msg = msg.replace("<bold>", "§l");

        if (msg.contains("<#")) {
            Matcher tagMatcher = HEX_TAG_PATTERN.matcher(msg);
            StringBuffer tagBuffer = new StringBuffer();
            while (tagMatcher.find()) {
                tagMatcher.appendReplacement(tagBuffer, convertHexToMinecraftFormat(tagMatcher.group(1)));
            }
            tagMatcher.appendTail(tagBuffer);
            msg = tagBuffer.toString();
        }

        if (msg.contains("&#")) {
            Matcher matcher = HEX_COLOR_PATTERN.matcher(msg);
            StringBuffer buffer = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(buffer, convertHexToMinecraftFormat(matcher.group(1)));
            }
            matcher.appendTail(buffer);
            msg = buffer.toString();
        }

        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    static void clearTranslateCache() {
        TRANSLATE_CACHE.clear();
    }

    static String convertHexToMinecraftFormat(String hex) {
        StringBuilder output = new StringBuilder("§x");
        for (char c : hex.toCharArray()) {
            output.append("§").append(c);
        }
        return output.toString();
    }

    static String detectEngine() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return "Folia";
        } catch (ClassNotFoundException ignored) {}
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return "Paper";
        } catch (ClassNotFoundException ignored) {}
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            return "Spigot";
        } catch (ClassNotFoundException ignored) {}
        return "Bukkit";
    }

    static String cfgFlag(String path, boolean def) {
        Lobby instance = Lobby.getInstance();
        if (instance == null || instance.getSettings() == null) return def ? "&a✔" : "&c✖";
        return instance.getSettings().getBoolean(path, def) ? "&a✔" : "&c✖";
    }

    static String pluginFlag(String name) {
        return Bukkit.getPluginManager().getPlugin(name) != null ? "&a✔" : "&c✖";
    }

    static void logstart() {
        PluginManager pm = Bukkit.getPluginManager();
        String version = pm.getPlugin("Lobby") != null
                ? pm.getPlugin("Lobby").getDescription().getVersion() : "?";

        String engine = detectEngine();

        String queue = cfgFlag("queue.enabled", true);
        String chatFormat = cfgFlag("chat-format.enabled", true);
        String launchPads = cfgFlag("launch-pads.enabled", true);
        String fly = cfgFlag("hub-fly.enabled", true);
        String enderbutt = cfgFlag("enderbutt.ender-butt-enabled", true);
        String potions = cfgFlag("joinpotions.enabled", true);

        String papi = pluginFlag("PlaceholderAPI");
        String vault = pluginFlag("Vault");
        String luckperms = pluginFlag("LuckPerms");
        String bluemap = pluginFlag("BlueMap");

        String banner = "\n"
                + "&7\n"
                + "&#F2C6DE██╗        &fLobby &7v" + version + "\n"
                + "&#F2C6DE██║        &7ʙʏ &d&lLorenzzz\n"
                + "&#F2C6DE██║        &7sᴛᴀᴛᴜs: &aActive\n"
                + "&#F2C6DE██║\n"
                + "&#F2C6DE███████╗   &7ᴇɴɢɪɴᴇ: &e" + engine + "\n"
                + "&#F2C6DE╚══════╝\n"
                + "&7\n"
                + "&7Modules &8» &7Queue " + queue + " &8| &7Chat " + chatFormat
                + " &8| &7LaunchPads " + launchPads + " &8| &7Fly " + fly
                + " &8| &7EnderButt " + enderbutt + " &8| &7Potions " + potions + "\n"
                + "&7Hooks   &8» &7PAPI " + papi + " &8| &7Vault " + vault
                + " &8| &7LuckPerms " + luckperms + " &8| &7BlueMap " + bluemap + "\n"
                + "&7\n";

        Bukkit.getConsoleSender().sendMessage(translate(banner));
    }

    static void logstop() {
        String version = Bukkit.getPluginManager().getPlugin("Lobby") != null
                ? Bukkit.getPluginManager().getPlugin("Lobby").getDescription().getVersion() : "?";

        String banner = "\n"
                + "&7\n"
                + "&#F2C6DE██╗        &fLobby &7v" + version + "\n"
                + "&#F2C6DE██║        &7sᴛᴀᴛᴜs: &cShutting down\n"
                + "&#F2C6DE██║        &7Goodbye, see you soon..!\n"
                + "&#F2C6DE██║\n"
                + "&#F2C6DE███████╗   &7Developed by &d&lLorenzzz\n"
                + "&#F2C6DE╚══════╝\n"
                + "&7\n";

        Bukkit.getConsoleSender().sendMessage(translate(banner));
    }

    static void info(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        message = "&#F2C6DE[Lobby] &7 " + message;
        Bukkit.getConsoleSender().sendMessage(translate(message));
    }

    static void warning(String message) {
        Bukkit.getLogger().warning("[Lobby] " + message);
    }

    static void warn(String message) {
        warning(message);
    }

    static void error(String message) {
        Bukkit.getLogger().severe("[Lobby] " + message);
    }

    static void debug(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        message = "&4[DEBUG] &7 " + message;
        Bukkit.getConsoleSender().sendMessage(translate(message));
    }

    static void line() {
        Bukkit.getConsoleSender().sendMessage(translate("&7----------------------------------------"));
    }

    static Sound resolveSound(String name) {
        if (name == null || name.isEmpty()) return null;

        // Try exact match first
        try {
            return Sound.valueOf(name);
        } catch (IllegalArgumentException ignored) {}

        // Try lowercase key via Registry
        try {
            org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.minecraft(name.toLowerCase());
            Sound s = Registry.SOUNDS.get(key);
            if (s != null) return s;
        } catch (Exception ignored) {}

        // Try common prefixes for old-style names
        String[] prefixes = {"ENTITY_", "BLOCK_", "ITEM_", "MUSIC_", "UI_", "AMBIENT_"};
        for (String prefix : prefixes) {
            try {
                return Sound.valueOf(prefix + name);
            } catch (IllegalArgumentException ignored) {}
        }

        // Try full legacy mappings
        return switch (name.toUpperCase()) {
            case "ORB_PICKUP" -> Sound.valueOf("ENTITY_EXPERIENCE_ORB_PICKUP");
            case "WITHER_SHOOT" -> Sound.valueOf("ENTITY_WITHER_SHOOT");
            case "CLICK" -> Sound.valueOf("UI_BUTTON_CLICK");
            case "NOTE_PLING" -> Sound.valueOf("BLOCK_NOTE_BLOCK_PLING");
            case "NOTE_BASS" -> Sound.valueOf("BLOCK_NOTE_BLOCK_BASS");
            case "NOTE_BASS_DRUM" -> Sound.valueOf("BLOCK_NOTE_BLOCK_BASEDRUM");
            case "LEVEL_UP" -> Sound.valueOf("ENTITY_PLAYER_LEVELUP");
            case "ENDERDRAGON_GROWL" -> Sound.valueOf("ENTITY_ENDER_DRAGON_GROWL");
            case "ANVIL_LAND" -> Sound.valueOf("BLOCK_ANVIL_LAND");
            case "VILLAGER_NO" -> Sound.valueOf("ENTITY_VILLAGER_NO");
            case "VILLAGER_YES" -> Sound.valueOf("ENTITY_VILLAGER_YES");
            default -> {
                C.warning("Unknown sound: " + name);
                yield null;
            }
        };
    }

    static void logload() {
        String banner = "\n"
                + "&7\n"
                + "&#F2C6DE██╗\n"
                + "&#F2C6DE██║        &4&lPLUGIN IS CURRENTLY LOADING, PLEASE WAIT...\n"
                + "&#F2C6DE██║\n"
                + "&#F2C6DE██║        &7Every project with this Development is Reserved\n"
                + "&#F2C6DE███████╗   &7and has a Copyright.\n"
                + "&#F2C6DE╚══════╝\n"
                + "&7\n";
        Bukkit.getConsoleSender().sendMessage(translate(banner));
    }
}
