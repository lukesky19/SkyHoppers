/*
    SkyHoppers adds upgradable hoppers that can suction items, transfer items wirelessly to linked containers.
    Copyright (C) 2025  lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.skyHoppers.util;

import com.github.lukesky19.skyHoppers.hopper.FilterItems;
import com.google.gson.Gson;
import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Other methods used throughout the plugin.
 */
public class PluginUtils {
    /**
     * Gets a list of Locations to spawn particles at to create a hollow cube.
     * @param corner1 The corner of the first location.
     * @param corner2 The corner of the second location.
     * @param particleDistance The distance between particles.
     * @return A List of Locations
     */
    public static List<Location> getHollowCube(@NotNull Location corner1, @NotNull Location corner2, double particleDistance) {
        final List<Location> particleLocations = new ArrayList<>();

        // If the corners are not in the same world or the world is null, return an empty list.
        World world = corner1.getWorld();
        if (world == null || !corner1.getWorld().equals(corner2.getWorld())) {
            return particleLocations;
        }

        // Find the minimum X, Y, and Z coordinates.
        double minX = Math.min(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());

        // Find the maximum X, Y, and Z, coordinates.
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());

        // Calculate the Locations along the X-axis.
        for(double x = minX; x <= maxX; x += particleDistance) {
            particleLocations.add(new Location(world, x, minY, minZ));
            particleLocations.add(new Location(world, x, maxY, minZ));
            particleLocations.add(new Location(world, x, minY, maxZ));
            particleLocations.add(new Location(world, x, maxY, maxZ));
        }

        // Calculate the Locations along the Y-axis.
        for(double y = minY; y <= maxY; y += particleDistance) {
            particleLocations.add(new Location(world, minX, y, minZ));
            particleLocations.add(new Location(world, maxX, y, minZ));
            particleLocations.add(new Location(world, minX, y, maxZ));
            particleLocations.add(new Location(world, maxX, y, maxZ));
        }

        // Calculate the Locations along the Z-axis.
        for(double z = minZ; z <= maxZ; z += particleDistance) {
            particleLocations.add(new Location(world, minX, minY, z));
            particleLocations.add(new Location(world, maxX, minY, z));
            particleLocations.add(new Location(world, minX, maxY, z));
            particleLocations.add(new Location(world, maxX, maxY, z));
        }

        return particleLocations;
    }

    /**
     * A method to deserialize a List of Materials from a String. Use dfor legacy support from the predecessor of this version of SkyHoppers.
     * @param serialized The String containing the serialized Material list.
     * @return The List of Materials or an empty list if deserialization failed.
     */
    public static List<Material> deserializeMaterials(final String serialized) {
        if (serialized == null)
            return new ArrayList<>();

        return new Gson().fromJson(serialized, FilterItems.class)
                .filterItems()
                .stream()
                .map(Material::matchMaterial)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * A method to deserialize a location from a String. Used for legacy support from the predecessor of this version of SkyHoppers.
     * @param serialized The String containing the serialized location.
     * @return The Location or null if the deserialization failed.
     */
    public static Location deserializeLocation(final String serialized) {
        if (serialized == null)
            return null;

        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(new String(Base64.getDecoder().decode(serialized)));
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        @Nullable Location loc = config.getLocation("location");
        if (loc == null || loc.getWorld() == null)
            return null;

        return loc;
    }
}