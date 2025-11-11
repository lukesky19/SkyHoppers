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
package com.github.lukesky19.skyHoppers.transfer.impl.brewing;

import com.github.lukesky19.skyHoppers.skyhopper.data.SkyContainer;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skyHoppers.util.ItemTypeUtils;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class handles transferring items from an inventory to a brewing stand.
 */
public class InventoryToBrewingStandTransfer extends BrewingTransferLogic  {
    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public InventoryToBrewingStandTransfer() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Transfer items from source inventory to the destination inventory until amount is exhausted or there are no more items to transfer.
     * @param sourceInventory The {@link Inventory} to transfer from.
     * @param destinationSkyHopper The {@link SkyHopper} being to transferred to. May be null.
     * @param destinationSkyContainer The {@link SkyContainer} being to transferred to. May be null.
     * @param destinationInventory The {@link BrewerInventory} being transferred to.
     * @param amount The amount of items to transfer.
     * @return The amount of items transferred.
     */
    public static int transfer(
            @NotNull Inventory sourceInventory,
            @Nullable SkyHopper destinationSkyHopper,
            @Nullable SkyContainer destinationSkyContainer,
            @NotNull BrewerInventory destinationInventory,
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

            if(itemType.equals(ItemType.BLAZE_POWDER)) {
                int fuelTransferred = transferToSlot(sourceInventory, sourceStack, sourceAmount, sourceSlot, destinationInventory, FUEL_SLOT_NUMBER, amount);

                amount -= fuelTransferred;
                sourceAmount -= fuelTransferred;
                amountTransferredOrDestroyed += fuelTransferred;

                if(amount <= 0) break;
                if(sourceAmount <= 0) continue;

                int ingredientTransferred = transferToSlot(sourceInventory, sourceStack, sourceAmount, sourceSlot, destinationInventory, INGREDIENT_SLOT_NUMBER, amount);

                amount -= ingredientTransferred;
                amountTransferredOrDestroyed += ingredientTransferred;

                if(amount <= 0) break;
            } else if(ItemTypeUtils.isBrewingIngredient(itemType)) {
                int transferred = transferToSlot(sourceInventory, sourceStack, sourceAmount, sourceSlot, destinationInventory, INGREDIENT_SLOT_NUMBER, amount);

                amount -= transferred;
                amountTransferredOrDestroyed += transferred;

                if(amount <= 0) break;
            } else if(ItemTypeUtils.isPotion(itemType)) {
                int transferred = transferToBottleSlots(sourceStack, sourceInventory, sourceSlot, sourceAmount, destinationInventory, amount);

                amount -= transferred;
                amountTransferredOrDestroyed += transferred;

                if(amount <= 0) break;
            }
        }

        return amountTransferredOrDestroyed;
    }

    private static int transferToSlot(
            @NotNull Inventory sourceInventory,
            @NotNull ItemStack sourceStack,
            int sourceAmount,
            int sourceSlot,
            @NotNull BrewerInventory destinationInventory,
            int destinationSlot,
            int amount) {
        int amountTransferred = 0;

        int minAmount = Math.min(sourceAmount, amount);

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

    private static int transferToBottleSlots(
            @NotNull ItemStack itemStack,
            @NotNull Inventory sourceInventory,
            int sourceSlot,
            int sourceAmount,
            @NotNull BrewerInventory destinationInventory,
            int amount) {
        int amountTransferred = 0;
        @Nullable ItemStack bottle1 = destinationInventory.getItem(BOTTLE_1_SLOT_NUMBER);
        @Nullable ItemStack bottle2 = destinationInventory.getItem(BOTTLE_2_SLOT_NUMBER);
        @Nullable ItemStack bottle3 = destinationInventory.getItem(BOTTLE_3_SLOT_NUMBER);

        int minAmount = Math.min(sourceAmount, amount);
        if(bottle1 != null && !bottle1.isEmpty()) {
            if(bottle1.isSimilar(itemStack)) {
                int transferred = addToItemStack(sourceInventory, itemStack, sourceSlot, bottle1, minAmount);

                amount -= transferred;
                sourceAmount -= transferred;
                amountTransferred += transferred;

                if(amount <= 0) return amountTransferred;
                if(sourceAmount <= 0) return amountTransferred;
            }
        } else {
            int transferred = setItem(sourceInventory, itemStack, sourceSlot, destinationInventory, 0, minAmount);

            amount -= transferred;
            sourceAmount -= transferred;
            amountTransferred += transferred;

            if(amount <= 0) return amountTransferred;
            if(sourceAmount <= 0) return amountTransferred;
        }

        minAmount = Math.min(sourceAmount, amount);

        if(bottle2 != null && !bottle2.isEmpty()) {
            if(bottle2.isSimilar(itemStack)) {
                int transferred = addToItemStack(sourceInventory, itemStack, sourceSlot, bottle2, minAmount);

                amount -= transferred;
                sourceAmount -= transferred;
                amountTransferred += transferred;

                if(amount <= 0) return amountTransferred;
                if(sourceAmount <= 0) return amountTransferred;
            }
        } else {
            int transferred = setItem(sourceInventory, itemStack, sourceSlot, destinationInventory, 1, minAmount);

            amount -= transferred;
            sourceAmount -= transferred;
            amountTransferred += transferred;

            if(amount <= 0) return amountTransferred;
            if(sourceAmount <= 0) return amountTransferred;
        }

        minAmount = Math.min(sourceAmount, amount);

        if(bottle3 != null && !bottle3.isEmpty()) {
            if(bottle3.isSimilar(itemStack)) {
                amountTransferred += addToItemStack(sourceInventory, itemStack, sourceSlot, bottle3, minAmount);
            }
        } else {
            amountTransferred += setItem(sourceInventory, itemStack, sourceSlot, destinationInventory, 2, minAmount);
        }

        return amountTransferred;
    }
}
