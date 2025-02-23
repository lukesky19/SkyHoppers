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
package com.github.lukesky19.skyHoppers.manager;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.task.TransferTask;
import com.github.lukesky19.skyHoppers.task.SuctionTask;
import org.bukkit.scheduler.BukkitTask;

public class TaskManager {
    private final SkyHoppers skyHoppers;
    private final HopperManager hopperManager;

    private BukkitTask transferTask;
    private BukkitTask suctionTask;

    public TaskManager(SkyHoppers skyHoppers, HopperManager hopperManager) {
        this.skyHoppers = skyHoppers;
        this.hopperManager = hopperManager;
    }

    public void startTransferTask() {
        transferTask = new TransferTask(skyHoppers, hopperManager).runTaskTimer(skyHoppers, 0L, 1L);
    }

    public void stopTransferTask() {
        if(transferTask != null && !transferTask.isCancelled()) {
            transferTask.cancel();
            transferTask = null;
        }
    }

    public void startSuctionTask() {
        suctionTask = new SuctionTask(skyHoppers, hopperManager).runTaskTimer(skyHoppers, 0L, 1L);
    }

    public void stopSuctionTask() {
        if(suctionTask != null && !suctionTask.isCancelled()) {
            suctionTask.cancel();
            suctionTask = null;
        }
    }
}
