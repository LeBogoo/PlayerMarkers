package me.lebogo.playermarkers;

import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.api.markers.POIMarker;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;


@SerializableAs("PlayerMarker")
public class PlayerMarker implements ConfigurationSerializable {
    private String text;
    private Location location;

    public PlayerMarker(String text, Location location) {
        this.text = text;
        this.location = location;
    }

    public static PlayerMarker deserialize(Map<String, Object> args) {
        String name = (String) args.getOrDefault("text", null);
        Location location = (Location) args.getOrDefault("location", null);

        return new PlayerMarker(name, location);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("text", text);
        result.put("location", location);

        return result;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public POIMarker toPOIMarker() {
        return new POIMarker(text, new Vector3d(location.getBlockX() + 0.5, location.getBlockY() + 0.5, location.getBlockZ() + 0.5));
    }

    @Override
    public int hashCode() {
        return text.hashCode() + location.hashCode();
    }
}
