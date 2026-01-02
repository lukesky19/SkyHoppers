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

import com.github.lukesky19.skyHoppers.util.ImmutableLocation;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Contains the data for a SkyHopper's linked container.
 */
public class SkyContainer extends Filterable {
    private final @NotNull ImmutableLocation location;
    private int priority = 1;

    /**
     * Constructor
     * @param location The {@link ImmutableLocation} of the linked container.
     * @param filterType The {@link FilterType} for the linked container.
     */
    public SkyContainer(@NotNull ImmutableLocation location, @NotNull FilterType filterType) {
        super(filterType, new ArrayList<>());
        this.location = location;
    }

    /**
     * Constructor
     * @param location The {@link ImmutableLocation} of the linked container.
     * @param filterType The {@link FilterType} for the linked container.
     * @param filterItems The {@link Set} of {@link ItemType}s that are filtered.
     */
    public SkyContainer(@NotNull ImmutableLocation location, @NotNull FilterType filterType, @NotNull List<ItemType> filterItems) {
        super(filterType, filterItems);
        this.location = location;
    }

    /**
     * Constructor
     * @param location The {@link ImmutableLocation} of the linked container.
     * @param filterType The {@link FilterType} for the linked container.
     * @param filterItems The {@link Set} of {@link ItemType}s that are filtered.
     * @param priority The {@link SkyContainer}'s priority.
     */
    public SkyContainer(@NotNull ImmutableLocation location, @NotNull FilterType filterType, @NotNull List<ItemType> filterItems, int priority) {
        super(filterType, filterItems);
        this.location = location;
        this.priority = priority;
    }

    /**
     * Get the {@link ImmutableLocation} of the linked container.
     * @return The {@link ImmutableLocation} of the linked container.
     */
    public @NotNull ImmutableLocation getLocation() {
        return location;
    }

    /**
     * Get the priority of the linked container.
     * @return The priority. 1 is highest priority.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Set the priority of the linked container.
     * @param priority The priority. 1 is the highest priority.
     */
    public void setPriority(int priority) {
        this.priority = Math.max(1, priority);
    }

    /**
     * Increases the priority of the linked container. Does nothing if already the highest priority (1)
     */
    public void increasePriority() {
        if(priority == 1) return;

        priority--;
    }

    /**
     * Decreases the priority of the linked container. Does nothing if already the lowest priority (2147483647)
     */
    public void decreasePriority() {
        if(priority == Integer.MAX_VALUE) return;

        priority++;
    }
}
