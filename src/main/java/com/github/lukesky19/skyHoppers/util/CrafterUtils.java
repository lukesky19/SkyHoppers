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

import org.bukkit.block.Crafter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import static com.github.lukesky19.skyHoppers.util.InventoryUtils.addToItem;
import static com.github.lukesky19.skyHoppers.util.InventoryUtils.setItem;

/**
 * This class contains methods for transferring items to Crafters.
 */
public class CrafterUtils {
    /**
     * Transfers an item to a Crafter.
     * @param sourceItem The item being transferred.
     * @param sourceInventory The Inventory containing the item being transferred.
     * @param sourceSlot The slot containing the item being transferred.
     * @param crafter The crafter the item is being transferred to.
     * @param destinationInventory The crafter's inventory the item is being transferred to.
     * @param amount The amount to transfer.
     * @return The amount transferred.
     */
    public static int transferItemToCrafter(ItemStack sourceItem, Inventory sourceInventory, int sourceSlot, Crafter crafter, Inventory destinationInventory, int amount) {
        int amountTransferred = 0;
        int itemAmount = sourceItem.getAmount();

        while(itemAmount > 0 && amount > 0) {
            SmallestSlotItemStack smallestSlotItemStack = getSmallestSlotItemStack(crafter, destinationInventory, sourceItem);
            int slot = smallestSlotItemStack.slot;
            ItemStack transferItem = smallestSlotItemStack.itemStack;

            if(slot == -1) return amountTransferred;

            int transferred;
            if(transferItem != null) {
                transferred = addToItem(sourceItem, transferItem, sourceSlot, sourceInventory, 1);
            } else {
                transferred = setItem(sourceItem, sourceInventory, destinationInventory, sourceSlot, slot, 1);
            }

            amount -= transferred;
            itemAmount -= transferred;
            amountTransferred += transferred;
        }

        return amountTransferred;
    }

    /**
     * Get the ItemStack and slot with the smallest amount of items inside the Crafter.
     * @param crafter The crafter to check.
     * @param inventory The crafter's inventory.
     * @param compareItem The ItemStack to find the smallest similar ItemStack and slot for.
     * @return A SmallestSlotItemStack record containing the slot and ItemStack that contains the smallest amount of items. The ItemStack may be null if the slot is empty.
     */
    private static SmallestSlotItemStack getSmallestSlotItemStack(Crafter crafter, Inventory inventory, ItemStack compareItem) {
        int smallestAmount = Integer.MAX_VALUE;
        int smallestSlot = -1;
        ItemStack smallestItem = null;

        for(int slot = 0; slot < inventory.getSize(); slot++) {
            if(!crafter.isSlotDisabled(slot)) {
                ItemStack item = inventory.getItem(slot);

                if(item != null && !item.isEmpty()) {
                    if(item.isSimilar(compareItem) && item.getAmount() < item.getMaxStackSize()) {
                        if(item.getAmount() < smallestAmount) {
                            smallestAmount = item.getAmount();
                            smallestSlot = slot;
                            smallestItem = item;
                        }
                    }
                } else {
                    smallestAmount = 0;
                    smallestSlot = slot;
                    smallestItem = null;
                }
            }
        }

        return new SmallestSlotItemStack(smallestSlot, smallestItem);
    }

    /**
     * A record with the slot and ItemStack with the smallest amount of items.
     * @param slot The slot of the ItemStack
     * @param itemStack The ItemStack of the smallest amount. The ItemStack may be null if the slot is empty.
     */
    private record SmallestSlotItemStack(int slot, @Nullable ItemStack itemStack) {}
}
