package dev.lorenzzz.lobby.manager;

import dev.lorenzzz.lobby.Lobby;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BuildModeManager implements Manager {

    @Getter
    private static BuildModeManager instance;
    private final Set<UUID> buildModePlayers = new HashSet<>();

    @Override
    public void start() {
        instance = this;
    }

    @Override
    public void stop() {
        buildModePlayers.clear();
        instance = null;
    }

    public boolean isInBuildMode(UUID uuid) {
        return buildModePlayers.contains(uuid);
    }

    public boolean toggle(UUID uuid) {
        if (buildModePlayers.contains(uuid)) {
            buildModePlayers.remove(uuid);
            return false;
        }
        buildModePlayers.add(uuid);
        return true;
    }

    public void remove(UUID uuid) {
        buildModePlayers.remove(uuid);
    }
}