package me.lebogo.playermarkers.gui;

import me.lebogo.playermarkers.PlayerMarker;
import me.lebogo.playermarkers.PlayerMarkerManager;
import me.lebogo.playermarkers.PlayerMarkers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class MarkersGUI {
    private static ItemStack getMarkerItemStack(PlayerMarker playerMarker) {
        String text = playerMarker.getText();
        Vector position = playerMarker.getPosition();

        ItemStack nametagItemStack = new ItemStack(Material.NAME_TAG);
        nametagItemStack.editMeta(meta -> {
            meta.displayName(Component.text(text).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = List.of(
                    Component.text("X: " + ((int) position.getX()) +
                                   " Y: " + ((int) position.getY()) +
                                   " Z: " + ((int) position.getZ())
                    ).decoration(TextDecoration.ITALIC, false)
            );
            meta.lore(lore);
        });
        return nametagItemStack;
    }


    public static void openInventory(Player player) {
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
                int markerPrice = PlayerMarkerManager.getMarkerPrice(finalI);
                List<Component> lore = List.of(
                        Component.text("Price: $" + String.format("%,d", markerPrice)).decoration(TextDecoration.ITALIC, false)
                );
                meta.lore(lore);
            });

            markersInventory.setItem(i, paperItemStack);
        }

        player.openInventory(markersInventory);
    }


    public static void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();
        List<PlayerMarker> playerMarkers = PlayerMarkers.markerManager.getPlayerMarkers(uuid);
        int slot = event.getSlot();

        if (slot >= playerMarkers.size()) {
            int minSlot = playerMarkers.size();
            BuyGUI.openInventory(player, minSlot, PlayerMarkerManager.getMarkerPrice(minSlot));
        } else {
            ManageGUI.openInventory(player, slot);
        }
    }
}
