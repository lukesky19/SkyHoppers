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
package com.github.lukesky19.skyHoppers.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.manager.GUIManager;
import com.github.lukesky19.skyHoppers.config.record.gui.GUIConfig;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.gui.GUIType;
import com.github.lukesky19.skylib.gui.GUIButton;
import com.github.lukesky19.skylib.gui.abstracts.ChestGUI;
import com.github.lukesky19.skylib.player.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * This class lets Players manage the players who can access this SkyHopper.
 */
public class MembersGUI extends ChestGUI {
    private final SkyHoppers plugin;
    private final GUIManager guiManager;
    private final HopperManager hopperManager;
    private final HopperGUI hopperGUI;
    private final Location location;
    private final GUIConfig guiConfig;
    private final GUIType guiType;

    private int playerNum = 0;
    private int added = 0;

    /**
     * Constructor
     * @param plugin The SkyHoppers Plugin
     * @param guiManager A GUIManager instance.
     * @param hopperManager A HopperManager instance.
     * @param hopperGUI The HopperGUI the Player came from.
     * @param location The Location of the SkyHopper.
     * @param player The Player viewing the GUI.
     */
    public MembersGUI(
            SkyHoppers plugin,
            GUIManager guiManager,
            HopperManager hopperManager,
            HopperGUI hopperGUI,
            Location location,
            Player player) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.hopperManager = hopperManager;
        this.hopperGUI = hopperGUI;
        this.location = location;

        guiConfig = guiManager.getGuiConfig("members.yml");
        if(guiConfig == null) {
            throw new RuntimeException("Unable to find loaded config file members.yml.");
        }

        guiType = GUIType.getType(guiConfig.guiType());
        if(guiType == null) {
            throw new RuntimeException("Unknown GUIType " + guiConfig.guiType() + " in members.yml.");
        }

        if(guiConfig.name() == null) {
            throw new RuntimeException("GUI name is null in members.yml.");
        }

        create(player, guiType, guiConfig.name(), null);

