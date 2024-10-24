package me.lebogo.playermarkers.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.lebogo.playermarkers.PlayerMarkers;
import me.lebogo.playermarkers.gui.MarkersGUI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class MarkersCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        if (args.length != 0 && args[0].equals("update")) {
            PlayerMarkers.markerManager.updatePlayerMarkers();
            return;
        }

        Entity executor = commandSourceStack.getExecutor();
        if (!(executor instanceof Player player)) return;

        MarkersGUI.openInventory(player);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "playermarkers.command.markers";
    }
}
