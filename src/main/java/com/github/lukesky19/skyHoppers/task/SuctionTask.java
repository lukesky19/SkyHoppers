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
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.*;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.lukesky19.skyHoppers.util.InventoryUtils.addGroundItemToInventory;
import static com.github.lukesky19.skyHoppers.util.RoseStackerUtils.*;

/**
 * This Task handles the custom suctioning for SkyHoppers.
 */
public class SuctionTask extends BukkitRunnable {
    private final @NotNull SkyHoppers plugin;
    private final @NotNull ComponentLogger logger;
    private final @NotNull HopperManager hopperManager;

    /**
     * Constructor
     * @param plugin The SkyHoppers Plugin.
     * @param hopperManager A HopperManager instance.
     */
    public SuctionTask(@NotNull SkyHoppers plugin, @NotNull HopperManager hopperManager) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
        this.hopperManager = hopperManager;
    }

    /**
     * The function ran every time this task is ran.
     */
    @Override
    public void run() {
        if(plugin.areSkyHoppersPaused()) return;

        for(SkyHopper currentSkyHopper : hopperManager.getSkyHoppers()) {
            if(currentSkyHopper == null
                    || currentSkyHopper.getLocation() == null
                    || currentSkyHopper.getNextSuctionTime() > System.currentTimeMillis()
                    || !currentSkyHopper.isSkyHopperEnabled()
                    || !currentSkyHopper.getLocation().isChunkLoaded()
                    || !(currentSkyHopper.getLocation().getBlock().getState(false) instanceof Hopper hopper))
                continue;

            final double suctionRange = currentSkyHopper.getSuctionRange() + 0.5;
            Location centered = currentSkyHopper.getLocation().clone().add(0.5, 0.5, 0.5);

            List<Item> groundItems = centered.getNearbyEntities(suctionRange, suctionRange, suctionRange).stream().filter(entity -> entity instanceof Item).map(entity -> (Item) entity).toList();
            if(groundItems.isEmpty()) continue;

            collect(currentSkyHopper, hopper, groundItems, currentSkyHopper.getSuctionAmount());

            double suctionSpeed = currentSkyHopper.getSuctionSpeed();

            long addMs = (long) (suctionSpeed * 1000);
            long time = System.currentTimeMillis() + addMs;
            
            currentSkyHopper.setNextSuctionTime(time);
        }
    }

    /**
     * The logic for taking an Item from the ground and adding it to the {@link SkyHopper}'s Inventory.
     * @param skyHopper The {@link SkyHopper} to add the Item to.
     * @param hopper The {@link SkyHopper}'s Hopper.
     * @param groundItems The List of Items around the SkyHopper.
     * @param suctionAmount The amount that should be transferred.
     */
    private void collect(@NotNull SkyHopper skyHopper, @NotNull Hopper hopper, @NotNull List<Item> groundItems, int suctionAmount) {
        int amountLeft = suctionAmount;

        for(Item groundItem : groundItems) {
            int groundItemAmount = getItemAmount(groundItem);

            ItemStack suctionItem = groundItem.getItemStack().clone();
            suctionItem.setAmount(Math.min(groundItemAmount, suctionAmount));
            
            ItemType suctionItemType = suctionItem.getType().asItemType();
            if(suctionItemType == null) {
                logger.warn(AdventureUtil.serialize("Unable to suction an item due to a null ItemType. [Method: collect (SuctionTask)]"));
                continue;
            }

            switch (skyHopper.getFilterType()) {
                case NONE -> {
                    int result = addGroundItemToInventory(groundItem, groundItemAmount, suctionItem, hopper.getInventory(), skyHopper.getSuctionAmount());
                    amountLeft -= result;

                    if(result > 0) {
                        if(skyHopper.isParticlesEnabled()) {
                            // Highlight hopper that sucked up the item
                            hopper.getWorld().spawnParticle(Particle.DUST, hopper.getLocation().clone(), 5, 0.5, 0.5, 0.5, 0.0, new Particle.DustOptions(Color.YELLOW, 1));

                            // Highlight the item that was sucked up
                            groundItem.getWorld().spawnParticle(Particle.WITCH, hopper.getLocation().clone(), 3, 0.0, 0.0, 0.0, 0.0);
                        }
                    }

                    if(amountLeft == 0) return;
                }

                case BLACKLIST -> {
                    List<ItemType> filterItems = skyHopper.getFilterItems();
                    if (!filterItems.isEmpty() && !filterItems.contains(suctionItemType)) {
                        int result = addGroundItemToInventory(groundItem, groundItemAmount, suctionItem, hopper.getInventory(), skyHopper.getSuctionAmount());
                        amountLeft -= result;

                        if(result > 0) {
                            if(skyHopper.isParticlesEnabled()) {
                                // Highlight hopper that sucked up the item
                                hopper.getWorld().spawnParticle(Particle.DUST, hopper.getLocation().clone(), 5, 0.5, 0.5, 0.5, 0.0, new Particle.DustOptions(Color.YELLOW, 1));

                                // Highlight the item that was sucked up
                                groundItem.getWorld().spawnParticle(Particle.WITCH, hopper.getLocation().clone(), 3, 0.0, 0.0, 0.0, 0.0);
                            }
                        }

                        if(amountLeft == 0) return;
                    }
                }

                case DESTROY -> {
                    List<ItemType> filterItems = skyHopper.getFilterItems();
                    if (!filterItems.isEmpty() && filterItems.contains(suctionItemType)) {
                        removeAmountFromGroundItem(groundItem, groundItemAmount, suctionAmount);

                        if(skyHopper.isParticlesEnabled()) {
                            // Highlight hopper that sucked up the item
                            hopper.getWorld().spawnParticle(Particle.DUST, hopper.getLocation().clone(), 5, 0.5, 0.5, 0.5, 0.0, new Particle.DustOptions(Color.YELLOW, 1));

                            // Highlight the item that was sucked up
                            groundItem.getWorld().spawnParticle(Particle.WITCH, groundItem.getLocation().clone(), 3, 0.0, 0.0, 0.0, 0.0);
                        }

                        return;
                    }

                    int result = addGroundItemToInventory(groundItem, groundItemAmount, suctionItem, hopper.getInventory(), skyHopper.getSuctionAmount());
                    amountLeft -= result;

                    if(result > 0) {
                        if(skyHopper.isParticlesEnabled()) {
                            // Highlight hopper that sucked up the item
                            hopper.getWorld().spawnParticle(Particle.DUST, hopper.getLocation().clone(), 5, 0.5, 0.5, 0.5, 0.0, new Particle.DustOptions(Color.YELLOW, 1));

                            // Highlight the item that was sucked up
                            groundItem.getWorld().spawnParticle(Particle.WITCH, hopper.getLocation().clone(), 3, 0.0, 0.0, 0.0, 0.0);
                        }
                    }

                    if(amountLeft == 0) return;
                }

                case WHITELIST -> {
                    List<ItemType> filterItems = skyHopper.getFilterItems();
                    if (!filterItems.isEmpty() && filterItems.contains(suctionItemType)) {
                        int result = addGroundItemToInventory(groundItem, groundItemAmount, suctionItem, hopper.getInventory(), skyHopper.getSuctionAmount());
                        amountLeft -= result;

                        if(result > 0) {
                            if(skyHopper.isParticlesEnabled()) {
                                // Highlight hopper that sucked up the item
                                hopper.getWorld().spawnParticle(Particle.DUST, hopper.getLocation().clone(), 5, 0.5, 0.5, 0.5, 0.0, new Particle.DustOptions(Color.YELLOW, 1));

                                // Highlight the item that was sucked up
                                groundItem.getWorld().spawnParticle(Particle.WITCH, hopper.getLocation().clone(), 3, 0.0, 0.0, 0.0, 0.0);
                            }
                        }

                        if(amountLeft == 0) return;
                    }
                }
            }
        }
    }
}
