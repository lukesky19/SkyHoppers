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
package com.github.lukesky19.skyHoppers.hook;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.SettingsManager;
import com.github.lukesky19.skyHoppers.config.data.Settings;
import com.github.lukesky19.skyHoppers.hook.impl.bentobox.BentoBoxHook;
import com.github.lukesky19.skyHoppers.hook.impl.quickshop.QuickShopHook;
import com.github.lukesky19.skyHoppers.hook.impl.rosestacker.RoseStackerHook;
import com.github.lukesky19.skyHoppers.hook.impl.vault.EconomyHook;
import com.github.lukesky19.skyHoppers.hook.interfaces.Hook;
import com.github.lukesky19.skyHoppers.hook.interfaces.ProtectionHook;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages Hooks into other plugins.
 */
public class HookManager {
    private final @NotNull SkyHoppers skyHoppers;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull Map<Class<?>, Hook> hooks = new HashMap<>();

    /**
     * Constructor
     * @param skyHoppers The SkyHoppers Plugin.
     * @param settingsManager A SettingsManager instance.
     */
    public HookManager(@NotNull SkyHoppers skyHoppers, @NotNull SettingsManager settingsManager) {
        this.skyHoppers = skyHoppers;
        this.settingsManager = settingsManager;

        registerHook(BentoBoxHook.class, new BentoBoxHook(skyHoppers));
        registerHook(RoseStackerHook.class, new RoseStackerHook(skyHoppers));
        registerHook(QuickShopHook.class, new QuickShopHook(skyHoppers));
        registerHook(EconomyHook.class, new EconomyHook(skyHoppers));
    }

    /**
     * Reload plugin hooks.
     */
    public void reload() {
        hooks.values().forEach(Hook::deinitialize);

        @Nullable Settings settings = settingsManager.getSettings();
        if(settings == null) {
            skyHoppers.getComponentLogger().warn(AdventureUtility.plain("Unable to setup hooks due to invalid plugin settings."));
            return;
        }

        List<String> disabledHooks = settings.disabledHooks().stream().map(String::toLowerCase).toList();

        hooks.values().forEach(hook -> {
            if(!disabledHooks.contains(hook.getHookName().toLowerCase())) {
                hook.initialize();
            }
        });
    }

    /**
     * Register a hook.
     * @param hookClass The class.
     * @param hook The class instance.
     * @param <T> Parameter for any class that extends {@link Hook}.
     */
    public <T extends Hook> void registerHook(@NotNull Class<T> hookClass, @NotNull Hook hook) {
        hooks.put(hookClass, hook);
        hook.initialize();
    }

    /**
     * Get a hook.
     * @param hookClass The class.
     * @return The class instance.
     * @param <T> Parameter for any class that extends {@link Hook}.
     */
    public @NotNull <T extends Hook> T getHook(@NotNull Class<T> hookClass) {
        return hookClass.cast(hooks.get(hookClass));
    }

    /**
     * Checks if a player can not build at a location.
     * @param player The Player
     * @param location The Location
     * @return true if the player can't build, false if not
     */
    public boolean canNotBuild(Player player, Location location) {
        return hooks.values()
                .stream()
                .filter(hook -> hook instanceof ProtectionHook)
                .map(hook -> (ProtectionHook) hook)
                .anyMatch(protectionHook -> !protectionHook.canPlayerBuild(player, location) && !player.hasPermission("skyhoppers.admin"));
    }

    /**
     * Checks if a player can not open containers (i.e., chests) at a location.
     * @param player The Player
     * @param location The Location
     * @return true if the player can't open, false if not
     */
    public boolean canNotOpen(Player player, Location location) {
        return hooks.values()
                .stream()
                .filter(hook -> hook instanceof ProtectionHook)
                .map(hook -> (ProtectionHook) hook)
                .anyMatch(protectionHook -> !protectionHook.canPlayerOpen(player, location) && !player.hasPermission("skyhoppers.admin"));
    }
}
