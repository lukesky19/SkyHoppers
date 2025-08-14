/*
    SkyHoppers adds upgradable hoppers that can suction items, transfer items wirelessly to linked containers.
    Copyright (C) 2025 lukeskywlker19

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
package com.github.lukesky19.skyHoppers.database.table;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.database.QueueManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.database.parameter.impl.IntegerParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.StringParameter;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This class is used to create and interface with the hoppers table in the database.
 */
public class HoppersTable {
    private final @NotNull SkyHoppers skyHoppers;
    private final @NotNull ComponentLogger logger;
    private final @NotNull QueueManager queueManager;
    private final @NotNull String tableName = "skyhoppers_hoppers";

    /**
     * Default Constructor.
     * You should use {@link #HoppersTable(SkyHoppers, QueueManager)} instead.
     * @deprecated You should use {@link #HoppersTable(SkyHoppers, QueueManager)} instead.
     */
    @Deprecated
    public HoppersTable() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param queueManager A {@link QueueManager} instance.
     */
    public HoppersTable(@NotNull SkyHoppers skyHoppers, @NotNull QueueManager queueManager) {
        this.skyHoppers = skyHoppers;
        this.logger = skyHoppers.getComponentLogger();
        this.queueManager = queueManager;
    }

    /**
     * Creates the table in the database if it doesn't exist.
     */
    public void createTable() {
        String tableCreationSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "world VARCHAR(50) NOT NULL, " +
                "x INTEGER NOT NULL, " +
                "y INTEGER NOT NULL, " +
                "z INTEGER NOT NULL)";

        queueManager.queueWriteTransaction(tableCreationSql);
    }

    /**
     * Get the list of all SkyHopper {@link Location}s.
     * @return A {@link CompletableFuture} containing {@link List} of {@link Location}s.
     */
    public @NotNull CompletableFuture<@NotNull List<@NotNull Location>> getSkyHopperLocations() {
        String querySql = "SELECT * FROM " + tableName;

        return queueManager.queueReadTransaction(querySql, resultSet -> {
            List<Location> hopperLocations = new ArrayList<>();

            try {
                while(resultSet.next()) {
                    String worldName = resultSet.getString("world");
                    int x = resultSet.getInt("x");
                    int y = resultSet.getInt("y");
                    int z = resultSet.getInt("z");

                    World world = skyHoppers.getServer().getWorld(worldName);
                    if(world == null) {
                        world = WorldCreator.name(worldName).createWorld();
                    }

                    hopperLocations.add(new Location(world, x, y, z));
                }
            } catch(SQLException e) {
                logger.error(AdventureUtil.serialize("Failed to load SkyHopper locations from the database."));
                return List.of();
            }

            return hopperLocations;
        });
    }

    /**
     * Add a SkyHopper location to the database.
     * @param location The {@link Location} to save.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> addSkyHopperLocation(@NotNull Location location) {
        String updateSql = "INSERT INTO " + tableName + " (world, x, y, z) VALUES (?, ?, ?, ?, ?) ON CONFLICT (x, y, z) DO NOTHING";

        StringParameter worldParameter = new StringParameter(location.getWorld().getName());
        IntegerParameter xParameter = new IntegerParameter(location.getBlockX());
        IntegerParameter yParameter = new IntegerParameter(location.getBlockY());
        IntegerParameter zParameter = new IntegerParameter(location.getBlockZ());

        return queueManager.queueWriteTransaction(updateSql, List.of(worldParameter, xParameter, yParameter, zParameter)).thenAccept(result -> {});
    }

    /**
     * Delete a SkyHopper location from the database.
     * @param location The {@link Location} to delete.
     */
    public void removeSkyHopperLocation(@NotNull Location location) {
        String updateSql = "DELETE FROM " + tableName + " WHERE world = ? AND x = ? AND y = ? AND z = ?";

        StringParameter worldParameter = new StringParameter(location.getWorld().getName());
        IntegerParameter xParameter = new IntegerParameter(location.getBlockX());
        IntegerParameter yParameter = new IntegerParameter(location.getBlockY());
        IntegerParameter zParameter = new IntegerParameter(location.getBlockZ());

        queueManager.queueWriteTransaction(updateSql, List.of(worldParameter, xParameter, yParameter, zParameter));
    }
}
