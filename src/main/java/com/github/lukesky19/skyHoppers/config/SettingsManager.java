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
package com.github.lukesky19.skyHoppers.config;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.data.Settings;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.CommentedConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * This class manages the loading and parsing of the plugin settings.
 */
public class SettingsManager {
    private final @NotNull SkyHoppers plugin;
    private @Nullable Settings settings;
    private final @NotNull Path path;

    private @Nullable TreeMap<Double, Double> suctionSpeedUpgrades;
    private @Nullable TreeMap<Integer, Double> suctionAmountUpgrades;
    private @Nullable TreeMap<Integer, Double> suctionRangeUpgrades;
    private @Nullable TreeMap<Double, Double> transferSpeedUpgrades;
    private @Nullable TreeMap<Integer, Double> transferAmountUpgrades;
    private @Nullable TreeMap<Integer, Double> containerUpgrades;

    /**
     * Constructor
     * @param plugin A {@link SkyHoppers} instance.
     */
    public SettingsManager(SkyHoppers plugin) {
        this.plugin = plugin;
        path = Path.of(plugin.getDataFolder() + File.separator + "settings.yml");
    }

    /**
     * Get the suction speed upgrades tree map.
     * @return A TreeMap of Double, Double
     */
    public @Nullable TreeMap<Double, Double> getSuctionSpeedUpgrades() {
        return suctionSpeedUpgrades;
    }

    /**
     * Get the suction amount upgrades tree map.
     * @return A TreeMap of Integer, Double
     */
    public @Nullable TreeMap<Integer, Double> getSuctionAmountUpgrades() {
        return suctionAmountUpgrades;
    }

    /**
     * Get the suction range upgrades tree map.
     * @return A TreeMap of Integer, Double
     */
    public @Nullable TreeMap<Integer, Double> getSuctionRangeUpgrades() {
        return suctionRangeUpgrades;
    }

    /**
     * Get the transfer speed upgrades tree map.
     * @return A TreeMap of Double, Double
     */
    public @Nullable TreeMap<Double, Double> getTransferSpeedUpgrades() {
        return transferSpeedUpgrades;
    }

    /**
     * Get the transfer amount upgrades tree map.
     * @return A TreeMap of Integer, Double
     */
    public @Nullable TreeMap<Integer, Double> getTransferAmountUpgrades() {
        return transferAmountUpgrades;
    }

    /**
     * Get the container upgrades tree map.
     * @return A TreeMap of Integer, Double
     */
    public @Nullable TreeMap<Integer, Double> getContainerUpgrades() {
        return containerUpgrades;
    }

    /**
     * Get the plugin's {@link Settings}.
     * @return The plugin's {@link Settings}. May be null.
     */
    public @Nullable Settings getSettings() {
        return settings;
    }

    /**
     * Get the configured highest priority or 1 if settings are invalid.
     * @return The configured highest priority or 1 if settings are invalid.
     */
    public int getHighestPriority() {
        return settings != null ? settings.skyContainerConfig().highestPriority() : 1;
    }

    /**
     * Get the configured highest priority or 255 if settings are invalid.
     * @return The configured highest priority or 255 if settings are invalid.
     */
    public int getLowestPriority() {
        return settings != null ? settings.skyContainerConfig().lowestPriority() : 255;
    }

    /**
     * Get the starting priority clamped to the highest and lowest priority values.
     * If the settings are invalid, the starting priority is 1.
     * @return The starting priority.
     */
    public int getStartingPriority() {
        if(settings == null) return 1;

        int highestPriority = getHighestPriority();
        int lowestPriority = getLowestPriority();
        int startingPriority = settings.skyContainerConfig().startingPriority();

        // Return the priority clamped to the highest and lowest priorities
        return Math.max(highestPriority, Math.min(lowestPriority, startingPriority));
    }

