package dev.lorenzzz.lobby.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import dev.lorenzzz.lobby.Lobby;
import dev.lorenzzz.lobby.manager.QueueManager;
import org.bukkit.entity.Player;

@CommandAlias("leavequeue|lq")
public class LeaveQueueCommand extends BaseCommand {

    private final Lobby plugin;

    public LeaveQueueCommand(Lobby plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onLeaveQueue(Player player) {
        if (QueueManager.getInstance() == null) return;
        QueueManager.getInstance().leaveQueue(player);
    }
}