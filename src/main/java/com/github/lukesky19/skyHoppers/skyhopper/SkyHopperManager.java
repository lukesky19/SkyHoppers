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

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.LocaleManager;
import com.github.lukesky19.skyHoppers.config.SettingsManager;
import com.github.lukesky19.skyHoppers.database.DatabaseManager;
import com.github.lukesky19.skyHoppers.gui.GUIManager;
import com.github.lukesky19.skyHoppers.skyhopper.data.HopperKeys;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skyHoppers.util.ImmutableLocation;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * This class manages the classes that manage SkyHopper data management, loading, saving, and creation.
 */
public class SkyHopperManager {
    private final @NotNull SkyHoppers skyHoppers;
    private final @NotNull ComponentLogger logger;
    private final @NotNull DatabaseManager databaseManager;

    private final @NotNull SkyHopperDataManager skyHopperDataManager;
    private final @NotNull SkyHopperProcessor skyHopperProcessor;
    private final @NotNull SkyHopperSaver skyHopperSaver;
    private final @NotNull SkyHopperCreator skyHopperCreator;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     */
    public SkyHopperManager(
            @NotNull SkyHoppers skyHoppers,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull DatabaseManager databaseManager,
            @NotNull GUIManager guiManager) {
        this.skyHoppers = skyHoppers;
        this.logger = skyHoppers.getComponentLogger();
        this.databaseManager = databaseManager;

        this.skyHopperDataManager = new SkyHopperDataManager(databaseManager, guiManager);
        this.skyHopperProcessor = new SkyHopperProcessor(skyHoppers, settingsManager, localeManager, this);
        this.skyHopperSaver = new SkyHopperSaver(skyHoppers);
        this.skyHopperCreator = new SkyHopperCreator(skyHoppers, settingsManager, skyHopperSaver);
    }

    /**
     * Get the {@link SkyHopperDataManager} class.
     * @return The {@link SkyHopperDataManager} class.
     */
    public @NotNull SkyHopperDataManager getSkyHopperDataManager() {
        return skyHopperDataManager;
    }

    /**
     * Get the {@link SkyHopperProcessor} class.
     * @return The {@link SkyHopperProcessor} class.
     */
    public @NotNull SkyHopperProcessor getSkyHopperProcessor() {
        return skyHopperProcessor;
    }

    /**
     * Get the {@link SkyHopperSaver} class.
     * @return The {@link SkyHopperSaver} class.
     */
    public @NotNull SkyHopperSaver getSkyHopperSaver() {
        return skyHopperSaver;
    }

    /**
     * Get the {@link SkyHopperCreator} class.
     * @return The {@link SkyHopperCreator} class.
     */
    public @NotNull SkyHopperCreator getSkyHopperCreator() {
        return skyHopperCreator;
    }

    /**
     * Migrates the legacy database if needed, caches all SkyHopper locations, and queues chunks to load SkyHoppers.
     */
    public void reload() {
        getSkyHopperDataManager().clearData();

        // Migrates the old database to the new
        databaseManager.migrateLegacyDatabase().thenAccept(v1 -> {
            // Load SkyHopper Locations
            databaseManager.getHoppersTable().getSkyHopperLocations().thenAccept(list -> {
                if(list.isEmpty()) {
                    logger.warn(AdventureUtil.deserialize("SkyHopper Locations List from the database is empty."));
                    return;
                }

                // Cache Locations
                skyHopperDataManager.cacheLocations(list);

                // Load SkyHoppers in loaded Chunks
                skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () ->
                        skyHoppers.getServer().getWorlds()
                                .forEach(world -> Arrays.stream(world.getLoadedChunks())
                                        .forEach(skyHopperProcessor::loadSkyHoppersInChunk)), 1L);
            }).exceptionally(ex -> {
                logger.warn(AdventureUtil.deserialize("Failed to get SkyHopper Locations from the database. " + ex.getMessage()));
                return null;
            });
        }).exceptionally(ex -> {
            logger.warn(AdventureUtil.deserialize("Failed to migrate legacy database. " + ex.getMessage()));
            return null;
        });
    }

    /**
     * Checks if an {@link ItemStack} is a {@link SkyHopper}.
     * @param itemStack The {@link ItemStack} to check.
     * @return true if a {@link SkyHopper}, false if not.
     */
    public boolean isItemStackSkyHopper(@NotNull ItemStack itemStack) {
        return itemStack.getItemMeta().getPersistentDataContainer()
                .get(HopperKeys.ENABLED.getKey(), PersistentDataType.INTEGER) != null;
    }

    /**
     * Is there a SkyHopper at the {@link Location} provided?
     * This checks based on location and does not consider if the SkyHopper is loaded or not.
     * @param location The {@link ImmutableLocation} to check.
     * @return true if there is a SkyHopper at that location, otherwise false.
     */
    public boolean isLocationSkyHopper(@NotNull ImmutableLocation location) {
        return getSkyHopperDataManager().isLocationSkyHopper(location);
    }
}
