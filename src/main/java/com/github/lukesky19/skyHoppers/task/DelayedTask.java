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
package com.github.lukesky19.skyHoppers.task;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.data.DelayedEntry;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static com.github.lukesky19.skyHoppers.util.InventoryUtils.transferContainerToSkyHopper;
import static com.github.lukesky19.skyHoppers.util.InventoryUtils.transferInventoryToContainer;

/**
 * This Task handles transfers scheduled from the HopperMoveItemListener.
 */
public class DelayedTask extends BukkitRunnable {
    private final SkyHoppers plugin;
    private final HopperManager hopperManager;
    private final HashMap<Location, DelayedEntry> delayedEntriesMap = new HashMap<>();

    /**
     * Constructor
     * @param plugin The SkyHopper Plugin.
     * @param hopperManager A HopperManager instance.
     */
    public DelayedTask(SkyHoppers plugin, HopperManager hopperManager) {
        this.plugin = plugin;
        this.hopperManager = hopperManager;
    }

    /**
     * Add a scheduled transfer to the map.
     * @param skyHopperLocation The location of the SkyHopper.
     * @param delayedEntry A DelayedEntry record containing the data required to complete the transfer.
     */
    public void add(Location skyHopperLocation, DelayedEntry delayedEntry) {
        delayedEntriesMap.put(skyHopperLocation, delayedEntry);
    }

    /**
     * The function ran every time this task is ran.
     */
    @Override
    public void run() {
        Iterator<DelayedEntry> iterator = delayedEntriesMap.values().iterator();
        while(iterator.hasNext()) {
            if(plugin.areSkyHoppersPaused()) {
                iterator.remove();
                continue;
            }

            DelayedEntry delayedEntry = iterator.next();
            Location sourceLocation = delayedEntry.sourceLocation();
            Location destinationLocation = delayedEntry.destinationLocation();

            SkyHopper sourceSkyHopper = hopperManager.getSkyHopper(sourceLocation);
            SkyHopper destinationSkyHopper = hopperManager.getSkyHopper(destinationLocation);

            if (sourceSkyHopper == null && destinationSkyHopper == null) {
                iterator.remove();
                continue;
            }

            if(sourceLocation.getBlock().getState(false) instanceof Container source
                    && delayedEntry.destinationLocation().getBlock().getState(false) instanceof Container destination) {
                if (sourceSkyHopper != null && destinationSkyHopper != null) {
                    if(!sourceSkyHopper.enabled()
                            || !destinationSkyHopper.enabled()
                            || source.getBlock().isBlockPowered()
                            || destination.getBlock().isBlockPowered()) {
                        iterator.remove();
                        continue;
                    }

                    if (delayedEntry.initiatorIsSource()) {
                        if (delayedEntry.isSuction()) {
                            if(sourceSkyHopper.nextSuctionTime() < System.currentTimeMillis()) {
                                transferContainerToSkyHopper(plugin, destinationSkyHopper, source, source.getInventory(), destination.getInventory(), sourceSkyHopper.suctionAmount());

                                updateSkyHopperSuctionTime(sourceSkyHopper);
                            }
                        } else {
                            if(sourceSkyHopper.nextTransferTime() < System.currentTimeMillis()) {
                                transferContainerToSkyHopper(plugin, destinationSkyHopper, source, source.getInventory(), destination.getInventory(), sourceSkyHopper.transferAmount());

                                updateSkyHopperTransferTime(sourceSkyHopper);
                            }
                        }
                    } else {
                        if (delayedEntry.isSuction()) {
                            if(destinationSkyHopper.nextSuctionTime() < System.currentTimeMillis()) {
                                transferContainerToSkyHopper(plugin, sourceSkyHopper, source, source.getInventory(), destination.getInventory(), destinationSkyHopper.suctionAmount());

                                updateSkyHopperSuctionTime(destinationSkyHopper);
                            }
                        } else {
                            if(destinationSkyHopper.nextTransferTime() < System.currentTimeMillis()) {
                                transferContainerToSkyHopper(plugin, sourceSkyHopper, source, source.getInventory(), destination.getInventory(), destinationSkyHopper.transferAmount());

                                updateSkyHopperTransferTime(destinationSkyHopper);
                            }
                        }
                    }
                } else if (sourceSkyHopper != null) {
                    if(!sourceSkyHopper.enabled() || source.getBlock().isBlockPowered()) {
                        iterator.remove();
                        continue;
                    }

                    if (delayedEntry.isSuction()) {
                        if(sourceSkyHopper.nextSuctionTime() < System.currentTimeMillis()) {
                            transferInventoryToContainer(plugin, source.getInventory(), source, destination, destination.getInventory(), sourceSkyHopper.suctionAmount());

                            updateSkyHopperSuctionTime(sourceSkyHopper);
                        }
                    } else {
                        if(sourceSkyHopper.nextTransferTime() < System.currentTimeMillis()) {
                            transferInventoryToContainer(plugin, source.getInventory(), source, destination, destination.getInventory(), sourceSkyHopper.transferAmount());

                            updateSkyHopperTransferTime(sourceSkyHopper);
                        }
                    }
                } else {
                    if(!destinationSkyHopper.enabled() || destination.getBlock().isBlockPowered()) {
                        iterator.remove();
                        continue;
                    }

                    if (delayedEntry.isSuction()) {
                        if(destinationSkyHopper.nextSuctionTime() < System.currentTimeMillis()) {
                            transferContainerToSkyHopper(plugin, destinationSkyHopper, source, source.getInventory(), destination.getInventory(), destinationSkyHopper.suctionAmount());

                            updateSkyHopperSuctionTime(destinationSkyHopper);
                        }
                    } else {
                        if(destinationSkyHopper.nextTransferTime() < System.currentTimeMillis()) {
                            transferContainerToSkyHopper(plugin, destinationSkyHopper, source, source.getInventory(), destination.getInventory(), destinationSkyHopper.transferAmount());

                            updateSkyHopperTransferTime(destinationSkyHopper);
                        }
                    }
                }

                iterator.remove();
            } else {
                iterator.remove();
            }
        }
    }

