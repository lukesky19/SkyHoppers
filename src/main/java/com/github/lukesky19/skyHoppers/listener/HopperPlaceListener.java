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

import com.github.lukesky19.skyHoppers.config.manager.LocaleManager;
import com.github.lukesky19.skyHoppers.config.record.Locale;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.HookManager;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

/**
 * This class listens for when a SkyHopper is placed.
 */
public class HopperPlaceListener implements Listener {
    private final LocaleManager localeManager;
    private final HopperManager hopperManager;
    private final HookManager hookManager;

    /**
     * Constructor
     * @param localeManager A LocaleManager instance.
     * @param hopperManager A HopperManager instance.
     * @param hookManager A HookManager instance.
     */
    public HopperPlaceListener(LocaleManager localeManager, HopperManager hopperManager, HookManager hookManager) {
        this.localeManager = localeManager;
        this.hopperManager = hopperManager;
        this.hookManager = hookManager;
    }

    /**
     * Listens to when a SkyHopperConfig is placed.
     * @param blockPlaceEvent A BlockPlaceEvent
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
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.noBuild()));

            blockPlaceEvent.setCancelled(true);

            return;
        }

        SkyHopper skyHopper = hopperManager.getSkyHopperFromPDC(null, itemInHand.getItemMeta().getPersistentDataContainer());
        if(skyHopper == null) return;

        skyHopper.setOwner(player.getUniqueId());
        skyHopper.setLocation(hopper.getLocation());

        hopperManager.saveSkyHopperToPDC(skyHopper);

        hopperManager.cacheSkyHopper(hopper.getLocation(), skyHopper);

        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.hopperPlaced()));
    }
}
