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
import com.github.lukesky19.skyHoppers.config.data.gui.*;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.platform.PlatformUtils;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.NodeStyle;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;

/**
 * This class manages the loading and parsing of the plugin gui configuration files.
 */
public class GUIConfigManager {
    private final @NotNull SkyHoppers plugin;
    private final @NotNull ComponentLogger logger;

    private @Nullable SkyHopperGUIConfig hopperGUIConfig;
    private @Nullable SkyContainerGUIConfig skyContainerGUIConfig;
    private @Nullable FilterGUIConfig inputFilterGUIConfig;
    private @Nullable LinkedContainersGUIConfig linkedContainersGUIConfig;
    private @Nullable FilterGUIConfig outputFilterGUIConfig;
    private @Nullable MembersGUIConfig membersGUIConfig;
    private @Nullable SelectPlayerGUIConfig selectPlayerGUIConfig;
    private @Nullable SelectUpgradeGUIConfig upgradesGUIConfig;
    private @Nullable PriorityGUIConfig priorityGUIConfig;

    private @Nullable UpgradeGUIConfig linksUpgradeGUIConfig;
    private @Nullable UpgradeGUIConfig suctionAmountUpgradeGUIConfig;
    private @Nullable UpgradeGUIConfig suctionRangeUpgradeGUIConfig;
    private @Nullable UpgradeGUIConfig suctionSpeedUpgradeGUIConfig;
    private @Nullable UpgradeGUIConfig transferAmountUpgradeGUIConfig;
    private @Nullable UpgradeGUIConfig transferSpeedUpgradeGUIConfig;

