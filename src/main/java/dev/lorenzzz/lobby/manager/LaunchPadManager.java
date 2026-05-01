package dev.lorenzzz.lobby.manager;

import com.cryptomorin.xseries.XMaterial;
import dev.lorenzzz.lobby.Lobby;
import dev.lorenzzz.lobby.util.C;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.Optional;

public class LaunchPadManager implements Manager, Listener {

    @Getter
    private static LaunchPadManager instance;
    private final Lobby plugin;
    private Material pressurePlateType;
    private Material belowBlock;

    public LaunchPadManager(Lobby plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        instance = this;
        if (!plugin.getSettings().getBoolean("launch-pads.enabled", false)) return;

        String plateName = plugin.getSettings().getString("launch-pads.pressure-plate-type", "STONE_PRESSURE_PLATE");
        Optional<XMaterial> plate = XMaterial.matchXMaterial(plateName);
        pressurePlateType = plate.map(XMaterial::parseMaterial).orElse(null);

        String belowName = plugin.getSettings().getString("launch-pads.below-block", "REDSTONE_BLOCK");
        Optional<XMaterial> below = XMaterial.matchXMaterial(belowName);
        belowBlock = below.map(XMaterial::parseMaterial).orElse(null);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void stop() {
        instance = null;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (pressurePlateType == null || belowBlock == null) return;
        if (!event.hasChangedBlock()) return;

        Player player = event.getPlayer();
        Block block = player.getLocation().getBlock();
        Block below = block.getRelative(BlockFace.DOWN);

        if (block.getType() != pressurePlateType) return;
        if (below.getType() != belowBlock) return;

        double multiply = plugin.getSettings().getDouble("launch-pads.velocity.multiply", 1.5);
        double vertical = plugin.getSettings().getDouble("launch-pads.velocity.vertical", 1.1);

        Vector velocity = player.getLocation().getDirection().multiply(multiply);
        velocity.setY(vertical);
        player.setVelocity(velocity);

        String soundName = plugin.getSettings().getString("launch-pads.launch-sound", "");
        if (!soundName.isEmpty()) {
            Sound sound = C.resolveSound(soundName);
            if (sound != null) {
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
        }
    }
}