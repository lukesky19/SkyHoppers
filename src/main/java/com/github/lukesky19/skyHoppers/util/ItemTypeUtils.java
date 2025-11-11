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

import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * This class is used to check if an {@link ItemType} matches a specific category.
 */
public class ItemTypeUtils {
    private static final @NotNull Set<ItemType> BREWING_INGREDIENTS = Set.of(
            ItemType.BLAZE_POWDER,
            ItemType.NETHER_WART,
            ItemType.GLISTERING_MELON_SLICE,
            ItemType.GHAST_TEAR,
            ItemType.RABBIT_FOOT,
            ItemType.SPIDER_EYE,
            ItemType.SUGAR,
            ItemType.MAGMA_CREAM,
            ItemType.GLOWSTONE_DUST,
            ItemType.REDSTONE,
            ItemType.GUNPOWDER,
            ItemType.FERMENTED_SPIDER_EYE,
            ItemType.GOLDEN_CARROT,
            ItemType.PUFFERFISH,
            ItemType.PHANTOM_MEMBRANE,
            ItemType.TURTLE_HELMET);

    private static final @NotNull Set<ItemType> POTIONS = Set.of(
            ItemType.POTION,
            ItemType.SPLASH_POTION,
            ItemType.LINGERING_POTION);

    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public ItemTypeUtils() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Is the {@link ItemType} a brewing stand ingredient?
     * @param itemType The {@link ItemType} to check.
     * @return true if a brewing stand ingredient, or false.
     */
    public static boolean isBrewingIngredient(@NotNull ItemType itemType) {
        return BREWING_INGREDIENTS.contains(itemType);
    }

    /**
     * Is the {@link ItemType} a potion?
     * @param itemType The {@link ItemType} to check.
     * @return true if a potion, or false.
     */
    public static boolean isPotion(@NotNull ItemType itemType) {
        return POTIONS.contains(itemType);
    }
}
