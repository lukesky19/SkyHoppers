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
package com.github.lukesky19.skyHoppers;

import com.github.lukesky19.skyHoppers.command.SkyHopperCommand;
import com.github.lukesky19.skyHoppers.config.GUIConfigManager;
import com.github.lukesky19.skyHoppers.config.LocaleManager;
import com.github.lukesky19.skyHoppers.config.SettingsManager;
import com.github.lukesky19.skyHoppers.database.ConnectionManager;
import com.github.lukesky19.skyHoppers.database.DatabaseManager;
import com.github.lukesky19.skyHoppers.database.QueueManager;
import com.github.lukesky19.skyHoppers.gui.GUIManager;
import com.github.lukesky19.skyHoppers.hook.HookManager;
import com.github.lukesky19.skyHoppers.listener.*;
import com.github.lukesky19.skyHoppers.skyhopper.SkyHopperManager;
import com.github.lukesky19.skyHoppers.task.TaskManager;
import com.github.lukesky19.skylib.libs.bstats.bukkit.Metrics;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * The main plugin's class
 */
public final class SkyHoppers extends JavaPlugin {
    private SkyHopperManager hopperManager;
    private HookManager hookManager;
    private SettingsManager settingsManager;
    private LocaleManager localeManager;
    private GUIConfigManager guiConfigManager;
    private TaskManager taskManager;
    private GUIManager guiManager;
    private boolean pauseSkyHoppers = true;

    /**
     * Are SkyHoppers paused globally?
     * @return true if SkyHoppers are paused, false if not
     */
    public boolean areSkyHoppersPaused() {
        return pauseSkyHoppers;
    }

    /**
     * Pauses all SkyHoppers globally.
     */
    public void pauseSkyHoppers() {
        this.getServer().getScheduler().runTaskLater(this, () -> pauseSkyHoppers = true, 1L);
    }

    /**
     * Unpauses all SkyHoppers globally.
     */
    public void unPauseSkyHoppers() {
        this.getServer().getScheduler().runTaskLater(this, () -> pauseSkyHoppers = false, 1L);
    }

    /**
     * Default Constructor
     */
    public SkyHoppers() {}

    /**
     * Plugin's startup logic
     */
    @Override
    public void onEnable() {
        // Setup dependencies
        setupBStats();

        ConnectionManager connectionManager = new ConnectionManager(this);
        QueueManager queueManager = new QueueManager(connectionManager);
        DatabaseManager databaseManager = new DatabaseManager(this, connectionManager, queueManager);

        settingsManager = new SettingsManager(this);
        localeManager = new LocaleManager(this, settingsManager);
        guiConfigManager = new GUIConfigManager(this);
        guiManager = new GUIManager(this);
        hookManager = new HookManager(this, settingsManager);

        hopperManager = new SkyHopperManager(this, settingsManager, localeManager, databaseManager, guiManager);
        taskManager = new TaskManager(this, settingsManager, hopperManager, hookManager);
        SkyHopperCommand skyHopperCommand = new SkyHopperCommand(this, settingsManager, localeManager, hopperManager);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands ->
                commands.registrar().register(skyHopperCommand.createCommand(),
                        "Command to manage and use the SkyHoppers plugin.", List.of("skyhopper", "skh")));

        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new EntityExplodeListener(hopperManager), this);
        pluginManager.registerEvents(new BlockExplodeListener(hopperManager), this);
        pluginManager.registerEvents(new EntityChangeBlockListener(hopperManager), this);

        HopperClickListener hopperClickListener = new HopperClickListener(this, settingsManager, localeManager, guiConfigManager, hopperManager, hookManager, guiManager);

        pluginManager.registerEvents(new BlockBreakListener(this, settingsManager, localeManager, hopperManager, hookManager, hopperClickListener), this);
        pluginManager.registerEvents(new HopperPlaceListener(localeManager, hopperManager, hookManager), this);
        pluginManager.registerEvents(new HopperPickupItemListener(this, hopperManager, hookManager), this);
        pluginManager.registerEvents(new ChunkListener(hopperManager), this);

        pluginManager.registerEvents(new HopperMoveItemListener(this, hopperManager), this);
        pluginManager.registerEvents(hopperClickListener, this);
        pluginManager.registerEvents(new InventoryListener(guiManager), this);

        // Register API
        SkyHoppersAPI skyHoppersAPI = new SkyHoppersAPI(hopperManager);
        this.getServer().getServicesManager().register(SkyHoppersAPI.class, skyHoppersAPI, this, ServicePriority.Lowest);

        reload();
    }

    /**
     * Plugin's shutdown logic
     */
    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);

        guiManager.closeOpenGUIs(true);
    }

    /**
     * Plugin's reload logic
     */
    public void reload() {
        this.pauseSkyHoppers();

        guiManager.closeOpenGUIs(false);

        settingsManager.reload();
        localeManager.reload();
        guiConfigManager.reload();
        hookManager.reload();
        hopperManager.reload();

        taskManager.startTransferTask();
        taskManager.startSuctionTask();
        taskManager.startQueuedTransferTask();
        taskManager.startSkyHopperLoadTask();
        taskManager.startSkyHopperUnloadTask();

        this.unPauseSkyHoppers();
    }

    /**
     * Set up bstats
     */
    private void setupBStats() {
        int pluginId = 23993;
        new Metrics(this, pluginId);
    }
}
