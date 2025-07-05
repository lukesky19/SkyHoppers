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
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import static com.github.lukesky19.skyHoppers.util.InventoryUtils.addToItem;
import static com.github.lukesky19.skyHoppers.util.InventoryUtils.setItem;

/**
 * This class contains methods for transferring items to Furnaces.
 */
public class FurnaceUtils {
    /**
     * Transfers an item to a Furnace. Will run different transfer logic depending on the item's material.
     * @param sourceItem The item being transferred.
     * @param sourceInventory The Inventory containing the item being transferred.
     * @param destinationInventory The Inventory to transfer the item to.
     * @param sourceSlot The slot containing the transfer item.
     * @param amount The amount to transfer.
     * @return The amount transferred.
     */
    public static int transferItemToFurnace(ItemStack sourceItem, Inventory sourceInventory, FurnaceInventory destinationInventory, int sourceSlot, int amount) {
        int amountTransferred = 0;
        int itemAmount = sourceItem.getAmount();

        if(destinationInventory.isFuel(sourceItem)) {
            ItemStack furnaceFuel = destinationInventory.getFuel();

            int fuelAmountToAdd = Math.min(itemAmount, amount);

            if(furnaceFuel != null && !furnaceFuel.isEmpty()) {
                if(furnaceFuel.isSimilar(sourceItem)) {
                    int transferred = addToItem(sourceItem, furnaceFuel, sourceSlot, sourceInventory, fuelAmountToAdd);

                    amount -= transferred;
                    itemAmount -= transferred;
                    amountTransferred += transferred;
                }
            } else {
                int transferred = setItem(sourceItem, sourceInventory, destinationInventory, sourceSlot, 1, fuelAmountToAdd);

                amount -= transferred;
                itemAmount -= transferred;
                amountTransferred += transferred;
            }

            if(itemAmount <= 0) return amountTransferred;
            if(amount <= 0) return amountTransferred;

            if(destinationInventory.canSmelt(sourceItem)) {
                // Some items can also be smeltable so if the fuel is full, move the rest to the smelt item slot.
                ItemStack smeltItem = destinationInventory.getSmelting();
                int smeltAmountToAdd = Math.min(itemAmount, amount);

                if (smeltItem != null && !smeltItem.isEmpty()) {
                    if(smeltItem.isSimilar(sourceItem)) {
                        int transferred = addToItem(sourceItem, smeltItem, sourceSlot, sourceInventory, smeltAmountToAdd);

                        amount -= transferred;
                        itemAmount -= transferred;
                        amountTransferred += transferred;
                    }
                } else {
                    int transferred = setItem(sourceItem, sourceInventory, destinationInventory, sourceSlot, 0, smeltAmountToAdd);

                    amount -= transferred;
                    itemAmount -= transferred;
                    amountTransferred += transferred;
                }

                if (itemAmount <= 0) return amountTransferred;
                if (amount <= 0) return amountTransferred;
            }
        } else {
            if(destinationInventory.canSmelt(sourceItem)) {
                ItemStack smeltItem = destinationInventory.getSmelting();

                int amountToAdd = Math.min(itemAmount, amount);

                if (smeltItem != null && !smeltItem.isEmpty()) {
                    if (smeltItem.isSimilar(sourceItem)) {
                        int transferred = addToItem(sourceItem, smeltItem, sourceSlot, sourceInventory, amountToAdd);

                        amount -= transferred;
                        itemAmount -= transferred;
                        amountTransferred += transferred;
                    }
                } else {
                    int transferred = setItem(sourceItem, sourceInventory, destinationInventory, sourceSlot, 0, amountToAdd);

                    amount -= transferred;
                    itemAmount -= transferred;
                    amountTransferred += transferred;
                }

                if (itemAmount <= 0) return amountTransferred;
                if (amount <= 0) return amountTransferred;
            }
        }

        return amountTransferred;
    }

