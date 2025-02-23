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
package com.github.lukesky19.skyHoppers.util;

import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import org.bukkit.Material;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static com.github.lukesky19.skyHoppers.util.InventoryUtils.*;

/**
 * This class contains methods for transferring items to Brewing Stands.
 */
public class BrewingStandUtils {
    /**
     * Transfers an item to a brewing stand. Will run different transfer logic depending on the item's material.
     * @param sourceItem The item being transferred.
     * @param sourceInventory The inventory containing the item being transferred.
     * @param destinationInventory The brewing stand inventory to transfer the item to.
     * @param sourceSlot The slot the item being transferred is in.
     * @param amount The amount to transfer.
     * @return The amount transferred.
     */
    public static int transferItemToBrewingStand(ItemStack sourceItem, Inventory sourceInventory, BrewerInventory destinationInventory, int sourceSlot, int amount) {
        int amountTransferred = 0;
        int sourceAmount = sourceItem.getAmount();

        Material type = sourceItem.getType();
        if(type == Material.BLAZE_POWDER) {
            int fuelAmountToAdd = Math.min(sourceAmount, amount);

            int fuelTransferred = transferItemToBrewingStandFuel(sourceItem, sourceInventory, destinationInventory, sourceSlot, fuelAmountToAdd);
            amount -= fuelTransferred;
            sourceAmount -= fuelTransferred;
            amountTransferred += fuelTransferred;

            if(sourceAmount <= 0) return amountTransferred;
            if(amount <= 0) return amountTransferred;

            int ingredientAmountToAdd = Math.min(sourceAmount, amount);

            int ingredientTransferred = transferItemToBrewingStandIngredient(sourceItem, sourceInventory, destinationInventory, sourceSlot, ingredientAmountToAdd);
            amount -= ingredientTransferred;
            sourceAmount -= ingredientTransferred;
            amountTransferred += ingredientTransferred;

            if(sourceAmount <= 0) return amountTransferred;
            if(amount <= 0) return amountTransferred;
        } else if(isMaterialBrewingStandIngredient(type)) {
            int ingredientTransferred = transferItemToBrewingStandIngredient(sourceItem, sourceInventory, destinationInventory, sourceSlot, amount);
            amount -= ingredientTransferred;
            amountTransferred += ingredientTransferred;

            if(amount <= 0) return amountTransferred;
        } else {
            if(isMaterialPotion(type)) {
                int amountToAdd = Math.min(sourceItem.getAmount(), amount);

                int bottlesTransferred = transferItemToBrewingStandOutput(sourceItem, sourceInventory, destinationInventory, sourceSlot, amountToAdd);
                amount -= bottlesTransferred;
                amountTransferred += bottlesTransferred;

                if(amount <= 0) return amountTransferred;
            }
        }

        return amountTransferred;
    }

    /**
     * Transfers an item to a brewing stand's fuel slot.
     * @param sourceItem The item being transferred.
     * @param sourceInventory The inventory containing the item being transferred.
     * @param destinationInventory The brewing stand inventory to transfer the item to.
     * @param sourceSlot The slot the item being transferred is in.
     * @param amount The amount to transfer.
     * @return The amount transferred.
     */
    public static int transferItemToBrewingStandFuel(ItemStack sourceItem, Inventory sourceInventory, BrewerInventory destinationInventory, int sourceSlot, int amount) {
        int amountTransferred = 0;

        int amountToAdd = Math.min(sourceItem.getAmount(), amount);

        ItemStack fuel = destinationInventory.getFuel();
        if(fuel != null && !fuel.isEmpty()) {
            if(sourceItem.isSimilar(fuel)) {
                int transferred = addToItem(sourceItem, fuel, sourceSlot, sourceInventory, amountToAdd);

                amountTransferred += transferred;
            }
        } else {
            amountTransferred += setItem(sourceItem, sourceInventory, destinationInventory, sourceSlot, 4, amountToAdd);
        }

        return amountTransferred;
    }

    /**
     * Transfers an item to a brewing stand's ingredient slot.
     * @param sourceItem The item being transferred.
     * @param sourceInventory The inventory containing the item being transferred.
     * @param destinationInventory The brewing stand inventory to transfer the item to.
     * @param sourceSlot The slot the item being transferred is in.
     * @param amount The amount to transfer.
     * @return The amount transferred.
     */
    public static int transferItemToBrewingStandIngredient(ItemStack sourceItem, Inventory sourceInventory, BrewerInventory destinationInventory, int sourceSlot, int amount) {
        int amountTransferred = 0;

        int amountToAdd = Math.min(sourceItem.getAmount(), amount);

        ItemStack ingredient = destinationInventory.getIngredient();
        if(ingredient != null && !ingredient.isEmpty()) {
            if(sourceItem.isSimilar(ingredient)) {
                int transferred = addToItem(sourceItem, ingredient, sourceSlot, sourceInventory, amountToAdd);

                amountTransferred += transferred;
            }
        } else {
            amountTransferred += setItem(sourceItem, sourceInventory, destinationInventory, sourceSlot,3, amountToAdd);
        }

        return amountTransferred;
    }

