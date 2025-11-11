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
package com.github.lukesky19.skyHoppers.task.tasks;

import com.github.lukesky19.skyHoppers.skyhopper.SkyHopperManager;
import com.github.lukesky19.skyHoppers.skyhopper.SkyHopperProcessor;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import org.bukkit.Chunk;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This task handles the processing of {@link Chunk}s to load {@link SkyHopper}s in those chunks.
 */
public class SkyHopperLoadTask extends BukkitRunnable {
    private final @NotNull SkyHopperProcessor skyHopperProcessor;
    private final int chunksPerRun;

    /**
     * Constructor
     * @param skyHopperManager A {@link SkyHopperManager} instance.
     * @param chunksPerRun The number of chunks to process per run.
     */
    public SkyHopperLoadTask(
            @NotNull SkyHopperManager skyHopperManager,
            int chunksPerRun) {
        this.skyHopperProcessor = skyHopperManager.getSkyHopperProcessor();
        this.chunksPerRun = chunksPerRun;
    }

    /**
     * Process the chunk load queue and load any SkyHoppers in the chunk.
     * Limited to 10 chunks process per run.
     */
    @Override
    public void run() {
        int chunksProcessed = 0;

        @Nullable Chunk chunk = skyHopperProcessor.getNextQueuedLoadChunk();
        while(chunk != null && chunksProcessed <= chunksPerRun) {
            skyHopperProcessor.loadSkyHoppersInChunk(chunk);

            chunksProcessed++;

            chunk = skyHopperProcessor.getNextQueuedLoadChunk();
        }
    }
}
