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

import org.bukkit.Location;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Contains the data for a SkyHopper's linked container.
 */
public class SkyContainer extends Filterable {
    private final @NotNull Location location;

    /**
     * Constructor
     * @param location The {@link Location} of the linked container.
     * @param filterType The {@link FilterType} for the linked container.
     */
    public SkyContainer(@NotNull Location location, @NotNull FilterType filterType) {
        super(filterType, new ArrayList<>());
        this.location = location;
    }

    /**
     * Constructor
     * @param location The {@link Location} of the linked container.
     * @param filterType The {@link FilterType} for the linked container.
     * @param filterItems The {@link Set} of {@link ItemType}s that are filtered.
     */
    public SkyContainer(@NotNull Location location, @NotNull FilterType filterType, @NotNull List<ItemType> filterItems) {
        super(filterType, filterItems);
        this.location = location;
    }

    /**
     * Get the {@link Location} of the linked container.
     * @return A copy of the {@link Location} of the linked container.
     */
    public @NotNull Location getLocation() {
        return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
