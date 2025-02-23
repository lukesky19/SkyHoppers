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

import com.github.lukesky19.skyHoppers.manager.HopperManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

/**
 * This class listens for when a chunk is loaded and loads any SkyHoppers in those chunks that aren't already loaded.
 */
public class ChunkLoadListener implements Listener {
    private final HopperManager hopperManager;

    /**
     * Constructor
     * @param hopperManager A HopperManager Instance.
     */
    public ChunkLoadListener(HopperManager hopperManager) {
        this.hopperManager = hopperManager;
    }

    /**
     * Listens to when a chunk is loaded and loads the SkyHoppers in that chunk that aren't already loaded.
     * @param chunkLoadEvent A ChunkLoadEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent chunkLoadEvent) {
        hopperManager.loadSkyHoppersInChunk(chunkLoadEvent.getChunk());
    }
}
