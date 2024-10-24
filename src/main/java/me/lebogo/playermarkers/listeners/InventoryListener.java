package me.lebogo.playermarkers.listeners;

import me.lebogo.playermarkers.PlayerMarkers;
import me.lebogo.playermarkers.gui.BuyGUI;
import me.lebogo.playermarkers.gui.DestroyGUI;
import me.lebogo.playermarkers.gui.ManageGUI;
import me.lebogo.playermarkers.gui.MarkersGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryListener implements Listener {


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Component title = event.getView().title();

        if (title.children().isEmpty()) return;
        if (event.getView().getBottomInventory().equals(event.getClickedInventory())) return;

        Component first = title.children().getFirst();
        if (PlayerMarkers.PLAYER_MARKERS_TEXT.equals(first)) {
            MarkersGUI.onInventoryClick(event);
        }

        if (PlayerMarkers.BUY_MARKERS_TEXT.equals(first)) {
            BuyGUI.onInventoryClick(event);
        }

        if (PlayerMarkers.MANAGE_MARKER_TEXT.equals(first)) {
            ManageGUI.onInventoryClick(event);
        }

        if (PlayerMarkers.DESTROY_MARKER_TEXT.equals(first)) {
            DestroyGUI.onInventoryClick(event);
        }
    }
}
