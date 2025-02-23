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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.block.Crafter;
import org.bukkit.entity.Item;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.github.lukesky19.skyHoppers.util.BrewingStandUtils.transferBrewingStandOutputToSkyHopper;
import static com.github.lukesky19.skyHoppers.util.BrewingStandUtils.transferItemToBrewingStand;
import static com.github.lukesky19.skyHoppers.util.CrafterUtils.transferItemToCrafter;
import static com.github.lukesky19.skyHoppers.util.FurnaceUtils.transferItemToFurnace;
import static com.github.lukesky19.skyHoppers.util.FurnaceUtils.transferFurnaceToSkyHopper;
import static com.github.lukesky19.skyHoppers.util.RoseStackerUtils.setItemAmount;

/**
 * Contains generic methods from transferring items from one Inventory to another and adding an item from the ground to an Inventory.
 */
public class InventoryUtils {
    /**
     * Transfers items from an Inventory to a SkyHopper.
     * @param skyHopper The SkyHopper to transfer to.
     * @param sourceInventory The Inventory to transfer from.
     * @param destinationInventory The SkyHopper's Inventory to transfer to.
     * @param amount The amount to transfer.
     * @return The amount transferred.
     */
    public static int transferInventoryToSkyHopper(SkyHopper skyHopper, Inventory sourceInventory, Inventory destinationInventory, int amount) {
        int amountTransferred = 0;

        for(int i = 0; i <= (sourceInventory.getSize() - 1); i++) {
            ItemStack sourceItem = sourceInventory.getItem(i);

            if(sourceItem != null && !sourceItem.isEmpty()) {
                switch(skyHopper.filterType()) {
                    case NONE -> {
                        int transferred = transferInventoryToInventory(sourceInventory, destinationInventory, i, amount);
                        amount -= transferred;
                        amountTransferred += transferred;

                        if(amount <= 0) return amountTransferred;
                    }

                    case WHITELIST -> {
                        if(skyHopper.filterItems().contains(sourceItem.getType())) {
                            int transferred = transferInventoryToInventory(sourceInventory, destinationInventory, i, amount);
                            amount -= transferred;
                            amountTransferred += transferred;

                            if(amount <= 0) return amountTransferred;
                        }
                    }

                    case BLACKLIST -> {
                        if(!skyHopper.filterItems().contains(sourceItem.getType())) {
                            int transferred = transferInventoryToInventory(sourceInventory, destinationInventory, i, amount);
                            amount -= transferred;
                            amountTransferred += transferred;

                            if(amount <= 0) return amountTransferred;
                        }
                    }

                    case DESTROY -> {
                        if(skyHopper.filterItems().contains(sourceItem.getType())) {
                            final int destroyResult = sourceItem.getAmount() - amount;
                            if(destroyResult <= 0) {
                                amount -= sourceItem.getAmount();

                                sourceInventory.setItem(i, new ItemStack(Material.AIR));
                            } else {
                                sourceItem.setAmount(destroyResult);
                            }

                            if(amount <= 0) return amountTransferred;
                        }

                        int transferred = transferInventoryToInventory(sourceInventory, destinationInventory, i, amount);
                        amount -= transferred;
                        amountTransferred += transferred;

                        if(amount <= 0) return amountTransferred;
                    }
                }
            }
        }

        return amountTransferred;
    }

    /**
     * Transfers items from one Inventory to another.
     * @param sourceInventory The Inventory to transfer from.
     * @param destinationInventory The Inventory to transfer to.
     * @param sourceSlot The slot containing the item to transfer.
     * @param amount The amount to transfer.
     * @return The amount transferred.
     */
    public static int transferInventoryToInventory(Inventory sourceInventory, Inventory destinationInventory, int sourceSlot, int amount) {
        int amountTransferred = 0;

        for(int i = 0; i <= (destinationInventory.getSize() - 1); i++) {
            ItemStack sourceItemStack = sourceInventory.getItem(sourceSlot);
            if(sourceItemStack == null || sourceItemStack.getType() == Material.AIR) return amountTransferred;
            int sourceAmount = sourceItemStack.getAmount();

            int amountToAdd = Math.min(sourceAmount, amount);
            ItemStack destItem = destinationInventory.getItem(i);

            if(destItem != null && !destItem.isEmpty()) {
                if(destItem.isSimilar(sourceItemStack)) {
                    int transferred = addToItem(sourceItemStack, destItem, sourceSlot, sourceInventory, amountToAdd);

                    amount -= transferred;
                    sourceAmount -= transferred;
                    amountTransferred += transferred;

                    if(sourceAmount <= 0) return amountTransferred;
                    if(amount <= 0) return amountTransferred;
                }
            } else {
                int transferred = setItem(sourceItemStack, sourceInventory, destinationInventory, sourceSlot, i, amountToAdd);

                amount -= transferred;
                sourceAmount -= transferred;
                amountTransferred += transferred;

                if(sourceAmount <= 0) return amountTransferred;
                if(amount <= 0) return amountTransferred;
            }
        }

        return amountTransferred;
    }

