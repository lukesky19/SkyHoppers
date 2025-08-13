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
package com.github.lukesky19.skyHoppers.gui.menu.member;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.data.config.gui.ButtonConfig;
import com.github.lukesky19.skyHoppers.manager.GUIConfigManager;
import com.github.lukesky19.skyHoppers.manager.LocaleManager;
import com.github.lukesky19.skyHoppers.data.config.Locale;
import com.github.lukesky19.skyHoppers.data.config.gui.GUIConfig;
import com.github.lukesky19.skyHoppers.gui.SkyHopperGUI;
import com.github.lukesky19.skyHoppers.gui.menu.HopperGUI;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.GUIManager;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This class lets Players manage the players who can access this SkyHopper.
 */
public class MembersGUI extends SkyHopperGUI {
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull HopperManager hopperManager;

    private final @NotNull SkyHopper skyHopper;

    private final @NotNull HopperGUI hopperGUI;

    private final @Nullable GUIConfig guiConfig;

    private int playerNum = 0;
    private int added = 0;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param location The {@link Location} of the {@link SkyHopper}.
     * @param skyHopper The {@link SkyHopper} the GUI is associated with.
     * @param player The {@link Player} viewing the GUI.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param hopperManager A {@link HopperManager} instance.
     * @param hopperGUI The {@link HopperGUI} the player came from.
     */
    public MembersGUI(
            @NotNull SkyHoppers skyHoppers,
            @NotNull GUIManager guiManager,
            @NotNull Location location,
            @NotNull SkyHopper skyHopper,
            @NotNull Player player,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull HopperManager hopperManager,
            @NotNull HopperGUI hopperGUI) {
        super(skyHoppers, guiManager, player, location);

        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.hopperManager = hopperManager;

        this.skyHopper = skyHopper;

        this.hopperGUI = hopperGUI;

        guiConfig = guiConfigManager.getGuiConfig("members.yml");
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(guiConfig == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the members.yml GUI due to invalid GUI configuration."));
            return false;
        }

        GUIType guiType = guiConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the members.yml GUI due to an invalid GUIType"));
            return false;
        }

        String guiName = guiConfig.name();
        if(guiName == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the members.yml GUI due to an invalid gui name."));
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
            logger.warn(AdventureUtil.serialize("Unable to decorate the GUI due to invalid configuration for the members GUI."));
            if(isOpen) close();
            return false;
        }

        if(inventoryView == null) {
            logger.warn(AdventureUtil.serialize("Unable to update the members GUI as the InventoryView was not created."));
            if(isOpen) close();
            return false;
        }

        clearButtons();

        int guiSize = inventoryView.getTopInventory().getSize();
        int membersCount = skyHopper.getMembers().size() - 1;

        createFiller(guiSize);
        // Dummy Buttons
        createDummyButtons();
        createMemberButtons(guiSize, membersCount);
        createNextPageButton(guiSize, membersCount);
        createPreviousPageButton(guiSize);
        createAddMemberButton();
        createExitButton();

