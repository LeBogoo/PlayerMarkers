package me.lebogo.playermarkers.listeners;

import me.lebogo.playermarkers.PlayerMarker;
import me.lebogo.playermarkers.PlayerMarkers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class InventoryListener implements Listener {


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Component title = event.getView().title();

        if (title.children().isEmpty()) return;
        if (event.getView().getBottomInventory().equals(event.getClickedInventory())) return;

        Component first = title.children().getFirst();
        if (PlayerMarkers.PLAYER_MARKERS_TEXT.equals(first)) {
            playerMarkersInventoryClick(event);
        }

        if (PlayerMarkers.BUY_MARKERS_TEXT.equals(first)) {
            buyInventoryClick(event);
        }

        if (PlayerMarkers.MANAGE_MARKER_TEXT.equals(first)) {
            manageInventoryClick(event);
        }

        if (PlayerMarkers.DESTROY_MARKER_TEXT.equals(first)) {
            destroyInventoryClick(event);
        }
    }

    private void playerMarkersInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();
        List<PlayerMarker> playerMarkers = PlayerMarkers.markerManager.getPlayerMarkers(uuid);
        int slot = event.getSlot();

        if (slot >= playerMarkers.size()) {
            // new marker
            openBuyInventory(player, slot, (slot + 1) * 5000);
        } else {
            PlayerMarker marker = playerMarkers.get(slot);
            openManageInventory(player, slot, marker);
        }

    }

    private void buyInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();

        List<PlayerMarker> playerMarkers = PlayerMarkers.markerManager.getPlayerMarkers(uuid);

        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null) return;
        ItemMeta itemMeta = currentItem.getItemMeta();
        Component displayName = itemMeta.displayName();
        if (displayName == null) return;
        List<Component> children = displayName.children();
        if (children.isEmpty()) return;

        if (PlayerMarkers.CANCEL_TEXT.equals(children.getFirst())) {
            player.closeInventory();
            return;
        }

        if (!PlayerMarkers.BUY_FOR_TEXT.equals(children.getFirst())) return;

        TextComponent last = (TextComponent) children.getLast();
        String priceString = last.content().replace("$", "").replaceAll("\\.", "").replaceAll(",", "").trim();
        int price = Integer.parseInt(priceString);

        EconomyResponse economyResponse = PlayerMarkers.econ.withdrawPlayer(player, price);

        if (economyResponse.type.equals(EconomyResponse.ResponseType.FAILURE)) {
            player.sendMessage(Component.text("Something went wrong during the transaction: " + economyResponse.errorMessage).color(TextColor.color(0xFB5454)));
            return;
        }

        playerMarkers.add(new PlayerMarker("My Marker", player.getLocation()));

        PlayerMarkers.markerManager.setPlayerMarkers(uuid, playerMarkers);

        player.closeInventory();
        player.sendMessage(Component.textOfChildren(
                Component.text("You have successfully bought a new marker!"),
                Component.newline(),
                Component.text("You can customize it in the markers menu.")
        ).color(TextColor.color(0x54FB54)));
    }

    private void manageInventoryClick(InventoryClickEvent event) {
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
            player.closeInventory();
            return;
        }

        TextComponent lastComponent = (TextComponent) event.getView().title().children().getLast();

        if (PlayerMarkers.DESTROY_MARKER_TEXT.equals(children.getFirst())) {
            openDestroyInventory(player, Integer.parseInt(lastComponent.content()) - 1);
        }
    }

    private void destroyInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();

        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null) return;
        ItemMeta itemMeta = currentItem.getItemMeta();
        Component displayName = itemMeta.displayName();
        if (displayName == null) return;
        List<Component> children = displayName.children();
        if (children.isEmpty()) return;

        if (PlayerMarkers.YES_TEXT.equals(children.getFirst())) {
            List<Component> viewChildren = event.getView().title().children();
            TextComponent markerIdComponent = (TextComponent) viewChildren.get(viewChildren.size() - 2);
            int markerId = Integer.parseInt(markerIdComponent.content()) - 1;

            System.out.println("Marker id: " + markerId);

            List<PlayerMarker> playerMarkers = PlayerMarkers.markerManager.getPlayerMarkers(player.getUniqueId());
            playerMarkers.remove(markerId);
            PlayerMarkers.markerManager.setPlayerMarkers(player.getUniqueId(), playerMarkers);

            player.sendMessage(Component.text("Successfully destroyed marker " + (markerId + 1)).color(TextColor.color(0x54FB54)));
            player.closeInventory();
            return;
        }

        if (PlayerMarkers.NO_TEXT.equals(children.getFirst())) {
            player.closeInventory();
            return;
        }

    }


    private void openBuyInventory(Player player, int markerIndex, int price) {
        Inventory buyInventory = player.getServer().createInventory(player, InventoryType.HOPPER, Component.textOfChildren(
                PlayerMarkers.BUY_MARKERS_TEXT,
                Component.space(),
                Component.text(markerIndex + 1),
                Component.space(),
                Component.text("for"),
                Component.space(),
                Component.text("$" + String.format("%,d", price))
        ));

        double balance = PlayerMarkers.econ.getBalance(player);

        ItemStack placehoderItemStack = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        placehoderItemStack.editMeta(meta -> meta.displayName(Component.empty()));

        ItemStack buyItemStack = new ItemStack(Material.LIME_STAINED_GLASS);
        buyItemStack.editMeta(meta -> meta.displayName(Component.textOfChildren(
                        PlayerMarkers.BUY_FOR_TEXT,
                        Component.space(),
                        Component.text("$" + String.format("%,d", price)))
                .decoration(TextDecoration.ITALIC, false)
                .color(TextColor.color(0x54FB54))
        ));

        ItemStack notEnoughMoneyItemStack = new ItemStack(Material.RED_STAINED_GLASS);
        notEnoughMoneyItemStack.editMeta(meta -> meta.displayName(
                Component.textOfChildren(PlayerMarkers.INSUFFICIENT_BALANCE_TEXT)
                        .decoration(TextDecoration.ITALIC, false)
                        .color(TextColor.color(0xFB5454))
        ));

        ItemStack cancelItemStack = new ItemStack(Material.BARRIER);
        cancelItemStack.editMeta(meta -> meta.displayName(
                Component.textOfChildren(PlayerMarkers.CANCEL_TEXT)
                        .decoration(TextDecoration.ITALIC, false)
                        .color(TextColor.color(0xFB5454))
        ));


        buyInventory.setItem(0, placehoderItemStack);
        if (price > balance) {
            buyInventory.setItem(1, notEnoughMoneyItemStack);
        } else {
            buyInventory.setItem(1, buyItemStack);
        }
        buyInventory.setItem(2, placehoderItemStack);
        buyInventory.setItem(3, cancelItemStack);
        buyInventory.setItem(4, placehoderItemStack);

        player.openInventory(buyInventory);
    }


    private void openManageInventory(Player player, int markerIndex, PlayerMarker marker) {
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
        manageInventory.setItem(1, placehoderItemStack);
        manageInventory.setItem(2, destroyItemStack);
        manageInventory.setItem(3, placehoderItemStack);
        manageInventory.setItem(4, cancelItemStack);

        player.openInventory(manageInventory);
    }

    private void openDestroyInventory(Player player, int markerIndex) {
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
}