    /**
     * Transfers an item to a brewing stand's bottles slot(s).
     * @param sourceItem The item being transferred.
     * @param sourceInventory The inventory containing the item being transferred.
     * @param destinationInventory The brewing stand inventory to transfer the item to.
     * @param sourceSlot The slot the item being transferred is in.
     * @param amount The amount to transfer.
     * @return The amount transferred.
     */
    public static int transferItemToBrewingStandOutput(ItemStack sourceItem, Inventory sourceInventory, BrewerInventory destinationInventory, int sourceSlot, int amount) {
        int amountTransferred = 0;
        ItemStack bottle1 = destinationInventory.getItem(0);
        ItemStack bottle2 = destinationInventory.getItem(1);
        ItemStack bottle3 = destinationInventory.getItem(2);

        if(bottle1 != null && !bottle1.isEmpty()) {
            if(sourceItem.isSimilar(bottle1)) {
                int amountToAdd = Math.min(1, bottle1.getMaxStackSize());

                int transferred = addToItem(sourceItem, bottle1, sourceSlot, sourceInventory, amountToAdd);

                amount -= transferred;
                amountTransferred += transferred;

                if(amount <= 0) return amountTransferred;
            }
        } else {
            int transferred = setItem(sourceItem, sourceInventory, destinationInventory, sourceSlot, 0, amount);

            amount -= transferred;
            amountTransferred += transferred;

            if(amount <= 0) return amountTransferred;
        }

        if(bottle2 != null && !bottle2.isEmpty()) {
            if(sourceItem.isSimilar(bottle2)) {
                int amountToAdd = Math.min(bottle2.getAmount(), amount);

                int transferred = addToItem(sourceItem, bottle2, sourceSlot, sourceInventory, amountToAdd);

                amount -= transferred;
                amountTransferred += transferred;

                if(amount <= 0) return amountTransferred;
            }
        } else {
            int transferred = setItem(sourceItem, sourceInventory, destinationInventory, sourceSlot, 1, amount);

            amount -= transferred;
            amountTransferred += transferred;

            if(amount <= 0) return amountTransferred;
        }

        if(bottle3 != null && !bottle3.isEmpty()) {
            if(sourceItem.isSimilar(bottle3)) {
                int amountToAdd = Math.min(bottle3.getAmount(), amount);

                int transferred = addToItem(sourceItem, bottle3, sourceSlot, sourceInventory, amountToAdd);

                amount -= transferred;
                amountTransferred += transferred;

                if(amount <= 0) return amountTransferred;
            }
        } else {
            int transferred = setItem(sourceItem, sourceInventory, destinationInventory, sourceSlot, 2, amount);

            amount -= transferred;
            amountTransferred += transferred;

            if(amount <= 0) return amountTransferred;
        }

        return amountTransferred;
    }

