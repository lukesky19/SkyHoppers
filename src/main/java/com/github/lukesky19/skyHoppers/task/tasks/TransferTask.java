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
package com.github.lukesky19.skyHoppers.task.tasks;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.hook.HookManager;
import com.github.lukesky19.skyHoppers.hook.impl.quickshop.QuickShopHook;
import com.github.lukesky19.skyHoppers.skyhopper.SkyHopperDataManager;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyContainer;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skyHoppers.transfer.impl.container.InventoryToContainerTransfer;
import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.github.lukesky19.skyHoppers.util.InventoryUtils.isInventoryEmpty;
import static com.github.lukesky19.skyHoppers.util.InventoryUtils.isInventoryFull;

/**
 * This Task handles the custom transfers for SkyHoppers.
 */
public class TransferTask extends BukkitRunnable {
    private final @NotNull SkyHoppers plugin;
    private final @NotNull SkyHopperDataManager hopperManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param plugin A {@link SkyHoppers} instance.
     * @param hopperManager A {@link SkyHopperDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public TransferTask(
            @NotNull SkyHoppers plugin,
            @NotNull SkyHopperDataManager hopperManager,
            @NotNull HookManager hookManager) {
        this.plugin = plugin;
        this.hopperManager = hopperManager;
        this.hookManager = hookManager;
    }

    /**
     * The function ran every time this task is ran.
     */
    @Override
    public void run() {
        if (plugin.areSkyHoppersPaused()) return;

        for(SkyHopper currentSkyHopper : hopperManager.getSkyHoppersList()) {
            if(currentSkyHopper == null
                    || !currentSkyHopper.isSkyHopperEnabled()
                    || currentSkyHopper.getLinkedContainers().isEmpty()
                    || System.currentTimeMillis() < currentSkyHopper.getNextTransferTime()
                    || currentSkyHopper.getLocation() == null
                    || !currentSkyHopper.getLocation().isChunkLoaded()
                    || !(currentSkyHopper.getLocation().getBlock().getState(false) instanceof Hopper hopper)
                    || hopper.getBlock().isBlockPowered()
                    || isInventoryEmpty(hopper.getInventory())) {
                continue;
            }

            transfer(currentSkyHopper, hopper.getInventory(), currentSkyHopper.getTransferAmount());

            long addMs = (long) (currentSkyHopper.getTransferSpeed() * 1000);
            long time = System.currentTimeMillis() + addMs;

            currentSkyHopper.setNextTransferTime(time);
        }
    }

    /**
     * The logic for taking an Item from a {@link SkyHopper}'s Inventory and transferring it to a linked container.
     * @param skyHopper The {@link SkyHopper} doing the transfer.
     * @param hopperInv The SkyHopper's/Hopper's Inventory.
     * @param amount The amount to transfer.
     */
    private void transfer(@NotNull SkyHopper skyHopper, @NotNull Inventory hopperInv, int amount) {
        for(SkyContainer skyContainer : skyHopper.getLinkedContainers()) {
            Location location = skyContainer.getLocation().clone();
            if(!location.isChunkLoaded()) continue;
            if(!(location.getBlock().getState(false) instanceof Container container)) continue;
            if(isInventoryFull(container.getInventory())) continue;
            @Nullable SkyHopper destinationSkyHopper = hopperManager.getSkyHopper(location);

            int transferred = InventoryToContainerTransfer.transfer(hookManager.getHook(QuickShopHook.class), hopperInv, destinationSkyHopper, skyContainer, container, container.getInventory(), amount);
            amount -= transferred;
            if(amount <= 0) break;
        }
    }
}