    /**
     * Update the SkyHopper's next scheduled suction time.
     * @param skyHopper The SkyHopper to update.
     */
    private void updateSkyHopperSuctionTime(SkyHopper skyHopper) {
        double suctionSpeed = skyHopper.suctionSpeed();

        long addMs = (long) (suctionSpeed * 1000);
        long time = System.currentTimeMillis() + addMs;

        SkyHopper updatedSkyHopper = new SkyHopper(
                true,
                skyHopper.particles(),
                skyHopper.owner(),
                skyHopper.members(),
                skyHopper.location(),
                skyHopper.containers(),
                skyHopper.filterType(),
                skyHopper.filterItems(),
                skyHopper.transferSpeed(),
                skyHopper.maxTransferSpeed(),
                skyHopper.transferAmount(),
                skyHopper.maxTransferAmount(),
                skyHopper.suctionSpeed(),
                skyHopper.maxSuctionSpeed(),
                skyHopper.suctionAmount(),
                skyHopper.maxSuctionAmount(),
                skyHopper.suctionRange(),
                skyHopper.maxSuctionRange(),
                skyHopper.maxContainers(),
                time,
                skyHopper.nextTransferTime());

        hopperManager.cacheSkyHopper(updatedSkyHopper.location(), updatedSkyHopper);
    }

    /**
     * Update the SkyHopper's next scheduled transfer time.
     * @param skyHopper The SkyHopper to update.
     */
    private void updateSkyHopperTransferTime(SkyHopper skyHopper) {
        double transferSpeed = skyHopper.transferSpeed();

        long addMs = (long) (transferSpeed * 1000);
        long time = System.currentTimeMillis() + addMs;

        SkyHopper updatedSkyHopper = new SkyHopper(
                true,
                skyHopper.particles(),
                skyHopper.owner(),
                skyHopper.members(),
                skyHopper.location(),
                skyHopper.containers(),
                skyHopper.filterType(),
                skyHopper.filterItems(),
                skyHopper.transferSpeed(),
                skyHopper.maxTransferSpeed(),
                skyHopper.transferAmount(),
                skyHopper.maxTransferAmount(),
                skyHopper.suctionSpeed(),
                skyHopper.maxSuctionSpeed(),
                skyHopper.suctionAmount(),
                skyHopper.maxSuctionAmount(),
                skyHopper.suctionRange(),
                skyHopper.maxSuctionRange(),
                skyHopper.maxContainers(),
                skyHopper.nextSuctionTime(),
                time);

        hopperManager.cacheSkyHopper(updatedSkyHopper.location(), updatedSkyHopper);
    }
}
