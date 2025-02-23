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
import com.github.lukesky19.skylib.format.FormatUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

/**
 * This class listens for when a SkyHopper is broken to check if it can be broken, to remove all necessary data, and to drop the SkyHopper item.
 */
public class BlockBreakListener implements Listener {
    private final LocaleManager localeManager;
    private final HopperManager hopperManager;
    private final HookManager hookManager;

    /**
     * Constructor
     * @param localeManager A LocaleManager instance.
     * @param hopperManager A HopperManager instance.
     * @param hookManager A HookManager instance.
     */
    public BlockBreakListener(LocaleManager localeManager, HopperManager hopperManager, HookManager hookManager) {
        this.localeManager = localeManager;
        this.hopperManager = hopperManager;
        this.hookManager = hookManager;
    }

    /**
     * Listens for when a Hopper is broken and checks if it is a SkyHopper and that is can be broken by the Player.
     * @param blockBreakEvent A BlockBreakEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopperBreak(BlockBreakEvent blockBreakEvent) {
        final Block block = blockBreakEvent.getBlock();
        final Locale locale = localeManager.getLocale();
        final Player player = blockBreakEvent.getPlayer();

        if (!(blockBreakEvent.getBlock().getState(false) instanceof org.bukkit.block.Hopper hopper)) {
            return;
        }

        Location hopperLocation = hopper.getLocation().clone();

        final SkyHopper skyHopper = hopperManager.getSkyHopper(hopperLocation);
        if (skyHopper == null)
            return;

        if (hookManager.canNotBuild(player, hopperLocation)) {
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.noBuild()));
            blockBreakEvent.setCancelled(true);
            return;
        }

        if(player.hasPermission("skyhoppers.admin") || (skyHopper.owner() != null && skyHopper.owner().equals(player.getUniqueId())) || skyHopper.members().contains(player.getUniqueId())) {
            // Delete the hopper's data
            hopperManager.removeSkyHopper(hopperLocation);

            player.sendMessage(FormatUtil.format(locale.prefix() + locale.hopperBroken()));

            // Drop the items in the hopper
            Arrays.stream(hopper.getInventory().getContents())
                    .filter(Objects::nonNull)
                    .forEach(itemStack -> hopper.getWorld().dropItemNaturally(hopperLocation, itemStack));

            // Cancel drops so that the hopper item doesn't drop.
            blockBreakEvent.setDropItems(false);

            final ItemStack skyHopperItem = hopperManager.createItemStackFromSkyHopper(skyHopper, 1);
            if(skyHopperItem != null) {
                block.getWorld().dropItem(block.getLocation().clone(), skyHopperItem);
            }
        } else {
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.noBreak()));
            blockBreakEvent.setCancelled(true);
        }
    }
}
