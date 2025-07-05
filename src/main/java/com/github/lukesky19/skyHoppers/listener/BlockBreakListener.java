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

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.manager.LocaleManager;
import com.github.lukesky19.skyHoppers.config.manager.SettingsManager;
import com.github.lukesky19.skyHoppers.config.record.Locale;
import com.github.lukesky19.skyHoppers.config.record.Settings;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.HookManager;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class listens for when a SkyHopper is broken to check if it can be broken, to remove all necessary data, and to drop the SkyHopper item.
 */
public class BlockBreakListener implements Listener {
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull HopperManager hopperManager;
    private final @NotNull HookManager hookManager;
    private final @NotNull HopperClickListener hopperClickListener;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param hopperManager A {@link HopperManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param hopperClickListener A {@link HopperClickListener} instance.
     */
    public BlockBreakListener(@NotNull SkyHoppers skyHoppers, @NotNull SettingsManager settingsManager, @NotNull LocaleManager localeManager, @NotNull HopperManager hopperManager, @NotNull HookManager hookManager, @NotNull HopperClickListener hopperClickListener) {
        this.logger = skyHoppers.getComponentLogger();
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.hopperManager = hopperManager;
        this.hookManager = hookManager;
        this.hopperClickListener = hopperClickListener;
    }

    /**
     * Listens for when a Hopper is broken and checks if it is a SkyHopper and that is can be broken by the Player.
     * @param blockBreakEvent A BlockBreakEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopperBreak(BlockBreakEvent blockBreakEvent) {
        Block block = blockBreakEvent.getBlock();
        Locale locale = localeManager.getLocale();
        Player player = blockBreakEvent.getPlayer();

        if(!(blockBreakEvent.getBlock().getState(false) instanceof Hopper hopper)) {
            return;
        }

        Location hopperLocation = hopper.getLocation().clone();

        SkyHopper skyHopper = hopperManager.getSkyHopper(hopperLocation);
        if(skyHopper == null) return;

        if(hookManager.canNotBuild(player, hopperLocation)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.noBuild()));
            blockBreakEvent.setCancelled(true);
            return;
        }

        if(player.hasPermission("skyhoppers.admin") || (skyHopper.getOwner() != null && skyHopper.getOwner().equals(player.getUniqueId())) || skyHopper.getMembers().contains(player.getUniqueId())) {
            // Delete the hopper's data
            hopperManager.removeSkyHopper(hopperLocation);

            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.hopperBroken()));

            boolean dropToInventory;
            @Nullable Settings settings = settingsManager.getSettings();
            if(settings == null) {
                logger.warn(AdventureUtil.serialize("Plugin settings are invalid. Broken SkyHoppers will be dropped to the ground by default."));
                dropToInventory = false;
            } else {
                dropToInventory = settings.dropToInventory();
            }

            // Cancel drops so that the hopper item doesn't drop.
            blockBreakEvent.setDropItems(false);

            // Drop the items in the hopper
            for(ItemStack itemStack : hopper.getInventory().getContents()) {
                if(itemStack == null || itemStack.isEmpty()) continue;

                if(dropToInventory) {
                    PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation());
                } else {
                    hopper.getWorld().dropItemNaturally(hopperLocation, itemStack);
                }
            }

            ItemStack skyHopperItem = hopperManager.createItemStackFromSkyHopper(skyHopper, 1);
            if(skyHopperItem != null) {
                if(dropToInventory) {
                    PlayerUtil.giveItem(player.getInventory(), skyHopperItem, skyHopperItem.getAmount(), player.getLocation());
                } else {
                    block.getWorld().dropItem(block.getLocation().clone(), skyHopperItem);
                }
            }

            hopperClickListener.disableLinkingForLocation(hopperLocation);
        } else {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.noBreak()));
            blockBreakEvent.setCancelled(true);
        }
    }

    /**
     * Handles when a {@link Container} is broken and checks if that container is a linked container.
     * For the purposes of refreshing GUIs.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLinkedContainerBreak(BlockBreakEvent blockBreakEvent) {
        if(!(blockBreakEvent.getBlock().getState(false) instanceof Container container)) {
            return;
        }

        hopperManager.handleContainerBroken(container);
    }
}
