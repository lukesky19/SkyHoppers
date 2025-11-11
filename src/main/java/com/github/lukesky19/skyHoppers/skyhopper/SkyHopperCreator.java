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
package com.github.lukesky19.skyHoppers.skyhopper;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.SettingsManager;
import com.github.lukesky19.skyHoppers.config.data.Settings;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class is used to create {@link SkyHopper}s.
 */
public class SkyHopperCreator {
    private final @NotNull SkyHoppers skyHoppers;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull SkyHopperSaver skyHopperSaver;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param skyHopperSaver A {@link SkyHopperSaver} instance.
     */
    public SkyHopperCreator(
            @NotNull SkyHoppers skyHoppers,
            @NotNull SettingsManager settingsManager,
            @NotNull SkyHopperSaver skyHopperSaver) {
        this.skyHoppers = skyHoppers;
        this.settingsManager = settingsManager;
        this.skyHopperSaver = skyHopperSaver;
    }

    /**
     * Creates an {@link ItemStack} for a {@link SkyHopper}.
     * @param skyHopper The {@link SkyHopper}.
     * @param amount The amount to set the {@link ItemStack} amount to.
     * @return An {@link ItemStack} for the {@link SkyHopper} or null if {@link ItemStack} creation failed.
     */
    public @Nullable ItemStack createSkyHopperItemStack(@NotNull SkyHopper skyHopper, int amount) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return null;

        ItemStackConfig itemStackConfig = settings.skyHopperConfig().item();
        Settings.Placeholders placeholdersConfig = settings.skyHopperConfig().placeholders();

        // Create a list of placeholders for the lore of the ItemStack.
        List<TagResolver.Single> placeholders = new ArrayList<>();

        if(placeholdersConfig.enabled() != null && placeholdersConfig.disabled() != null) {
            if(skyHopper.isSkyHopperEnabled()) {
                placeholders.add(Placeholder.parsed("status", placeholdersConfig.enabled()));
            } else {
                placeholders.add(Placeholder.parsed("status", placeholdersConfig.disabled()));
            }
        }

        if(skyHopper.getOwner() != null) {
            String playerName = skyHoppers.getServer().getOfflinePlayer(skyHopper.getOwner()).getName();
            placeholders.add(Placeholder.parsed("name", String.valueOf(playerName)));
        } else {
            placeholders.add(Placeholder.parsed("name", "none"));
        }

        placeholders.add(Placeholder.parsed("member_count", String.valueOf(skyHopper.getMembers().size())));
        placeholders.add(Placeholder.parsed("filter_type", skyHopper.getFilterType().name()));
        placeholders.add(Placeholder.parsed("current_links", String.valueOf(skyHopper.getLinkedContainers().size())));
        placeholders.add(Placeholder.parsed("max_links", String.valueOf(skyHopper.getMaxContainers())));
        placeholders.add(Placeholder.parsed("transfer_amount", String.valueOf(skyHopper.getTransferAmount())));
        placeholders.add(Placeholder.parsed("transfer_speed", String.valueOf(skyHopper.getTransferSpeed())));
        placeholders.add(Placeholder.parsed("suction_amount", String.valueOf(skyHopper.getSuctionAmount())));
        placeholders.add(Placeholder.parsed("suction_speed", String.valueOf(skyHopper.getSuctionSpeed())));
        placeholders.add(Placeholder.parsed("suction_range", String.valueOf(skyHopper.getSuctionRange())));

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(skyHoppers.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(itemStackConfig, null, null, placeholders);
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        ItemStack itemStack = null;
        if(optionalItemStack.isPresent()) {
            itemStack = optionalItemStack.get();
        }

        if(itemStack == null) {
            skyHoppers.getComponentLogger().error(AdventureUtil.serialize("Failed to create the ItemStack for a SkyHopper."));
            return null;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();

        skyHopperSaver.saveSkyHopper(skyHopper, itemMeta.getPersistentDataContainer());

        itemStack.setItemMeta(itemMeta);

        itemStack.setAmount(amount);

        return itemStack;
    }
}
