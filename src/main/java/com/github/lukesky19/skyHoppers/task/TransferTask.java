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
import com.github.lukesky19.skyHoppers.hopper.SkyContainer;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.github.lukesky19.skyHoppers.util.InventoryUtils.isInventoryFull;
import static com.github.lukesky19.skyHoppers.util.InventoryUtils.transferInventoryToContainer;

/**
 * This Task handles the custom transfers for SkyHoppers.
 */
public class TransferTask extends BukkitRunnable {
    private final SkyHoppers plugin;
    private final HopperManager hopperManager;

    /**
     * Constructor
     * @param plugin The SkyHoppers Plugin.
     * @param hopperManager A HopperManager instance.
     */
    public TransferTask(SkyHoppers plugin, HopperManager hopperManager) {
        this.plugin = plugin;
        this.hopperManager = hopperManager;
    }

    /**
     * The function ran every time this task is ran.
     */
    @Override
    public void run() {
        if(plugin.areSkyHoppersPaused()) return;

        for (SkyHopper currentSkyHopper : hopperManager.getSkyHoppers()) {
            if (currentSkyHopper == null
                    || currentSkyHopper.location() == null
                    || System.currentTimeMillis() < currentSkyHopper.nextTransferTime()
                    || !currentSkyHopper.enabled()
                    || !currentSkyHopper.location().isChunkLoaded()
                    || !(currentSkyHopper.location().getBlock().getState(false) instanceof Hopper hopper)
                    || hopper.getBlock().isBlockPowered()
                    || currentSkyHopper.containers().isEmpty())
                continue;

            transfer(currentSkyHopper, hopper, hopper.getInventory(), currentSkyHopper.transferAmount());

            long addMs = (long) (currentSkyHopper.transferSpeed() * 1000);
            long time = System.currentTimeMillis() + addMs;

            SkyHopper updatedSkyHopper = new SkyHopper(
                    true,
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
                    currentSkyHopper.nextSuctionTime(),
                    time);

            hopperManager.cacheSkyHopper(updatedSkyHopper.location(), updatedSkyHopper);
        }
    }

    /**
     * The logic for taking an Item from a SkyHopper's Inventory and transferring it to a linked container.
     * @param skyHopper The SkyHopper doing the transfer.
     * @param hopper The SkyHopper's Hopper.
     * @param hopperInv The SkyHopper's/Hopper's Inventory.
     * @param amount The amount to transfer.
     */
    private void transfer(SkyHopper skyHopper, @NotNull Hopper hopper, @NotNull Inventory hopperInv, int amount) {
        for (int i = 0; i <= (hopperInv.getSize() - 1); i++) {
            ItemStack hopperItem = hopperInv.getItem(i);

            if(hopperItem != null && !hopperItem.isEmpty()) {
                int amountToAdd = Math.min(hopperItem.getAmount(), amount);

                containerLoop:
                for(SkyContainer skyContainer : skyHopper.containers()) {
                    Location location = skyContainer.location().clone();

                    if(location.isChunkLoaded()) {
                        if(location.getBlock().getState(false) instanceof Container container) {
                            Inventory output = container.getInventory();
                            List<Material> filterItems = skyContainer.filterItems();

                            if(isInventoryFull(output)) continue;

                            switch(skyContainer.filterType()) {
                                case NONE -> {
                                    int result = transferInventoryToContainer(plugin, hopperItem, hopperInv, i, hopper, container, container.getInventory(), skyHopper.transferAmount());
                                    amount -= result;
                                    amountToAdd -= result;

                                    if (amount <= 0) return;
                                    if (amountToAdd <= 0) break containerLoop;
                                }

                                case WHITELIST -> {
                                    if (!filterItems.isEmpty() && filterItems.contains(hopperItem.getType())) {
                                        int result = transferInventoryToContainer(plugin, hopperItem, hopperInv, i, hopper, container, container.getInventory(), skyHopper.transferAmount());
                                        amount -= result;
                                        amountToAdd -= result;

                                        if (amount <= 0) return;
                                        if (amountToAdd <= 0) break containerLoop;
                                    }
                                }

                                case BLACKLIST -> {
                                    if (!filterItems.isEmpty() && !filterItems.contains(hopperItem.getType())) {
                                        int result = transferInventoryToContainer(plugin, hopperItem, hopperInv, i, hopper, container, container.getInventory(), skyHopper.transferAmount());
                                        amount -= result;
                                        amountToAdd -= result;

                                        if (amount <= 0) return;
                                        if (amountToAdd <= 0) break containerLoop;
                                    }
                                }

                                case DESTROY -> {
                                    if (!filterItems.isEmpty() && filterItems.contains(hopperItem.getType())) {
                                        hopperInv.setItem(i, new ItemStack(Material.AIR));

                                        return;
                                    }

                                    int result = transferInventoryToContainer(plugin, hopperItem, hopperInv, i, hopper, container, container.getInventory(), skyHopper.transferAmount());
                                    amount -= result;
                                    amountToAdd -= result;

                                    if (amount <= 0) return;
                                    if (amountToAdd <= 0) break containerLoop;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}