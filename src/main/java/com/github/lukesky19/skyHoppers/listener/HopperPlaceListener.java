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
package com.github.lukesky19.skyHoppers.listener;

import com.github.lukesky19.skyHoppers.config.LocaleManager;
import com.github.lukesky19.skyHoppers.config.data.Locale;
import com.github.lukesky19.skyHoppers.hook.HookManager;
import com.github.lukesky19.skyHoppers.skyhopper.SkyHopperManager;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skyHoppers.util.ImmutableLocation;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * This class listens for when a SkyHopper is placed.
 */
public class HopperPlaceListener implements Listener {
    private final @NotNull LocaleManager localeManager;
    private final @NotNull SkyHopperManager hopperManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param localeManager A {@link LocaleManager} instance.
     * @param hopperManager A {@link SkyHopperManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public HopperPlaceListener(
            @NotNull LocaleManager localeManager,
            @NotNull SkyHopperManager hopperManager,
            @NotNull HookManager hookManager) {
        this.localeManager = localeManager;
        this.hopperManager = hopperManager;
        this.hookManager = hookManager;
    }

    /**
     * Listens to when a SkyHopperConfig is placed.
     * @param blockPlaceEvent A {@link BlockPlaceEvent}.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopperPlace(BlockPlaceEvent blockPlaceEvent) {
        final Locale locale = localeManager.getLocale();
        final Player player = blockPlaceEvent.getPlayer();

        if (!(blockPlaceEvent.getBlock().getState(false) instanceof org.bukkit.block.Hopper hopper)) return;

        final ItemStack itemInHand = blockPlaceEvent.getItemInHand();
        boolean result = hopperManager.isItemStackSkyHopper(itemInHand);
        if (!result) return;

        if (hookManager.canNotBuild(player, hopper.getLocation())) {
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.noBuild()));

            blockPlaceEvent.setCancelled(true);

            return;
        }

        SkyHopper skyHopper = hopperManager.getSkyHopperProcessor().loadSkyHopper(null, itemInHand.getItemMeta().getPersistentDataContainer());
        if(skyHopper == null) {
            blockPlaceEvent.setCancelled(true);
            return;
        }

        try {
            ImmutableLocation immutableLocation = ImmutableLocation.fromBukkitLocation(hopper.getLocation());

            skyHopper.setOwner(player.getUniqueId());
            skyHopper.setLocation(immutableLocation);

            hopperManager.getSkyHopperSaver().saveSkyHopper(skyHopper);

            hopperManager.getSkyHopperDataManager().cacheSkyHopper(immutableLocation, skyHopper);

            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.hopperPlaced()));
        } catch (RuntimeException e) {
            blockPlaceEvent.setCancelled(true);

            throw new RuntimeException(e);
        }
    }
}
