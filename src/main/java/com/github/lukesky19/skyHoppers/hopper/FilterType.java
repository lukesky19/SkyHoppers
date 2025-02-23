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

import org.jetbrains.annotations.NotNull;

/**
 * The options available for a SkyHopper's or SkyContainer's filter.
 */
public enum FilterType {
    NONE,
    WHITELIST,
    BLACKLIST,
    DESTROY;

    /**
     * Gets the filter type for a given string or returns the default FilterType {@link FilterType#NONE}
     * @param string The name of the filter type.
     * @return The FilterType for the given string or the default FilterType {@link FilterType#NONE}
     */
    @NotNull
    public static FilterType getType(String string) {
        try {
            return FilterType.valueOf(string);
        } catch (IllegalArgumentException e) {
            return FilterType.NONE;
        }
    }
}
