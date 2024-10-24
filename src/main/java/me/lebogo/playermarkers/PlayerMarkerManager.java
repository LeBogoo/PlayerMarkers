package me.lebogo.playermarkers;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerMarkerManager {
    YamlConfiguration markersConfig = new YamlConfiguration();
    Map<UUID, List<PlayerMarker>> playerMarkers = new HashMap<>();
    File markersFile;

    public PlayerMarkerManager(File markersFile) {
        this.markersFile = markersFile;
        try {
            markersConfig.load(markersFile);
        } catch (IOException | InvalidConfigurationException ignored) {
        }

        for (String playerUUIDString : markersConfig.getKeys(false)) {
            UUID playerUUID = UUID.fromString(playerUUIDString);
            List<PlayerMarker> playerMarkerList = (List<PlayerMarker>) markersConfig.getList(playerUUIDString);
            playerMarkers.put(playerUUID, playerMarkerList);
        }
    }

    public static int getMarkerPrice(int index) {
        int markerPrice = PlayerMarkers.config.getInt("markerPrice", 5000);
        double markerMultiplier = PlayerMarkers.config.getDouble("markerMultiplier", 1);
        return (int) (markerPrice + (0.0 + index * markerMultiplier * markerPrice));
    }

    public List<PlayerMarker> getPlayerMarkers(UUID playerUUID) {
        return playerMarkers.getOrDefault(playerUUID, new ArrayList<>());
    }

    public void setPlayerMarkers(UUID playerUUID, List<PlayerMarker> playerMarkerList) {
        updatePlayerMarkers();

        playerMarkers.put(playerUUID, playerMarkerList);
        markersConfig.set(playerUUID.toString(), playerMarkerList);
        try {
            markersConfig.save(markersFile);
        } catch (IOException ignored) {
        }
    }

    public void updatePlayerMarkers() {
        BlueMapAPI.getInstance().ifPresent(api -> {
            Collection<BlueMapWorld> worlds = api.getWorlds();
            Map<String, BlueMapWorld> worldMap = new HashMap<>();
            for (BlueMapWorld world : worlds) {
                String worldName = world.getId().split("#")[0];
                worldMap.put(worldName, world);
            }

            Map<String, List<PlayerMarker>> playerMarkersByWorld = new HashMap<>();
            List<PlayerMarker> allMarkers = getAllMarkers();

            for (PlayerMarker playerMarker : allMarkers) {
                String worldName = playerMarker.getWorldName();
                List<PlayerMarker> markers = playerMarkersByWorld.getOrDefault(worldName, new ArrayList<>());
                markers.add(playerMarker);
                playerMarkersByWorld.put(worldName, markers);
            }

            Set<String> blueMapWorldNames = worldMap.keySet();
            for (String worldName : blueMapWorldNames) {
                BlueMapWorld blueMapWorld = worldMap.get(worldName);
                MarkerSet markerSet = new MarkerSet("Player Markers");
                List<PlayerMarker> markers = playerMarkersByWorld.getOrDefault(worldName, new ArrayList<>());

                for (PlayerMarker playerMarker : markers) {
                    markerSet.put(String.valueOf(playerMarker.hashCode()), playerMarker.toPOIMarker());
                }

                Collection<BlueMapMap> maps = blueMapWorld.getMaps();
                for (BlueMapMap map : maps) {
                    map.getMarkerSets().put("player-markers", markerSet);
                }

            }

        });
    }

    public List<PlayerMarker> getAllMarkers() {
        return playerMarkers.values().stream().reduce((markers1, markers2) -> {
            markers1.addAll(markers2);
            return markers1;
        }).orElse(new ArrayList<>());
    }
}
