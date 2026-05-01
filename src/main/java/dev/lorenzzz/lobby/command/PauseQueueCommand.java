package dev.lorenzzz.lobby.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.lorenzzz.lobby.Lobby;
import dev.lorenzzz.lobby.manager.QueueManager;
import org.bukkit.entity.Player;

@CommandAlias("pausequeue|pq")
public class PauseQueueCommand extends BaseCommand {

    private final Lobby plugin;

    public PauseQueueCommand(Lobby plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandPermission("lobby.admin")
    @CommandCompletion("@servers")
    public void onPauseQueue(Player player, @Single String server) {
        if (QueueManager.getInstance() == null) return;
        QueueManager.getInstance().togglePause(server, player);
    }
}