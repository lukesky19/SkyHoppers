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
package com.github.lukesky19.skyHoppers.skyhopper;

import com.github.lukesky19.skyHoppers.database.DatabaseManager;
import com.github.lukesky19.skyHoppers.gui.GUIManager;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import org.bukkit.Location;
import org.bukkit.block.Container;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class manages {@link SkyHopper}s including storage, creation, and saving.
 */
public class SkyHopperDataManager {
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull GUIManager guiManager;

    private final @NotNull List<@NotNull Location> hopperLocations = new ArrayList<>();
    private final @NotNull Map<@NotNull Location, @NotNull SkyHopper> skyHopperMap = new HashMap<>();
    private final @NotNull Map<@NotNull Integer, @Nullable Map<Integer, @Nullable List<@NotNull Location>>> locationGrid = new HashMap<>();
    private final @NotNull Map<@NotNull Integer, @Nullable Map<Integer, @Nullable List<@NotNull SkyHopper>>> hopperGrid = new HashMap<>();

    /**
     * Constructor
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     */
    public SkyHopperDataManager(
            @NotNull DatabaseManager databaseManager,
            @NotNull GUIManager guiManager) {
        this.databaseManager = databaseManager;
        this.guiManager = guiManager;
    }

    /**
     * Get the {@link SkyHopper} at a given location.
     * @param location The {@link Location} of the SkyHopper.
     * @return The {@link SkyHopper} or null if there is no {@link SkyHopper} at that {@link Location}.
     */
    public @Nullable SkyHopper getSkyHopper(@NotNull Location location) {
        return skyHopperMap.get(location);
    }

    /**
     * Get a {@link List} of {@link SkyHopper}s that are loaded.
     * @return A {@link List} of {@link SkyHopper}s that are loaded.
     */
    public @NotNull List<SkyHopper> getSkyHoppersList() {
        return new ArrayList<>(skyHopperMap.values());
    }

    /**
     * Get a {@link Map} mapping {@link Location}s to {@link SkyHopper}s that are loaded.
     * @return A {@link Map} mapping {@link Location}s to {@link SkyHopper}s that are loaded.
     */
    public @NotNull Map<Location, SkyHopper> getSkyHoppersMap() {
        return skyHopperMap;
    }

    /**
     * Get a {@link List} of {@link SkyHopper}s that are between the chunkX and chunkZ provided.
     * @param chunkX The chunk's X coordinate.
     * @param chunkZ The chunk's Z coordinate.
     * @return A {@link List} of {@link SkyHopper}s inside the chunk bounds provided.
     */
    public @NotNull List<SkyHopper> getSkyHoppersInChunk(int chunkX, int chunkZ) {
        return new ArrayList<>(hopperGrid
                .computeIfAbsent(chunkX, k -> new HashMap<>())
                .computeIfAbsent(chunkZ, k -> new ArrayList<>()));
    }

    /**
     * Get a {@link List} of {@link Location}s that are between the chunkX and chunkZ provided.
     * @param chunkX The chunk's X coordinate.
     * @param chunkZ The chunk's Z coordinate.
     * @return A {@link List} of {@link Location}s inside the chunk bounds provided.
     */
    public @NotNull List<Location> getLocationsInChunk(int chunkX, int chunkZ) {
        return new ArrayList<>(locationGrid
                .computeIfAbsent(chunkX, k -> new HashMap<>())
                .computeIfAbsent(chunkZ, k -> new ArrayList<>()));
    }

    /**
     * Is there a SkyHopper at the {@link Location} provided?
     * This checks based on location and does not consider if the SkyHopper is loaded or not.
     * @param location The {@link Location} to check.
     * @return true if there is a SkyHopper at that location, otherwise false.
     */
    public boolean isLocationSkyHopper(@NotNull Location location) {
        return hopperLocations.contains(location);
    }

