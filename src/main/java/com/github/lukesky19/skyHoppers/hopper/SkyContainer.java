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
package com.github.lukesky19.skyHoppers.hopper;

import org.bukkit.Location;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the data for a SkyHopper's linked container.
 */
public class SkyContainer {
    private final @NotNull Location location;
    private @NotNull FilterType filterType;
    private final @NotNull List<ItemType> filterItems = new ArrayList<>();

    /**
     * Constructor
     * @param location The {@link Location} of the linked container.
     * @param filterType The {@link FilterType} for the linked container.
     */
    public SkyContainer(@NotNull Location location, @NotNull FilterType filterType) {
        this.location = location;
        this.filterType = filterType;
    }

    /**
     * Constructor
     * @param location The {@link Location} of the linked container.
     * @param filterType The {@link FilterType} for the linked container.
     * @param filterItems The {@link List} of {@link ItemType}s that are filtered.
     */
    public SkyContainer(@NotNull Location location, @NotNull FilterType filterType, @NotNull List<ItemType> filterItems) {
        this.location = location;
        this.filterType = filterType;
        this.filterItems.addAll(filterItems);
    }

    /**
     * Get the {@link Location} of the linked container.
     * @return A copy of the {@link Location} of the linked container.
     */
    public @NotNull Location getLocation() {
        return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Get the {@link FilterType} of the linked container.
     * @return The {@link FilterType} of the linked container.
     */
    public @NotNull FilterType getFilterType() {
        return filterType;
    }

    /**
     * Set the {@link FilterType} of the linked container.
     * @param filterType The {@link FilterType} to set.
     */
    public void setFilterType(@NotNull FilterType filterType) {
        this.filterType = filterType;
    }

    /**
     * Adds an {@link ItemType} to the filter items.
     * @param itemType The {@link ItemType} to add.
     */
    public void addFilterItem(@NotNull ItemType itemType) {
        if(!this.filterItems.contains(itemType)) {
            this.filterItems.add(itemType);
        }
    }

    /**
     * Removes an {@link ItemType} from the filter items.
     * @param itemType The {@link ItemType} to remove.
     */
    public void removeFilterItem(@NotNull ItemType itemType) {
        this.filterItems.remove(itemType);
    }

    /**
     * Get the {@link List} of {@link ItemType}s that are filtered.
     * @return A {@link List} of {@link ItemType}.
     */
    public @NotNull List<ItemType> getFilterItems() {
        return filterItems;
    }
}
