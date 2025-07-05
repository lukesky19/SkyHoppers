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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.QuickShopBukkit;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.github.lukesky19.skyHoppers.command.SkyHopperCommand;
import com.github.lukesky19.skyHoppers.config.manager.GUIConfigManager;
import com.github.lukesky19.skyHoppers.config.manager.LocaleManager;
import com.github.lukesky19.skyHoppers.config.manager.SettingsManager;
import com.github.lukesky19.skyHoppers.database.ConnectionManager;
import com.github.lukesky19.skyHoppers.database.DatabaseManager;
import com.github.lukesky19.skyHoppers.database.QueueManager;
import com.github.lukesky19.skyHoppers.listener.*;
import com.github.lukesky19.skyHoppers.manager.*;
import com.github.lukesky19.skyHoppers.task.DelayedTask;
import com.github.lukesky19.skylib.libs.bstats.bukkit.Metrics;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The main plugin's class
 */
public final class SkyHoppers extends JavaPlugin {
    private HopperManager hopperManager;
    private HookManager hookManager;
    private SettingsManager settingsManager;
    private LocaleManager localeManager;
    private GUIConfigManager guiConfigManager;
    private TaskManager taskManager;
    private GUIManager guiManager;
    private Economy economy;
    private QuickShop quickShop;
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
     * Get the Economy instance
     * @return The Economy from vault
     */
    public Economy getEconomy() {
        return this.economy;
    }

    /**
     * Get the QuickShop plugin if loaded, or null
     * @return Gets the QuickShop plugin if loaded or returns null
     */
    @Nullable
    public QuickShop getQuickShop() {
        return quickShop;
    }

    /**
     * Plugin's startup logic
     */
    @Override
    public void onEnable() {
        // Setup dependencies
        setupBStats();
        boolean result = setupEconomy();
        if(!result) {
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if(this.getServer().getPluginManager().isPluginEnabled("QuickShop-Hikari")) {
            quickShop = ((QuickShopBukkit) QuickShopAPI.getPluginInstance()).getQuickShop();
        }

        ConnectionManager connectionManager = new ConnectionManager(this);
        QueueManager queueManager = new QueueManager(connectionManager);
        DatabaseManager databaseManager = new DatabaseManager(this, connectionManager, queueManager);

        settingsManager = new SettingsManager(this);
        localeManager = new LocaleManager(this, settingsManager);
        guiConfigManager = new GUIConfigManager(this);
        guiManager = new GUIManager(this);
        hookManager = new HookManager(this, settingsManager);
        hopperManager = new HopperManager(this, settingsManager, localeManager, databaseManager, guiManager);
        taskManager = new TaskManager(this, hopperManager);
        SkyHopperCommand skyHopperCommand = new SkyHopperCommand(this, localeManager, hopperManager, settingsManager);

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
        pluginManager.registerEvents(new HopperPickupItemListener(this, hopperManager), this);
        pluginManager.registerEvents(new ChunkLoadListener(hopperManager), this);

        DelayedTask delayedTask = new DelayedTask(this, hopperManager);
        delayedTask.runTaskTimer(this, 0L, 1L);

        pluginManager.registerEvents(new HopperMoveItemListener(this, hopperManager, delayedTask), this);
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
        taskManager.stopTransferTask();
        taskManager.startTransferTask();
        taskManager.stopSuctionTask();
        taskManager.startSuctionTask();

        // Unpause SkyHoppers once all data is loaded
        this.unPauseSkyHoppers();
    }

    /**
     * Set up the Vault/Economy dependency
     * @return true if setup successfully, false if not
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.economy = rsp.getProvider();
                return true;
            }
        } else {
            getComponentLogger().error(MiniMessage.miniMessage().deserialize("<red>SkyHoppers has been disabled due to no Vault dependency found!</red>"));
        }

        return false;
    }

    /**
     * Set up bstats
     */
    private void setupBStats() {
        int pluginId = 23993;
        new Metrics(this, pluginId);
    }
}
