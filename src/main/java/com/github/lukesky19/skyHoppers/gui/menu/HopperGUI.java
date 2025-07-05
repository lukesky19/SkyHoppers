/*
    SkyHoppers adds upgradable hoppers that can suction items, transfer items wirelessly to linked containers.
    Copyright (C) 2025 lukeskywlker19

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
package com.github.lukesky19.skyHoppers.gui.menu;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.manager.GUIConfigManager;
import com.github.lukesky19.skyHoppers.config.manager.LocaleManager;
import com.github.lukesky19.skyHoppers.config.manager.SettingsManager;
import com.github.lukesky19.skyHoppers.config.record.Locale;
import com.github.lukesky19.skyHoppers.config.record.gui.GUIConfig;
import com.github.lukesky19.skyHoppers.gui.SkyHopperGUI;
import com.github.lukesky19.skyHoppers.gui.menu.filter.InputFilterGUI;
import com.github.lukesky19.skyHoppers.gui.menu.links.LinksGUI;
import com.github.lukesky19.skyHoppers.gui.menu.member.MembersGUI;
import com.github.lukesky19.skyHoppers.gui.menu.upgrades.UpgradesGUI;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.listener.HopperClickListener;
import com.github.lukesky19.skyHoppers.manager.GUIManager;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skyHoppers.task.HopperViewTask;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This class lets Players manage a SkyHopper's settings.
 */
public class HopperGUI extends SkyHopperGUI {
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull HopperManager hopperManager;
    private final @NotNull HopperClickListener hopperClickListener;

    private final @NotNull SkyHopper skyHopper;

    private final @Nullable GUIConfig guiConfig;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param location The {@link Location} of the {@link SkyHopper}.
     * @param skyHopper The {@link SkyHopper} the GUI is associated with.
     * @param player The {@link Player} viewing the GUI.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param hopperManager A {@link HopperManager} instance.
     * @param hopperClickListener A {@link HopperClickListener} instance.
     */
    public HopperGUI(
            @NotNull SkyHoppers skyHoppers,
            @NotNull GUIManager guiManager,
            @NotNull Location location,
            @NotNull SkyHopper skyHopper,
            @NotNull Player player,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull HopperManager hopperManager,
            @NotNull HopperClickListener hopperClickListener) {
        super(skyHoppers, guiManager, player, location);
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.hopperManager = hopperManager;
        this.hopperClickListener = hopperClickListener;

        this.skyHopper = skyHopper;

        guiConfig = guiConfigManager.getGuiConfig("hopper.yml");
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(guiConfig == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the hopper.yml GUI due to invalid GUI configuration."));
            return false;
        }

        GUIType guiType = guiConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the hopper.yml GUI due to an invalid GUIType"));
            return false;
        }

        String guiName = guiConfig.name();
        if(guiName == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the hopper.yml GUI due to an invalid gui name."));
            return false;
        }

