package com.github.lukesky19.skyHoppers.config.record.gui.upgrade;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        @Nullable String guiType,
        @Nullable String name,
        Entries entries) {

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
    public record Entries(
            Filler filler,
            GenericEntry exit,
            GenericEntry increase,
            GenericEntry increaseMax,
            GenericEntry upgrade,
            GenericEntry upgradeMax,
            GenericEntry decrease,
            GenericEntry decreaseMin) {}

    /**
     * The item configuration for Filler buttons.
     * @param item The item configuration
     */
    @ConfigSerializable
    public record Filler(Item item) {}

    /**
     * The generic item configuration for all other buttons.
     * @param slot The slot for the button.
     * @param item The item configuration
     */
    @ConfigSerializable
    public record GenericEntry(int slot, Item item) {}

    /**
     * The item configuration to create an ItemStack.
     * @param material The material name.
     * @param name The name of the item.
     * @param lore The lore of the item.
     * @param itemFlags The item flags to add.
     */
    @ConfigSerializable
    public record Item(
            @Nullable String material,
            @Nullable String name,
            List<String> lore,
            List<String> itemFlags) {}
}

