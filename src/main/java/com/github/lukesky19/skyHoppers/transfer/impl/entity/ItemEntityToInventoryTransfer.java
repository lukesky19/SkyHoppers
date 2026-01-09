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
package com.github.lukesky19.skyHoppers.transfer.impl.entity;

import com.github.lukesky19.skyHoppers.hook.impl.rosestacker.RoseStackerHook;
import com.github.lukesky19.skyHoppers.skyhopper.data.Filterable;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skyHoppers.transfer.TransferLogic;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class handles transferring items from an {@link Item} entity to an inventory.
 */
public class ItemEntityToInventoryTransfer extends TransferLogic {
    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public ItemEntityToInventoryTransfer() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Transfer items from an {@link Item} entity to the destination inventory until amount is exhausted or there are no more items to transfer.
     * @param roseStackerHook A {@link RoseStackerHook} instance.
     * @param groundItem The {@link Item} entity to transfer.
     * @param groundStack The {@link ItemStack} of the {@link Item} entity.
     * @param groundItemType The {@link ItemType} of the ground stack.
     * @param groundItemAmount The amount of items stored in the item entity.
     * @param destinationSkyHopper The {@link SkyHopper} being transferred to.
     * @param destinationInventory The {@link Inventory} being transferred to.
     * @param amount The amount of items to transfer.
     * @return The amount of items transferred.
     */
    public static int transfer(
            @NotNull RoseStackerHook roseStackerHook,
            @NotNull Item groundItem,
            @NotNull ItemStack groundStack,
            @NotNull ItemType groundItemType,
            int groundItemAmount,
            @NotNull SkyHopper destinationSkyHopper,
            @NotNull Inventory destinationInventory,
            int amount) {
        int amountTransferredOrDestroyed = 0;
        int amountToTransfer = Math.min(groundItemAmount, amount);
        amountToTransfer = Math.min(groundStack.getMaxStackSize(), amountToTransfer);

        // SkyHopper Filter Check
        @NotNull FilterResult skyHopperFilterResult = processFilter(roseStackerHook, groundItem, groundItemType, groundItemAmount, destinationSkyHopper, amountToTransfer);
        if(skyHopperFilterResult.destroyed() > 0) {
            amount -= skyHopperFilterResult.destroyed();
            groundItemAmount -= skyHopperFilterResult.destroyed();
            amountTransferredOrDestroyed += skyHopperFilterResult.destroyed();
        }
        if(skyHopperFilterResult.shouldBreak()) return amountTransferredOrDestroyed;

        int groundStackMaxSize = groundStack.getMaxStackSize();

        for(int destinationSlot = 0; destinationSlot < destinationInventory.getSize(); destinationSlot++) {
            @Nullable ItemStack destinationStack = destinationInventory.getItem(destinationSlot);
            amountToTransfer = Math.min(groundItemAmount, amount);
            amountToTransfer = Math.min(groundStack.getMaxStackSize(), amountToTransfer);

            if(destinationStack != null && !destinationStack.isEmpty()) {
                if(!destinationStack.isSimilar(groundStack)) continue;
                int destinationStackMaxSize = destinationStack.getMaxStackSize();
                if(destinationStack.getAmount() >= destinationStackMaxSize) continue;

                final int result = destinationStack.getAmount() + amountToTransfer;

                if(result <= groundStackMaxSize) {
                    destinationStack.setAmount(result);

                    int updatedAmount = groundItemAmount - amountToTransfer;

                    if(updatedAmount > 0) {
                        roseStackerHook.setItemAmount(groundItem, updatedAmount);
                    } else {
                        groundItem.remove();
                    }

                    amountTransferredOrDestroyed += amountToTransfer;
                    groundItemAmount -= amountToTransfer;
                    amount -= amountToTransfer;
                } else {
                    int leftover = result - destinationStackMaxSize;
                    int transferred = amountToTransfer - leftover;
                    int updatedAmount = groundItemAmount - transferred;

                    amountTransferredOrDestroyed += transferred;
                    amount -= transferred;
                    groundItemAmount -= transferred;

                    destinationStack.setAmount(destinationStackMaxSize);

                    roseStackerHook.setItemAmount(groundItem, updatedAmount);
                }
            } else {
                ItemStack cloneItem = groundStack.clone();
                cloneItem.setAmount(amountToTransfer);

                destinationInventory.setItem(destinationSlot, cloneItem);

                int updatedAmount = groundItemAmount - amountToTransfer;

                amountTransferredOrDestroyed += amountToTransfer;
                groundItemAmount -= amountToTransfer;
                amount -= amountToTransfer;

                roseStackerHook.setItemAmount(groundItem, updatedAmount);
            }

            if(groundItemAmount <= 0) break;
            if(amount <= 0) break;
        }

        return amountTransferredOrDestroyed;
    }

    /**
     * Process the filter.
     * @param roseStackerHook A {@link RoseStackerHook} instance.
     * @param groundItem The ground {@link Item}.
     * @param groundType The {@link ItemType} of the ground item.
     * @param groundItemAmount The ground item's amount
     * @param filterable The {@link Filterable} to check with.
     * @param transferAmount The amount to transfer or destroy if applicable.
     * @return A {@link FilterResult}.
     */
    protected static @NotNull FilterResult processFilter(
            @NotNull RoseStackerHook roseStackerHook,
            @NotNull Item groundItem,
            @NotNull ItemType groundType,
            int groundItemAmount,
            @Nullable Filterable filterable,
            int transferAmount) {
        if(filterable == null) return new FilterResult(0, false, false);

        if(!filterable.isAllowed(groundType)) {
            if(filterable.shouldItemTypeBeDestroyed(groundType)) {
                int minAmount = Math.min(groundItemAmount, transferAmount);
                int amountToDestroy = groundItemAmount - minAmount;
                groundItemAmount -= amountToDestroy;
                transferAmount -= amountToDestroy;

                roseStackerHook.setItemAmount(groundItem, groundItemAmount);

                boolean shouldBreak = transferAmount <= 0;

                return new FilterResult(amountToDestroy, shouldBreak, false);
            } else {
                return new FilterResult(0, true, false);
            }
        }

        return new FilterResult(0, false, false);
    }
}
