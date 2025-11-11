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
package com.github.lukesky19.skyHoppers.transfer.impl.furnace;

import com.github.lukesky19.skyHoppers.skyhopper.data.SkyContainer;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class handles transferring items from a furnace to an inventory.
 */
public class FurnaceToInventoryTransfer extends FurnaceTransferLogic {
    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public FurnaceToInventoryTransfer() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Transfer items from source inventory to the destination inventory until amount is exhausted or there are no more items to transfer.
     * @param sourceInventory The {@link FurnaceInventory} to transfer from.
     * @param destinationSkyHopper The {@link SkyHopper} being to transferred to. May be null.
     * @param destinationSkyContainer The {@link SkyContainer} being to transferred to. May be null.
     * @param destinationInventory The {@link Inventory} being transferred to.
     * @param amount The amount of items to transfer.
     * @return The amount of items transferred.
     */
    public static int transfer(
            @NotNull FurnaceInventory sourceInventory,
            @Nullable SkyHopper destinationSkyHopper,
            @Nullable SkyContainer destinationSkyContainer,
            @NotNull Inventory destinationInventory,
            int amount) {
        int amountTransferredOrDestroyed = 0;

        // Transfer Fuel (Empty Buckets)
        int fuelTransferredOrDestroyed = transferFromSlot(sourceInventory, FUEL_SLOT_NUMBER, destinationSkyHopper, destinationSkyContainer, destinationInventory, amount);

        amount -= fuelTransferredOrDestroyed;
        amountTransferredOrDestroyed += fuelTransferredOrDestroyed;
        if(amount <= 0) return amountTransferredOrDestroyed;

        // Transfer Output/Result
        int outputTransferredOrDestroyed = transferFromSlot(sourceInventory, OUTPUT_SLOT_NUMBER, destinationSkyHopper, destinationSkyContainer, destinationInventory, amount);

        amountTransferredOrDestroyed += outputTransferredOrDestroyed;

        return amountTransferredOrDestroyed;
    }

    /**
     * Transfer an item at a specific slot in the source inventory to the destination inventory.
     * @param sourceInventory The {@link FurnaceInventory} to transfer from.
     * @param sourceSlot The slot that contains the item to transfer.
     * @param destinationSkyHopper The {@link SkyHopper} being to transferred to. May be null.
     * @param destinationSkyContainer The {@link SkyContainer} being to transferred to. May be null.
     * @param destinationInventory The {@link Inventory} being transferred to.
     * @param amount The amount of items to transfer.
     * @return The amount of items transferred.
     */
    private static int transferFromSlot(
            @NotNull FurnaceInventory sourceInventory,
            int sourceSlot,
            @Nullable SkyHopper destinationSkyHopper,
            @Nullable SkyContainer destinationSkyContainer,
            @NotNull Inventory destinationInventory,
            int amount) {
        int amountTransferredOrDestroyed = 0;

        @Nullable ItemStack sourceStack = sourceInventory.getItem(sourceSlot);
        if(sourceStack == null || sourceStack.isEmpty()) return 0;
        @Nullable ItemType sourceType = sourceStack.getType().asItemType();
        if(sourceType == null) return 0;
        if(sourceSlot == FUEL_SLOT_NUMBER && !sourceType.equals(ItemType.BUCKET)) return 0;
        int sourceAmount = sourceStack.getAmount();

        // SkyHopper Filter Check
        @NotNull FilterResult skyHopperFilterResult = processFilter(sourceInventory, sourceStack, sourceType, sourceSlot, sourceAmount, destinationSkyHopper, amount);
        if(skyHopperFilterResult.destroyed() > 0) {
            amount -= skyHopperFilterResult.destroyed();
            sourceAmount -= skyHopperFilterResult.destroyed();
            amountTransferredOrDestroyed += skyHopperFilterResult.destroyed();
        }
        if(skyHopperFilterResult.shouldBreak()) return amountTransferredOrDestroyed;
        if(skyHopperFilterResult.shouldContinue()) return amountTransferredOrDestroyed;

        // SkyContainer Filter Check
        @NotNull FilterResult skyContainerFilterResult = processFilter(sourceInventory, sourceStack, sourceType, sourceSlot, sourceAmount, destinationSkyContainer, amount);
        if(skyHopperFilterResult.destroyed() > 0) {
            amount -= skyHopperFilterResult.destroyed();
            sourceAmount -= skyHopperFilterResult.destroyed();
            amountTransferredOrDestroyed += skyHopperFilterResult.destroyed();
        }
        if(skyContainerFilterResult.shouldBreak()) return amountTransferredOrDestroyed;
        if(skyContainerFilterResult.shouldContinue()) return amountTransferredOrDestroyed;
        
        for(int destinationSlot = 0; destinationSlot < destinationInventory.getSize(); destinationSlot++) {
            @Nullable ItemStack destinationStack = destinationInventory.getItem(destinationSlot);

            int minAmount = Math.min(sourceAmount, amount);
            int transferred;
            if(destinationStack != null && !destinationStack.isEmpty()) {
                if(!destinationStack.isSimilar(sourceStack)) continue;

                transferred = addToItemStack(sourceInventory, sourceStack, sourceSlot, destinationStack, minAmount);
            } else {
                transferred = setItem(sourceInventory, sourceStack, sourceSlot, destinationInventory, destinationSlot, minAmount);
            }

            amount -= transferred;
            sourceAmount -= transferred;
            amountTransferredOrDestroyed += transferred;
            
            if(amount <= 0) break;
            if(sourceAmount <= 0) break;
        }
        
        return amountTransferredOrDestroyed;
    }
}
