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
import com.github.lukesky19.skyHoppers.data.config.gui.GUIConfig;
import com.github.lukesky19.skyHoppers.gui.SkyHopperGUI;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.GUIManager;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import com.google.common.collect.ImmutableList;
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
 * This class lets Players add an Online Player to the SkyHopper's member list.
 */
public class SelectPlayerGUI extends SkyHopperGUI {
    private final @NotNull HopperManager hopperManager;

    private final @NotNull SkyHopper skyHopper;

    private final @NotNull MembersGUI membersGUI;

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
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param hopperManager A {@link HopperManager} instance.
     * @param membersGUI The {@link MembersGUI} the player came from.
     */
    public SelectPlayerGUI(
            @NotNull SkyHoppers skyHoppers,
            @NotNull GUIManager guiManager,
            @NotNull Location location,
            @NotNull SkyHopper skyHopper,
            @NotNull Player player,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull HopperManager hopperManager,
            @NotNull MembersGUI membersGUI) {
        super(skyHoppers, guiManager, player, location);

        this.hopperManager = hopperManager;

        this.skyHopper = skyHopper;

        this.membersGUI = membersGUI;

        guiConfig = guiConfigManager.getGuiConfig("select_player.yml");
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
            logger.warn(AdventureUtil.serialize("Unable to decorate the GUI due to invalid configuration for the select player GUI."));
            if(isOpen) close();
            return false;
        }

        if(inventoryView == null) {
            logger.warn(AdventureUtil.serialize("Unable to update the select player GUI as the InventoryView was not created."));
            if(isOpen) close();
            return false;
        }

        clearButtons();

        int guiSize = inventoryView.getTopInventory().getSize();
        int playerCount = skyHoppers.getServer().getOnlinePlayers().size() - 1;

        createFiller(guiSize);
        // Dummy Buttons
        createDummyButtons();
        createPlayerButtons(guiSize, playerCount);
        createNextPageButton(guiSize, playerCount);
        createPreviousPageButton(guiSize);
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
     * Close the current GUI and open the {@link MembersGUI} that the player came from.
     */
    @Override
    public void close() {
        super.close();

        membersGUI.update();

        membersGUI.open();
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

        membersGUI.update();

        membersGUI.open();
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
     * Creates the buttons for all Players who can be added to this {@link SkyHopper} as a member.
     * @param guiSize The size of this GUI.
     * @param playerCount The number of players on the server.
     */
    private void createPlayerButtons(int guiSize, int playerCount) {
        List<Player> onlinePlayers = ImmutableList.copyOf(skyHoppers.getServer().getOnlinePlayers());
        List<UUID> skyHopperMembers = skyHopper.getMembers();

        assert guiConfig != null;
        ItemStackConfig itemStackConfig = guiConfig.entries().playerHead().item();

        if(guiSize - 10 >= 17) {
            for (int i = 0; i <= guiSize - 10; i++) {
                if (playerCount >= playerNum) {
                    UUID onlinePlayerId = onlinePlayers.get(playerNum).getUniqueId();

                    if(skyHopperMembers.contains(uuid) || (skyHopper.getOwner() != null && skyHopper.getOwner().equals(uuid))) {
                        i--;
                        playerNum++;
                        continue;
                    }

                    PlayerProfile profile = PlayerUtil.getCachedPlayerProfile(uuid);
                    String playerName = "<red><bold>Player Name Not Found</bold></red>";

                    if(profile == null) {
                        OfflinePlayer offlinePlayer = skyHoppers.getServer().getOfflinePlayer(onlinePlayerId);
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
                            skyHopper.addMember(onlinePlayerId);

                            hopperManager.saveSkyHopperToPDC(skyHopper);

                            guiManager.refreshViewersGUI(location);

                            added = 0;
                            playerNum = 0;

                            update();
                        });

                        setButton(i, buttonBuilder.build());

                        added++;
                    }

                    playerNum++;
                }
            }
        }
    }

    /**
     * Creates the next page button if needed
     */
    private void createNextPageButton(int guiSize, int playerCount) {
        if(added > guiSize - 10 && (playerNum - 1) < playerCount) {
            assert guiConfig != null;
            ButtonConfig buttonConfig = guiConfig.entries().nextPage();

            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.serialize("Unable to create the next page button in the select player gui due to no slot configured."));
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
                logger.warn(AdventureUtil.serialize("Unable to create the previous page button in the select player gui due to no slot configured."));
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
     * Creates the Exit button.
     */
    private void createExitButton() {
        assert guiConfig != null;
        ButtonConfig buttonConfig = guiConfig.entries().exit();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the exit button in the select player gui due to no slot configured."));
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
                logger.warn(AdventureUtil.serialize("Unable to add a dummy button to the select player GUI due to an invalid slot."));
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
