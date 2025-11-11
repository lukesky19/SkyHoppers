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
package com.github.lukesky19.skyHoppers.transfer;

import com.github.lukesky19.skyHoppers.skyhopper.data.Filterable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class can be extended to implement transfer logic with helper methods to add to, remove from, and set ItemStacks.
 */
public abstract class TransferLogic {
    /**
     * Default Constructor.
     */
    public TransferLogic() {}

    /**
     * Attempts to remove the amount provided from the source ItemStack.
     * @param sourceInventory The Inventory containing the source ItemStack.
     * @param sourceItem The source ItemStack.
     * @param sourceSlot The slot the source ItemStack is in.
     * @param amount The amount to remove.
     * @return The amount of items removed.
     */
    protected static int removeFromItemStack(
            @NotNull Inventory sourceInventory,
            @NotNull ItemStack sourceItem,
            int sourceSlot,
            int amount) {
        int amountRemoved = 0;

        int itemAmount = sourceItem.getAmount();
        int amountToRemove = Math.min(itemAmount, amount);
        int newAmount = itemAmount - amountToRemove;

        amountRemoved += amountToRemove;

        if(newAmount <= 0) {
            sourceInventory.clear(sourceSlot);
        } else {
            sourceItem.setAmount(newAmount);
        }

        return amountRemoved;
    }

    /**
     * Attempts to add the amount provided to the target ItemStack from the source ItemStack.
     * @param sourceInventory The Inventory containing the item to transfer from.
     * @param sourceItem The ItemStack to transfer from.
     * @param sourceSlot The slot containing the ItemStack to transfer from.
     * @param targetItem The ItemStack to transfer to.
     * @param amount The amount to transfer.
     * @return The amount that was transferred.
     */
    protected static int addToItemStack(
            @NotNull Inventory sourceInventory,
            @NotNull ItemStack sourceItem,
            int sourceSlot,
            @NotNull ItemStack targetItem,
            int amount) {
        // If the source and target ItemStacks are not similar, return 0 (none transferred).
        if(!sourceItem.isSimilar(targetItem)) return 0;

        // Get the target ItemStack's max stack size
        int maxSize = targetItem.getMaxStackSize();
        // If the target ItemStack's amount is at its maximum, return 0 (none transferred).
        if(targetItem.getAmount() == maxSize) return 0;

        // Calculate the stack size of the target ItemStack when the transfer amount is added.
        int targetItemNewAmount = targetItem.getAmount() + amount;

        // Calculate the amount of items that can be added to the ItemStack.
        int transferredAmount;
        if(targetItemNewAmount > maxSize) {
            transferredAmount = maxSize - targetItem.getAmount();
        } else {
            transferredAmount = amount;
        }

        // Update the target ItemStack's amount.
        targetItem.setAmount(targetItem.getAmount() + transferredAmount);

        // Calculate the source ItemStack's new amount.
        int newSourceAmount = sourceItem.getAmount() - transferredAmount;
        // Modify or remove the source ItemStack based on the new amount.
        if(newSourceAmount <= 0) {
            // If the new amount is less than or equal to 0, remove the source ItemStack from the source Inventory
            sourceInventory.clear(sourceSlot);
        } else {
            // Otherwise update the source ItemStack's amount
            sourceItem.setAmount(newSourceAmount);
        }

        // Return the amount transferred.
        return transferredAmount;
    }

    /**
     * Takes the source ItemStack provided and places it at the target slot of the destination inventory provided.
     * @apiNote This will replace any existing items in that slot. Safety checks should be done before calling this method.
     * @param sourceInventory The Inventory containing the ItemStack to transfer from.
     * @param sourceItem The ItemStack to transfer from.
     * @param sourceSlot The slot of the transfer item.
     * @param destinationInventory The Inventory to place the ItemStack in.
     * @param targetSlot The slot to place the transfer item.
     * @param amount The amount to transfer.
     * @return The amount transferred.
     */
    protected static int setItem(
            @NotNull Inventory sourceInventory,
            @NotNull ItemStack sourceItem,
            int sourceSlot,
            @NotNull Inventory destinationInventory,
            int targetSlot,
            int amount) {
        // Get the source ItemStack's max stack size.
        int maxSize = sourceItem.getMaxStackSize();
        // Clone the source ItemStack to create the ItemStack that will be added to the destination Inventory.
        ItemStack targetItem = sourceItem.clone();

        // Calculate the amount to transfer
        int transferAmount = Math.min(amount, maxSize);
        targetItem.setAmount(transferAmount);

        // Place the target ItemStack inside the destination Inventory
        destinationInventory.setItem(targetSlot, targetItem);

        // Calculate the source ItemStack's new amount.
        int newSourceAmount = sourceItem.getAmount() - transferAmount;
        // Modify or remove the source ItemStack based on the new amount.
        if(newSourceAmount <= 0) {
            // If the new amount is less than or equal to 0, remove the source ItemStack from the source Inventory
            sourceInventory.clear(sourceSlot);
        } else {
            // Otherwise update the source ItemStack's amount
            sourceItem.setAmount(newSourceAmount);
        }

        // Return the amount transferred.
        return transferAmount;
    }

    /**
     * Process the filter.
     * @param sourceInventory The {@link Inventory} containing the source stack.
     * @param sourceStack The {@link ItemStack} to check against the filter.
     * @param sourceType The {@link ItemType} to check against the filter.
     * @param sourceSlot The slot the source stack is in.
     * @param sourceAmount The amount of items in the source stack.
     * @param filterable The {@link Filterable} to check with.
     * @param transferAmount The amount to transfer or destroy if applicable.
     * @return A {@link FilterResult}.
     */
    protected static @NotNull FilterResult processFilter(
            @NotNull Inventory sourceInventory,
            @NotNull ItemStack sourceStack,
            @NotNull ItemType sourceType,
            int sourceSlot,
            int sourceAmount,
            @Nullable Filterable filterable,
            int transferAmount) {
        if(filterable == null) return new FilterResult(0, false, false);

        if(!filterable.isAllowed(sourceType)) {
            if(filterable.shouldItemTypeBeDestroyed(sourceType)) {
                int amountDestroyed = removeFromItemStack(sourceInventory, sourceStack, sourceSlot, transferAmount);

                int remainingTransferAmount = transferAmount - amountDestroyed;
                int remainingSourceAmount = sourceAmount - amountDestroyed;

                boolean shouldBreak = remainingTransferAmount <= 0;
                boolean shouldContinue = remainingSourceAmount <= 0;

                return new FilterResult(amountDestroyed, shouldBreak, shouldContinue);
            } else {
                return new FilterResult(0, false, true);
            }
        }

        return new FilterResult(0, false, false);
    }

    /**
     * This record contains the result of checking against the filter.
     * @param destroyed The number of items destroyed.
     * @param shouldBreak If the loop should be broken out of.
     * @param shouldContinue If the loop should not be broken out of, but should continue to the next object.
     */
    public record FilterResult(int destroyed, boolean shouldBreak, boolean shouldContinue) {}
}
