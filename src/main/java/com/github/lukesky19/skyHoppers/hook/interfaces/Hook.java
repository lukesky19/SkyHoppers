/*
    SkyHoppers adds upgradable hoppers that can suction items, transfer items wirelessly to linked containers.
    Copyright (C) 2025 lukeskywlker19

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
package com.github.lukesky19.skyHoppers.hook.interfaces;

import org.jetbrains.annotations.NotNull;

/**
 * This class is implemented to create hooks into different plugins.
 */
public interface Hook {
    /**
     * Attempts to initialize the hook into a plugin.
     */
    void initialize();

    /**
     * Deinitialize any hooks into a plugin.
     */
    void deinitialize();

    /**
     * Checks if the hook was initialized or not.
     * @return true if hooked or false.
     */
    boolean isHooked();

    /**
     * Get the name of the plugin hooked into.
     * @return The name of the plugin hooked into.
     */
    @NotNull String getHookName();
}

