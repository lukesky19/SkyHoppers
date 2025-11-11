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
 * This class handles transferring items from an inventory to a furnace.
 */
public class InventoryToFurnaceTransfer extends FurnaceTransferLogic {
    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public InventoryToFurnaceTransfer() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Transfer items from source inventory to the destination inventory until amount is exhausted or there are no more items to transfer.
     * @param sourceInventory The {@link Inventory} to transfer from.
     * @param destinationSkyHopper The {@link SkyHopper} being to transferred to. May be null.
     * @param destinationSkyContainer The {@link SkyContainer} being to transferred to. May be null.
     * @param destinationInventory The {@link FurnaceInventory} being transferred to.
     * @param amount The amount of items to transfer.
     * @return The amount of items transferred.
     */
    public static int transfer(
            @NotNull Inventory sourceInventory,
            @Nullable SkyHopper destinationSkyHopper,
            @Nullable SkyContainer destinationSkyContainer,
            @NotNull FurnaceInventory destinationInventory,
            int amount) {
        int amountTransferredOrDestroyed = 0;

        for(int sourceSlot = 0; sourceSlot < sourceInventory.getSize(); sourceSlot++) {
            @Nullable ItemStack sourceStack = sourceInventory.getItem(sourceSlot);
            if(sourceStack == null || sourceStack.isEmpty()) continue;
            @Nullable ItemType itemType = sourceStack.getType().asItemType();
            if(itemType == null) continue;

            int sourceAmount = sourceStack.getAmount();

            // SkyHopper Filter Check
            @NotNull FilterResult skyHopperFilterResult = processFilter(sourceInventory, sourceStack, itemType, sourceSlot, sourceAmount, destinationSkyHopper, amount);
            if(skyHopperFilterResult.destroyed() > 0) {
                amount -= skyHopperFilterResult.destroyed();
                sourceAmount -= skyHopperFilterResult.destroyed();
                amountTransferredOrDestroyed += skyHopperFilterResult.destroyed();
            }
            if(skyHopperFilterResult.shouldBreak()) break;
            if(skyHopperFilterResult.shouldContinue()) continue;

            // SkyContainer Filter Check
            @NotNull FilterResult skyContainerFilterResult = processFilter(sourceInventory, sourceStack, itemType, sourceSlot, sourceAmount, destinationSkyContainer, amount);
            if(skyHopperFilterResult.destroyed() > 0) {
                amount -= skyHopperFilterResult.destroyed();
                sourceAmount -= skyHopperFilterResult.destroyed();
                amountTransferredOrDestroyed += skyHopperFilterResult.destroyed();
            }
            if(skyContainerFilterResult.shouldBreak()) break;
            if(skyContainerFilterResult.shouldContinue()) continue;

            if(destinationInventory.isFuel(sourceStack)) {
                int fuelAmountTransferred = transferToSlot(sourceInventory, sourceStack, sourceSlot, destinationInventory, FUEL_SLOT_NUMBER, amount);

                amount -= fuelAmountTransferred;
                sourceAmount -= fuelAmountTransferred;
                amountTransferredOrDestroyed += fuelAmountTransferred;

                if(amount <= 0) break;
                if(sourceAmount <= 0) continue;

                int amountTransferred = transferToSlot(sourceInventory, sourceStack, sourceSlot, destinationInventory, INPUT_SLOT_NUMBER, amount);

                amount -= amountTransferred;
                amountTransferredOrDestroyed += amountTransferred;

                if(amount <= 0) break;
            } else {
                int amountTransferred = transferToSlot(sourceInventory, sourceStack, sourceSlot, destinationInventory, INPUT_SLOT_NUMBER, amount);

                amount -= amountTransferred;
                amountTransferredOrDestroyed += amountTransferred;

                if(amount <= 0) break;
            }
        }

        return amountTransferredOrDestroyed;
    }

    /**
     * Transfer an item in the source inventory to the destination inventory.
     * @param sourceInventory The {@link Inventory} to transfer from.
     * @param sourceStack The {@link ItemStack} in the source inventory to transfer.
     * @param sourceSlot The slot that contains the item to transfer.
     * @param destinationInventory The {@link FurnaceInventory} being transferred to.
     * @param destinationSlot The slot to transfer the item to.
     * @param amount The amount of items to transfer.
     * @return The amount of items transferred.
     */
    private static int transferToSlot(
            @NotNull Inventory sourceInventory,
            @NotNull ItemStack sourceStack,
            int sourceSlot,
            @NotNull FurnaceInventory destinationInventory,
            int destinationSlot,
            int amount) {
        int amountTransferred = 0;

        int minAmount = Math.min(sourceStack.getAmount(), amount);

        @Nullable ItemStack destinationStack = destinationInventory.getItem(destinationSlot);
        if(destinationStack != null && !destinationStack.isEmpty()) {
            if(destinationStack.isSimilar(sourceStack)) {
                amountTransferred += addToItemStack(sourceInventory, sourceStack, sourceSlot, destinationStack, minAmount);
            }
        } else {
            amountTransferred += setItem(sourceInventory, sourceStack, sourceSlot, destinationInventory, destinationSlot, minAmount);
        }

        return amountTransferred;
    }
}
