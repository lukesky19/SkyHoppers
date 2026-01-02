package com.github.lukesky19.skyHoppers.config.data.gui;

import com.github.lukesky19.skyHoppers.config.data.button.ButtonConfig;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The GUI configuration for the upgrade selection GUI.
 * @param configVersion The config version of the file.
 * @param guiType The gui type for this GUI.
 * @param name The name of this GUI.
 * @param entries The items to display inside the GUI.
 */
@ConfigSerializable
public record SelectUpgradeGUIConfig(
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
     * @param dummyButtons A {@link List} of {@link ButtonConfig}s to display in the GUI.
     */
    @ConfigSerializable
    public record Buttons(
            @NotNull ItemStackConfig filler,
            @NotNull ButtonConfig previousPage,
            @NotNull ButtonConfig exit,
            @NotNull ButtonConfig nextPage,
            @NotNull ButtonConfig hopperEnabled,
            @NotNull ButtonConfig hopperDisabled,
            @NotNull ButtonConfig particlesEnabled,
            @NotNull ButtonConfig particlesDisabled,
            @NotNull ButtonConfig link,
            @NotNull ButtonConfig filter,
            @NotNull ButtonConfig upgrades,
            @NotNull ButtonConfig visualize,
            @NotNull ButtonConfig members,
            @NotNull ButtonConfig info,
            @NotNull ButtonConfig add,
            @NotNull ButtonConfig playerHead,
            @NotNull ButtonConfig filterItem,
            @NotNull ButtonConfig linkedItem,
            @NotNull ButtonConfig transferSpeed,
            @NotNull ButtonConfig transferAmount,
            @NotNull ButtonConfig suctionSpeed,
            @NotNull ButtonConfig suctionAmount,
            @NotNull ButtonConfig maxLinks,
            @NotNull ButtonConfig suctionRange,
            @NotNull List<ButtonConfig> dummyButtons) {}
}
