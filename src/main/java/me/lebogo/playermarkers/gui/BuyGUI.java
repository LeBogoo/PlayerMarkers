package me.lebogo.playermarkers.gui;

import me.lebogo.playermarkers.PlayerMarker;
import me.lebogo.playermarkers.PlayerMarkers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class BuyGUI {
    public static void openInventory(Player player, int markerIndex, int price) {
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

    public static void onInventoryClick(InventoryClickEvent event) {
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
            MarkersGUI.openInventory(player);
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

        playerMarkers.add(new PlayerMarker("My Marker", player.getLocation().toVector(), player.getLocation().getWorld().getName()));

        PlayerMarkers.markerManager.setPlayerMarkers(uuid, playerMarkers);

        player.closeInventory();
        player.sendMessage(Component.textOfChildren(
                Component.text("You have successfully bought a new marker!"),
                Component.newline(),
                Component.text("You can customize it in the markers menu.")
        ).color(TextColor.color(0x54FB54)));

    }
}
