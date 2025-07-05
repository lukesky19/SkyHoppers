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
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.lukesky19.skyHoppers.util.InventoryUtils.addGroundItemToInventory;
import static com.github.lukesky19.skyHoppers.util.RoseStackerUtils.getItemAmount;
import static com.github.lukesky19.skyHoppers.util.RoseStackerUtils.removeAmountFromGroundItem;

/**
 * This class listens to when a SkyHopper picks up an ItemStack
 */
public class HopperPickupItemListener implements Listener {
    private final @NotNull SkyHoppers plugin;
    private final @NotNull ComponentLogger logger;
    private final @NotNull HopperManager hopperManager;

    /**
     * Constructor
     * @param plugin The {@link SkyHoppers} instance.
     * @param hopperManager A {@link HopperManager} instance.
     */
    public HopperPickupItemListener(@NotNull SkyHoppers plugin, @NotNull HopperManager hopperManager) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
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
        if(skyHopper == null || skyHopper.getLocation() == null) return;

        // Cancel the inventoryPickupItemEvent
        inventoryPickupItemEvent.setCancelled(true);
        // If SkyHoppers are paused globally, do nothing.
        if(plugin.areSkyHoppersPaused()) return;
        // If the SkyHopper is disabled, do nothing.
        if(!skyHopper.isSkyHopperEnabled()) return;
        // If the next suction time hasn't been reached, do nothing
        if(skyHopper.getNextSuctionTime() > System.currentTimeMillis()) return;

        // Get the item entity and item amount
        Item item = inventoryPickupItemEvent.getItem();
        int itemAmount = getItemAmount(item);

        // Clone the item entity's ItemStack
        ItemStack suctionItem = item.getItemStack();
        
        ItemType suctionItemType = suctionItem.getType().asItemType();
        if(suctionItemType == null) {
            logger.warn(AdventureUtil.serialize("Unable to pick up an item and add it to a SkyHopper as the ItemType is null. [Method: onHopperPickup]"));
            return;
        }

        // Get the SkyHopper's location
        Location location = skyHopper.getLocation();
        // Get the Hopper's Inventory
        Inventory hopperInv = hopper.getSnapshotInventory();

        switch(skyHopper.getFilterType()) {
            case NONE -> {
                int result = addGroundItemToInventory(item, itemAmount, suctionItem, hopperInv, skyHopper.getSuctionAmount());

                if(result > 0) {
                    if(skyHopper.isParticlesEnabled()) {
                        // Highlight hopper that sucked up the item
                        hopper.getWorld().spawnParticle(Particle.DUST, location, 5, 0.0, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.YELLOW, 1));

                        // Highlight the item that was sucked up
                        item.getWorld().spawnParticle(Particle.WITCH, item.getLocation(), 3, 0.0, 0.0, 0.0, 0.0);
                    }

                    updateSuctionTime(skyHopper);
                }
            }

            case WHITELIST -> {
                List<ItemType> filterItems = skyHopper.getFilterItems();
                if (!filterItems.isEmpty() && filterItems.contains(suctionItemType)) {
                    int result = addGroundItemToInventory(item, itemAmount, suctionItem, hopperInv, skyHopper.getSuctionAmount());

                    if(result > 0) {
                        if (skyHopper.isParticlesEnabled()) {
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
                List<ItemType> filterItems = skyHopper.getFilterItems();

                if(!filterItems.isEmpty() && !filterItems.contains(suctionItemType)) {
                    int result = addGroundItemToInventory(item, itemAmount, suctionItem, hopperInv, skyHopper.getSuctionAmount());

                    if(result > 0) {
                        if (skyHopper.isParticlesEnabled()) {
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
                List<ItemType> filterItems = skyHopper.getFilterItems();
                if(!filterItems.isEmpty() && filterItems.contains(suctionItemType)) {
                    // Get the amount to be destroyed
                    int destroyAmount = Math.min(itemAmount, skyHopper.getSuctionAmount());
                    removeAmountFromGroundItem(item, itemAmount, destroyAmount);

                    if(skyHopper.isParticlesEnabled()) {
                        // Highlight hopper that sucked up the item
                        hopper.getWorld().spawnParticle(Particle.DUST, location, 5, 0.0, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.YELLOW, 1));

                        // Highlight the item that was sucked up
                        item.getWorld().spawnParticle(Particle.WITCH, item.getLocation(), 3, 0.0, 0.0, 0.0, 0.0);
                    }

                    updateSuctionTime(skyHopper);

                    return;
                }

                int result = addGroundItemToInventory(item, itemAmount, suctionItem, hopperInv, skyHopper.getSuctionAmount());

                if(result > 0) {
                    if (skyHopper.isParticlesEnabled()) {
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
     * Updates the next suction time for the {@link SkyHopper}.
     * @param skyHopper The {@link SkyHopper} to update the next suction time for.
     */
    private void updateSuctionTime(@NotNull SkyHopper skyHopper) {
        double suctionSpeed = skyHopper.getSuctionSpeed();

        long addMs = (long) (suctionSpeed * 1000);
        long time = System.currentTimeMillis() + addMs;
        
        skyHopper.setNextSuctionTime(time);
    }
}