    /**
     * Constructor
     * @param plugin A {@link SkyHoppers} instance.
     */
    public GUIConfigManager(@NotNull SkyHoppers plugin) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
    }

    /**
     * Reload the plugin's GUI configurations
     */
    public void reload() {
        hopperGUIConfig = loadConfiguration(getGUIPath("hopper.yml"), SkyHopperGUIConfig.class, false);
        skyContainerGUIConfig = loadConfiguration(getGUIPath("skycontainer.yml"), SkyContainerGUIConfig.class, false);
        inputFilterGUIConfig = loadConfiguration(getGUIPath("input_filter.yml"), FilterGUIConfig.class, false);
        linkedContainersGUIConfig = loadConfiguration(getGUIPath("links.yml"), LinkedContainersGUIConfig.class, false);
        outputFilterGUIConfig = loadConfiguration(getGUIPath("output_filter.yml"), FilterGUIConfig.class, false);
        membersGUIConfig = loadConfiguration(getGUIPath("members.yml"), MembersGUIConfig.class, false);
        selectPlayerGUIConfig = loadConfiguration(getGUIPath("select_player.yml"), SelectPlayerGUIConfig.class, false);
        upgradesGUIConfig = loadConfiguration(getGUIPath("upgrades.yml"), SelectUpgradeGUIConfig.class, false);

        linksUpgradeGUIConfig = loadConfiguration(getUpgradePath("links.yml"), UpgradeGUIConfig.class, true);
        suctionAmountUpgradeGUIConfig = loadConfiguration(getUpgradePath("suction_amount.yml"), UpgradeGUIConfig.class, true);
        suctionRangeUpgradeGUIConfig = loadConfiguration(getUpgradePath("suction_range.yml"), UpgradeGUIConfig.class, true);
        suctionSpeedUpgradeGUIConfig = loadConfiguration(getUpgradePath("suction_speed.yml"), UpgradeGUIConfig.class, true);
        transferAmountUpgradeGUIConfig = loadConfiguration(getUpgradePath("transfer_amount.yml"), UpgradeGUIConfig.class, true);
        transferSpeedUpgradeGUIConfig = loadConfiguration(getUpgradePath("transfer_speed.yml"), UpgradeGUIConfig.class, true);

        priorityGUIConfig = loadConfiguration(getGUIPath("priority.yml"), PriorityGUIConfig.class, false);
    }

    /**
     * Load the configuration.
     * @param path The path to load the config for.
     * @param clazz The class to load configuration to.
     * @param isUpgrade Is the config an upgrade GUI config?
     * @return The configuration or null.
     * @param <T> The class created for the configuration.
     */
    private <T> @Nullable T loadConfiguration(@NotNull Path path, @NotNull Class<T> clazz, boolean isUpgrade) {
        saveDefaultConfig(path, isUpgrade);

        YamlConfigurationLoader loader = createLoader(path);

        try {
            T config = loader.load().get(clazz);

            if(config != null) {
                if(config instanceof IGUIConfig guiConfig) {
                    if(!checkConfigVersion(path.getFileName().toString(), guiConfig)) {
                        return null;
                    }
                }
            }

            return config;
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtility.plain("Unable to load GUI config for record " + clazz.getName() + ". Error: " + configurateException.getMessage()));
            return null;
        }
    }

    /**
     * Get the {@link Path} for the file name.
     * This is for normal gui configurations.
     * @param fileName The file name.
     * @return A {@link Path}.
     */
    private @NotNull Path getGUIPath(@NotNull String fileName) {
        return Path.of(plugin.getDataFolder() + File.separator + "gui" + File.separator + fileName);
    }

    /**
     * Get the {@link Path} for the file name.
     * This is for upgrade gui configurations.
     * @param fileName The file name.
     * @return A {@link Path}.
     */
    private @NotNull Path getUpgradePath(@NotNull String fileName) {
        return Path.of(plugin.getDataFolder() + File.separator + "gui" + File.separator + "upgrades" + File.separator + fileName);
    }

    /**
     * Check the config version.
     * @param fileName The file name.
     * @param config The {@link IGUIConfig}.
     * @return true if valid, or false if out of date.
     */
    private boolean checkConfigVersion(@NotNull String fileName, @NotNull IGUIConfig config) {
        @Nullable String version = config.getConfigVersion();

        if(version == null) {
            logger.warn(AdventureUtility.plain("Unable to check the config version in " + fileName + " as it is not configured."));
            return false;
        }

        if(config instanceof LinkedContainersGUIConfig) {
            if(version.equals("1.1.1.0")) {
                return true;
            } else if(version.equals("1.1.0.0")) {
                logger.info(AdventureUtility.plain("The gui configuration for " + fileName + " is outdated. Current version: " + version + ". Latest version: 1.1.1.0."));
                logger.info(AdventureUtility.plain("This is a minor update that changes the lore of one of the button's lore with updated functionality."));
                logger.info(AdventureUtility.plain("You may wish you update your configuration as well, but will continue to work regardless."));

                return true;
            } else {
                logger.warn(AdventureUtility.plain("The gui configuration for " + fileName + " is outdated. Current version: " + version + ". Latest version: 1.1.0.0."));
                logger.warn(AdventureUtility.plain("You should regenerate your " + fileName + " or migrate your " + fileName + " to the new version."));
                logger.warn(AdventureUtility.plain("The GUI for " + fileName + " will not be able to open until this is corrected."));

                return false;
            }
        } else if(config instanceof PriorityGUIConfig || config instanceof SkyContainerGUIConfig) {
            return true;
        } else {
            if(!version.equals("1.1.0.0")) {
                logger.warn(AdventureUtility.plain("The gui configuration for " + fileName + " is outdated. Current version: " + version + ". Latest version: 1.1.0.0."));
                logger.warn(AdventureUtility.plain("You should regenerate your " + fileName + " or migrate your " + fileName + " to the new version."));
                logger.warn(AdventureUtility.plain("The GUI for " + fileName + " will not be able to open until this is corrected."));

                return false;
            }
        }

        return true;
    }

    /**
     * Saves the default GUI configuration file bundled with the plugin for the path provided.
     * @param path The {@link Path}.
     * @param isUpgrade Is an upgrade GUI config path.
     */
    private void saveDefaultConfig(@NotNull Path path, boolean isUpgrade) {
        if(isUpgrade) {
            if(!path.toFile().exists()) {
                saveResource("gui" + File.separator + "upgrades" + File.separator + path.getFileName().toString());
            }
        } else {
            if(!path.toFile().exists()) {
                saveResource("gui" + File.separator + path.getFileName().toString());
            }
        }
    }

    /**
     * Saves a bundled configuration file to the disk.
     * @param resourcePath The path of the file to save.
     */
    private void saveResource(@NotNull String resourcePath) {
        plugin.saveResource(resourcePath, false);
    }

    /**
     * Get the {@link SkyHopperGUIConfig} for the main SkyHopper GUI.
     * @return The {@link SkyHopperGUIConfig} or null.
     */
    public @Nullable SkyHopperGUIConfig getHopperGUIConfig() {
        return hopperGUIConfig;
    }

    /**
     * Get the {@link SkyContainerGUIConfig} for the main SkyContainer GUI.
     * @return The {@link SkyContainerGUIConfig} or null.
     */
    public @Nullable SkyContainerGUIConfig getSkyContainerGUIConfig() {
        return skyContainerGUIConfig;
    }

    /**
     * Get the {@link FilterGUIConfig} for viewing the input filter.
     * @return The {@link FilterGUIConfig} or null.
     */
    public @Nullable FilterGUIConfig getInputFilterGUIConfig() {
        return inputFilterGUIConfig;
    }

    /**
     * Get the {@link LinkedContainersGUIConfig} for viewing linked containers.
     * @return The {@link LinkedContainersGUIConfig} or null.
     */
    public @Nullable LinkedContainersGUIConfig getLinkedContainersGUIConfig() {
        return linkedContainersGUIConfig;
    }

    /**
     * Get the {@link FilterGUIConfig} for viewing the output filter.
     * @return The {@link FilterGUIConfig} or null.
     */
    public @Nullable FilterGUIConfig getOutputFilterGUIConfig() {
        return outputFilterGUIConfig;
    }

    /**
     * Get the {@link MembersGUIConfig} for viewing members.
     * @return The {@link MembersGUIConfig} or null.
     */
    public @Nullable MembersGUIConfig getMembersGUIConfig() {
        return membersGUIConfig;
    }

    /**
     * Get the {@link SelectPlayerGUIConfig} for selecting a player.
     * @return The {@link SelectPlayerGUIConfig} or null.
     */
    public @Nullable SelectPlayerGUIConfig getSelectPlayerGUIConfig() {
        return selectPlayerGUIConfig;
    }

    /**
     * Get the {@link SelectUpgradeGUIConfig} for viewing upgrade selections.
     * @return The {@link SelectUpgradeGUIConfig} or null.
     */
    public @Nullable SelectUpgradeGUIConfig getUpgradesGUIConfig() {
        return upgradesGUIConfig;
    }

    /**
     * Get the {@link UpgradeGUIConfig} for linked container upgrades.
     * @return The {@link UpgradeGUIConfig} or null.
     */
    public @Nullable UpgradeGUIConfig getLinksUpgradeGUIConfig() {
        return linksUpgradeGUIConfig;
    }

    /**
     * Get the {@link UpgradeGUIConfig} for suction amount upgrades.
     * @return The {@link UpgradeGUIConfig} or null.
     */
    public @Nullable UpgradeGUIConfig getSuctionAmountUpgradeGUIConfig() {
        return suctionAmountUpgradeGUIConfig;
    }

    /**
     * Get the {@link UpgradeGUIConfig} for suction range upgrades.
     * @return The {@link UpgradeGUIConfig} or null.
     */
    public @Nullable UpgradeGUIConfig getSuctionRangeUpgradeGUIConfig() {
        return suctionRangeUpgradeGUIConfig;
    }

    /**
     * Get the {@link UpgradeGUIConfig} for suction speed upgrades.
     * @return The {@link UpgradeGUIConfig} or null.
     */
    public @Nullable UpgradeGUIConfig getSuctionSpeedUpgradeGUIConfig() {
        return suctionSpeedUpgradeGUIConfig;
    }

    /**
     * Get the {@link UpgradeGUIConfig} for transfer amount upgrades.
     * @return The {@link UpgradeGUIConfig} or null.
     */
    public @Nullable UpgradeGUIConfig getTransferAmountUpgradeGUIConfig() {
        return transferAmountUpgradeGUIConfig;
    }

    /**
     * Get the {@link UpgradeGUIConfig} for transfer speed upgrades.
     * @return The {@link UpgradeGUIConfig} or null.
     */
    public @Nullable UpgradeGUIConfig getTransferSpeedUpgradeGUIConfig() {
        return transferSpeedUpgradeGUIConfig;
    }

    /**
     * Get the {@link PriorityGUIConfig}.
     * @return The {@link PriorityGUIConfig} or null.
     */
    public @Nullable PriorityGUIConfig getPriorityGUIConfig() {
        return priorityGUIConfig;
    }

    /**
     * Create the {@link YamlConfigurationLoader} for the path provided.
     * @apiNote {@link PlatformUtils#getSerializers()} are included by default.
     * @param path The {@link Path}.
     * @return The {@link YamlConfigurationLoader}.
     */
    protected @NonNull YamlConfigurationLoader createLoader(@NonNull Path path) {
        return YamlConfigurationLoader.builder()
                .path(path)
                .nodeStyle(NodeStyle.BLOCK)
                .indent(4)
                .defaultOptions(configurationOptions ->
                        configurationOptions.serializers(builder ->
                                builder.registerAll(PlatformUtils.getSerializers())))
                .build();
    }
}