    /**
     * Saves a {@link SkyHopper} to the {@link #skyHopperMap}.
     * @param location The {@link Location} of the {@link SkyHopper}.
     * @param skyHopper The {@link SkyHopper}.
     */
    public void cacheSkyHopper(@NotNull Location location, @NotNull SkyHopper skyHopper) {
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;

        cacheLocation(location, chunkX, chunkZ);

        hopperGrid
                .computeIfAbsent(chunkX, k -> new HashMap<>())
                .computeIfAbsent(chunkZ, k -> new ArrayList<>())
                .add(skyHopper);

        skyHopperMap.put(location, skyHopper);
    }

    /**
     * Cache the {@link List} of {@link Location}s provided.
     * @param locationList The {@link List} of {@link Location}s to cache.
     */
    public void cacheLocations(@NotNull List<Location> locationList) {
        locationList.forEach(this::cacheLocation);
    }

    /**
     * Cache the location provided.
     * @param location The {@link Location} to cache.
     */
    public void cacheLocation(@NotNull Location location) {
        cacheLocation(location, location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    /**
     * Cache the location provided.
     * @param location The {@link Location} to cache.
     * @param chunkX The location's chunk's X coordinate.
     * @param chunkZ The location's chunk's Z coordinate.
     */
    public void cacheLocation(@NotNull Location location, int chunkX, int chunkZ) {
        if(!hopperLocations.contains(location)) {
            databaseManager.getHoppersTable().addSkyHopperLocation(location);

            hopperLocations.add(location);
        }

        locationGrid
                .computeIfAbsent(chunkX, k -> new HashMap<>())
                .computeIfAbsent(chunkZ, k -> new ArrayList<>())
                .add(location);
    }

    /**
     * Removes a {@link SkyHopper} from the cache, the {@link Location} database, and closes any open GUIs for the {@link SkyHopper}'s {@link Location}.
     * @param location The {@link Location} of the {@link SkyHopper}.
     */
    public void removeSkyHopper(@NotNull Location location) {
        databaseManager.getHoppersTable().removeSkyHopperLocation(location);

        guiManager.closeOpenGUIsForLocation(location);

        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;

        List<SkyHopper> hoppers = getSkyHoppersInChunk(chunkX, chunkZ);
        List<Location> locationList = getLocationsInChunk(chunkX, chunkZ);

        if(!hoppers.isEmpty()) {
            @Nullable SkyHopper skyHopper = skyHopperMap.get(location);
            if(skyHopper != null) {
                hoppers.remove(skyHopper);
            }

            if(hoppers.isEmpty()) {
                Map<Integer, List<SkyHopper>> chunkMap = hopperGrid.get(chunkX);
                if(chunkMap != null) {
                    chunkMap.remove(chunkZ);

                    if(chunkMap.isEmpty()) {
                        hopperGrid.remove(chunkX);
                    }
                }
            }
        }

        if(!locationList.isEmpty()) {
            locationList.remove(location);

            if(locationList.isEmpty()) {
                Map<Integer, List<Location>> locationMap = locationGrid.get(chunkX);

                if(locationMap != null) {
                    locationMap.remove(chunkZ);

                    if(locationMap.isEmpty()) {
                        hopperGrid.remove(chunkX);
                    }
                }
            }
        }

        locationList.remove(location);

        skyHopperMap.remove(location);
    }

    /**
     * Clears any stored data.
     */
    public void clearData() {
        hopperLocations.clear();
        locationGrid.clear();
        skyHopperMap.clear();
        hopperGrid.clear();
    }

    /**
     * Check if the container broken is linked to any SkyHoppers and refresh any open GUIs for that SkyHopper.
     * @param container The {@link Container} broken.
     */
    public void handleContainerBroken(@NotNull Container container) {
        Location containerLocation = container.getLocation();

        // Loop through all loaded SkyHoppers
        skyHopperMap.forEach((location, skyHopper) -> {
            // Loop through the SkyHopper's Linked Containers
            skyHopper.getLinkedContainers().forEach(skyContainer -> {
                // Check if the broken container matches a linked container's location
                if(skyContainer.getLocation().equals(containerLocation)) {
                    // Close any output filter GUIs for the SkyContainer provided
                    guiManager.closeOutputFilterGUIs(location);

                    // Refresh any other open GUIs for the SkyHopper.
                    guiManager.refreshViewersGUI(location);
                }
            });
        });
    }
}