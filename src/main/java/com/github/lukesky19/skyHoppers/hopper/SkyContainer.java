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
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Contains the data for a SkyHopper's linked container.
 * @param location The Location of the Container.
 * @param filterType The FilterType
 * @param filterItems The items to filter.
 */
public record SkyContainer(@NotNull Location location, @NotNull FilterType filterType, @NotNull List<Material> filterItems) {
    /**
     * Adds a Material to the filter items.
     * @param material The Material to add.
     */
    public void addFilterItem(Material material) {
        if(!this.filterItems.contains(material)) {
            this.filterItems.add(material);
        }
    }

    /**
     * Removes a Material from the filter items.
     * @param material The Material to remove.
     */
    public void removeFilterItem(Material material) {
        this.filterItems.remove(material);
    }
}