        return super.update();
    }

    /**
     * Refreshes the buttons in the GUI.
     */
    @Override
    public boolean refresh() {
        playerNum = 0;
        added = 0;

        return update();
    }

    /**
     * Close the current GUI and open the {@link HopperGUI} that the player came from.
     */
    @Override
    public void close() {
        super.close();

        hopperGUI.update();

        hopperGUI.open();
    }

    /**
     * Handles when the player closes the GUI.
     * @param inventoryCloseEvent An InventoryCloseEvent
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        guiManager.removeViewer(location, uuid);

        isOpen = false;

        hopperGUI.update();

        hopperGUI.open();
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
     * Creates the buttons for all Players who can access this {@link SkyHopper}.
     * @param guiSize The size of this GUI.
     * @param membersCount The number of members who can access this {@link SkyHopper}.
     */
    private void createMemberButtons(int guiSize, int membersCount) {
        List<UUID> membersList = skyHopper.getMembers();

        assert guiConfig != null;
        ItemStackConfig itemStackConfig = guiConfig.entries().playerHead().item();

        if(guiSize - 10 >= 17) {
            for (int i = 0; i <= guiSize - 10; i++) {
                if(membersCount >= playerNum) {
                    UUID memberId = membersList.get(playerNum);
                    PlayerProfile profile = PlayerUtil.getCachedPlayerProfile(memberId);
                    String playerName = "<red><bold>Player Name Not Found</bold></red>";

                    if(profile == null) {
                        OfflinePlayer offlinePlayer = skyHoppers.getServer().getOfflinePlayer(memberId);
                        if(offlinePlayer.getName() != null) {
                            playerName = offlinePlayer.getName();
                        }
                    } else {
                        if(profile.getName() != null) {
                            playerName = profile.getName();
                        }
                    }

                    ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                    itemStackBuilder.setItemType(ItemType.PLAYER_HEAD);

                    if(itemStackConfig.name() != null) {
                        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("player_name", playerName));

                        itemStackBuilder.setName(AdventureUtil.serialize(itemStackConfig.name(), placeholders));
                    }

                    List<Component> lore = itemStackConfig.lore().stream().map(AdventureUtil::serialize).toList();
                    List<ItemFlag> itemFlags = itemStackConfig.itemFlags().stream().map(ItemFlag::valueOf).toList();

                    itemStackBuilder.setLore(lore);
                    itemFlags.forEach(itemStackBuilder::addItemFlag);

                    itemStackBuilder.setPlayer(player);

                    Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
                    if(optionalItemStack.isPresent()) {
                        GUIButton.Builder buttonBuilder = new GUIButton.Builder();

                        buttonBuilder.setItemStack(optionalItemStack.get());

                        buttonBuilder.setAction(inventoryClickEvent -> {
                            skyHopper.removeMember(memberId);

                            hopperManager.saveSkyHopperToPDC(skyHopper);

                            guiManager.refreshViewersGUI(location);

                            added = 0;
                            playerNum = 0;

                            update();
                        });

                        setButton(i, buttonBuilder.build());

                        added++;
                        playerNum++;
                    } else {
                        logger.warn(AdventureUtil.serialize("Failed to create the ItemStack for a member button."));
                    }
                }
            }
        }
    }

    /**
     * Creates the next page button if needed
     */
    private void createNextPageButton(int guiSize, int membersCount) {
        if(added > guiSize - 10 && (playerNum - 1) < membersCount) {
            assert guiConfig != null;
            ButtonConfig buttonConfig = guiConfig.entries().nextPage();

            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.serialize("Unable to create the next page button in the members gui due to no slot configured."));
                return;
            }

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
            Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

            if(optionalItemStack.isPresent()) {
                GUIButton.Builder builder = new GUIButton.Builder();

                builder.setItemStack(optionalItemStack.get());

                builder.setAction(event -> {
                    added = 0;
                    update();
                });

                setButton(buttonConfig.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the previous page button if needed
     */
    private void createPreviousPageButton(int guiSize) {
        if(playerNum > guiSize - 9) {
            assert guiConfig != null;
            ButtonConfig buttonConfig = guiConfig.entries().previousPage();
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.serialize("Unable to create the previous page button in the members gui due to no slot configured."));
                return;
            }

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
            Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

            if(optionalItemStack.isPresent()) {
                GUIButton.Builder builder = new GUIButton.Builder();

                builder.setItemStack(optionalItemStack.get());

                builder.setAction(event -> {
                    if (playerNum > (guiSize - 9) + added) {
                        playerNum -= (guiSize - 9) + added;
                    } else {
                        playerNum -= added;
                    }

                    added = 0;

                    update();
                });

                setButton(buttonConfig.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the button to add a Player to access this SkyHopper.
     */
    private void createAddMemberButton() {
        Locale locale = localeManager.getLocale();

        assert guiConfig != null;
        ButtonConfig buttonConfig = guiConfig.entries().add();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the add member button in the members gui due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder buttonBuilder = new GUIButton.Builder();

            buttonBuilder.setItemStack(optionalItemStack.get());

            buttonBuilder.setAction(event -> {
                skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () ->
                        player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                guiManager.removeViewer(location, player.getUniqueId());

                SelectPlayerGUI selectPlayerGUI = new SelectPlayerGUI(skyHoppers, guiManager, location, skyHopper, player, guiConfigManager, hopperManager, this);

                boolean creationResult = selectPlayerGUI.create();
                if(!creationResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean updateResult = selectPlayerGUI.update();
                if(!updateResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean openResult = selectPlayerGUI.open();
                if(!openResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                }
            });

            setButton(buttonConfig.slot(), buttonBuilder.build());
        }
    }

    /**
     * Creates the Exit button.
     */
    private void createExitButton() {
        assert guiConfig != null;
        ButtonConfig buttonConfig = guiConfig.entries().exit();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the exit button in the members gui due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event -> close());

            setButton(buttonConfig.slot(), builder.build());
        }
    }

    /**
     * Create the dummy buttons for the GUI.
     */
    private void createDummyButtons() {
        if(guiConfig == null) return;

        guiConfig.entries().dummyButtons().forEach(buttonConfig -> {
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.serialize("Unable to add a dummy button to the members GUI due to an invalid slot."));
                return;
            }

            ItemStackConfig itemStackConfig = buttonConfig.item();
            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(itemStackConfig, player, null, List.of());
            Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            optionalItemStack.ifPresent(itemStack -> {
                GUIButton.Builder builder = new GUIButton.Builder();

                builder.setItemStack(itemStack);

                setButton(buttonConfig.slot(), builder.build());
            });
        });
    }
}