    /**
     * Transfers items from a Container to a SkyHopper. Runs different methods depending on the container.
     * @param plugin The SkyHopper Plugin.
     * @param skyHopper The SkyHopper to transfer to.
     * @param sourceContainer The container to transfer from.
     * @param sourceInventory The container's Inventory.
     * @param destinationInventory The SkyHopper's Inventory.
     * @param amount The amount to transfer.
     */
    public static void transferContainerToSkyHopper(SkyHoppers plugin, SkyHopper skyHopper, Container sourceContainer, Inventory sourceInventory, Inventory destinationInventory, int amount) {
        if(sourceInventory instanceof FurnaceInventory furnaceInventory) {
            transferFurnaceToSkyHopper(skyHopper, furnaceInventory, destinationInventory, amount);
        } else if(sourceInventory instanceof BrewerInventory brewerInventory) {
            transferBrewingStandOutputToSkyHopper(skyHopper, brewerInventory, destinationInventory, amount);
        } else {
            int result = transferInventoryToSkyHopper(skyHopper, sourceInventory, destinationInventory, amount);

            // If any items were transferred, update the QuickShop shop sign (if container is also a QuickShop shop)
            if(result > 0) {
                QuickShop quickShop = plugin.getQuickShop();
                if(quickShop != null) {
                    Location cloneSrc = sourceContainer.getLocation().clone();
                    cloneSrc.setX(cloneSrc.getBlockX());
                    cloneSrc.setY(cloneSrc.getBlockY());
                    cloneSrc.setZ(cloneSrc.getBlockZ());

                    Shop srcShop = quickShop.getShopManager().getShopIncludeAttached(cloneSrc);
                    if(srcShop != null) {
                        srcShop.setSignText(quickShop.text().findRelativeLanguages(srcShop.getOwner(), false));
                    }
                }
            }
        }
    }

