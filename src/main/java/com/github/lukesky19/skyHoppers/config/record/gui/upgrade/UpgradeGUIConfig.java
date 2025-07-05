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
package com.github.lukesky19.skyHoppers.config.record.gui.upgrade;

import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * he GUI configuration for only upgrade GUIs.
 * @param configVersion The config version of the file.
 * @param guiType The gui type for this GUI.
 * @param name The name of this GUI.
 * @param entries The items to display inside the GUI.
 */
@ConfigSerializable
public record UpgradeGUIConfig(
        @Nullable String configVersion,
        @Nullable GUIType guiType,
        @Nullable String name,
        @NotNull Buttons entries) {

    /**
     * The possible items that can be displayed inside GUIs.
     * NOTE: Not all are available in every GUI.
     * @param filler The filler item configuration.
     * @param exit The exit item configuration.
     * @param increase The increase item configuration.
     * @param increaseMax The increase max item configuration.
     * @param upgrade The upgrade item configuration.
     * @param upgradeMax The upgrade max item configuration.
     * @param decrease The decrease item configuration.
     * @param decreaseMin The decrease min item configuration.
     */
    @ConfigSerializable
    public record Buttons(
            @NotNull ItemStackConfig filler,
            @NotNull Button exit,
            @NotNull Button increase,
            @NotNull Button increaseMax,
            @NotNull Button upgrade,
            @NotNull Button upgradeMax,
            @NotNull Button decrease,
            @NotNull Button decreaseMin) {}

    /**
     * The generic item configuration for all other buttons.
     * @param slot The slot for the button.
     * @param item The item configuration
     */
    @ConfigSerializable
    public record Button(@Nullable Integer slot, @NotNull ItemStackConfig item) {}
}

