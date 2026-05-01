package dev.lorenzzz.lobby.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.lorenzzz.lobby.Lobby;
import dev.lorenzzz.lobby.manager.*;
import dev.lorenzzz.lobby.util.C;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@CommandAlias("lobby|hub|ninfeahub")
public class LobbyCommand extends BaseCommand {

    private final Lobby plugin;

    public LobbyCommand(Lobby plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onDefault(Player player) {
        if (MenuManager.getInstance() != null) {
            MenuManager.getInstance().openMenu(player, "menu2");
        }
    }

    @Subcommand("reload")
    @CommandPermission("lobby.admin")
    public void onReload(Player player) {
        long start = System.currentTimeMillis();
        player.sendMessage(C.translate(prefix() + " " + plugin.getMessages().getString("RELOAD", "")));

        plugin.getMessages().reload();
        plugin.getMenus().reload();
        plugin.getSettings().reload();
        plugin.getActionbar().reload();
        plugin.getCore().reload();

        if (MenuManager.getInstance() != null) {
            MenuManager.getInstance().reload();
        }

        long elapsed = System.currentTimeMillis() - start;
        String msg = plugin.getMessages().getString("RELOADED", "")
                .replace("<ms>", String.valueOf(elapsed));
        player.sendMessage(C.translate(prefix() + " " + msg));
    }

    @Subcommand("setspawn")
    @CommandPermission("lobby.admin")
    public void onSetSpawn(Player player) {
        Location loc = player.getLocation();
        plugin.getSettings().set("spawn.world", loc.getWorld().getName());
        plugin.getSettings().set("spawn.x", loc.getX());
        plugin.getSettings().set("spawn.y", loc.getY());
        plugin.getSettings().set("spawn.z", loc.getZ());
        plugin.getSettings().set("spawn.yaw", loc.getYaw());
        plugin.getSettings().set("spawn.pitch", loc.getPitch());
        plugin.getSettings().saveFile();

        player.sendMessage(C.translate(prefix() + " " + plugin.getMessages().getString("spawn-set", "")));
    }

    @Subcommand("buildmode")
    @CommandPermission("lobby.admin")
    public void onBuildMode(Player player) {
        if (BuildModeManager.getInstance() == null) return;

        boolean enabled = BuildModeManager.getInstance().toggle(player.getUniqueId());
        String key = enabled ? "BUILDMODE-ON" : "BUILDMODE-OFF";
        player.sendMessage(C.translate(prefix() + " " + plugin.getMessages().getString(key, "")));
    }

    @Subcommand("buildmode")
    @CommandPermission("lobby.admin")
    @CommandCompletion("@players")
    public void onBuildModeOther(Player player, @Single String targetName) {
        if (BuildModeManager.getInstance() == null) return;

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(C.translate(prefix() + " " + plugin.getMessages().getString("BUILDMODE-ERROR", "")));
            return;
        }

        boolean enabled = BuildModeManager.getInstance().toggle(target.getUniqueId());
        String key = enabled ? "BUILDMODE-ON-OTHER" : "BUILDMODE-OFF-OTHER";
        String msg = plugin.getMessages().getString(key, "").replace("<player>", target.getName());
        player.sendMessage(C.translate(prefix() + " " + msg));
    }

    @Subcommand("fly")
    @CommandPermission("lobby.fly")
    public void onFly(Player player) {
        if (FlyManager.getInstance() != null) {
            FlyManager.getInstance().toggle(player);
        }
    }

    private String prefix() {
        return plugin.getMessages().getString("prefix", "");
    }
}