    /**
     * Transfers items from the brewing stand's output bottle slot(s) to a SkyHopper.
     * @param skyHopper The SkyHopper involved in the transfer.
     * @param sourceInventory The inventory containing the item being transferred.
     * @param destinationInventory The SkyHopper's Inventory to transfer to.
     * @param amount The amount to transfer.
     */
    public static void transferBrewingStandOutputToSkyHopper(SkyHopper skyHopper, BrewerInventory sourceInventory, Inventory destinationInventory, int amount) {
        ItemStack bottle1 = sourceInventory.getItem(0);
        ItemStack bottle2 = sourceInventory.getItem(1);
        ItemStack bottle3 = sourceInventory.getItem(2);

        if(bottle1 != null && !bottle1.isEmpty()) {
            switch(skyHopper.filterType()) {
                case NONE -> {
                    int transferred = transferInventoryToInventory(sourceInventory, destinationInventory, 0, amount);
                    amount -= transferred;

                    if(amount <= 0) return;
                }

                case WHITELIST -> {
                    if(skyHopper.filterItems().contains(bottle1.getType())) {
                        int transferred = transferInventoryToInventory(sourceInventory, destinationInventory,0, amount);
                        amount -= transferred;

                        if(amount <= 0) return;
                    }
                }

                case BLACKLIST -> {
                    if(!skyHopper.filterItems().contains(bottle1.getType())) {
                        int transferred = transferInventoryToInventory(sourceInventory, destinationInventory, 0, amount);
                        amount -= transferred;

                        if(amount <= 0) return;
                    }
                }

                case DESTROY -> {
                    if(skyHopper.filterItems().contains(bottle1.getType())) {
                        final int destroyResult = bottle1.getAmount() - amount;
                        if(destroyResult <= 0) {
                            amount -= bottle1.getAmount();

                            sourceInventory.setItem(2, new ItemStack(Material.AIR));
                        } else {
                            bottle1.setAmount(destroyResult);
                            amount -= amount;
                        }

                        if(amount <= 0) return;
                    }

                    int transferred = transferInventoryToInventory(sourceInventory, destinationInventory, 0, amount);
                    amount -= transferred;

                    if(amount <= 0) return;
                }
            }
        }

        if(bottle2 != null && !bottle2.isEmpty()) {
            switch(skyHopper.filterType()) {
                case NONE -> {
                    int transferred = transferInventoryToInventory(sourceInventory, destinationInventory, 1, amount);
                    amount -= transferred;

                    if(amount <= 0) return;
                }

                case WHITELIST -> {
                    if(skyHopper.filterItems().contains(bottle2.getType())) {
                        int transferred = transferInventoryToInventory(sourceInventory, destinationInventory, 1, amount);
                        amount -= transferred;

                        if(amount <= 0) return;
                    }
                }

                case BLACKLIST -> {
                    if(!skyHopper.filterItems().contains(bottle2.getType())) {
                        int transferred = transferInventoryToInventory(sourceInventory, destinationInventory, 1, amount);
                        amount -= transferred;

                        if(amount <= 0) return;
                    }
                }

                case DESTROY -> {
                    if(skyHopper.filterItems().contains(bottle2.getType())) {
                        final int destroyResult = bottle2.getAmount() - amount;
                        if(destroyResult <= 0) {
                            amount -= bottle2.getAmount();

                            sourceInventory.setItem(3, new ItemStack(Material.AIR));
                        } else {
                            bottle2.setAmount(destroyResult);
                            amount -= amount;
                        }

                        if(amount <= 0) return;
                    }

                    int transferred = transferInventoryToInventory(sourceInventory, destinationInventory, 1, amount);
                    amount -= transferred;

                    if(amount <= 0) return;
                }
            }
        }

        if(bottle3 != null && !bottle3.isEmpty()) {
            switch(skyHopper.filterType()) {
                case NONE -> transferInventoryToInventory(sourceInventory, destinationInventory, 2, amount);

                case WHITELIST -> {
                    if(skyHopper.filterItems().contains(bottle3.getType())) {
                        transferInventoryToInventory(sourceInventory, destinationInventory, 2, amount);
                    }
                }

                case BLACKLIST -> {
                    if(!skyHopper.filterItems().contains(bottle3.getType())) {
                        transferInventoryToInventory(sourceInventory, destinationInventory, 2, amount);
                    }
                }

                case DESTROY -> {
                    if(skyHopper.filterItems().contains(bottle3.getType())) {
                        final int destroyResult = bottle3.getAmount() - amount;
                        if(destroyResult <= 0) {
                            amount -= bottle3.getAmount();

                            sourceInventory.setItem(3, new ItemStack(Material.AIR));
                        } else {
                            bottle3.setAmount(destroyResult);
                            amount -= amount;
                        }
                    }

                    transferInventoryToInventory(sourceInventory, destinationInventory, 2, amount);
                }
            }
        }
    }

    /**
     * Checks if a Material is a potion ingredient.
     * @param material The Material to check.
     * @return true if the Material is an ingredient, false if not.
     */
    private static boolean isMaterialBrewingStandIngredient(Material material) {
        return material == Material.BLAZE_POWDER
                || material == Material.NETHER_WART
                || material == Material.GLISTERING_MELON_SLICE
                || material == Material.GHAST_TEAR
                || material == Material.RABBIT_FOOT
                || material == Material.SPIDER_EYE
                || material == Material.SUGAR
                || material == Material.MAGMA_CREAM
                || material == Material.GLOWSTONE_DUST
                || material == Material.REDSTONE
                || material == Material.GUNPOWDER
                || material == Material.FERMENTED_SPIDER_EYE
                || material == Material.GOLDEN_CARROT
                || material == Material.PUFFERFISH
                || material == Material.PHANTOM_MEMBRANE
                || material == Material.TURTLE_HELMET;
    }

    /**
     * Checks if a Material is a potion.
     * @param material The Material to check.
     * @return true if the Material is a potion, false if not.
     */
    private static boolean isMaterialPotion(Material material) {
        return material == Material.POTION
                || material == Material.SPLASH_POTION
                || material == Material.LINGERING_POTION;
    }
}
