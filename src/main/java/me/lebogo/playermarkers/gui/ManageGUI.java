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

public class ManageGUI {
    public static void openInventory(Player player, int markerIndex) {
        Inventory manageInventory = player.getServer().createInventory(player, InventoryType.HOPPER, Component.textOfChildren(
                PlayerMarkers.MANAGE_MARKER_TEXT,
                Component.space(),
                Component.text(markerIndex + 1)
        ));

        ItemStack placehoderItemStack = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        placehoderItemStack.editMeta(meta -> meta.displayName(Component.empty()));

        ItemStack renameItemStack = new ItemStack(Material.WRITABLE_BOOK);
        renameItemStack.editMeta(meta -> {
                    meta.displayName(PlayerMarkers.RENAME_MARKER_TEXT
                            .decoration(TextDecoration.ITALIC, false)
                            .color(TextColor.color(0xffffff))
                    );

                    meta.lore(List.of(
                            Component.text("Rename this marker using")
                                    .decoration(TextDecoration.ITALIC, false)
                                    .color(TextColor.color(0xffffff)),
                            Component.text("\"/marker " + (markerIndex + 1) + " rename <new name>\"")
                                    .decoration(TextDecoration.ITALIC, false)
                                    .color(TextColor.color(0xffffff))
                    ));
                }
        );

        ItemStack moveItemStack = new ItemStack(Material.ENDER_PEARL);
        moveItemStack.editMeta(meta -> {
                    meta.displayName(
                            Component.textOfChildren(PlayerMarkers.MOVE_MARKER_TEXT)
                                    .decoration(TextDecoration.ITALIC, false)
                                    .color(TextColor.color(0xffffff))
                    );

                    meta.lore(List.of(
                            Component.text("Move this marker to")
                                    .decoration(TextDecoration.ITALIC, false)
                                    .color(TextColor.color(0xffffff)),
                            Component.text("your current location.")
                                    .decoration(TextDecoration.ITALIC, false)
                                    .color(TextColor.color(0xffffff))
                    ));
                }
        );

        ItemStack destroyItemStack = new ItemStack(Material.TNT);
        destroyItemStack.editMeta(meta -> meta.displayName(
                        Component.textOfChildren(PlayerMarkers.DESTROY_MARKER_TEXT)
                                .decoration(TextDecoration.ITALIC, false)
                                .color(TextColor.color(0xFB5454))
                )
        );

        ItemStack cancelItemStack = new ItemStack(Material.BARRIER);
        cancelItemStack.editMeta(meta -> meta.displayName(
                Component.textOfChildren(PlayerMarkers.CANCEL_TEXT)
                        .decoration(TextDecoration.ITALIC, false)
                        .color(TextColor.color(0xFB5454))
        ));


        manageInventory.setItem(0, renameItemStack);
        manageInventory.setItem(1, moveItemStack);
        manageInventory.setItem(2, destroyItemStack);
        manageInventory.setItem(3, placehoderItemStack);
        manageInventory.setItem(4, cancelItemStack);

        player.openInventory(manageInventory);
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

        if (PlayerMarkers.CANCEL_TEXT.equals(children.getFirst())) {
            MarkersGUI.openInventory(player);
            return;
        }

        TextComponent lastComponent = (TextComponent) event.getView().title().children().getLast();

        if (PlayerMarkers.DESTROY_MARKER_TEXT.equals(children.getFirst())) {
            DestroyGUI.openInventory(player, Integer.parseInt(lastComponent.content()) - 1);
        }

        if (PlayerMarkers.MOVE_MARKER_TEXT.equals(children.getFirst())) {
            List<PlayerMarker> playerMarkers = PlayerMarkers.markerManager.getPlayerMarkers(player.getUniqueId());
            int markerId = Integer.parseInt(lastComponent.content()) - 1;

            PlayerMarker playerMarker = playerMarkers.get(markerId);
            playerMarker.setPosition(player.getLocation().toVector());
            playerMarkers.set(markerId, playerMarker);

            PlayerMarkers.markerManager.setPlayerMarkers(player.getUniqueId(), playerMarkers);

            player.sendMessage(Component.text("Successfully moved marker " + (markerId + 1)).color(TextColor.color(0x54FB54)));
            player.closeInventory();
        }
    }
}
