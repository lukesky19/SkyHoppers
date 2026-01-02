/*
    SkyHoppers adds upgradable hoppers that can suction items, transfer items wirelessly to linked containers.
    Copyright (C) 2024  lukeskywlker19

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
package com.github.lukesky19.skyHoppers.config.data.gui;

import com.github.lukesky19.skyHoppers.config.data.button.ButtonConfig;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The GUI configuration for the main SkyContainer GUI.
 * @param configVersion The config version of the file.
 * @param guiType The gui type for this GUI.
 * @param name The name of this GUI.
 * @param entries The items to display inside the GUI.
 */
@ConfigSerializable
public record SkyContainerGUIConfig(
        @Nullable String configVersion,
        @Nullable GUIType guiType,
        @Nullable String name,
        @NotNull Buttons entries) implements IGUIConfig {
    @Override
    public @Nullable String getConfigVersion() {
        return configVersion;
    }

    /**
     * The buttons to display in the GUI.
     * @param filler The filler item configuration
     * @param exit The exit item configuration
     * @param filter The button config to open the filter GUI.
     * @param priority The button config to open the priority GUI.
     * @param info The button config for the info button.
     * @param dummyButtons A {@link List} of {@link ButtonConfig}s to display in the GUI.
     */
    @ConfigSerializable
    public record Buttons(
            @NotNull ItemStackConfig filler,
            @NotNull ButtonConfig exit,
            @NotNull ButtonConfig filter,
            @NotNull ButtonConfig priority,
            @NotNull ButtonConfig info,
            @NotNull List<ButtonConfig> dummyButtons) {}
}