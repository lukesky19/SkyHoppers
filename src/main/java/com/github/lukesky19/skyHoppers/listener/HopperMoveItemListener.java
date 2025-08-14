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
import com.github.lukesky19.skyHoppers.data.DelayedEntry;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skyHoppers.task.DelayedTask;
import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;

/**
 * Listens to when a SkyHopper moves an ItemStack to another Container.
 */
public class HopperMoveItemListener implements Listener {
    private final SkyHoppers plugin;
    private final HopperManager hopperManager;
    private final DelayedTask delayedTask;

    /**
     * Constructor
     * @param plugin A SkyHoppers Plugin.
     * @param hopperManager A HopperManager instance.
     * @param delayedTask A DelayedTask instance.
     */
    public HopperMoveItemListener(SkyHoppers plugin, HopperManager hopperManager, DelayedTask delayedTask) {
        this.plugin = plugin;
        this.hopperManager = hopperManager;
        this.delayedTask = delayedTask;
    }

    /**
     * Listens to when a SkyHopper moves an item using the vanilla method, i.e., a Hopper facing into a Chest.
     * @param inventoryMoveItemEvent An InventoryMoveItemEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopperMoveItem(InventoryMoveItemEvent inventoryMoveItemEvent) {
        Inventory initiatorInventory = inventoryMoveItemEvent.getInitiator();
        Inventory sourceInventory = inventoryMoveItemEvent.getSource();
        Inventory destinationInventory = inventoryMoveItemEvent.getDestination();

        if (initiatorInventory.getHolder(false) instanceof Container initiator
                && destinationInventory.getHolder(false) instanceof Container destination) {
            if(sourceInventory.getHolder(false) instanceof Container source) {
                SkyHopper initiatorSkyHopper = hopperManager.getSkyHopper(initiator.getLocation());
                SkyHopper sourceSkyHopper = hopperManager.getSkyHopper(source.getLocation());
                SkyHopper destinationSkyHopper = hopperManager.getSkyHopper(destination.getLocation());

                if((sourceSkyHopper == null && destinationSkyHopper == null) || initiatorSkyHopper == null) return;

                if(sourceSkyHopper != null && destinationSkyHopper != null) {
                    inventoryMoveItemEvent.setCancelled(true);
                    if(plugin.areSkyHoppersPaused()) return;
                    if (!initiatorSkyHopper.isSkyHopperEnabled()
                            || !sourceSkyHopper.isSkyHopperEnabled()
                            || !destinationSkyHopper.isSkyHopperEnabled()) return;

                    if(initiatorSkyHopper.equals(sourceSkyHopper)) {
                        if(sourceSkyHopper.getNextTransferTime() < System.currentTimeMillis()) {
                            double transferSpeed = sourceSkyHopper.getTransferSpeed();

                            long addMs = (long) (transferSpeed * 1000);
                            long time = System.currentTimeMillis() + addMs;

                            sourceSkyHopper.setNextTransferTime(time);

                            if(sourceSkyHopper.getLocation() == null) return;

                            delayedTask.add(sourceSkyHopper.getLocation(), new DelayedEntry(source.getLocation(), destination.getLocation(), false, true));
                        }
                    } else if(initiatorSkyHopper.equals(destinationSkyHopper)) {
                        if (destinationSkyHopper.getNextSuctionTime() < System.currentTimeMillis()) {
                            double suctionSpeed = destinationSkyHopper.getSuctionSpeed();

                            long addMs = (long) (suctionSpeed * 1000);
                            long time = System.currentTimeMillis() + addMs;

                            destinationSkyHopper.setNextSuctionTime(time);

                            if(destinationSkyHopper.getLocation() == null) return;

                            delayedTask.add(destinationSkyHopper.getLocation(), new DelayedEntry(source.getLocation(), destination.getLocation(), false, false));
                        }
                    }
                } else if(sourceSkyHopper != null) {
                    inventoryMoveItemEvent.setCancelled(true);
                    if(plugin.areSkyHoppersPaused()) return;
                    if (!sourceSkyHopper.isSkyHopperEnabled()) return;

                    if (sourceSkyHopper.getNextTransferTime() < System.currentTimeMillis()) {
                        if(sourceSkyHopper.getLocation() == null) return;
                        delayedTask.add(sourceSkyHopper.getLocation(), new DelayedEntry(source.getLocation(), destination.getLocation(), false, true));
                    }
                } else {
                    inventoryMoveItemEvent.setCancelled(true);
                    if(plugin.areSkyHoppersPaused()) return;
                    if(!destinationSkyHopper.isSkyHopperEnabled()) return;

                    if (destinationSkyHopper.getNextSuctionTime() < System.currentTimeMillis()) {
                        if(destinationSkyHopper.getLocation() == null) return;
                        delayedTask.add(destinationSkyHopper.getLocation(), new DelayedEntry(source.getLocation(), destination.getLocation(), true, false));
                    }
                }
            } else if(sourceInventory.getHolder(false) instanceof DoubleChest doubleChest) {
                if(doubleChest.getLeftSide(false) instanceof Container leftContainer
                        && doubleChest.getRightSide(false) instanceof Container rightContainer) {
                    SkyHopper destinationSkyHopper = hopperManager.getSkyHopper(destination.getLocation());
                    if(destinationSkyHopper == null) return;

                    inventoryMoveItemEvent.setCancelled(true);
                    if(plugin.areSkyHoppersPaused()) return;
                    if (!destinationSkyHopper.isSkyHopperEnabled()) return;

                    if(destinationSkyHopper.getNextSuctionTime() < System.currentTimeMillis()) {
                        Location skyHopperLocation = destinationSkyHopper.getLocation();
                        if(skyHopperLocation == null) return;
                        Location containerLocation = new Location(skyHopperLocation.getWorld(), skyHopperLocation.x(), skyHopperLocation.y() + 1, skyHopperLocation.z());

                        if(containerLocation.equals(leftContainer.getLocation())) {
                            delayedTask.add(skyHopperLocation, new DelayedEntry(leftContainer.getLocation(), destination.getLocation(), true, false));
                        } else if(containerLocation.equals(rightContainer.getLocation())) {
                            delayedTask.add(skyHopperLocation, new DelayedEntry(rightContainer.getLocation(), destination.getLocation(), true, false));
                        }
                    }
                }
            }
        }
    }
}
