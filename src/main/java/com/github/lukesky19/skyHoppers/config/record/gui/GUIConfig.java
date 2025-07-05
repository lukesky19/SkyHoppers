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
package com.github.lukesky19.skyHoppers.config.record.gui;

import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The GUI configuration for non-upgrade GUIs.
 * @param configVersion The config version of the file.
 * @param guiType The gui type for this GUI.
 * @param name The name of this GUI.
 * @param entries The items to display inside the GUI.
 */
@ConfigSerializable
public record GUIConfig(
        @Nullable String configVersion,
        @Nullable GUIType guiType,
        @Nullable String name,
        @NotNull Buttons entries) {

    /**
     * The possible items that can be displayed inside GUIs.
     * NOTE: Not all are available in every GUI.
     * @param filler The filler item configuration
     * @param previousPage The previous page item configuration
     * @param exit The exit item configuration
     * @param nextPage The next page item configuration
     * @param hopperEnabled The hopper enabled item configuration
     * @param hopperDisabled The hopper disabled item configuration
     * @param particlesEnabled The particles enabled item configuration
     * @param particlesDisabled The particles disabled item configuration
     * @param link The link item configuration
     * @param filter The filter item configuration
     * @param upgrades The upgrades item configuration
     * @param visualize The visualization item configuration
     * @param members The members item configuration
     * @param info The info item configuration
     * @param add The add item configuration
     * @param playerHead The player head item configuration
     * @param filterItem The filter item's item configuration
     * @param linkedItem The linked item's item configuration
     * @param transferSpeed The transfer speed item configuration
     * @param transferAmount The transfer amount item configuration
     * @param suctionSpeed The suction speed item configuration
     * @param suctionAmount The suction amount item configuration
     * @param maxLinks The max links item configuration
     * @param suctionRange The suction range item configuration
     */
    @ConfigSerializable
    public record Buttons(
            @NotNull ItemStackConfig filler,
            @NotNull Button previousPage,
            @NotNull Button exit,
            @NotNull Button nextPage,
            @NotNull Button hopperEnabled,
            @NotNull Button hopperDisabled,
            @NotNull Button particlesEnabled,
            @NotNull Button particlesDisabled,
            @NotNull Button link,
            @NotNull Button filter,
            @NotNull Button upgrades,
            @NotNull Button visualize,
            @NotNull Button members,
            @NotNull Button info,
            @NotNull Button add,
            @NotNull Button playerHead,
            @NotNull Button filterItem,
            @NotNull Button linkedItem,
            @NotNull Button transferSpeed,
            @NotNull Button transferAmount,
            @NotNull Button suctionSpeed,
            @NotNull Button suctionAmount,
            @NotNull Button maxLinks,
            @NotNull Button suctionRange) {}

    /**
     * The generic item configuration for all other buttons.
     * @param slot The slot for the button.
     * @param item The item configuration
     */
    @ConfigSerializable
    public record Button(@Nullable Integer slot, @NotNull ItemStackConfig item) {}
}
