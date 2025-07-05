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
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.github.lukesky19.skyHoppers.util.InventoryUtils.transferContainerToSkyHopper;
import static com.github.lukesky19.skyHoppers.util.InventoryUtils.transferInventoryToContainer;

/**
 * This Task handles transfers scheduled from the HopperMoveItemListener.
 */
public class DelayedTask extends BukkitRunnable {
    private final @NotNull SkyHoppers plugin;
    private final @NotNull HopperManager hopperManager;
    private final @NotNull Map<Location, DelayedEntry> delayedEntriesMap = new HashMap<>();

    /**
     * Constructor
     * @param plugin A {@link SkyHoppers} instance.
     * @param hopperManager A {@link HopperManager} instance.
     */
    public DelayedTask(@NotNull SkyHoppers plugin, @NotNull HopperManager hopperManager) {
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

            if(sourceSkyHopper == null && destinationSkyHopper == null) {
                iterator.remove();
                continue;
            }

            if(sourceLocation.getBlock().getState(false) instanceof Container source
                    && delayedEntry.destinationLocation().getBlock().getState(false) instanceof Container destination) {
                if (sourceSkyHopper != null && destinationSkyHopper != null) {
                    if(!sourceSkyHopper.isSkyHopperEnabled()
                            || !destinationSkyHopper.isSkyHopperEnabled()
                            || source.getBlock().isBlockPowered()
                            || destination.getBlock().isBlockPowered()) {
                        iterator.remove();
                        continue;
                    }

                    if (delayedEntry.initiatorIsSource()) {
                        if (delayedEntry.isSuction()) {
                            if(sourceSkyHopper.getNextSuctionTime() < System.currentTimeMillis()) {
                                transferContainerToSkyHopper(plugin, destinationSkyHopper, source, source.getInventory(), destination.getInventory(), sourceSkyHopper.getSuctionAmount());

                                updateSkyHopperSuctionTime(sourceSkyHopper);
                            }
                        } else {
                            if(sourceSkyHopper.getNextTransferTime() < System.currentTimeMillis()) {
                                transferContainerToSkyHopper(plugin, destinationSkyHopper, source, source.getInventory(), destination.getInventory(), sourceSkyHopper.getTransferAmount());

                                updateSkyHopperTransferTime(sourceSkyHopper);
                            }
                        }
                    } else {
                        if (delayedEntry.isSuction()) {
                            if(destinationSkyHopper.getNextSuctionTime() < System.currentTimeMillis()) {
                                transferContainerToSkyHopper(plugin, sourceSkyHopper, source, source.getInventory(), destination.getInventory(), destinationSkyHopper.getSuctionAmount());

                                updateSkyHopperSuctionTime(destinationSkyHopper);
                            }
                        } else {
                            if(destinationSkyHopper.getNextTransferTime() < System.currentTimeMillis()) {
                                transferContainerToSkyHopper(plugin, sourceSkyHopper, source, source.getInventory(), destination.getInventory(), destinationSkyHopper.getTransferAmount());

                                updateSkyHopperTransferTime(destinationSkyHopper);
                            }
                        }
                    }
                } else if (sourceSkyHopper != null) {
                    if(!sourceSkyHopper.isSkyHopperEnabled() || source.getBlock().isBlockPowered()) {
                        iterator.remove();
                        continue;
                    }

                    if (delayedEntry.isSuction()) {
                        if(sourceSkyHopper.getNextSuctionTime() < System.currentTimeMillis()) {
                            transferInventoryToContainer(plugin, source.getInventory(), source, destination, destination.getInventory(), sourceSkyHopper.getSuctionAmount());

                            updateSkyHopperSuctionTime(sourceSkyHopper);
                        }
                    } else {
                        if(sourceSkyHopper.getNextTransferTime() < System.currentTimeMillis()) {
                            transferInventoryToContainer(plugin, source.getInventory(), source, destination, destination.getInventory(), sourceSkyHopper.getTransferAmount());

                            updateSkyHopperTransferTime(sourceSkyHopper);
                        }
                    }
                } else {
                    if(!destinationSkyHopper.isSkyHopperEnabled() || destination.getBlock().isBlockPowered()) {
                        iterator.remove();
                        continue;
                    }

                    if (delayedEntry.isSuction()) {
                        if(destinationSkyHopper.getNextSuctionTime() < System.currentTimeMillis()) {
                            transferContainerToSkyHopper(plugin, destinationSkyHopper, source, source.getInventory(), destination.getInventory(), destinationSkyHopper.getSuctionAmount());

                            updateSkyHopperSuctionTime(destinationSkyHopper);
                        }
                    } else {
                        if(destinationSkyHopper.getNextTransferTime() < System.currentTimeMillis()) {
                            transferContainerToSkyHopper(plugin, destinationSkyHopper, source, source.getInventory(), destination.getInventory(), destinationSkyHopper.getTransferAmount());

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
     * Update the {@link SkyHopper}'s next scheduled suction time.
     * @param skyHopper The {@link SkyHopper} to update the next suction time for.
     */
    private void updateSkyHopperSuctionTime(@NotNull SkyHopper skyHopper) {
        double suctionSpeed = skyHopper.getSuctionSpeed();

        long addMs = (long) (suctionSpeed * 1000);
        long time = System.currentTimeMillis() + addMs;

        skyHopper.setNextSuctionTime(time);
    }

    /**
     * Update the {@link SkyHopper}'s next scheduled transfer time.
     * @param skyHopper The {@link SkyHopper} to update the next transfer time for.
     */
    private void updateSkyHopperTransferTime(@NotNull SkyHopper skyHopper) {
        double transferSpeed = skyHopper.getTransferSpeed();

        long addMs = (long) (transferSpeed * 1000);
        long time = System.currentTimeMillis() + addMs;

        skyHopper.setNextTransferTime(time);
    }
}
