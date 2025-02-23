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
package com.github.lukesky19.skyHoppers.manager;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages interfacing with the database that saves SkyHopper locations.
 */
public class DataManager {
    private final SkyHoppers plugin;
    private final Connection connection;

    /**
     * Constructor
     * @param plugin The SkyHoppers Plugin.
     * @param path The path of the database.
     * @throws SQLException If the database connection fails to be created.
     */
    public DataManager(SkyHoppers plugin, String path) throws SQLException {
        this.plugin = plugin;

        connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        try(Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS skyhoppers_hoppers (" +
                    "world VARCHAR(50) NOT NULL, " +
                    "x INTEGER NOT NULL, " +
                    "y INTEGER NOT NULL, " +
                    "z INTEGER NOT NULL)");
        }
    }

    /**
     * Loads the list of all SkyHopper Locations.
     * @return A List of Locations
     */
    public List<Location> loadHoppers() {
        List<Location> hopperLocations = new ArrayList<>();

        try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM skyhoppers_hoppers")) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                String worldName = resultSet.getString("world");
                int x = resultSet.getInt("x");
                int y = resultSet.getInt("y");
                int z = resultSet.getInt("z");

                World world = plugin.getServer().getWorld(worldName);
                if(world == null) {
                    world = WorldCreator.name(worldName).createWorld();
                }

                hopperLocations.add(new Location(world, x, y, z));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return hopperLocations;
    }

    /**
     * Add a SkyHopper location to the database.
     * @param location The Location to save.
     */
    public void addHopper(Location location) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO skyhoppers_hoppers (world, x, y, z) VALUES (?, ?, ?, ?)")) {
                preparedStatement.setString(1, location.getWorld().getName());
                preparedStatement.setInt(2, location.getBlockX());
                preparedStatement.setInt(3, location.getBlockY());
                preparedStatement.setInt(4, location.getBlockZ());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * If a SkyHopper location is not inside the database, save it to the database
     * @param location The Location of the SkyHopper to save to the database.
     */
    public void saveLocationIfDoesNotExit(@NotNull Location location) {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM skyhoppers_hoppers WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
                preparedStatement.setString(1, location.getWorld().getName());
                preparedStatement.setInt(2, location.getBlockX());
                preparedStatement.setInt(3, location.getBlockY());
                preparedStatement.setInt(4, location.getBlockZ());

                ResultSet resultSet = preparedStatement.executeQuery();
                return resultSet.next();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        future.thenAcceptAsync(bool -> {
            if(!bool) {
                // Save SkyHopper Location to Database
                addHopper(location);
            }
        });
    }

    /**
     * Delete a SkyHopper location from the database.
     * @param location The Location to delete.
     */
    public void removeHopper(Location location) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try(PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM skyhoppers_hoppers WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
                preparedStatement.setString(1, location.getWorld().getName());
                preparedStatement.setInt(2, location.getBlockX());
                preparedStatement.setInt(3, location.getBlockY());
                preparedStatement.setInt(4, location.getBlockZ());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Migrates SkyHoppers from <a href="https://github.com/lukesky19/Legacy_SkyHoppers">Legacy SkyHoppers</a>
     */
    public void migrateSkyHoppers(){
        Path legacyPath = Path.of(plugin.getDataFolder().getAbsolutePath() + File.separator + "skyhoppers.db");
        Path copyPath = Path.of(plugin.getDataFolder().getAbsolutePath() + File.separator + "skyhoppers.db.migrated");
        if(!legacyPath.toFile().exists()) return;

        try(Connection legacy = DriverManager.getConnection("jdbc:sqlite:" + legacyPath)) {
            try(PreparedStatement preparedStatement = legacy.prepareStatement("SELECT * FROM skyhoppers_hoppers")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                while(resultSet.next()) {
                    String worldName = resultSet.getString("world");
                    int x = resultSet.getInt("x");
                    int y = resultSet.getInt("y");
                    int z = resultSet.getInt("z");

                    World world = plugin.getServer().getWorld(worldName);
                    if(world == null) {
                        world = WorldCreator.name(worldName).createWorld();
                    }

                    addHopper(new Location(world, x, y, z));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }  catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            Files.copy(legacyPath, copyPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        legacyPath.toFile().delete();
    }
}
