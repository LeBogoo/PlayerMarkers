package me.lebogo.playermarkers.gui;

import me.lebogo.playermarkers.PlayerMarker;
import me.lebogo.playermarkers.PlayerMarkers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class DestroyGUI {

    public static void openInventory(Player player, int markerIndex) {
        Inventory destroyInventory = player.getServer().createInventory(player, InventoryType.HOPPER, Component.textOfChildren(
                PlayerMarkers.DESTROY_MARKER_TEXT,
                Component.space(),
                Component.text(markerIndex + 1),
                Component.text("?")
        ));

        ItemStack placehoderItemStack = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        placehoderItemStack.editMeta(meta -> meta.displayName(Component.empty()));

        ItemStack yesItemStack = new ItemStack(Material.LIME_STAINED_GLASS);
        yesItemStack.editMeta(meta -> meta.displayName(Component.textOfChildren(
                        PlayerMarkers.YES_TEXT)
                .decoration(TextDecoration.ITALIC, false)
                .color(TextColor.color(0x54FB54))
        ));

        ItemStack noItemStack = new ItemStack(Material.RED_STAINED_GLASS);
        noItemStack.editMeta(meta -> meta.displayName(
                Component.textOfChildren(PlayerMarkers.NO_TEXT)
                        .decoration(TextDecoration.ITALIC, false)
                        .color(TextColor.color(0xFB5454))
        ));

        destroyInventory.setItem(0, placehoderItemStack);
        destroyInventory.setItem(1, yesItemStack);
        destroyInventory.setItem(2, placehoderItemStack);
        destroyInventory.setItem(3, noItemStack);
        destroyInventory.setItem(4, placehoderItemStack);

        player.openInventory(destroyInventory);
    }


    public static void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();

        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null) return;
        ItemMeta itemMeta = currentItem.getItemMeta();
        Component displayName = itemMeta.displayName();
        if (displayName == null) return;
        List<Component> children = displayName.children();
        if (children.isEmpty()) return;

        List<Component> viewChildren = event.getView().title().children();
        TextComponent markerIdComponent = (TextComponent) viewChildren.get(viewChildren.size() - 2);
        int markerId = Integer.parseInt(markerIdComponent.content()) - 1;

        if (PlayerMarkers.YES_TEXT.equals(children.getFirst())) {
            List<PlayerMarker> playerMarkers = PlayerMarkers.markerManager.getPlayerMarkers(player.getUniqueId());
            playerMarkers.remove(markerId);
            PlayerMarkers.markerManager.setPlayerMarkers(player.getUniqueId(), playerMarkers);

            player.sendMessage(Component.text("Successfully destroyed marker " + (markerId + 1)).color(TextColor.color(0x54FB54)));
            player.closeInventory();
            return;
        }

        if (PlayerMarkers.NO_TEXT.equals(children.getFirst())) {
            ManageGUI.openInventory(player, markerId);
        }
    }
}