    /**
     * Transfers items from an Inventory to a Container. Runs different methods depending on the container.
     * @param plugin The SkyHopper Plugin.
     * @param sourceInventory The Inventory to transfer from.
     * @param sourceContainer The Container to transfer from.
     * @param destinationContainer The Container to transfer to.
     * @param destinationInventory The Inventory to transfer to.
     * @param amount The amount to transfer.
     */
    public static void transferInventoryToContainer(SkyHoppers plugin, Inventory sourceInventory, Container sourceContainer, Container destinationContainer, Inventory destinationInventory, int amount) {
        if(destinationInventory instanceof FurnaceInventory furnaceInventory) {
            for(int i = 0; i <= (sourceInventory.getSize() - 1); i++) {
                ItemStack sourceItem = sourceInventory.getItem(i);

                if (sourceItem != null && !sourceItem.isEmpty()) {
                    amount -= transferItemToFurnace(sourceItem, sourceInventory, furnaceInventory, i, amount);

                    if(amount <= 0) return;
                }
            }
        } else if(destinationContainer instanceof Crafter crafter) {
            for(int i = 0; i <= (sourceInventory.getSize() - 1); i++) {
                ItemStack sourceItem = sourceInventory.getItem(i);

                if (sourceItem != null && !sourceItem.isEmpty()) {
                    amount -= transferItemToCrafter(sourceItem, sourceInventory, i, crafter, crafter.getInventory(), amount);

                    if(amount <= 0) return;
                }
            }
        } else if(destinationInventory instanceof BrewerInventory brewerInventory) {
            for(int i = 0; i <= (sourceInventory.getSize() - 1); i++) {
                ItemStack sourceItem = sourceInventory.getItem(i);

                if(sourceItem != null && !sourceItem.isEmpty()) {
                    amount -= transferItemToBrewingStand(sourceItem, sourceInventory, brewerInventory, i, amount);

                    if(amount <= 0) return;
                }
            }
        } else {
            for(int i = 0; i <= (sourceInventory.getSize() - 1); i++) {
                ItemStack sourceItem = sourceInventory.getItem(i);
                if (sourceItem != null && !sourceItem.isEmpty()) {
                    int result = transferInventoryToInventory(sourceInventory, destinationInventory, i, amount);
                    amount -= result;

                    // If any items were transferred, update the QuickShop shop sign (if container is also a QuickShop shop)
                    if(result > 0) {
                        QuickShop quickShop = plugin.getQuickShop();
                        if(quickShop != null) {
                            Location cloneDest = destinationContainer.getLocation();
                            cloneDest.setX(cloneDest.getBlockX());
                            cloneDest.setY(cloneDest.getBlockY());
                            cloneDest.setZ(cloneDest.getBlockZ());

                            Location cloneSrc = sourceContainer.getLocation().clone();
                            cloneSrc.setX(cloneSrc.getBlockX());
                            cloneSrc.setY(cloneSrc.getBlockY());
                            cloneSrc.setZ(cloneSrc.getBlockZ());

                            Shop destShop = quickShop.getShopManager().getShopIncludeAttached(cloneDest);
                            Shop srcShop = quickShop.getShopManager().getShopIncludeAttached(cloneSrc);
                            if(destShop != null) {
                                destShop.setSignText(quickShop.text().findRelativeLanguages(destShop.getOwner(), false));
                            }

                            if(srcShop != null) {
                                srcShop.setSignText(quickShop.text().findRelativeLanguages(srcShop.getOwner(), false));
                            }
                        }
                    }

                    if(amount <= 0) return;
                }
            }
        }
    }

    /**
     * Transfers an item from one Inventory to another. Runs different methods depending on the container.
     * @param plugin The SkyHopper Plugin.
     * @param item The item to transfer.
     * @param sourceInventory The Inventory containing the item to transfer.
     * @param sourceSlot The slot containing the item to transfer.
     * @param sourceContainer The Container to transfer from.
     * @param destinationContainer The Container to transfer to.
     * @param destinationInventory The Inventory to transfer to.
     * @param amount The amount to transfer.
     * @return The amount transferred.
     */
    public static int transferInventoryToContainer(SkyHoppers plugin, ItemStack item, Inventory sourceInventory, int sourceSlot, Container sourceContainer, Container destinationContainer, Inventory destinationInventory, int amount) {
        if(destinationInventory instanceof FurnaceInventory furnaceInventory) {
            return transferItemToFurnace(item, sourceInventory, furnaceInventory, sourceSlot, amount);
        } else if(destinationContainer instanceof Crafter crafter) {
            return transferItemToCrafter(item, sourceInventory, sourceSlot, crafter, crafter.getInventory(), amount);
        } else {
            int result = transferInventoryToInventory(sourceInventory, destinationInventory, sourceSlot, amount);

            // If any items were transferred, update the QuickShop shop sign (if container is also a QuickShop shop)
            if(result > 0) {
                QuickShop quickShop = plugin.getQuickShop();
                if(quickShop != null) {
                    Location cloneDest = destinationContainer.getLocation();
                    cloneDest.setX(cloneDest.getBlockX());
                    cloneDest.setY(cloneDest.getBlockY());
                    cloneDest.setZ(cloneDest.getBlockZ());

                    Location cloneSrc = sourceContainer.getLocation().clone();
                    cloneSrc.setX(cloneSrc.getBlockX());
                    cloneSrc.setY(cloneSrc.getBlockY());
                    cloneSrc.setZ(cloneSrc.getBlockZ());

                    Shop destShop = quickShop.getShopManager().getShopIncludeAttached(cloneDest);
                    Shop srcShop = quickShop.getShopManager().getShopIncludeAttached(cloneSrc);
                    if(destShop != null) {
                        destShop.setSignText(quickShop.text().findRelativeLanguages(destShop.getOwner(), false));
                    }

                    if(srcShop != null) {
                        srcShop.setSignText(quickShop.text().findRelativeLanguages(srcShop.getOwner(), false));
                    }
                }
            }

            return result;
        }
    }

