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

import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

/**
 * Listens for when a block explodes handle the breaking of SkyHoppers.
 */
public class BlockExplodeListener implements Listener {
    private final HopperManager hopperManager;

    /**
     * Constructor
     * @param hopperManager A HopperManager instance.
     */
    public BlockExplodeListener(HopperManager hopperManager) {
        this.hopperManager = hopperManager;
    }

    /**
     * Handles when an entity explodes and checks if a block is a SkyHopper to properly remove it and drop the item.
     * @param blockExplodeEvent A BlockExplodeEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent blockExplodeEvent) {
        Iterator<Block> iterator = blockExplodeEvent.blockList().iterator();
        while(iterator.hasNext()) {
            Block block = iterator.next();

            SkyHopper skyHopper = hopperManager.getSkyHopper(block.getLocation());
            if (skyHopper != null) {
                iterator.remove();

                block.setType(Material.AIR);

                hopperManager.removeSkyHopper(skyHopper.location());

                final ItemStack skyHopperItem = hopperManager.createItemStackFromSkyHopper(skyHopper, 1);
                if(skyHopperItem != null) {
                    block.getWorld().dropItem(block.getLocation(), skyHopperItem);
                }
            }
        }
    }
}
