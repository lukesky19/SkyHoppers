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
import com.github.lukesky19.skyHoppers.config.record.Locale;
import com.github.lukesky19.skyHoppers.config.record.Settings;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * This class manages the loading and parsing of the plugin locale.
 */
public class LocaleManager {
    private final SkyHoppers plugin;
    private final SettingsManager settingsManager;
    private Locale locale;
    private final Locale DEFAULT_LOCALE = new Locale(
            "1.0.0",
            "<#99ff99><bold>SkyHoppers</bold></#99ff99><gray> ▪ </gray>",
            List.of(
                    "<#99ff99>SkyHoppers is developed by <white><bold>lukeskywlker19</bold></white>.</#99ff99>",
                    "<#99ff99>Source code is released on GitHub: <click:OPEN_URL:https://github.com/lukesky19><yellow><underlined><bold>https://github.com/lukesky19</bold></underlined></yellow></click></#99ff99>",
                    " ",
                    "<#99ff99><bold>List of Commands:</bold></#99ff99>",
                    "<white>/<#99ff99>skyhoppers</#99ff99> <yellow>reload</yellow></white>",
                    "<white>/<#99ff99>skyhoppers</#99ff99> <yellow>help</yellow></white>",
                    "<white>/<#99ff99>skyhoppers</#99ff99> <yellow>give</yellow> <yellow><player name></yellow> <yellow><amount></yellow> <yellow>[suction speed]</yellow> <yellow>[suction amount]</yellow> <yellow>[suction range]</yellow> <yellow>[transfer speed]</yellow> <yellow>[transfer amount]</yellow> <yellow>[max containers]</yellow></white>"),
            "<#99ff99>The plugin has been reloaded.</#99ff99>",
            "<#99ff99>A SkyHopper has been added to your inventory.</#99ff99>",
            "<#99ff99>A SkyHopper was given to <player>.</#99ff99>",
            "<#ff4343>You do not have permission to build here.</#ff4343>",
            "<#ff4343>You do not have permission to break this hopper.</#ff4343>",
            "<#ff4343>You do not have permission to access this hopper.</#ff4343>",
            "<#99ff99>Hopper successfully placed.</#99ff99>",
            "<#99ff99>Hopper successfully removed.</#99ff99>",
            "<#99ff99>Linking mode enabled. Click a container to link it to the SkyHopper.</#99ff99>",
            "<#99ff99>Linking mode disabled.</#99ff99>",
            "<#99ff99>To exit linking mode, click the SkyHopper you are linking to.</#99ff99>",
            "<#99ff99>Container has been linked.</#99ff99>",
            "<#99ff99>Container has been unlinked.</#99ff99>",
            "<#ff4343>This SkyHopper is already linked to it's maximum number of containers.</#ff4343>",
            "<#ff4343>You cannot access this container.</#ff4343>",
            "<#99ff99>Upgraded suction speed from <yellow><current></yellow> to <yellow><next></yellow>.</#99ff99>",
            "<#99ff99>Upgraded suction amount from <yellow><current></yellow> to <yellow><next></yellow>.</#99ff99>",
            "<#99ff99>Upgraded suction range from <yellow><current></yellow> to <yellow><next></yellow>.</#99ff99>",
            "<#99ff99>Upgraded max linked containers from <yellow><current></yellow> to <yellow><next></yellow>.</#99ff99>",
            "<#99ff99>Upgraded transfer speed from <yellow><current></yellow> to <yellow><next></yellow>.</#99ff99>",
            "<#99ff99>Upgraded transfer amount from <yellow><current></yellow> to <yellow><next></yellow>.</#99ff99>",
            "<#ff4343>You do not have enough money to purchase this upgrade.</#ff4343>",
            "<#ff4343>This upgrade is already maxed out.</#ff4343>",
            "<red>The plugin failed to create the SkyHopper.</red>",
            "<green>Forced loaded all skyhoppers unless they were already cached.</green>",
            "<green>Forced loaded all skyhoppers regardless if they were cached.</green>",
            "<red>All SkyHoppers are now paused.</red>",
            "<red>All SkyHoppers are now unpaused</red>",
            "<red>The plugin's settings is invalid.</red>",
            "<red>The plugin failed to load a SkyHopper's settings.<red>",
            "<red>The item in your hand is not a SkyHopper.</red>",
            "<red>You don't have access to this SkyHopper to change the owner.</red>",
            "<green>This SkyHopper's owner is now <yellow><player_name></yellow>.</green>",
            "<red>Unable to open this GUI because of a configuration error.</red>");

    /**
     * Constructor
     * @param plugin The SkyHoppers Plugin
     * @param settingsManager A SettingsManager instance.
     */
    public LocaleManager(SkyHoppers plugin, SettingsManager settingsManager) {
        this.plugin = plugin;
        this.settingsManager = settingsManager;
    }

    /**
     * Get the plugins locale or the default locale if the locale configuration failed to load.
     * @return A Locale object.
     */
    @NotNull
    public Locale getLocale() {
        if(locale == null) return DEFAULT_LOCALE;
        return locale;
    }

    /**
     * Reloads the plugin's locale.
     */
    public void reload() {
        locale = null;
        ComponentLogger logger = plugin.getComponentLogger();

        copyDefaultLocales();

        Settings settings = settingsManager.getSettings();
        if(settings == null) {
            logger.warn(AdventureUtil.serialize("Unable to load locale configuration as the plugin's settings.yml is invalid."));
            return;
        }

        String localeString = settingsManager.getSettings().locale();
        if(localeString == null) {
            logger.warn(AdventureUtil.serialize("Unable to load locale configuration as no locale name is configured in settings.yml."));
            return;
        }

        Path path = Path.of(plugin.getDataFolder() + File.separator + "locale" + File.separator + (localeString + ".yml"));
        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            locale = loader.load().get(Locale.class);

            checkVersion(localeString);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check the version of locale file and display warnings if outdated.
     */
    private void checkVersion(@NotNull String localeString) {
        if(locale == null) return;
        ComponentLogger logger = plugin.getComponentLogger();

        if(locale.configVersion() == null) {
            logger.warn(AdventureUtil.serialize("Unable to check locale version as it is not configured."));
            return;
        }

        if(!locale.configVersion().equals("1.1.0.0")) {
            logger.warn(AdventureUtil.serialize("Your plugin locale is outdated. Current version: " + locale.configVersion() + ". Latest version: 1.1.0.0."));
            logger.warn(AdventureUtil.serialize("You should regenerate your " + localeString + ".yml or migrate your " + localeString + ".yml to the new version."));
            logger.warn(AdventureUtil.serialize("The default config will be used until you fix your locale configuration."));

            locale = null;
        }
    }

    /**
     * Copies the plugin's default locales bundled with the plugin.
     */
    private void copyDefaultLocales() {
        Path path = Path.of(plugin.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if (!path.toFile().exists()) {
            plugin.saveResource("locale" + File.separator + "en_US.yml", false);
        }
    }
}
