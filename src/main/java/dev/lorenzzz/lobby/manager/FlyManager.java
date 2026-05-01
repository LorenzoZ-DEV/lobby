package dev.lorenzzz.lobby.manager;

import dev.lorenzzz.lobby.Lobby;
import dev.lorenzzz.lobby.util.C;
import lombok.Getter;
import org.bukkit.entity.Player;

public class FlyManager implements Manager {

    @Getter
    private static FlyManager instance;
    private final Lobby plugin;

    public FlyManager(Lobby plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        instance = this;
    }

    @Override
    public void stop() {
        instance = null;
    }

    public boolean isEnabled() {
        return plugin.getSettings().getBoolean("hub-fly.enabled", false);
    }

    public void toggle(Player player) {
        if (!isEnabled()) return;
        boolean fly = !player.getAllowFlight();
        player.setAllowFlight(fly);
        player.setFlying(fly);
        String key = fly ? "FLY-TOGGLED-ON" : "FLY-TOGGLED-OFF";
        player.sendMessage(C.translate(plugin.getMessages().getString("prefix", "") + " "
                + plugin.getMessages().getString(key, "")));
    }

    public void applyOnJoin(Player player) {
        if (!isEnabled()) return;
        if (player.hasPermission("lobby.fly")) {
            player.setAllowFlight(true);
        }
    }
}