        return create(guiType, guiName, List.of());
    }

    /**
     * Creates and populates the buttons for this GUI.
     */
    @Override
    public boolean update() {
        if(guiConfig == null) {
            logger.warn(AdventureUtil.serialize("Unable to decorate the GUI due to invalid configuration for the hopper.yml GUI."));
            return false;
        }

        clearButtons();

        if(inventoryView == null) {
            logger.warn(AdventureUtil.serialize("Unable to update the main hopper GUI as the InventoryView was not created."));
            if(isOpen) close();
            return false;
        }

        // GUI Size
        int guiSize = inventoryView.getTopInventory().getSize();

        // Filler
        createFiller(guiSize);

        // SkyHopper Status Enabled/Disabled Buttons
        if(skyHopper.isSkyHopperEnabled()) {
            createStatusEnabledButton();
        } else {
            createStatusDisabledButton();
        }

        // Particles Enabled/Disabled Buttons
        if(skyHopper.isParticlesEnabled()) {
            createParticlesEnabledButton();
        } else {
            createParticlesDisabledButton();
        }

        // Linked Containers Button
        createLinkedContainersButton();

        // Input Filter Button
        createInputFilterButton();

        // Upgrades Button
        createUpgradesButton();

        // Members
        createMembersButton();

        // Visualize
        createVisualizeButton();

        // Exit Button
        createExitButton();

        // Info Button
        createInfoButton();

        return super.update();
    }

    /**
     * Refreshes the buttons in the GUI.
     */
    @Override
    public boolean refresh() {
        return this.update();
    }

    /**
     * Handles when the player closes the GUI.
     * @param inventoryCloseEvent An InventoryCloseEvent
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        Player player = (Player) inventoryCloseEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        guiManager.removeViewer(location, uuid);

        this.isOpen = false;
    }

    /**
     * Handles when items are dragged across the bottom (player's) inventory.
     * This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}.
     */
    @Override
    public void handleBottomDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handles when items are dragged across the top or bottom inventory.
     * This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}.
     */
    @Override
    public void handleGlobalDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handles when a slot is clicked in the bottom (player's) inventory.
     * This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @Override
    public void handleBottomClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Handles when a slot is clicked in the top or bottom inventory.
     * This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @Override
    public void handleGlobalClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Creates all the Filler buttons.
     */
    private void createFiller(int guiSize) {
        assert guiConfig != null;
        ItemStackConfig filler = guiConfig.entries().filler();

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(filler, null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            GUIButton button = builder.build();

            for(int i = 0; i <= guiSize - 1; i++) {
                setButton(i, button);
            }
        }
    }

    /**
     * Creates the Status Enabled Button.
     */
    private void createStatusEnabledButton() {
        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().hopperEnabled();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the status enabled button due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event -> {
                skyHopper.toggleEnabled();

                hopperManager.saveSkyHopperToPDC(skyHopper);

                guiManager.refreshViewersGUI(location);

                update();
            });

            setButton(buttonConfig.slot(), builder.build());
        }
    }

    /**
     * Creates the Status Disabled Button.
     */
    private void createStatusDisabledButton() {
        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().hopperDisabled();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the status disabled button due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event -> {
                skyHopper.toggleEnabled();

                hopperManager.saveSkyHopperToPDC(skyHopper);

                guiManager.refreshViewersGUI(location);

                update();
            });

            setButton(buttonConfig.slot(), builder.build());
        }
    }

    /**
     * Creates the Particles Enabled Button.
     */
    private void createParticlesEnabledButton() {
        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().particlesEnabled();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the particles enabled button due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event -> {
                skyHopper.toggleParticles();

                hopperManager.saveSkyHopperToPDC(skyHopper);

                guiManager.refreshViewersGUI(location);

                update();
            });

            setButton(buttonConfig.slot(), builder.build());
        }
    }

    /**
     * Creates the Particles Disabled Button.
     */
    private void createParticlesDisabledButton() {
        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().particlesDisabled();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the particles disabled button due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event -> {
                skyHopper.toggleParticles();

                hopperManager.saveSkyHopperToPDC(skyHopper);

                guiManager.refreshViewersGUI(location);

                update();
            });

            setButton(buttonConfig.slot(), builder.build());
        }
    }

    /**
     * Creates the Linked Containers Button.
     */
    private void createLinkedContainersButton() {
        Locale locale = localeManager.getLocale();

        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().link();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the linked containers button due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event -> {
                skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () ->
                        player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                guiManager.removeViewer(location, player.getUniqueId());

                LinksGUI linksGUI = new LinksGUI(skyHoppers, guiManager, location, skyHopper, player, localeManager, guiConfigManager, hopperManager, hopperClickListener, this);

                boolean creationResult = linksGUI.create();
                if(!creationResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean updateResult = linksGUI.update();
                if(!updateResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean openResult = linksGUI.open();
                if(!openResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                }
            });

            setButton(buttonConfig.slot(), builder.build());
        }
    }

    /**
     * Creates the Input Filter Button.
     */
    private void createInputFilterButton() {
        Locale locale = localeManager.getLocale();
        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().filter();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the input filter button due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event -> {
                skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () ->
                        player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                InputFilterGUI inputFilterGUI = new InputFilterGUI(skyHoppers, guiManager, location, skyHopper, player, guiConfigManager, hopperManager, this);

                boolean creationResult = inputFilterGUI.create();
                if(!creationResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean updateResult = inputFilterGUI.update();
                if(!updateResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean openResult = inputFilterGUI.open();
                if(!openResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                }
            });

            setButton(buttonConfig.slot(), builder.build());
        }
    }

    /**
     * Creates the Upgrades button.
     */
    private void createUpgradesButton() {
        Locale locale = localeManager.getLocale();

        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().upgrades();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the upgrades button due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event -> {
                Bukkit.getScheduler().runTaskLater(skyHoppers, () ->
                        player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                guiManager.removeViewer(location, player.getUniqueId());

                UpgradesGUI upgradesGUI = new UpgradesGUI(skyHoppers, guiManager, location, skyHopper, player, settingsManager, localeManager, guiConfigManager, hopperManager, this);

                boolean creationResult = upgradesGUI.create();
                if(!creationResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean updateResult = upgradesGUI.update();
                if(!updateResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean openResult = upgradesGUI.open();
                if(!openResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                }
            });

            setButton(buttonConfig.slot(), builder.build());
        }
    }

    /**
     * Creates the Members button.
     */
    private void createMembersButton() {
        Locale locale = localeManager.getLocale();

        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().members();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the members button due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event -> {
                skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () ->
                        player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                guiManager.removeViewer(location, player.getUniqueId());

                MembersGUI membersGUI = new MembersGUI(skyHoppers, guiManager, location, skyHopper, player, localeManager, guiConfigManager, hopperManager, this);

                boolean creationResult = membersGUI.create();
                if(!creationResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean updateResult = membersGUI.update();
                if(!updateResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean openResult = membersGUI.open();
                if(!openResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                }
            });

            setButton(buttonConfig.slot(), builder.build());
        }
    }

    /**
     * Creates the visualize button.
     */
    private void createVisualizeButton() {
        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().visualize();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the members button due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event -> {
                close();

                new HopperViewTask(hopperManager, location, player).runTaskTimerAsynchronously(skyHoppers, 0L, 1L);
            });

            setButton(buttonConfig.slot(), builder.build());
        }
    }

    /**
     * Creates the Exit button.
     */
    private void createExitButton() {
        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().exit();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the exit button due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event -> {
                skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () ->
                        player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);

                guiManager.removeViewer(location, player.getUniqueId());
            });

            setButton(buttonConfig.slot(), builder.build());
        }
    }

    /**
     * Creates the info button.
     */
    private void createInfoButton() {
        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().info();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the info button due to no slot configured."));
            return;
        }

        if(skyHopper.getOwner() != null) {
            String playerName = skyHoppers.getServer().getOfflinePlayer(skyHopper.getOwner()).getName();
            if(playerName != null) {
                List<TagResolver.Single> lorePlaceholders = List.of(
                        Placeholder.parsed("status", String.valueOf(skyHopper.isSkyHopperEnabled())),
                        Placeholder.parsed("owner", playerName),
                        Placeholder.parsed("member_count", String.valueOf(skyHopper.getMembers().size())),
                        Placeholder.parsed("filter_type", skyHopper.getFilterType().name()),
                        Placeholder.parsed("links_count", String.valueOf(skyHopper.getLinkedContainers().size())),
                        Placeholder.parsed("links_amount", String.valueOf(skyHopper.getMaxContainers())),
                        Placeholder.parsed("transfer_amount", String.valueOf(skyHopper.getTransferAmount())),
                        Placeholder.parsed("transfer_speed", String.valueOf(skyHopper.getTransferSpeed())),
                        Placeholder.parsed("suction_amount", String.valueOf(skyHopper.getSuctionAmount())),
                        Placeholder.parsed("suction_speed", String.valueOf(skyHopper.getSuctionSpeed())),
                        Placeholder.parsed("suction_range", String.valueOf(skyHopper.getSuctionRange())));

                ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, lorePlaceholders);
                Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

                if(optionalItemStack.isPresent()) {
                    GUIButton.Builder builder = new GUIButton.Builder();

                    builder.setItemStack(optionalItemStack.get());

                    setButton(buttonConfig.slot(), builder.build());
                }
            }
        }
    }
}
