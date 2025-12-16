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
import com.github.lukesky19.skyHoppers.hook.HookManager;
import com.github.lukesky19.skyHoppers.skyhopper.SkyHopperManager;
import com.github.lukesky19.skyHoppers.task.tasks.QueuedTransferTask;
import com.github.lukesky19.skyHoppers.task.tasks.SuctionTask;
import com.github.lukesky19.skyHoppers.task.tasks.TransferTask;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class manages the plugin's {@link BukkitTask}s.
 */
public class TaskManager {
    private final @NotNull SkyHoppers skyHoppers;
    private final @NotNull SkyHopperManager hopperManager;
    private final @NotNull HookManager hookManager;

    private @Nullable BukkitTask transferTask;
    private @Nullable BukkitTask suctionTask;
    private @Nullable BukkitTask queuedTransferTask;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param hopperManager A {@link SkyHopperManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public TaskManager(
            @NotNull SkyHoppers skyHoppers,
            @NotNull SkyHopperManager hopperManager,
            @NotNull HookManager hookManager) {
        this.skyHoppers = skyHoppers;
        this.hopperManager = hopperManager;
        this.hookManager = hookManager;
    }

    /**
     * Start the plugin's {@link TransferTask}.
     */
    public void startTransferTask() {
        stopTransferTask();

        transferTask = new TransferTask(skyHoppers, hopperManager.getSkyHopperDataManager(), hookManager).runTaskTimer(skyHoppers, 0L, 1L);
    }

    /**
     * Stop the plugin's {@link TransferTask}.
     */
    public void stopTransferTask() {
        if(transferTask != null) {
            if(!transferTask.isCancelled()) {
                transferTask.cancel();
            }

            transferTask = null;
        }
    }

    /**
     * Start the plugin's {@link SuctionTask}.
     */
    public void startSuctionTask() {
        stopSuctionTask();

        suctionTask = new SuctionTask(skyHoppers, hopperManager.getSkyHopperDataManager(), hookManager).runTaskTimer(skyHoppers, 0L, 1L);
    }

    /**
     * Stop the plugin's {@link SuctionTask}.
     */
    public void stopSuctionTask() {
        if(suctionTask != null) {
            if(!suctionTask.isCancelled()) {
                suctionTask.cancel();
            }

            suctionTask = null;
        }
    }

    /**
     * Start the plugin's {@link QueuedTransferTask}.
     */
    public void startQueuedTransferTask() {
        stopQueuedTransferTask();

        queuedTransferTask = new QueuedTransferTask(skyHoppers, hopperManager, hookManager).runTaskTimer(skyHoppers, 0L, 1L);
    }

    /**
     * Stop the plugin's {@link QueuedTransferTask}.
     */
    public void stopQueuedTransferTask() {
        if(queuedTransferTask != null) {
            if(!queuedTransferTask.isCancelled()) {
                queuedTransferTask.cancel();
            }

            queuedTransferTask = null;
        }
    }
}
