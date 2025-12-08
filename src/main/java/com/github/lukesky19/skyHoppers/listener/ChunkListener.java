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
package com.github.lukesky19.skyHoppers.listener;

import com.github.lukesky19.skyHoppers.skyhopper.SkyHopperManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 * This class listens for when a chunk is loaded and loads any SkyHoppers in those chunks that aren't already loaded.
 */
public class ChunkListener implements Listener {
    private final SkyHopperManager hopperManager;

    /**
     * Constructor
     * @param hopperManager A {@link SkyHopperManager} instance.
     */
    public ChunkListener(SkyHopperManager hopperManager) {
        this.hopperManager = hopperManager;
    }

    /**
     * Listens to when a chunk is loaded and loads the SkyHoppers in that chunk that aren't already loaded.
     * @param chunkLoadEvent A {@link ChunkLoadEvent}.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent chunkLoadEvent) {
        hopperManager.getSkyHopperProcessor().queueLoadChunk(chunkLoadEvent.getChunk());
    }

    /**
     * Listens to when a chunk is unloaded and unloads the SkyHoppers in that chunk that aren't already loaded.
     * @param chunkUnloadEvent A {@link ChunkUnloadEvent}.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent chunkUnloadEvent) {
        hopperManager.getSkyHopperProcessor().queueUnloadChunk(chunkUnloadEvent.getChunk());
    }
}
