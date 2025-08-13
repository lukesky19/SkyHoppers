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
import com.github.lukesky19.skyHoppers.hook.protection.BentoBoxHook;
import com.github.lukesky19.skyHoppers.hook.protection.ProtectionHook;
import com.github.lukesky19.skyHoppers.hook.stacker.RoseStackerHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages Hooks into other plugins.
 */
public class HookManager {
    private final SkyHoppers skyHoppers;
    private final SettingsManager settingsManager;
    private final List<ProtectionHook> protectionHooks = new ArrayList<>();
    private static RoseStackerHook roseStackerHook;

    /**
     * Constructor
     * @param skyHoppers The SkyHoppers Plugin.
     * @param settingsManager A SettingsManager instance.
     */
    public HookManager(SkyHoppers skyHoppers, SettingsManager settingsManager) {
        this.skyHoppers = skyHoppers;
        this.settingsManager = settingsManager;
    }

    /**
     * Get the RoseStacker hook.
     * @return A RoseStacker hook.
     */
    public static RoseStackerHook getRoseStackerHook() {
        return roseStackerHook;
    }

    /**
     * Reload plugin hooks.
     */
    public void reload() {
        protectionHooks.clear();

        final PluginManager pluginManager = skyHoppers.getServer().getPluginManager();

        if(settingsManager.getSettings() != null) {
            Plugin bentoBox = pluginManager.getPlugin("BentoBox");
            if (bentoBox != null && bentoBox.isEnabled()) {
                if (!settingsManager.getSettings().disabledHooks().contains("BentoBox")) {
                    protectionHooks.add(new BentoBoxHook());
                }
            }

            Plugin roseStacker = pluginManager.getPlugin("RoseStacker");
            if (roseStacker != null && roseStacker.isEnabled()) {
                if (!settingsManager.getSettings().disabledHooks().contains("RoseStacker")) {
                    roseStackerHook = new RoseStackerHook();
                }
            }
        }
    }

    /**
     * Checks if a player can not build at a location.
     * @param player The Player
     * @param location The Location
     * @return true if the player can't build, false if not
     */
    public boolean canNotBuild(Player player, Location location) {
        for(ProtectionHook hook : this.protectionHooks) {
            if(!hook.canPlayerBuild(player, location) && !player.hasPermission("skyhoppers.admin"))
                return true;
        }

        return false;
    }

    /**
     * Checks if a player can not open containers (i.e., chests) at a location.
     * @param player The Player
     * @param location The Location
     * @return true if the player can't open, false if not
     */
    public boolean canNotOpen(Player player, Location location) {
        for (ProtectionHook hook : this.protectionHooks) {
            if (!hook.canPlayerOpen(player, location) && !player.hasPermission("skyhoppers.admin"))
                return true;
        }

        return false;
    }
}
