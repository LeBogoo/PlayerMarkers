package me.lebogo.playermarkers.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.lebogo.playermarkers.PlayerMarker;
import me.lebogo.playermarkers.PlayerMarkers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class MarkersCommand implements BasicCommand {
    @NotNull
    private static ItemStack getMarkerItemStack(PlayerMarker playerMarker) {
        String text = playerMarker.getText();
        Location location = playerMarker.getLocation();

        ItemStack nametagItemStack = new ItemStack(Material.NAME_TAG);
        nametagItemStack.editMeta(meta -> {
            meta.displayName(Component.text(text).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = List.of(
                    Component.text("X: " + location.getBlock().getX() +
                                   " Y: " + location.getBlock().getY() +
                                   " Z: " + location.getBlock().getZ()
                    ).decoration(TextDecoration.ITALIC, false)
            );
            meta.lore(lore);
        });
        return nametagItemStack;
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        if (args.length != 0 && args[0].equals("update")) {
            PlayerMarkers.markerManager.updatePlayerMarkers();
            return;
        }

        Entity executor = commandSourceStack.getExecutor();
        if (!(executor instanceof Player player)) return;

        Inventory markersInventory = player.getServer().createInventory(player, InventoryType.HOPPER, Component.textOfChildren(PlayerMarkers.PLAYER_MARKERS_TEXT));

        List<PlayerMarker> playerMarkers = PlayerMarkers.markerManager.getPlayerMarkers(player.getUniqueId());

        for (PlayerMarker playerMarker : playerMarkers) {
            int index = playerMarkers.indexOf(playerMarker);
            ItemStack nametagItemStack = getMarkerItemStack(playerMarker);
            markersInventory.setItem(index, nametagItemStack);
        }

        for (int i = playerMarkers.size(); i < 5; i++) {
            ItemStack paperItemStack = new ItemStack(Material.PAPER);
            int finalI = i;
            paperItemStack.editMeta(meta -> {
                meta.displayName(Component.text("Buy new Marker").decoration(TextDecoration.ITALIC, false));
                List<Component> lore = List.of(
                        Component.text("Price: $" + String.format("%,d", (((finalI - 1) * 5000) + 10000))).decoration(TextDecoration.ITALIC, false)
                );
                meta.lore(lore);
            });

            markersInventory.setItem(i, paperItemStack);
        }

        player.openInventory(markersInventory);

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