        update();
    }

    /**
     * Opens this GUI for the Player.
     * @param plugin The SkyHoppers Plugin.
     * @param player The Player to open the GUI for.
     */
    @Override
    public void open(@NotNull Plugin plugin, @NotNull Player player) {
        super.open(plugin, player);

        hopperManager.addViewer(location, player.getUniqueId(), this);
    }

    /**
     * Creates and populates the buttons for this GUI.
     */
    @Override
    public void update() {
        clearButtons();

        SkyHopper skyHopper = hopperManager.getSkyHopper(location);
        if(skyHopper == null) return;

        int guiSize = guiType.getSize();
        int membersCount = skyHopper.members().size() - 1;

        createFiller();
        createMemberButtons(skyHopper, guiSize, membersCount);
        createNextPageButton(guiSize, membersCount);
        createPreviousPageButton(guiSize);
        createAddMemberButton();
        createExitButton();

        super.update();
    }

    /**
     * Refreshes the buttons in the GUI.
     */
    @Override
    public void refresh() {
        playerNum = 0;
        added = 0;

        update();
    }

    /**
     * Closes the current GUI and re-opens the HopperGUI.
     * @param plugin The SkyHoppers plugin.
     * @param player The Player to close the GUI for.
     */
    @Override
    public void close(@NotNull Plugin plugin, @NotNull Player player) {
        UUID uuid = player.getUniqueId();

        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

        hopperManager.removeViewer(location, uuid);

        hopperGUI.update();

        hopperGUI.open(plugin, player);
    }

    /**
     * Completely closes the GUI and doesn't re-open the HopperGUI.
     * @param plugin The SkyHoppers Plugin.
     * @param player The Player to unload the GUI for.
     * @param onDisable Whether the unload is occurring on plugin disable or not.
     */
    @Override
    public void unload(@NotNull Plugin plugin, @NotNull Player player, boolean onDisable) {
        if(onDisable) {
            player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);
        } else {
            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                    player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);
        }

        hopperManager.removeViewer(location, player.getUniqueId());
    }

    /**
     * Handles when the player closes the GUI.
     * @param inventoryCloseEvent An InventoryCloseEvent
     */
    @Override
    public void handleClose(InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        Player player = (Player) inventoryCloseEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        hopperManager.removeViewer(location, uuid);

        hopperGUI.update();

        hopperGUI.open(plugin, player);

        hopperManager.addViewer(location, uuid, hopperGUI);
    }

    /**
     * Creates all the Filler buttons.
     */
    private void createFiller() {
        GUIConfig.Filler filler = guiConfig.entries().filler();
        if(filler.item().material() != null) {
            Material fillerMaterial = Material.getMaterial(filler.item().material());

            if (fillerMaterial != null) {
                List<Component> lore = filler.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = filler.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(fillerMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = filler.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);

                builder.setItemStack(itemStack);

                GUIButton button = builder.build();

                for (int i = 0; i <= guiType.getSize() - 1; i++) {
                    setButton(i, button);
                }
            }
        }
    }

    /**
     * Creates the buttons for all Players who can access this SkyHopper.
     * @param skyHopper The SkyHopper associated with this GUI.
     * @param guiSize The size of this GUI.
     * @param membersCount The number of members who can access this SkyHopper.
     */
    private void createMemberButtons(@NotNull SkyHopper skyHopper, int guiSize, int membersCount) {
        List<UUID> membersList = skyHopper.members();
        GUIConfig.Item itemConfig = guiConfig.entries().playerHead().item();

        if(guiSize - 10 >= 17) {
            for (int i = 0; i <= guiSize - 10; i++) {
                if (membersCount >= playerNum) {
                    UUID uuid = membersList.get(playerNum);
                    PlayerProfile profile = PlayerUtil.getCachedPlayerProfile(uuid);
                    String playerName = "<red><bold>Player Name Not Found</bold></red>";

                    if (profile == null) {
                        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(uuid);
                        if (offlinePlayer.getName() != null) {
                            playerName = offlinePlayer.getName();
                        }
                    } else {
                        if (profile.getName() != null) {
                            playerName = profile.getName();
                        }
                    }

                    GUIButton.Builder builder = new GUIButton.Builder();

                    ItemStack itemStack = ItemStack.of(Material.PLAYER_HEAD);
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("player_name", playerName));

                    if(itemConfig.name() != null) {
                        itemMeta.displayName(FormatUtil.format(itemConfig.name(), placeholders));
                    }

                    List<Component> lore = itemConfig.lore().stream().map(FormatUtil::format).toList();
                    List<ItemFlag> itemFlags = itemConfig.itemFlags().stream().map(ItemFlag::valueOf).toList();

                    itemMeta.lore(lore);
                    itemFlags.forEach(itemMeta::addItemFlags);

                    if (profile != null) {
                        SkullMeta skullMeta = (SkullMeta) itemMeta;
                        skullMeta.setPlayerProfile(profile);

                        itemStack.setItemMeta(skullMeta);
                    } else {
                        itemStack.setItemMeta(itemMeta);
                    }

                    builder.setItemStack(itemStack);

                    builder.setAction(event -> {
                        SkyHopper currentSkyHopper = hopperManager.getSkyHopper(location);
                        if (currentSkyHopper == null) return;

                        if (currentSkyHopper.location() != null && currentSkyHopper.location().getBlock().getState(false) instanceof Hopper hopper) {
                            List<UUID> members = currentSkyHopper.members();
                            members.removeIf(listUUID -> listUUID.equals(uuid));

                            SkyHopper updatedSkyHopper = new SkyHopper(
                                    currentSkyHopper.enabled(),
                                    currentSkyHopper.particles(),
                                    currentSkyHopper.owner(),
                                    members,
                                    currentSkyHopper.location(),
                                    currentSkyHopper.containers(),
                                    currentSkyHopper.filterType(),
                                    currentSkyHopper.filterItems(),
                                    currentSkyHopper.transferSpeed(),
                                    currentSkyHopper.maxTransferSpeed(),
                                    currentSkyHopper.transferAmount(),
                                    currentSkyHopper.maxTransferAmount(),
                                    currentSkyHopper.suctionSpeed(),
                                    currentSkyHopper.maxSuctionSpeed(),
                                    currentSkyHopper.suctionAmount(),
                                    currentSkyHopper.maxSuctionAmount(),
                                    currentSkyHopper.suctionRange(),
                                    currentSkyHopper.maxSuctionRange(),
                                    currentSkyHopper.maxContainers(),
                                    currentSkyHopper.nextSuctionTime(),
                                    currentSkyHopper.nextTransferTime());

                            hopperManager.saveSkyHopperToBlockPDC(updatedSkyHopper, hopper);

                            hopperManager.cacheSkyHopper(location, updatedSkyHopper);

                            hopperManager.refreshViewersGUI(location);

                            added = 0;
                            playerNum = 0;

                            update();
                        }
                    });

                    setButton(i, builder.build());

                    added++;
                    playerNum++;
                }
            }
        }
    }

    /**
     * Creates the next page button if needed
     */
    private void createNextPageButton(int guiSize, int membersCount) {
        if(added > guiSize - 10 && (playerNum - 1) < membersCount) {
            GUIConfig.GenericEntry nextPage = guiConfig.entries().nextPage();

            if (nextPage.item().material() != null) {
                Material nextPageMaterial = Material.getMaterial(nextPage.item().material());
                List<Component> lore = nextPage.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = nextPage.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                if (nextPageMaterial != null) {
                    GUIButton.Builder builder = new GUIButton.Builder();

                    ItemStack itemStack = ItemStack.of(nextPageMaterial);
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    String name = nextPage.item().name();
                    if (name != null) {
                        itemMeta.displayName(FormatUtil.format(name));
                    }

                    itemMeta.lore(lore);
                    itemFlags.forEach(itemMeta::addItemFlags);

                    itemStack.setItemMeta(itemMeta);

                    builder.setItemStack(itemStack);

                    builder.setAction(event -> {
                        added = 0;
                        update();
                    });

                    setButton(nextPage.slot(), builder.build());
                }
            }
        }
    }

    /**
     * Creates the previous page button if needed
     */
    private void createPreviousPageButton(int guiSize) {
        if(playerNum > guiSize - 9) {
            GUIConfig.GenericEntry previousPage = guiConfig.entries().previousPage();

            if (previousPage.item().material() != null) {
                Material previousPageMaterial = Material.getMaterial(previousPage.item().material());
                List<Component> lore = previousPage.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = previousPage.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                if (previousPageMaterial != null) {
                    GUIButton.Builder builder = new GUIButton.Builder();

                    ItemStack itemStack = ItemStack.of(previousPageMaterial);
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    String name = previousPage.item().name();
                    if (name != null) {
                        itemMeta.displayName(FormatUtil.format(name));
                    }

                    itemMeta.lore(lore);
                    itemFlags.forEach(itemMeta::addItemFlags);

                    itemStack.setItemMeta(itemMeta);

                    builder.setItemStack(itemStack);

                    builder.setAction(event -> {
                        if (playerNum > (guiSize - 9) + added) {
                            playerNum -= (guiSize - 9) + added;
                        } else {
                            playerNum -= added;
                        }

                        added = 0;

                        update();
                    });

                    setButton(previousPage.slot(), builder.build());
                }
            }
        }
    }

    /**
     * Creates the button to add a Player to access this SkyHopper.
     */
    private void createAddMemberButton() {
        GUIConfig.GenericEntry add = guiConfig.entries().add();

        if(add.item().material() != null) {
            Material addMaterial = Material.getMaterial(add.item().material());

            if (addMaterial != null) {
                List<Component> lore = add.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = add.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(addMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = add.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);

                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);

                builder.setItemStack(itemStack);

                builder.setAction(event -> {
                    Player player = (Player) event.getWhoClicked();

                    plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                    hopperManager.removeViewer(location, player.getUniqueId());

                    new SelectPlayerGUI(plugin, guiManager, hopperManager, this, location, player).open(plugin, player);
                });

                setButton(add.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the Exit button.
     */
    private void createExitButton() {
        GUIConfig.GenericEntry exit = guiConfig.entries().exit();
        if (exit.item().material() != null) {
            Material exitMaterial = Material.getMaterial(exit.item().material());

            if (exitMaterial != null) {
                List<Component> lore = exit.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = exit.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(exitMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = exit.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);

                builder.setItemStack(itemStack);

                builder.setAction(event -> {
                    Player player = (Player) event.getWhoClicked();

                    close(plugin, player);
                });

                setButton(exit.slot(), builder.build());
            }
        }
    }
}
