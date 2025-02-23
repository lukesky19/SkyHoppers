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
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static com.github.lukesky19.skyHoppers.util.InventoryUtils.addGroundItemToInventory;
import static com.github.lukesky19.skyHoppers.util.RoseStackerUtils.getItemAmount;
import static com.github.lukesky19.skyHoppers.util.RoseStackerUtils.removeAmountFromGroundItem;

/**
 * This class listens to when a SkyHopper picks up an ItemStack
 */
public class HopperPickupItemListener implements Listener {
    private final SkyHoppers plugin;
    private final HopperManager hopperManager;

    /**
     * Constructor
     * @param plugin The SkyHoppers Plugin.
     * @param hopperManager A HopperManager instance.
     */
    public HopperPickupItemListener(SkyHoppers plugin, HopperManager hopperManager) {
        this.plugin = plugin;
        this.hopperManager = hopperManager;
    }

    /**
     * Listens to when a SkyHopper picks up an item using the vanilla method, i.e., an ItemStack directly on-top of the Hopper.
     * @param inventoryPickupItemEvent An InventoryPickupItemEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopperPickup(InventoryPickupItemEvent inventoryPickupItemEvent) {
        // If the inventory is not that of a Hopper, do nothing.
        if (!(inventoryPickupItemEvent.getInventory().getHolder(false) instanceof Hopper hopper)) return;
        if(inventoryPickupItemEvent.getInventory().getLocation() == null) return;

        // Get the SkyHopper for the given location
        SkyHopper skyHopper = hopperManager.getSkyHopper(inventoryPickupItemEvent.getInventory().getLocation());
        // If no SkyHopper exists at that location, do nothing
        if (skyHopper == null || skyHopper.location() == null) return;

        // Cancel the inventoryPickupItemEvent
        inventoryPickupItemEvent.setCancelled(true);
        // If SkyHoppers are paused globally, do nothing.
        if(plugin.areSkyHoppersPaused()) return;
        // If the SkyHopper is disabled, do nothing.
        if (!skyHopper.enabled()) return;
        // If the next suction time hasn't been reached, do nothing
        if(skyHopper.nextSuctionTime() > System.currentTimeMillis()) return;

        // Get the item entity and item amount
        final Item item = inventoryPickupItemEvent.getItem();
        final int itemAmount = getItemAmount(item);

        // Clone the item entity's ItemStack
        ItemStack suctionItem = item.getItemStack();

        // Get the SkyHopper's location
        Location location = skyHopper.location().clone();
        // Get the Hopper's Inventory
        Inventory hopperInv = hopper.getSnapshotInventory();

        switch(skyHopper.filterType()) {
            case NONE -> {
                int result = addGroundItemToInventory(item, itemAmount, suctionItem, hopperInv, skyHopper.suctionAmount());

                if(result > 0) {
                    if (skyHopper.particles()) {
                        // Highlight hopper that sucked up the item
                        hopper.getWorld().spawnParticle(Particle.DUST, location, 5, 0.0, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.YELLOW, 1));

                        // Highlight the item that was sucked up
                        item.getWorld().spawnParticle(Particle.WITCH, item.getLocation(), 3, 0.0, 0.0, 0.0, 0.0);
                    }

                    updateSuctionTime(skyHopper);
                }
            }

            case WHITELIST -> {
                List<Material> filterItems = skyHopper.filterItems();
                if (!filterItems.isEmpty() && filterItems.contains(suctionItem.getType())) {
                    int result = addGroundItemToInventory(item, itemAmount, suctionItem, hopperInv, skyHopper.suctionAmount());

                    if(result > 0) {
                        if (skyHopper.particles()) {
                            // Highlight hopper that sucked up the item
                            hopper.getWorld().spawnParticle(Particle.DUST, location, 5, 0.0, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.YELLOW, 1));

                            // Highlight the item that was sucked up
                            item.getWorld().spawnParticle(Particle.WITCH, item.getLocation(), 3, 0.0, 0.0, 0.0, 0.0);
                        }

                        updateSuctionTime(skyHopper);
                    }
                }
            }

            case BLACKLIST -> {
                List<Material> filterItems = skyHopper.filterItems();

                if (!filterItems.isEmpty() && !filterItems.contains(suctionItem.getType())) {
                    int result = addGroundItemToInventory(item, itemAmount, suctionItem, hopperInv, skyHopper.suctionAmount());

                    if(result > 0) {
                        if (skyHopper.particles()) {
                            // Highlight hopper that sucked up the item
                            hopper.getWorld().spawnParticle(Particle.DUST, location, 5, 0.0, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.YELLOW, 1));

                            // Highlight the item that was sucked up
                            item.getWorld().spawnParticle(Particle.WITCH, item.getLocation(), 3, 0.0, 0.0, 0.0, 0.0);
                        }

                        updateSuctionTime(skyHopper);
                    }
                }
            }

            case DESTROY -> {
                List<Material> filterItems = skyHopper.filterItems();
                if (!filterItems.isEmpty() && filterItems.contains(suctionItem.getType())) {
                    // Get the amount to be destroyed
                    int destroyAmount = Math.min(itemAmount, skyHopper.suctionAmount());
                    removeAmountFromGroundItem(item, itemAmount, destroyAmount);

                    if(skyHopper.particles()) {
                        // Highlight hopper that sucked up the item
                        hopper.getWorld().spawnParticle(Particle.DUST, location, 5, 0.0, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.YELLOW, 1));

                        // Highlight the item that was sucked up
                        item.getWorld().spawnParticle(Particle.WITCH, item.getLocation(), 3, 0.0, 0.0, 0.0, 0.0);
                    }

                    updateSuctionTime(skyHopper);

                    return;
                }

                int result = addGroundItemToInventory(item, itemAmount, suctionItem, hopperInv, skyHopper.suctionAmount());

                if(result > 0) {
                    if (skyHopper.particles()) {
                        // Highlight hopper that sucked up the item
                        hopper.getWorld().spawnParticle(Particle.DUST, location, 5, 0.0, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.YELLOW, 1));

                        // Highlight the item that was sucked up
                        item.getWorld().spawnParticle(Particle.WITCH, item.getLocation(), 3, 0.0, 0.0, 0.0, 0.0);
                    }

                    updateSuctionTime(skyHopper);
                }
            }
        }
    }

    /**
     * Updates the next suction time for the SkyHopper.
     * @param currentSkyHopper The SkyHopper to update
     */
    private void updateSuctionTime(SkyHopper currentSkyHopper) {
        double suctionSpeed = currentSkyHopper.suctionSpeed();

        long addMs = (long) (suctionSpeed * 1000);
        long time = System.currentTimeMillis() + addMs;

        SkyHopper updatedSkyHopper = new SkyHopper(
                currentSkyHopper.enabled(),
                currentSkyHopper.particles(),
                currentSkyHopper.owner(),
                currentSkyHopper.members(),
                currentSkyHopper.location(),
                currentSkyHopper.containers(),
                currentSkyHopper.filterType(),
                currentSkyHopper.filterItems(),
                currentSkyHopper.transferSpeed(),
                currentSkyHopper.maxTransferSpeed(),
                currentSkyHopper.transferAmount(),
                currentSkyHopper.maxTransferAmount(),
                currentSkyHopper.suctionSpeed(),
                currentSkyHopper.maxSuctionSpeed(),
                currentSkyHopper.suctionAmount(),
                currentSkyHopper.maxSuctionAmount(),
                currentSkyHopper.suctionRange(),
                currentSkyHopper.maxSuctionRange(),
                currentSkyHopper.maxContainers(),
                time,
                currentSkyHopper.nextTransferTime());

        hopperManager.cacheSkyHopper(updatedSkyHopper.location(), updatedSkyHopper);
    }
}
