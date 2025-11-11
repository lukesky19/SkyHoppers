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

import com.github.lukesky19.skyHoppers.hook.impl.rosestacker.RoseStackerHook;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Contains to check if an Inventory is full or empty.
 */
public class InventoryUtils {
    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public InventoryUtils() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Takes an Item on the ground and adds it to an Inventory.
     * @param roseStackerHook A {@link RoseStackerHook} instance.
     * @param groundItem The Item entity on the ground.
     * @param groundItemAmount The amount of items on the ground.
     * @param transferItem The ItemStack to transfer.
     * @param destinationInventory The Inventory to transfer to.
     * @param amount The amount to transfer.
     * @return The amount transferred.
     * @deprecated Replaced by {@link com.github.lukesky19.skyHoppers.transfer.impl.entity.ItemEntityToInventoryTransfer}.
     */
    @Deprecated(since = "1.2.0.0", forRemoval = true)
    public static int addGroundItemToInventory(
            @NotNull RoseStackerHook roseStackerHook,
            @NotNull Item groundItem,
            int groundItemAmount,
            @NotNull ItemStack transferItem,
            @NotNull Inventory destinationInventory,
            int amount) {
        int transferItemMaxSize = transferItem.getMaxStackSize();

        int transferAmount = Math.min(amount, groundItemAmount);
        int amountTransferred = 0;

        int targetSize = destinationInventory.getSize() - 1;
        for(int i = 0; i <= targetSize; i++) {
            ItemStack itemStack = destinationInventory.getItem(i);

            if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                int itemStackMaxSize = itemStack.getMaxStackSize();

                if(itemStack.isSimilar(transferItem)) {
                    if(itemStack.getAmount() < itemStackMaxSize) {
                        final int result = itemStack.getAmount() + transferAmount;

                        if(result <= transferItemMaxSize) {
                            itemStack.setAmount(result);

                            int updatedAmount = groundItemAmount - transferAmount;

                            if(updatedAmount > 0) {
                                roseStackerHook.setItemAmount(groundItem, updatedAmount);
                            } else {
                                groundItem.remove();
                            }

                            amountTransferred += transferAmount;
                            amount -= transferAmount;
                            groundItemAmount -= transferAmount;
                        } else {
                            int leftover = result - itemStackMaxSize;
                            int transferred = transferAmount - leftover;
                            int updatedAmount = groundItemAmount - transferred;

                            amountTransferred += transferred;
                            amount -= transferred;
                            groundItemAmount -= transferred;

                            itemStack.setAmount(itemStackMaxSize);

                            roseStackerHook.setItemAmount(groundItem, updatedAmount);
                        }

                        if(groundItemAmount <= 0) return amountTransferred;
                        if(amount <= 0) return amountTransferred;
                    }
                }
            } else {
                ItemStack cloneItem = transferItem.clone();
                cloneItem.setAmount(transferAmount);

                destinationInventory.setItem(i, transferItem);

                int updatedAmount = groundItemAmount - transferAmount;

                amountTransferred += transferAmount;
                amount -= transferAmount;
                groundItemAmount -= transferAmount;

                roseStackerHook.setItemAmount(groundItem, updatedAmount);

                if(groundItemAmount <= 0) return amountTransferred;
                if(amount <= 0) return amountTransferred;
            }
        }

        return amountTransferred;
    }

    /**
     * Checks if an Inventory is full.
     * @param inventory The Inventory to check.
     * @return true if full, false if not.
     */
    public static boolean isInventoryFull(@NotNull Inventory inventory) {
        for(ItemStack item : inventory.getContents()) {
            if(item == null || item.getAmount() < item.getMaxStackSize()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if an Inventory is empty.
     * @param inventory The Inventory to check.
     * @return true if empty, false if not.
     */
    public static boolean isInventoryEmpty(@NotNull Inventory inventory) {
        for(ItemStack itemStack : inventory.getContents()) {
            if(itemStack != null && !itemStack.isEmpty()) {
                return false;
            }
        }

        return true;
    }
}