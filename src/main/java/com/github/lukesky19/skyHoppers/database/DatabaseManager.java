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
package com.github.lukesky19.skyHoppers.database;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.database.table.HoppersTable;
import com.github.lukesky19.skylib.api.database.AbstractDatabaseManager;
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
 * This class manages the database for SkyHoppers.
 */
public class DatabaseManager extends AbstractDatabaseManager {
    private final @NotNull SkyHoppers skyHoppers;
    private final @NotNull HoppersTable hoppersTable;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param connectionManager A {@link ConnectionManager} instance.
     * @param queueManager A {@link QueueManager} instance.
     */
    public DatabaseManager(@NotNull SkyHoppers skyHoppers, @NotNull ConnectionManager connectionManager, QueueManager queueManager) {
        super(connectionManager, queueManager);
        this.skyHoppers = skyHoppers;

        hoppersTable = new HoppersTable(skyHoppers, queueManager);
        hoppersTable.createTable();
    }

    /**
     * Get the {@link HoppersTable} class to interface with the database with.
     * @return The {@link HoppersTable}.
     */
    public @NotNull HoppersTable getHoppersTable() {
        return hoppersTable;
    }

    /**
     * Migrates the legacy database from <a href="https://github.com/lukesky19/Legacy_SkyHoppers">Legacy SkyHoppers</a>
     * @apiNote The legacy database is connected to without the use of SkyLib's Database API. The new API is used to save data to the new database though.
     * @return A {@link CompletableFuture} of type {@link Void} when all operations are complete.
     */
    public @NotNull CompletableFuture<Void> migrateLegacyDatabase(){
        Path legacyPath = Path.of(skyHoppers.getDataFolder().getAbsolutePath() + File.separator + "skyhoppers.db");
        Path copyPath = Path.of(skyHoppers.getDataFolder().getAbsolutePath() + File.separator + "skyhoppers.db.migrated");
        if(!legacyPath.toFile().exists()) return CompletableFuture.completedFuture(null);

        List<CompletableFuture<Void>> futureList = new ArrayList<>();

        try(Connection legacyConnection = DriverManager.getConnection("jdbc:sqlite:" + legacyPath)) {
            try(PreparedStatement preparedStatement = legacyConnection.prepareStatement("SELECT * FROM skyhoppers_hoppers")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                while(resultSet.next()) {
                    String worldName = resultSet.getString("world");
                    int x = resultSet.getInt("x");
                    int y = resultSet.getInt("y");
                    int z = resultSet.getInt("z");

                    World world = skyHoppers.getServer().getWorld(worldName);
                    if(world == null) {
                        world = WorldCreator.name(worldName).createWorld();
                    }

                    futureList.add(hoppersTable.addSkyHopperLocation(new Location(world, x, y, z)));
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

        return CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
    }
}
