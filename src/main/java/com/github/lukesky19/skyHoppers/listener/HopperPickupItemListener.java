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
import com.github.lukesky19.skyHoppers.hook.HookManager;
import com.github.lukesky19.skyHoppers.hook.impl.rosestacker.RoseStackerHook;
import com.github.lukesky19.skyHoppers.skyhopper.SkyHopperManager;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skyHoppers.transfer.impl.entity.ItemEntityToInventoryTransfer;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

/**
 * This class listens to when a SkyHopper picks up an ItemStack
 */
public class HopperPickupItemListener implements Listener {
    private final @NotNull SkyHoppers plugin;
    private final @NotNull SkyHopperManager hopperManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param plugin The {@link SkyHoppers} instance.
     * @param hopperManager A {@link SkyHopperManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public HopperPickupItemListener(
            @NotNull SkyHoppers plugin,
            @NotNull SkyHopperManager hopperManager,
            @NotNull HookManager hookManager) {
        this.plugin = plugin;
        this.hopperManager = hopperManager;
        this.hookManager = hookManager;
    }

    /**
     * Listens to when a SkyHopper picks up an item using the vanilla method, i.e., an ItemStack directly on-top of the Hopper.
     * @param inventoryPickupItemEvent An {@link InventoryPickupItemEvent}
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopperPickup(InventoryPickupItemEvent inventoryPickupItemEvent) {
        // If the inventory is not that of a Hopper, do nothing.
        if (!(inventoryPickupItemEvent.getInventory().getHolder(false) instanceof Hopper hopper)) return;
        if(inventoryPickupItemEvent.getInventory().getLocation() == null) return;

        // Get the SkyHopper for the given location
        SkyHopper skyHopper = hopperManager.getSkyHopperDataManager().getSkyHopper(inventoryPickupItemEvent.getInventory().getLocation());
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

        // Get the RoseStacker hook
        RoseStackerHook roseStackerHook = hookManager.getHook(RoseStackerHook.class);

        // Get the item entity and item amount
        Item groundItem = inventoryPickupItemEvent.getItem();
        int groundItemAmount = roseStackerHook.getItemAmount(groundItem);
        ItemStack groundStack = groundItem.getItemStack();
        ItemType groundType = groundStack.getType().asItemType();
        if(groundType == null) return;

        int result = ItemEntityToInventoryTransfer.transfer(roseStackerHook, groundItem, groundStack, groundType, groundItemAmount, skyHopper, hopper.getInventory(), skyHopper.getSuctionAmount());

        if(result > 0) {
            if(skyHopper.isParticlesEnabled()) {
                // Highlight hopper that sucked up the item
                hopper.getWorld().spawnParticle(Particle.DUST, hopper.getLocation().clone(), 5, 0.5, 0.5, 0.5, 0.0, new Particle.DustOptions(Color.YELLOW, 1));

                // Highlight the item that was sucked up
                groundItem.getWorld().spawnParticle(Particle.WITCH, hopper.getLocation().clone(), 3, 0.0, 0.0, 0.0, 0.0);
            }

            updateSuctionTime(skyHopper);
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
