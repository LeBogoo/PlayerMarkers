package me.lebogo.playermarkers.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.lebogo.playermarkers.PlayerMarker;
import me.lebogo.playermarkers.PlayerMarkers;
import me.lebogo.playermarkers.gui.ManageGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MarkerCommand implements BasicCommand {


    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        Entity executor = commandSourceStack.getExecutor();
        if (!(executor instanceof Player player)) return;

        List<PlayerMarker> playerMarkers = PlayerMarkers.markerManager.getPlayerMarkers(player.getUniqueId());

        int markerId = -1;
        try {
            markerId = Integer.parseInt(args[0]);
            markerId--;
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Marker \"" + args[0] + "\" does not exist.").color(TextColor.color(0xFB5454)));
            return;
        }

        if (markerId < 0 || markerId >= playerMarkers.size()) {
            player.sendMessage(Component.text("Marker \"" + args[0] + "\" does not exist.").color(TextColor.color(0xFB5454)));
            return;
        }

        PlayerMarker playerMarker = playerMarkers.get(markerId);

        if (args.length == 1) {
            ManageGUI.openInventory(player, markerId);
        }

        if (args.length < 2) return;

        if (args[1].equalsIgnoreCase("rename")) {
            String newName = String.join(" ", args).substring(args[0].length() + args[1].length() + 2);
            playerMarker.setText(newName);
            player.sendMessage(Component.text("Marker was renamed to \"" + newName + "\".").color(TextColor.color(0x54fb54)));
            PlayerMarkers.markerManager.setPlayerMarkers(player.getUniqueId(), playerMarkers);
        } else if (args[1].equalsIgnoreCase("move")) {
            playerMarker.setPosition(player.getLocation().toVector());
            player.sendMessage(Component.text("Marker was moved to your current location.").color(TextColor.color(0x54fb54)));
            PlayerMarkers.markerManager.setPlayerMarkers(player.getUniqueId(), playerMarkers);
        } else {
            player.sendMessage(Component.text("Marker action \"" + args[1] + "\" does not exist.").color(TextColor.color(0xFB5454)));
        }

    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        Entity executor = commandSourceStack.getExecutor();
        if (!(executor instanceof Player player)) return List.of();

        List<PlayerMarker> playerMarkers = PlayerMarkers.markerManager.getPlayerMarkers(player.getUniqueId());

        int size = playerMarkers.size();

        if (args.length <= 1) {
            // list of numbers from 1 to size
            List<String> suggestions = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                if (args.length == 1 && !String.valueOf(i + 1).startsWith(args[0])) {
                    continue;
                }
                suggestions.add(String.valueOf(i + 1));
            }
            return suggestions;
        }

        if (args.length == 2) {
            return List.of("rename", "move");
        }

        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "playermarkers.command.marker";
    }
}