    /**
     * Transfers an {@link ItemStack} from a Furnace's output to a {@link SkyHopper}.
     * @param logger The {@link ComponentLogger} of the plugin.
     * @param skyHopper The {@link SkyHopper} to transfer to.
     * @param sourceInventory The {@link FurnaceInventory} to transfer from.
     * @param destinationInventory The Inventory of the {@link SkyHopper} to transfer to.
     * @param amount The amount to transfer.
     */
    public static void transferFurnaceToSkyHopper(@NotNull ComponentLogger logger, @NotNull SkyHopper skyHopper, @NotNull FurnaceInventory sourceInventory, @NotNull Inventory destinationInventory, int amount) {
        ItemStack fuel = sourceInventory.getFuel();
        if(fuel != null && !fuel.isEmpty() && fuel.getType().equals(Material.BUCKET)) {
            ItemType fuelType = fuel.getType().asItemType();
            if(fuelType == null) {
                logger.warn(AdventureUtil.serialize("Unable to transfer fuel to a SkyHopper as the ItemType is null. [Method: transferFurnaceToSkyHopper]"));
                return;
            }

            int fuelAmount = fuel.getAmount();
            int amountToAdd = Math.min(fuelAmount, amount);

            switch(skyHopper.getFilterType()) {
                case NONE -> {
                    amount -= transferFurnaceFuelToSkyHopper(fuel, sourceInventory, destinationInventory, amountToAdd);

                    if(amount <= 0) return;
                }

                case WHITELIST -> {
                    if(skyHopper.getFilterItems().contains(fuelType)) {
                        amount -= transferFurnaceFuelToSkyHopper(fuel, sourceInventory, destinationInventory, amountToAdd);

                        if(amount <= 0) return;
                    }
                }

                case BLACKLIST -> {
                    if(!skyHopper.getFilterItems().contains(fuelType)) {
                        amount -= transferFurnaceFuelToSkyHopper(fuel, sourceInventory, destinationInventory, amountToAdd);

                        if(amount <= 0) return;
                    }
                }

                case DESTROY -> {
                    if(skyHopper.getFilterItems().contains(fuelType)) {
                        final int fuelResult = fuelAmount - amountToAdd;
                        if (fuelResult <= 0) {
                            amount -= fuelAmount;

                            sourceInventory.setFuel(new ItemStack(Material.AIR));
                        } else {
                            fuel.setAmount(fuelResult);
                        }

                        if(amount <= 0) return;
                    }

                    amount -= transferFurnaceFuelToSkyHopper(fuel, sourceInventory, destinationInventory, amountToAdd);

                    if(amount <= 0) return;
                }
            }
        }

        ItemStack output = sourceInventory.getResult();
        if(output != null && !output.isEmpty()) {
            ItemType outputType = output.getType().asItemType();
            if(outputType == null) {
                logger.warn(AdventureUtil.serialize("Unable to transfer the furnace's output to a SkyHopper as the ItemType is null. [Method: transferFurnaceToSkyHopper]"));
                return;
            }

            int outputAmount = output.getAmount();
            int amountToAdd = Math.min(outputAmount, amount);

            switch(skyHopper.getFilterType()) {
                case NONE -> transferFurnaceResultToSkyHopper(output, sourceInventory, destinationInventory, amountToAdd);

                case WHITELIST -> {
                    if(skyHopper.getFilterItems().contains(outputType)) {
                        transferFurnaceResultToSkyHopper(output, sourceInventory, destinationInventory, amountToAdd);
                    }
                }

                case BLACKLIST -> {
                    if(!skyHopper.getFilterItems().contains(outputType)) {
                        transferFurnaceResultToSkyHopper(output, sourceInventory, destinationInventory, amountToAdd);
                    }
                }

                case DESTROY -> {
                    if(skyHopper.getFilterItems().contains(outputType)) {
                        final int outputResult = outputAmount - amountToAdd;
                        if (outputResult <= 0) {
                            amount -= outputAmount;

                            sourceInventory.setResult(new ItemStack(Material.AIR));
                        } else {
                            output.setAmount(outputResult);
                        }

                        if(amount <= 0) return;
                    }

                    transferFurnaceResultToSkyHopper(output, sourceInventory, destinationInventory, amountToAdd);
                }
            }
        }
    }

    /**
     * Transfers a Furnace's fuel to a SkyHopper if it is an empty bucket.
     * @param fuel The Furnace's Fuel ItemStack.
     * @param sourceInventory The Furnace's Inventory.
     * @param destinationInventory The SkyHopper's Inventory.
     * @param amount The amount to transfer.
     * @return The amount transferred.
     */
    private static int transferFurnaceFuelToSkyHopper(ItemStack fuel, FurnaceInventory sourceInventory, Inventory destinationInventory, int amount) {
        int amountTransferred = 0;
        int fuelAmount = fuel.getAmount();

        for(int i = 0; i <= (destinationInventory.getSize() - 1); i++) {
            ItemStack destItem = destinationInventory.getItem(i);

            if(destItem != null && !destItem.isEmpty()) {
                if(destItem.isSimilar(fuel)) {
                    int transferred = addToItem(fuel, destItem, 1, sourceInventory, amount);

                    fuelAmount -= transferred;
                    amount -= transferred;
                    amountTransferred += transferred;

                    if(fuelAmount <= 0) return amountTransferred;
                    if(amount <= 0) return amountTransferred;
                }
            } else {
                int transferred = setItem(fuel, sourceInventory, destinationInventory, 1, i, amount);

                fuelAmount -= transferred;
                amount -= transferred;
                amountTransferred += transferred;

                if(fuelAmount <= 0) return amountTransferred;
                if(amount <= 0) return amountTransferred;
            }
        }

        return amountTransferred;
    }

    /**
     * Transfers a Furnace's output to a SkyHopper.
     * @param output The Furnace's Output ItemStack
     * @param sourceInventory The Furnace's Inventory.
     * @param destinationInventory The SkyHopper's Inventory.
     * @param amount The amount to transfer.
     */
    private static void transferFurnaceResultToSkyHopper(ItemStack output, FurnaceInventory sourceInventory, Inventory destinationInventory, int amount) {
        int outputAmount = output.getAmount();

        for(int i = 0; i <= (destinationInventory.getSize() - 1); i++) {
            ItemStack destItem = destinationInventory.getItem(i);

            if(destItem != null && !destItem.isEmpty()) {
                if(destItem.isSimilar(output)) {
                    int amountToAdd = Math.min(destItem.getAmount(), amount);

                    int transferred = addToItem(output, destItem, 2, sourceInventory, amountToAdd);

                    outputAmount -= transferred;
                    amount -= transferred;

                    if(outputAmount <= 0) return;
                    if(amount <= 0) return;
                }
            } else {
                int transferred = setItem(output, sourceInventory, destinationInventory, 2, i, amount);

                outputAmount -= transferred;
                amount -= transferred;

                if(outputAmount <= 0) return;
                if(amount <= 0) return;
            }
        }
    }
}
