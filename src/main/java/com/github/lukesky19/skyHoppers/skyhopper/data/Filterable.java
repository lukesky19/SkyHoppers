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
package com.github.lukesky19.skyHoppers.skyhopper.data;

import com.google.common.collect.ImmutableList;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a {@link FilterType} and a list of {@link ItemType}s that are filtered.
 */
public abstract class Filterable {
    private @NotNull FilterType filterType;
    private final @NotNull List<ItemType> filterItems;

    /**
     * Default Constructor
     */
    public Filterable() {
        this.filterType = FilterType.NONE;
        filterItems = new ArrayList<>();
    }

    /**
     * Constructor
     * @param filterType A {@link FilterType}
     * @param filterItems A {@link List} of {@link ItemType}s that are filtered.
     */
    public Filterable(@NotNull FilterType filterType, @NotNull List<ItemType> filterItems) {
        this.filterType = filterType;
        this.filterItems = filterItems;
    }

    /**
     * Set the {@link FilterType}.
     * @param filterType The {@link FilterType} to set.
     */
    public void setFilterType(@NotNull FilterType filterType) {
        this.filterType = filterType;
    }

    /**
     * Get the current {@link FilterType}.
     * @return The current {@link FilterType}.
     */
    public @NotNull FilterType getFilterType() {
        return filterType;
    }

    /**
     * Add an {@link ItemType} to the filter items.
     * @param itemType The {@link ItemType} to add.
     */
    public void addFilterItem(@NotNull ItemType itemType) {
        if(!filterItems.contains(itemType)) {
            filterItems.add(itemType);
        }
    }

    /**
     * Remove an {@link ItemType} from the filter items.
     * @param itemType The {@link ItemType} to remove.
     */
    public void removeFilterItem(@NotNull ItemType itemType) {
        filterItems.remove(itemType);
    }

    /**
     * Get the {@link List} of {@link ItemType}s that are filtered.
     * @return An immutable copy of the list of filtered {@link ItemType}s.
     */
    public @NotNull List<ItemType> getFilterItems() {
        return ImmutableList.copyOf(filterItems);
    }

    /**
     * Is the {@link ItemType} allowed according to the {@link FilterType} and filtered items?
     * @apiNote {@link FilterType#DESTROY} returns as not allowed (false) for this method. See {@link #shouldItemTypeBeDestroyed(ItemType)}.
     * @param itemType The {@link ItemType} to check.
     * @return true if allowed, otherwise false.
     */
    public boolean isAllowed(@NotNull ItemType itemType) {
        return switch(filterType) {
            case WHITELIST -> filterItems.contains(itemType);

            case BLACKLIST -> !filterItems.contains(itemType);

            case DESTROY -> false;

            case NONE -> true;
        };
    }

    /**
     * Should the {@link ItemType} be destroyed according to the {@link FilterType} and filtered items?
     * @param itemType The {@link ItemType} to check.
     * @return true if it should be destroyed, otherwise false.
     */
    public boolean shouldItemTypeBeDestroyed(@NotNull ItemType itemType) {
        if(!filterType.equals(FilterType.DESTROY)) return false;
        return filterItems.contains(itemType);
    }

    /**
     * The options available for a {@link SkyHopper}'s or {@link SkyContainer}'s filter.
     */
    public enum FilterType {
        /**
         * The filter type to not filter anything.
         */
        NONE,
        /**
         * The filter type to only allow the {@link ItemType}s in the list of filtered items.
         */
        WHITELIST,
        /**
         * The filter type to not allow the {@link ItemType}s in the list of filtered items.
         */
        BLACKLIST,
        /**
         * The filter type to destroy any {@link ItemType}s in the list of filtered items and allow the rest.
         */
        DESTROY;

        /**
         * Gets the filter type for a given string or returns the default FilterType {@link FilterType#NONE}
         * @param string The name of the filter type.
         * @return The FilterType for the given string or the default FilterType {@link FilterType#NONE}
         */
        public static @NotNull FilterType getType(String string) {
            try {
                return FilterType.valueOf(string);
            } catch (IllegalArgumentException e) {
                return FilterType.NONE;
            }
        }
    }
}
