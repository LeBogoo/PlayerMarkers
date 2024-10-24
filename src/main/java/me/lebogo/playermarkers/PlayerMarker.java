package me.lebogo.playermarkers;

import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.api.markers.POIMarker;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;


@SerializableAs("PlayerMarker")
public class PlayerMarker implements ConfigurationSerializable {
    private String text;
    private Vector position;
    private String worldName;

    public PlayerMarker(String text, Vector position, String worldName) {
        this.text = text;
        this.position = position;
        this.worldName = worldName;
    }

    public static PlayerMarker deserialize(Map<String, Object> args) {
        String name = (String) args.getOrDefault("text", null);
        Vector position = (Vector) args.getOrDefault("position", null);
        String worldName = (String) args.getOrDefault("worldName", null);

        return new PlayerMarker(name, position, worldName);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("text", text);
        result.put("position", position);
        result.put("worldName", worldName);

        return result;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Vector getPosition() {
        return position;
    }

    public void setPosition(Vector position) {
        this.position = position;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public POIMarker toPOIMarker() {
        return new POIMarker(text, new Vector3d(position.getX(), position.getY() + 0.5, position.getZ() ));
    }

    @Override
    public int hashCode() {
        return text.hashCode() + position.hashCode();
    }
}
