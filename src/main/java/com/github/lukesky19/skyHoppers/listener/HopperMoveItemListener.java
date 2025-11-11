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

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.skyhopper.SkyHopperManager;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skyHoppers.task.data.QueuedTransfer;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.type.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens to when a SkyHopper moves an ItemStack to another Container.
 */
public class HopperMoveItemListener implements Listener {
    private final @NotNull SkyHoppers plugin;
    private final @NotNull SkyHopperManager hopperManager;

    /**
     * Constructor
     * @param plugin A SkyHoppers Plugin.
     * @param hopperManager A {@link SkyHopperManager} instance.
     */
    public HopperMoveItemListener(
            @NotNull SkyHoppers plugin,
            @NotNull SkyHopperManager hopperManager) {
        this.plugin = plugin;
        this.hopperManager = hopperManager;
    }

    /**
     * Listens to when a SkyHopper moves an item using the vanilla method, i.e., a Hopper facing into a Chest.
     * @param inventoryMoveItemEvent An {@link InventoryMoveItemEvent}
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopperMoveItem(InventoryMoveItemEvent inventoryMoveItemEvent) {
        Inventory initiatorInventory = inventoryMoveItemEvent.getInitiator();
        Inventory sourceInventory = inventoryMoveItemEvent.getSource();
        Inventory destinationInventory = inventoryMoveItemEvent.getDestination();

        InventoryHolder sourceHolder = sourceInventory.getHolder(false);
        InventoryHolder destinationHolder = destinationInventory.getHolder(false);

        if(!(initiatorInventory.getHolder(false) instanceof Container initiatorContainer)) return;

        if(sourceHolder instanceof Container sourceContainer) {
            @Nullable SkyHopper initiatorSkyHopper = hopperManager.getSkyHopperDataManager().getSkyHopper(initiatorContainer.getLocation());
            @Nullable SkyHopper sourceSkyHopper = hopperManager.getSkyHopperDataManager().getSkyHopper(sourceContainer.getLocation());

            if(destinationHolder instanceof Container destinationContainer) {
                @Nullable SkyHopper destinationSkyHopper = hopperManager.getSkyHopperDataManager().getSkyHopper(destinationContainer.getLocation());

                if((sourceSkyHopper == null && destinationSkyHopper == null) || initiatorSkyHopper == null) return;

                inventoryMoveItemEvent.setCancelled(true);
                if(plugin.areSkyHoppersPaused()) return;
                if(!initiatorSkyHopper.isSkyHopperEnabled()) return;

                if(sourceSkyHopper != null && destinationSkyHopper != null) {
                    if(!sourceSkyHopper.isSkyHopperEnabled()
                            || !destinationSkyHopper.isSkyHopperEnabled()) return;

                    if(initiatorSkyHopper.equals(sourceSkyHopper)) {
                        if(sourceSkyHopper.getNextTransferTime() < System.currentTimeMillis()) {
                            double transferSpeed = sourceSkyHopper.getTransferSpeed();

                            long addMs = (long) (transferSpeed * 1000);
                            long time = System.currentTimeMillis() + addMs;

                            sourceSkyHopper.setNextTransferTime(time);

                            if(sourceSkyHopper.getLocation() == null) return;

                            hopperManager.getSkyHopperProcessor().queueQueuedTransfer(new QueuedTransfer(sourceContainer.getLocation(), destinationContainer.getLocation(), false, true));
                        }
                    } else if(initiatorSkyHopper.equals(destinationSkyHopper)) {
                        if(destinationSkyHopper.getNextSuctionTime() < System.currentTimeMillis()) {
                            double suctionSpeed = destinationSkyHopper.getSuctionSpeed();

                            long addMs = (long) (suctionSpeed * 1000);
                            long time = System.currentTimeMillis() + addMs;

                            destinationSkyHopper.setNextSuctionTime(time);

                            if(destinationSkyHopper.getLocation() == null) return;

                            hopperManager.getSkyHopperProcessor().queueQueuedTransfer(new QueuedTransfer(sourceContainer.getLocation(), destinationContainer.getLocation(), false, false));
                        }
                    }
                } else if(sourceSkyHopper != null) {
                    if(!sourceSkyHopper.isSkyHopperEnabled()) return;

                    if(sourceSkyHopper.getNextTransferTime() < System.currentTimeMillis()) {
                        if(sourceSkyHopper.getLocation() == null) return;

                        hopperManager.getSkyHopperProcessor().queueQueuedTransfer(new QueuedTransfer(sourceContainer.getLocation(), destinationContainer.getLocation(), false, true));
                    }
                } else {
                    if(!destinationSkyHopper.isSkyHopperEnabled()) return;

                    if (destinationSkyHopper.getNextSuctionTime() < System.currentTimeMillis()) {
                        if(destinationSkyHopper.getLocation() == null) return;

                        hopperManager.getSkyHopperProcessor().queueQueuedTransfer(new QueuedTransfer(sourceContainer.getLocation(), destinationContainer.getLocation(), true, false));
                    }
                }
            } else if(destinationHolder instanceof DoubleChest) {
                if((sourceSkyHopper == null || !sourceSkyHopper.isSkyHopperEnabled()) || initiatorSkyHopper == null) return;

                inventoryMoveItemEvent.setCancelled(true);
                if(plugin.areSkyHoppersPaused()) return;
                if(!initiatorSkyHopper.isSkyHopperEnabled()) return;

                if(sourceSkyHopper.getNextTransferTime() > System.currentTimeMillis()) return;

                if(!(sourceContainer.getBlockData() instanceof Hopper hopper)) return;
                BlockFace blockFace = hopper.getFacing();
                BlockState destinationState = sourceContainer.getBlock().getRelative(blockFace).getState(false);

                if(destinationState instanceof Container destinationContainer) {
                    Location destinationLocation = destinationContainer.getLocation();

                    inventoryMoveItemEvent.setCancelled(true);
                    if(plugin.areSkyHoppersPaused()) return;
                    if(!initiatorSkyHopper.isSkyHopperEnabled()) return;

                    hopperManager.getSkyHopperProcessor().queueQueuedTransfer(new QueuedTransfer(sourceContainer.getLocation(), destinationLocation, false, true));
                }
            }
        } else if(sourceHolder instanceof DoubleChest doubleChest) {
            if(!(doubleChest.getLeftSide(false) instanceof Container leftContainer
                    && doubleChest.getRightSide(false) instanceof Container rightContainer)) return;

            @Nullable Location destinationLocation = destinationInventory.getLocation();
            if(destinationLocation == null) return;
            SkyHopper destinationSkyHopper = hopperManager.getSkyHopperDataManager().getSkyHopper(destinationLocation);
            if(destinationSkyHopper == null) return;

            if(!destinationSkyHopper.isSkyHopperEnabled()) return;

            inventoryMoveItemEvent.setCancelled(true);
            if(plugin.areSkyHoppersPaused()) return;

            if(destinationSkyHopper.getNextSuctionTime() < System.currentTimeMillis()) {
                Location skyHopperLocation = destinationSkyHopper.getLocation();
                if(skyHopperLocation == null) return;
                Location containerLocation = new Location(skyHopperLocation.getWorld(), skyHopperLocation.x(), skyHopperLocation.y() + 1, skyHopperLocation.z());

                if(containerLocation.equals(leftContainer.getLocation())) {
                    hopperManager.getSkyHopperProcessor().queueQueuedTransfer(new QueuedTransfer(leftContainer.getLocation(), destinationLocation, true, false));
                } else if(containerLocation.equals(rightContainer.getLocation())) {
                    hopperManager.getSkyHopperProcessor().queueQueuedTransfer(new QueuedTransfer(rightContainer.getLocation(), destinationLocation, true, false));
                }
            }
        }
    }
}