    /**
     * Takes an Item on the ground and adds it to an Inventory.
     * @param groundItem The Item entity on the ground.
     * @param groundItemAmount The amount of items on the ground.
     * @param transferItem The ItemStack to transfer.
     * @param destinationInventory The Inventory to transfer to.
     * @param amount The amount to transfer.
     * @return The amount transferred.
     */
    public static int addGroundItemToInventory(Item groundItem, int groundItemAmount, ItemStack transferItem, Inventory destinationInventory, int amount) {
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
                                setItemAmount(groundItem, updatedAmount);
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

                            setItemAmount(groundItem, updatedAmount);
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

                setItemAmount(groundItem, updatedAmount);

                if(groundItemAmount <= 0) return amountTransferred;
                if(amount <= 0) return amountTransferred;
            }
        }

        return amountTransferred;
    }

    /**
     * Takes one ItemStack and attempts to add it to another.
     * @param sourceItem The Item to transfer from.
     * @param destItem The Item to transfer to.
     * @param sourceSlot The slot containing the item to transfer from.
     * @param sourceInventory The Inventory containing the item to transfer from.
     * @param amount The amount to transfer.
     * @return The amount that was transferred.
     */
    public static int addToItem(@NotNull ItemStack sourceItem, @NotNull ItemStack destItem, int sourceSlot, @NotNull Inventory sourceInventory, int amount) {
        int amountTransferred = 0;

        int maxSize = destItem.getMaxStackSize();

        if(destItem.getAmount() != maxSize) {
            final int result = destItem.getAmount() + amount;
            if(result <= maxSize) {
                destItem.setAmount(result);

                final int sourceResult = sourceItem.getAmount() - amount;
                if (sourceResult <= 0) {
                    sourceInventory.setItem(sourceSlot, new ItemStack(Material.AIR));
                } else {
                    sourceItem.setAmount(sourceResult);
                }

                amountTransferred += amount;
            } else {
                int leftover = result - maxSize;
                int transferred = amount - leftover;
                int sourceResult = sourceItem.getAmount() - transferred;

                destItem.setAmount(maxSize);

                if (sourceResult <= 0) {
                    sourceInventory.setItem(sourceSlot, new ItemStack(Material.AIR));
                } else {
                    sourceItem.setAmount(sourceResult);
                }

                amountTransferred += transferred;
            }
        }

        return amountTransferred;
    }

    /**
     * Takes an ItemStack and puts it at the provided slot.
     * @param sourceItem The ItemStack to transfer from.
     * @param sourceInventory The Inventory containing the ItemStack to transfer from.
     * @param destinationInventory The Inventory to place the ItemStack in.
     * @param sourceSlot The slot of the transfer item.
     * @param destSlot The slot to place the transfer item.
     * @param amount The amount to transfer.
     * @return The amount transferred.
     */
    public static int setItem(@NotNull ItemStack sourceItem, @NotNull Inventory sourceInventory, @NotNull Inventory destinationInventory, int sourceSlot, int destSlot, int amount) {
        int amountTransferred = 0;
        int maxSize = sourceItem.getMaxStackSize();
        ItemStack destItem = sourceItem.clone();

        if(amount <= maxSize) {
            destItem.setAmount(amount);

            destinationInventory.setItem(destSlot, destItem);

            final int sourceResult = sourceItem.getAmount() - amount;

            if (sourceResult <= 0) {
                sourceInventory.setItem(sourceSlot, new ItemStack(Material.AIR));
            } else {
                sourceItem.setAmount(sourceResult);
            }

            amountTransferred += amount;
        } else {
            destItem.setAmount(maxSize);

            destinationInventory.setItem(destSlot, destItem);

            final int sourceResult = sourceItem.getAmount() - maxSize;

            if (sourceResult <= 0) {
                sourceInventory.setItem(sourceSlot, new ItemStack(Material.AIR));
            } else {
                sourceItem.setAmount(sourceResult);
            }

            amountTransferred += maxSize;
        }

        return amountTransferred;
    }

    /**
     * Checks if an Inventory is full.
     * @param inventory The Inventory to check.
     * @return true if full, false if not.
     */
    public static boolean isInventoryFull(Inventory inventory) {
        for(ItemStack item : inventory.getContents()) {
            if(item == null || item.getAmount() < item.getMaxStackSize()) {
                return false;
            }
        }

        return true;
    }
}
