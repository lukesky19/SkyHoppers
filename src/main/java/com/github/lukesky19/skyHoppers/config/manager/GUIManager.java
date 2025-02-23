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
package com.github.lukesky19.skyHoppers.config.manager;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.record.gui.GUIConfig;
import com.github.lukesky19.skyHoppers.config.record.gui.upgrade.UpgradeGUIConfig;
import com.github.lukesky19.skylib.config.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * This class manages the loading and parsing of the plugin gui configuration files.
 */
public class GUIManager {
    private final SkyHoppers plugin;
    private final Map<String, GUIConfig> guiConfigs = new HashMap<>();
    private final Map<String, UpgradeGUIConfig> upgradeGuiConfigs = new HashMap<>();

    private static final String GUI_PATH = "gui" + File.separator;
    private static final String UPGRADES_PATH = GUI_PATH + "upgrades" + File.separator;

    /**
     * Gets the plugin's non-upgrade GUI configuration based on the file name provided.
     * @param name The name of the file
     * @return A GUIConfig object or null if no config is loaded for that file name.
     */
    @Nullable
    public GUIConfig getGuiConfig(String name) {
        return guiConfigs.get(name);
    }

    /**
     * Gets the plugin's upgrade GUI configuration based on the file name provided.
     * @param name The name of the file
     * @return An UpgradeGUIConfig object or null if no config is loaded for that file name.
     */
    @Nullable
    public UpgradeGUIConfig getUpgradeConfig(String name) {
        return upgradeGuiConfigs.get(name);
    }

    /**
     * Constructor
     * @param plugin The SkyHoppers Plugin
     */
    public GUIManager(SkyHoppers plugin) {
        this.plugin = plugin;
    }

    /**
     * Initialize the path of GUI config files and a default null configuration.
     */
    private void initializePaths() {
        guiConfigs.clear();
        upgradeGuiConfigs.clear();

        String[] guiFiles = {
                "hopper.yml", "input_filter.yml", "links.yml", "output_filter.yml",
                "members.yml", "select_player.yml", "upgrades.yml"
        };

        String[] upgradeGuiFiles = {
                "links.yml", "suction_amount.yml", "suction_range.yml",
                "suction_speed.yml", "transfer_amount.yml", "transfer_speed.yml"
        };

        for (String file : guiFiles) {
            guiConfigs.put(file, null);
        }

        for (String file : upgradeGuiFiles) {
            upgradeGuiConfigs.put(file, null);
        }
    }

    /**
     * Reload the plugin's GUI configurations
     */
    public void reload() {
        initializePaths();

        saveDefaultConfig();

        loadConfigs();
    }

    /**
     * Loads the plugin's GUI configurations,
     */
    private void loadConfigs() {
        for (String file : guiConfigs.keySet()) {
            loadGuiConfig(file);
        }

        for (String file : upgradeGuiConfigs.keySet()) {
            loadUpgradeGuiConfig(file);
        }
    }

    /**
     * Loads an individual non-upgrade GUI configuration file.
     * @param file The name of the file to load.
     */
    private void loadGuiConfig(String file) {
        Path path = Path.of(plugin.getDataFolder() + File.separator + GUI_PATH + file);
        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            guiConfigs.put(file, loader.load().get(GUIConfig.class));
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads an individual upgrade GUI configuration file.
     * @param file The name of the file to load.
     */
    private void loadUpgradeGuiConfig(String file) {
        Path path = Path.of(plugin.getDataFolder() + File.separator + UPGRADES_PATH + file);
        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            upgradeGuiConfigs.put(file, loader.load().get(UpgradeGUIConfig.class));
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves the default GUI configuration files bundled with the plugin.
     */
    private void saveDefaultConfig() {
        for (String file : guiConfigs.keySet()) {
            Path path = Path.of(plugin.getDataFolder() + File.separator + GUI_PATH + file);
            if(!path.toFile().exists()) {
                saveResource(GUI_PATH + file);
            }
        }

        for (String file : upgradeGuiConfigs.keySet()) {
            Path path = Path.of(plugin.getDataFolder() + File.separator + UPGRADES_PATH + file);

            if(!path.toFile().exists()) {
                saveResource(UPGRADES_PATH + file);
            }
        }
    }

    /**
     * Saves a bundled configuration file to the disk.
     * @param resourcePath The path of the file to save.
     */
    private void saveResource(String resourcePath) {
        plugin.saveResource(resourcePath, false);
    }
}