    /**
     * Reloads the plugin's settings.
     */
    public void reload() {
        settings = null;

        if(!path.toFile().exists()) {
            plugin.saveResource("settings.yml", false);
        }

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            settings = loader.load().get(Settings.class);

            checkVersion();

            parseUpgrades();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Save the plugin's settings to the disk.
     */
    private void saveSettings() {
        if(settings == null) return;

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            CommentedConfigurationNode node = loader.createNode();

            node.set(Settings.class, settings);

            loader.save(node);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check the version of the settings.yml file and display warnings if outdated.
     */
    private void checkVersion() {
        if(settings == null) return;
        ComponentLogger logger = plugin.getComponentLogger();

        switch(settings.configVersion()) {
            case "1.2.0.0" -> {
                // Latest version, do nothing
            }

            case "1.1.0.0" -> {
                Settings.SkyHopperConfig oldSkyHopperConfig = settings.skyHopperConfig();
                ItemStackConfig oldItemStackConfig = oldSkyHopperConfig.item();

                ItemStackConfig newItemStackConfig = new ItemStackConfig(
                        Objects.requireNonNullElse(oldItemStackConfig.itemType(), ItemType.HOPPER),
                        oldItemStackConfig.amount(),
                        oldItemStackConfig.maxStackSize(),
                        oldItemStackConfig.name(),
                        oldItemStackConfig.lore(),
                        oldItemStackConfig.entityType(),
                        oldItemStackConfig.instrument(),
                        oldItemStackConfig.enchantments(),
                        oldItemStackConfig.potionConfig(),
                        oldItemStackConfig.color(),
                        oldItemStackConfig.modelName(),
                        oldItemStackConfig.itemFlags(),
                        oldItemStackConfig.decoratedPot(),
                        oldItemStackConfig.armorTrim(),
                        oldItemStackConfig.attributes(),
                        oldItemStackConfig.options());
                Settings.SkyHopperConfig newSkyHopperConfig = new Settings.SkyHopperConfig(
                        oldSkyHopperConfig.startingTransferSpeed(),
                        oldSkyHopperConfig.startingTransferAmount(),
                        oldSkyHopperConfig.startingSuctionSpeed(),
                        oldSkyHopperConfig.startingSuctionAmount(),
                        oldSkyHopperConfig.startingSuctionRange(),
                        oldSkyHopperConfig.startingMaxContainers(),
                        newItemStackConfig,
                        oldSkyHopperConfig.placeholders());

                settings = new Settings(
                        "1.2.0.0",
                        settings.locale(),
                        settings.dropToInventory(),
                        settings.disabledHooks(),
                        newSkyHopperConfig,
                        new Settings.SkyContainerConfig(1, 1, 256),
                        settings.upgrades());

                saveSettings();
            }

            case null -> logger.warn(AdventureUtil.deserialize("Unable to check settings version as it is not configured."));

            default -> {
                logger.warn(AdventureUtil.deserialize("Your plugin settings are outdated. Current version: " + settings.configVersion() + ". Latest version: 1.2.0.0."));
                logger.warn(AdventureUtil.deserialize("You should regenerate your settings.yml or migrate your settings.yml to the new version."));
            }
        }
    }

    /**
     * Parses the upgrades from the Plugin's settings into TreeMaps
     */
    private void parseUpgrades() {
        if(settings == null) return;
        Settings.Upgrades upgrades = settings.upgrades();

        suctionSpeedUpgrades = createTreeMap(upgrades.suctionSpeed().upgrades());
        suctionAmountUpgrades = createTreeMap(upgrades.suctionAmount().upgrades());
        suctionRangeUpgrades = createTreeMap(upgrades.suctionRange().upgrades());
        transferSpeedUpgrades = createTreeMap(upgrades.transferSpeed().upgrades());
        transferAmountUpgrades = createTreeMap(upgrades.transferAmount().upgrades());
        containerUpgrades = createTreeMap(upgrades.containers().upgrades());
    }

    /**
     * Creates a new TreeMap for an upgrade
     * @param map The existing unsorted Map of upgrades
     * @return A TreeMap of Double, Double or Integer, Double
     * @param <K> Double or Integer
     * @param <V> Double
     */
    private <K, V> TreeMap<K, V> createTreeMap(Map<K, V> map) {
        return new TreeMap<>(map);
    }
}