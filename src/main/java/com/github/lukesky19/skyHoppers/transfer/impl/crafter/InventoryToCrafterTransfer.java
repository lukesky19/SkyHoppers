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
package com.github.lukesky19.skyHoppers.transfer.impl.crafter;

import com.github.lukesky19.skyHoppers.skyhopper.data.SkyContainer;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skyHoppers.transfer.TransferLogic;
import org.bukkit.block.Crafter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class handles transferring items from an Inventory to a Crafter.
 */
public class InventoryToCrafterTransfer extends TransferLogic {
    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public InventoryToCrafterTransfer() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Transfer items from source inventory to the destination inventory until amount is exhausted or there are no more items to transfer.
     * @param sourceInventory The {@link Inventory} to transfer from.
     * @param destinationSkyHopper The {@link SkyHopper} being transferred to. May be null.
     * @param destinationSkyContainer The {@link SkyContainer} being transferred to. May be null.
     * @param crafter The {@link Crafter} being transferred to.
     * @param destinationInventory The {@link Inventory} being transferred to.
     * @param amount The amount of items to transfer.
     * @return The amount of items transferred.
     */
    public static int transfer(
            @NotNull Inventory sourceInventory,
            @Nullable SkyHopper destinationSkyHopper,
            @Nullable SkyContainer destinationSkyContainer,
            @NotNull Crafter crafter,
            @NotNull Inventory destinationInventory,
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

            while(sourceAmount > 0) {
                @NotNull SlotWithSmallestStack slotWithSmallestStack = getSlotWithSmallestStack(crafter, destinationInventory, sourceStack);

                int destinationSlot = slotWithSmallestStack.slot;
                if(destinationSlot == -1) return amountTransferredOrDestroyed;

                int transferred;
                @Nullable ItemStack destinationStack = slotWithSmallestStack.itemStack;
                if(destinationStack != null) {
                    transferred = addToItemStack(sourceInventory, sourceStack, sourceSlot, destinationStack, 1);
                } else {
                    transferred = setItem(sourceInventory, sourceStack, sourceSlot, destinationInventory, destinationSlot, 1);
                }

                amount -= transferred;
                sourceAmount -= transferred;
                amountTransferredOrDestroyed += transferred;

                if(amount <= 0) return amountTransferredOrDestroyed;
            }
        }

        return amountTransferredOrDestroyed;
    }

    private static @NotNull SlotWithSmallestStack getSlotWithSmallestStack(
            @NotNull Crafter crafter,
            @NotNull Inventory destinationInventory,
            @NotNull ItemStack compareStack) {
        int smallestAmount = Integer.MAX_VALUE;
        int smallestSlot = -1;
        ItemStack smallestStack = null;

        for(int slot = 0; slot < destinationInventory.getSize(); slot++) {
            if(crafter.isSlotDisabled(slot)) continue;

            @Nullable ItemStack crafterStack = destinationInventory.getItem(slot);
            if(crafterStack == null || crafterStack.isEmpty()) {
                return new SlotWithSmallestStack(slot, null);
            }

            if(!crafterStack.isSimilar(compareStack)) continue;
            int crafterStackAmount = crafterStack.getAmount();
            if(crafterStackAmount >= crafterStack.getMaxStackSize()) continue;

            if(crafterStackAmount < smallestAmount)  {
                smallestAmount = crafterStackAmount;
                smallestSlot = slot;
                smallestStack = crafterStack;
            }
        }

        return new SlotWithSmallestStack(smallestSlot, smallestStack);
    }

    /**
     * A record with the slot and ItemStack with the smallest amount of items.
     * @param slot The slot of the ItemStack
     * @param itemStack The ItemStack of the smallest amount. The ItemStack may be null if the slot is empty.
     */
    private record SlotWithSmallestStack(int slot, @Nullable ItemStack itemStack) {}
}
