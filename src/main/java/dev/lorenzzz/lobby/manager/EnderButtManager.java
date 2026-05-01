package dev.lorenzzz.lobby.manager;

import dev.lorenzzz.lobby.Lobby;
import dev.lorenzzz.lobby.util.C;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class EnderButtManager implements Manager {

    @Getter
    private static EnderButtManager instance;
    private final Lobby plugin;

    public EnderButtManager(Lobby plugin) {
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
        return plugin.getSettings().getBoolean("enderbutt.ender-butt-enabled", false);
    }

    public void launch(Player player) {
        if (!isEnabled()) return;

        double velocity = plugin.getSettings().getDouble("enderbutt.ender-butt-velocity", 3.0);
        String type = plugin.getSettings().getString("enderbutt.ender-butt-type", "VELOCITY");

        if ("VELOCITY".equalsIgnoreCase(type)) {
            Vector direction = player.getLocation().getDirection().multiply(velocity);
            direction.setY(Math.max(direction.getY(), 0.5));
            player.setVelocity(direction);
        }

        String soundName = plugin.getSettings().getString("enderbutt.ender-butt-sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        Sound sound = C.resolveSound(soundName);
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }
}