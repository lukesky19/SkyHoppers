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
import com.github.lukesky19.skyHoppers.config.SettingsManager;
import com.github.lukesky19.skyHoppers.config.data.Settings;
import com.github.lukesky19.skyHoppers.hook.HookManager;
import com.github.lukesky19.skyHoppers.skyhopper.SkyHopperManager;
import com.github.lukesky19.skyHoppers.task.tasks.*;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class manages the plugin's {@link BukkitTask}s.
 */
public class TaskManager {
    private final @NotNull SkyHoppers skyHoppers;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull SkyHopperManager hopperManager;
    private final @NotNull HookManager hookManager;

    private @Nullable BukkitTask transferTask;
    private @Nullable BukkitTask suctionTask;
    private @Nullable BukkitTask skyHopperLoadTask;
    private @Nullable BukkitTask skyHopperUnloadTask;
    private @Nullable BukkitTask queuedTransferTask;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param hopperManager A {@link SkyHopperManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public TaskManager(
            @NotNull SkyHoppers skyHoppers,
            @NotNull SettingsManager settingsManager,
            @NotNull SkyHopperManager hopperManager,
            @NotNull HookManager hookManager) {
        this.skyHoppers = skyHoppers;
        this.settingsManager = settingsManager;
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
     * Start the plugin's {@link SkyHopperLoadTask}.
     */
    public void startSkyHopperLoadTask() {
        stopSkyHopperLoadTask();

        @Nullable Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        skyHopperLoadTask = new SkyHopperLoadTask(hopperManager, settings.chunksPerPeriod()).runTaskTimer(skyHoppers, settings.periodInTicks(), settings.periodInTicks());
    }

    /**
     * Stop the plugin's {@link SkyHopperLoadTask}.
     */
    public void stopSkyHopperLoadTask() {
        if(skyHopperLoadTask != null) {
            if(!skyHopperLoadTask.isCancelled()) {
                skyHopperLoadTask.cancel();
            }

            skyHopperLoadTask = null;
        }
    }

    /**
     * Start the plugin's {@link SkyHopperUnloadTask}.
     */
    public void startSkyHopperUnloadTask() {
        stopSkyHopperUnloadTask();

        @Nullable Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        skyHopperUnloadTask = new SkyHopperUnloadTask(hopperManager, settings.chunksPerPeriod()).runTaskTimer(skyHoppers, settings.periodInTicks(), settings.periodInTicks());
    }

    /**
     * Stop the plugin's {@link SkyHopperUnloadTask}.
     */
    public void stopSkyHopperUnloadTask() {
        if(skyHopperUnloadTask != null) {
            if(!skyHopperUnloadTask.isCancelled()) {
                skyHopperUnloadTask.cancel();
            }

            skyHopperUnloadTask = null;
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
