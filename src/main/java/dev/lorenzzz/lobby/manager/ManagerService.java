package dev.lorenzzz.lobby.manager;

import dev.lorenzzz.lobby.Lobby;
import dev.lorenzzz.lobby.util.C;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ManagerService {

    @Getter
    private static ManagerService instance;
    private List<Manager> managers;
    private final Lobby core;

    public ManagerService(Lobby core) {
        this.core = core;
        instance = this;
    }

    public void registerManagers() {
        addManager(new BuildModeManager());
        addManager(new MenuManager(core));
        addManager(new PlayerVisibilityManager(core));
        addManager(new FlyManager(core));
        addManager(new EnderButtManager(core));
        addManager(new QueueManager(core));
        addManager(new JoinManager(core));
        addManager(new ProtectionManager(core));
        addManager(new ChatManager(core));
        addManager(new LaunchPadManager(core));
    }

    public void init() {
        this.managers = new ArrayList<>();
        registerManagers();
    }

    private void addManager(final Manager manager) {
        if (managers.contains(manager)) return;
        managers.add(manager);
        try {
            manager.start();
            C.info("&aLoaded manager: &f" + manager.getClass().getSimpleName());
        } catch (Exception e) {
            C.error("Error starting manager " + manager.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    public void shutdown() {
        if (managers == null) return;
        for (int i = managers.size() - 1; i >= 0; i--) {
            try {
                managers.get(i).stop();
            } catch (Exception ex) {
                C.error("Error stopping manager: " + ex.getMessage());
            }
        }
        managers.clear();
        instance = null;
    }
}