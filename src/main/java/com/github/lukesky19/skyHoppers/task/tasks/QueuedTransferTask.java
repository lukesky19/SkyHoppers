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
import com.github.lukesky19.skyHoppers.skyhopper.SkyHopperManager;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skyHoppers.task.data.QueuedTransfer;
import com.github.lukesky19.skyHoppers.transfer.impl.container.ContainerToInventoryTransfer;
import com.github.lukesky19.skyHoppers.transfer.impl.container.InventoryToContainerTransfer;
import com.github.lukesky19.skyHoppers.util.ImmutableLocation;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This Task handles transfers scheduled from the HopperMoveItemListener.
 */
public class QueuedTransferTask extends BukkitRunnable {
    private final @NotNull SkyHoppers plugin;
    private final @NotNull SkyHopperManager hopperManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param plugin A {@link SkyHoppers} instance.
     * @param hopperManager A {@link SkyHopperManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public QueuedTransferTask(
            @NotNull SkyHoppers plugin,
            @NotNull SkyHopperManager hopperManager,
            @NotNull HookManager hookManager) {
        this.plugin = plugin;
        this.hopperManager = hopperManager;
        this.hookManager = hookManager;
    }

    /**
     * Process the transfers that are queued.
     */
    @Override
    public void run() {
        if(plugin.areSkyHoppersPaused()) hopperManager.getSkyHopperProcessor().clearQueuedTransfers();

        @Nullable QueuedTransfer queuedTransfer = hopperManager.getSkyHopperProcessor().getQueuedTransfer();
        while(queuedTransfer != null) {
            long now = System.currentTimeMillis();
            ImmutableLocation sourceLocation = queuedTransfer.sourceLocation();
            ImmutableLocation destinationLocation = queuedTransfer.destinationLocation();
            SkyHopper sourceSkyHopper = hopperManager.getSkyHopperDataManager().getSkyHopper(sourceLocation);
            SkyHopper destinationSkyHopper = hopperManager.getSkyHopperDataManager().getSkyHopper(destinationLocation);

            // remove if neither hopper exists
            if(sourceSkyHopper == null && destinationSkyHopper == null) {
                queuedTransfer = hopperManager.getSkyHopperProcessor().getQueuedTransfer();
                continue;
            }

            BlockState sourceState = sourceLocation.getBlock().getState(false);
            BlockState destinationState = destinationLocation.getBlock().getState(false);
            if(!(sourceState instanceof Container sourceContainer) || !(destinationState instanceof Container destinationContainer)) {
                queuedTransfer = hopperManager.getSkyHopperProcessor().getQueuedTransfer();
                continue;
            }

            // choose active hopper and direction
            boolean initiatorIsSource = queuedTransfer.initiatorIsSource();
            @Nullable SkyHopper actingHopper = initiatorIsSource ? sourceSkyHopper : destinationSkyHopper;
            boolean isSuction = queuedTransfer.isSuction();

            // if both hoppers present, both must be enabled and both blocks unpowered
            if(sourceSkyHopper != null && destinationSkyHopper != null) {
                if(!sourceSkyHopper.isSkyHopperEnabled() || !destinationSkyHopper.isSkyHopperEnabled()
                        || sourceContainer.getBlock().isBlockPowered() || destinationContainer.getBlock().isBlockPowered()) {
                    queuedTransfer = hopperManager.getSkyHopperProcessor().getQueuedTransfer();
                    continue;
                }
            } else {
                // only one hopper present: ensure acting hopper is enabled and its block unpowered
                if(actingHopper == null || !actingHopper.isSkyHopperEnabled()
                        || (initiatorIsSource ? sourceContainer.getBlock().isBlockPowered() : destinationContainer.getBlock().isBlockPowered())) {
                    queuedTransfer = hopperManager.getSkyHopperProcessor().getQueuedTransfer();
                    continue;
                }
            }

            // determine timing and amount based on suction/transfer and which hopper is acting
            long nextTime = (isSuction ? actingHopper.getNextSuctionTime() : actingHopper.getNextTransferTime());
            if(nextTime > now) {
                queuedTransfer = hopperManager.getSkyHopperProcessor().getQueuedTransfer();
                continue;
            }

            QuickShopHook quickShopHook = hookManager.getHook(QuickShopHook.class);
            int amount = isSuction ? actingHopper.getSuctionAmount() : actingHopper.getTransferAmount();

            // perform the appropriate transfer call depending on which hopper(s) exist and initiator
            if (sourceSkyHopper != null && destinationSkyHopper != null) {
                // both exist: transfer between container and hopper (direction depends on initiator)
                if(initiatorIsSource) {
                    ContainerToInventoryTransfer.transfer(quickShopHook, sourceContainer.getInventory(), destinationSkyHopper, destinationSkyHopper.getSkyContainerByLocation(destinationLocation), destinationContainer.getInventory(), amount);
                } else {
                    ContainerToInventoryTransfer.transfer(quickShopHook, destinationContainer.getInventory(), sourceSkyHopper, sourceSkyHopper.getSkyContainerByLocation(destinationLocation), sourceContainer.getInventory(), amount);
                }
            } else if (sourceSkyHopper != null) {
                // only source hopper exists -> move from source container to destination inventory (container)
                InventoryToContainerTransfer.transfer(quickShopHook, sourceContainer.getInventory(), null, null, destinationContainer, destinationContainer.getInventory(), amount);
            } else {
                // only destination hopper exists -> move from source container into hopper
                ContainerToInventoryTransfer.transfer(quickShopHook, sourceContainer.getInventory(), destinationSkyHopper, destinationSkyHopper.getSkyContainerByLocation(destinationLocation), destinationContainer.getInventory(), amount);
            }

            // update acting hopper timing
            if(isSuction) {
                updateSkyHopperSuctionTime(actingHopper);
            } else {
                updateSkyHopperTransferTime(actingHopper);
            }

            queuedTransfer = hopperManager.getSkyHopperProcessor().getQueuedTransfer();
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