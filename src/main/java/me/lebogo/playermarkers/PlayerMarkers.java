package me.lebogo.playermarkers;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.lebogo.playermarkers.commands.MarkerCommand;
import me.lebogo.playermarkers.commands.MarkersCommand;
import me.lebogo.playermarkers.listeners.InventoryListener;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;


public final class PlayerMarkers extends JavaPlugin {
    public static final Component PLAYER_MARKERS_TEXT = Component.text("Player Markers");
    public static final Component MANAGE_MARKER_TEXT = Component.text("Manage Marker");
    public static final Component BUY_MARKERS_TEXT = Component.text("Buy Marker");
    public static final Component DESTROY_MARKER_TEXT = Component.text("Destroy Marker");
    public static final Component MOVE_MARKER_TEXT = Component.text("Move Marker");
    public static final Component RENAME_MARKER_TEXT = Component.text("Rename Marker");
    public static final Component BUY_FOR_TEXT = Component.text("Buy for");
    public static final Component YES_TEXT = Component.text("Yes");
    public static final Component NO_TEXT = Component.text("No");
    public static final Component CANCEL_TEXT = Component.text("Cancel");
    public static final Component INSUFFICIENT_BALANCE_TEXT = Component.text("Insufficient balance");
    public static PlayerMarkerManager markerManager;
    public static Economy econ = null;
    public static FileConfiguration config = null;


    static {
        ConfigurationSerialization.registerClass(PlayerMarker.class, "PlayerMarker");
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        config = getConfig();

        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("marker", new MarkerCommand());
            commands.register("markers", new MarkersCommand());
        });

        getServer().getPluginManager().registerEvents(new InventoryListener(), this);

        markerManager = new PlayerMarkerManager(new File(getDataFolder(), "markers.yml"));

        // Update player markers 5 seconds after server start and every 5 minutes after that
        getServer().getScheduler().runTaskTimerAsynchronously(this, markerManager::updatePlayerMarkers, 20*5, 20 * 60 * 5);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

}
