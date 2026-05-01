package dev.lorenzzz.lobby.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.lorenzzz.lobby.Lobby;
import dev.lorenzzz.lobby.manager.QueueManager;
import dev.lorenzzz.lobby.util.C;
import org.bukkit.entity.Player;

@CommandAlias("joinqueue|jq")
public class QueueCommand extends BaseCommand {

    private final Lobby plugin;

    public QueueCommand(Lobby plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("@servers")
    public void onJoinQueue(Player player, @Single String server) {
        if (QueueManager.getInstance() == null) return;
        QueueManager.getInstance().joinQueue(player, server);
    }
}