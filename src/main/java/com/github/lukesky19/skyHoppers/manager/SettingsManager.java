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
package com.github.lukesky19.skyHoppers.manager;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.data.config.Settings;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class manages the loading and parsing of the plugin settings.
 */
public class SettingsManager {
    private final SkyHoppers plugin;
    private Settings settings;
    private final Path path;

    private TreeMap<Double, Double> suctionSpeedUpgrades;
    private TreeMap<Integer, Double> suctionAmountUpgrades;
    private TreeMap<Integer, Double> suctionRangeUpgrades;
    private TreeMap<Double, Double> transferSpeedUpgrades;
    private TreeMap<Integer, Double> transferAmountUpgrades;
    private TreeMap<Integer, Double> containerUpgrades;

    /**
     * Get the suction speed upgrades tree map.
     * @return A TreeMap of Double, Double
     */
    public TreeMap<Double, Double> getSuctionSpeedUpgrades() {
        return suctionSpeedUpgrades;
    }

    /**
     * Get the suction amount upgrades tree map.
     * @return A TreeMap of Integer, Double
     */
    public TreeMap<Integer, Double> getSuctionAmountUpgrades() {
        return suctionAmountUpgrades;
    }

    /**
     * Get the suction range upgrades tree map.
     * @return A TreeMap of Integer, Double
     */
    public TreeMap<Integer, Double> getSuctionRangeUpgrades() {
        return suctionRangeUpgrades;
    }

    /**
     * Get the transfer speed upgrades tree map.
     * @return A TreeMap of Double, Double
     */
    public TreeMap<Double, Double> getTransferSpeedUpgrades() {
        return transferSpeedUpgrades;
    }

    /**
     * Get the transfer amount upgrades tree map.
     * @return A TreeMap of Integer, Double
     */
    public TreeMap<Integer, Double> getTransferAmountUpgrades() {
        return transferAmountUpgrades;
    }

    /**
     * Get the container upgrades tree map.
     * @return A TreeMap of Integer, Double
     */
    public TreeMap<Integer, Double> getContainerUpgrades() {
        return containerUpgrades;
    }

    /**
     * Get the plugin's settings.
     * @return A Settings object
     */
    @Nullable
    public Settings getSettings() {
        return settings;
    }

    /**
     * Constructor
     * @param plugin The SkyHoppers Plugin
     */
    public SettingsManager(SkyHoppers plugin) {
        this.plugin = plugin;
        path = Path.of(plugin.getDataFolder() + File.separator + "settings.yml");
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
     * Check the version of the settings.yml file and display warnings if outdated.
     */
    private void checkVersion() {
        if(settings == null) return;
        ComponentLogger logger = plugin.getComponentLogger();

        if(settings.configVersion() == null) {
            logger.warn(AdventureUtil.serialize("Unable to check settings version as it is not configured."));
            return;
        }

        if(!settings.configVersion().equals("1.1.0.0")) {
            logger.warn(AdventureUtil.serialize("Your plugin settings are outdated. Current version: " + settings.configVersion() + ". Latest version: 1.1.0.0."));
            logger.warn(AdventureUtil.serialize("You should regenerate your settings.yml or migrate your settings.yml to the new version."));
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
