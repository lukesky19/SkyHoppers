package com.github.lukesky19.skyHoppers.gui.upgrades;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.manager.GUIManager;
import com.github.lukesky19.skyHoppers.config.manager.LocaleManager;
import com.github.lukesky19.skyHoppers.config.manager.SettingsManager;
import com.github.lukesky19.skyHoppers.config.record.Locale;
import com.github.lukesky19.skyHoppers.config.record.Settings;
import com.github.lukesky19.skyHoppers.config.record.gui.upgrade.UpgradeGUIConfig;
import com.github.lukesky19.skyHoppers.gui.UpgradesGUI;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.gui.GUIType;
import com.github.lukesky19.skylib.gui.GUIButton;
import com.github.lukesky19.skylib.gui.abstracts.ChestGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class lets Players upgrade the range SkyHoppers can suction items.
 */
public class SuctionRangeUpgradeGUI extends ChestGUI {
    private final SkyHoppers plugin;
    private final SettingsManager settingsManager;
    private final LocaleManager localeManager;
    private final HopperManager hopperManager;
    private final UpgradesGUI upgradesGUI;
    private final Location location;
    private final UpgradeGUIConfig guiConfig;
    private final GUIType guiType;

    /**
     * Constructor
     * @param plugin The SkyHoppers Plugin.
     * @param settingsManager A SettingsManager instance.
     * @param localeManager A LocaleManager instance.
     * @param guiManager A GUIManager instance.
     * @param hopperManager A HopperManager instance.
     * @param upgradesGUI The UpgradesGUI the player cane from.
     * @param location The Location of the SkyHopper.
     * @param player The Player viewing this GUI.
     */
    public SuctionRangeUpgradeGUI(
            SkyHoppers plugin,
            SettingsManager settingsManager,
            LocaleManager localeManager,
            GUIManager guiManager,
            HopperManager hopperManager,
            UpgradesGUI upgradesGUI,
            Location location,
            Player player) {
        this.plugin = plugin;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.hopperManager = hopperManager;
        this.upgradesGUI = upgradesGUI;
        this.location = location;

        guiConfig = guiManager.getUpgradeConfig("suction_range.yml");
        if(guiConfig == null) {
            throw new RuntimeException("Unable to find loaded config file suction_range.yml.");
        }

        guiType = GUIType.getType(guiConfig.guiType());
        if(guiType == null) {
            throw new RuntimeException("Unknown GUIType " + guiConfig.guiType() + " in suction_range.yml");
        }

        if(guiConfig.name() == null) {
            throw new RuntimeException("GUI name is null in suction_range.yml.");
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

        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        TreeMap<Integer, Double> displayUpgrades = settingsManager.getSuctionRangeUpgrades();
        if(displayUpgrades == null) return;

        SkyHopper skyHopper = hopperManager.getSkyHopper(location);
        if(skyHopper == null) return;

        createFiller();
        createExitButton();
        createIncreaseButton(skyHopper, displayUpgrades);
        createDecreaseButton(skyHopper, displayUpgrades);
        createUpgradeButton(skyHopper, displayUpgrades);

        super.update();
    }

    /**
     * Refreshes the buttons in the GUI.
     */
    @Override
    public void refresh() {
        update();
    }

    /**
     * Closes the current GUI and re-opens the UpgradesGUI.
     * @param plugin The SkyHoppers plugin.
     * @param player The Player to close the GUI for.
     */
    @Override
    public void close(@NotNull Plugin plugin, @NotNull Player player) {
        UUID uuid = player.getUniqueId();

        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

        hopperManager.removeViewer(location, uuid);

        upgradesGUI.update();

        upgradesGUI.close(plugin, player);
    }

    /**
     * Completely closes the GUI and does not open the UpgradesGUI.
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
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        Player player = (Player) inventoryCloseEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        hopperManager.removeViewer(location, uuid);

        upgradesGUI.update();

        upgradesGUI.open(plugin, player);
    }

    /**
     * Creates all the Filler buttons.
     */
    private void createFiller() {
        UpgradeGUIConfig.Filler fillerConfig = guiConfig.entries().filler();
        int guiSize = guiType.getSize();

        GUIButton.Builder fillerBuilder = new GUIButton.Builder();

        if(fillerConfig.item().material() != null) {
            Material material = Material.getMaterial(fillerConfig.item().material());
            if (material != null) {
                ItemStack itemStack = ItemStack.of(material);
                ItemMeta itemMeta = itemStack.getItemMeta();

                if(fillerConfig.item().name() != null) {
                    itemMeta.displayName(FormatUtil.format(fillerConfig.item().name()));
                }

                List<Component> lore = fillerConfig.item().lore().stream().map(FormatUtil::format).toList();
                itemMeta.lore(lore);

                List<ItemFlag> flags = fillerConfig.item().itemFlags().stream().map(ItemFlag::valueOf).toList();
                flags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);

                fillerBuilder.setItemStack(itemStack);

                GUIButton button = fillerBuilder.build();

                for (int i = 0; i <= guiSize - 1; i++) {
                    setButton(i, button);
                }
            }
        }
    }

    /**
     * Creates the Exit button.
     */
    private void createExitButton() {
        UpgradeGUIConfig.GenericEntry exitConfig = guiConfig.entries().exit();

        GUIButton.Builder exitBuilder = new GUIButton.Builder();

        if(exitConfig.item().material() != null) {
            Material material = Material.getMaterial(exitConfig.item().material());
            if (material != null) {
                ItemStack itemStack = ItemStack.of(material);
                ItemMeta itemMeta = itemStack.getItemMeta();

                if(exitConfig.item().name() != null) {
                    itemMeta.displayName(FormatUtil.format(exitConfig.item().name()));
                }

                List<Component> lore = exitConfig.item().lore().stream().map(FormatUtil::format).toList();
                itemMeta.lore(lore);

                List<ItemFlag> flags = exitConfig.item().itemFlags().stream().map(ItemFlag::valueOf).toList();
                flags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);

                exitBuilder.setItemStack(itemStack);

                exitBuilder.setAction(event -> {
                    Player player = (Player) event.getWhoClicked();

                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                    upgradesGUI.update();

                    upgradesGUI.open(plugin, player);
                });

                setButton(exitConfig.slot(), exitBuilder.build());
            }
        }
    }

    /**
     * Creates the button to increase the suction range.
     */
    private void createIncreaseButton(@NotNull SkyHopper skyHopper, @NotNull TreeMap<Integer, Double> displayUpgrades) {
        GUIButton.Builder increaseBuilder = new GUIButton.Builder();

        if(skyHopper.suctionRange() != skyHopper.maxSuctionRange()) {
            UpgradeGUIConfig.GenericEntry increaseConfig = guiConfig.entries().increase();
            if (increaseConfig.item().material() != null) {
                Material material = Material.getMaterial(increaseConfig.item().material());
                if (material != null) {
                    ItemStack itemStack = ItemStack.of(material);
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    if (increaseConfig.item().name() != null) {
                        itemMeta.displayName(FormatUtil.format(increaseConfig.item().name()));
                    }

                    Optional<Map.Entry<Integer, Double>> displayUpgrade = getLargerRange(displayUpgrades, skyHopper.suctionRange());
                    if (displayUpgrade.isEmpty()) return;

                    List<TagResolver.Single> lorePlaceholders = List.of(
                            Placeholder.parsed("current", String.valueOf(skyHopper.suctionRange())),
                            Placeholder.parsed("change", String.valueOf(displayUpgrade.get().getKey())));

                    List<Component> lore = increaseConfig.item().lore().stream().map(line -> FormatUtil.format(line, lorePlaceholders)).toList();
                    itemMeta.lore(lore);

                    List<ItemFlag> flags = increaseConfig.item().itemFlags().stream().map(ItemFlag::valueOf).toList();
                    flags.forEach(itemMeta::addItemFlags);

                    itemStack.setItemMeta(itemMeta);

                    increaseBuilder.setItemStack(itemStack);

                    increaseBuilder.setAction(inventoryClickEvent -> {
                        if (!(location.getBlock().getState(false) instanceof Hopper hopper)) return;
                        SkyHopper currentSkyHopper = hopperManager.getSkyHopper(location);
                        if (currentSkyHopper == null) return;

                        TreeMap<Integer, Double> upgrades = settingsManager.getSuctionRangeUpgrades();
                        if (upgrades == null) return;

                        Optional<Map.Entry<Integer, Double>> optionalUpgrade = getLargerRange(upgrades, skyHopper.suctionRange());
                        if (optionalUpgrade.isPresent()) {
                            Map.Entry<Integer, Double> upgrade = optionalUpgrade.get();

                            SkyHopper updatedSkyHopper = new SkyHopper(
                                    currentSkyHopper.enabled(),
                                    currentSkyHopper.particles(),
                                    currentSkyHopper.owner(),
                                    currentSkyHopper.members(),
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
                                    upgrade.getKey(),
                                    currentSkyHopper.maxSuctionRange(),
                                    currentSkyHopper.maxContainers(),
                                    currentSkyHopper.nextSuctionTime(),
                                    currentSkyHopper.nextTransferTime());

                            hopperManager.saveSkyHopperToBlockPDC(updatedSkyHopper, hopper);

                            hopperManager.cacheSkyHopper(location, updatedSkyHopper);

                            hopperManager.refreshViewersGUI(location);

                            update();
                        }
                    });

                    setButton(increaseConfig.slot(), increaseBuilder.build());
                }
            }
        } else {
            UpgradeGUIConfig.GenericEntry increaseConfig = guiConfig.entries().increaseMax();
            if (increaseConfig.item().material() != null) {
                Material material = Material.getMaterial(increaseConfig.item().material());
                if (material != null) {
                    ItemStack itemStack = ItemStack.of(material);
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    if (increaseConfig.item().name() != null) {
                        itemMeta.displayName(FormatUtil.format(increaseConfig.item().name()));
                    }

                    List<Component> lore = increaseConfig.item().lore().stream().map(FormatUtil::format).toList();
                    itemMeta.lore(lore);

                    List<ItemFlag> flags = increaseConfig.item().itemFlags().stream().map(ItemFlag::valueOf).toList();
                    flags.forEach(itemMeta::addItemFlags);

                    itemStack.setItemMeta(itemMeta);

                    increaseBuilder.setItemStack(itemStack);

                    setButton(increaseConfig.slot(), increaseBuilder.build());
                }
            }
        }
    }

    /**
     * Creates the button to decrease the suction range.
     */
    private void createDecreaseButton(@NotNull SkyHopper skyHopper, @NotNull TreeMap<Integer, Double> displayUpgrades) {
        GUIButton.Builder decreaseBuilder = new GUIButton.Builder();

        if(skyHopper.suctionRange() != displayUpgrades.firstKey()) {
            UpgradeGUIConfig.GenericEntry decreaseConfig = guiConfig.entries().decrease();
            if (decreaseConfig.item().material() != null) {
                Material material = Material.getMaterial(decreaseConfig.item().material());
                if (material != null) {
                    ItemStack itemStack = ItemStack.of(material);
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    if (decreaseConfig.item().name() != null) {
                        itemMeta.displayName(FormatUtil.format(decreaseConfig.item().name()));
                    }

                    Optional<Map.Entry<Integer, Double>> displayUpgrade = getSmallerRange(displayUpgrades, skyHopper.suctionRange());
                    if (displayUpgrade.isEmpty()) return;

                    List<TagResolver.Single> lorePlaceholders = List.of(
                            Placeholder.parsed("current", String.valueOf(skyHopper.suctionRange())),
                            Placeholder.parsed("change", String.valueOf(displayUpgrade.get().getKey())));

                    List<Component> lore = decreaseConfig.item().lore().stream().map(line -> FormatUtil.format(line, lorePlaceholders)).toList();
                    itemMeta.lore(lore);

                    List<ItemFlag> flags = decreaseConfig.item().itemFlags().stream().map(ItemFlag::valueOf).toList();
                    flags.forEach(itemMeta::addItemFlags);

                    itemStack.setItemMeta(itemMeta);

                    decreaseBuilder.setItemStack(itemStack);

                    decreaseBuilder.setAction(inventoryClickEvent -> {
                        if (!(location.getBlock().getState(false) instanceof Hopper hopper)) return;
                        SkyHopper currentSkyHopper = hopperManager.getSkyHopper(location);
                        if (currentSkyHopper == null) return;

                        TreeMap<Integer, Double> upgrades = settingsManager.getSuctionRangeUpgrades();
                        if (upgrades == null) return;

                        Optional<Map.Entry<Integer, Double>> optionalUpgrade = getSmallerRange(upgrades, skyHopper.suctionRange());
                        if (optionalUpgrade.isPresent()) {
                            Map.Entry<Integer, Double> upgrade = optionalUpgrade.get();

                            SkyHopper updatedSkyHopper = new SkyHopper(
                                    currentSkyHopper.enabled(),
                                    currentSkyHopper.particles(),
                                    currentSkyHopper.owner(),
                                    currentSkyHopper.members(),
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
                                    upgrade.getKey(),
                                    currentSkyHopper.maxSuctionRange(),
                                    currentSkyHopper.maxContainers(),
                                    currentSkyHopper.nextSuctionTime(),
                                    currentSkyHopper.nextTransferTime());

                            hopperManager.saveSkyHopperToBlockPDC(updatedSkyHopper, hopper);

                            hopperManager.cacheSkyHopper(location, updatedSkyHopper);

                            hopperManager.refreshViewersGUI(location);

                            update();
                        }
                    });

                    setButton(decreaseConfig.slot(), decreaseBuilder.build());
                }
            }
        } else {
            UpgradeGUIConfig.GenericEntry increaseConfig = guiConfig.entries().decreaseMin();
            if (increaseConfig.item().material() != null) {
                Material material = Material.getMaterial(increaseConfig.item().material());
                if (material != null) {
                    ItemStack itemStack = ItemStack.of(material);
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    if (increaseConfig.item().name() != null) {
                        itemMeta.displayName(FormatUtil.format(increaseConfig.item().name()));
                    }

                    List<Component> lore = increaseConfig.item().lore().stream().map(FormatUtil::format).toList();
                    itemMeta.lore(lore);

                    List<ItemFlag> flags = increaseConfig.item().itemFlags().stream().map(ItemFlag::valueOf).toList();
                    flags.forEach(itemMeta::addItemFlags);

                    itemStack.setItemMeta(itemMeta);

                    decreaseBuilder.setItemStack(itemStack);

                    setButton(increaseConfig.slot(), decreaseBuilder.build());
                }
            }
        }
    }

    /**
     * Creates the button to upgrade the suction range.
     */
    private void createUpgradeButton(@NotNull SkyHopper skyHopper, @NotNull TreeMap<Integer, Double> displayUpgrades) {
        GUIButton.Builder upgradeBuilder = new GUIButton.Builder();

        Optional<Map.Entry<Integer, Double>> displayUpgrade = getNextUpgrade(displayUpgrades, skyHopper.maxSuctionRange());
        if(displayUpgrade.isPresent()) {
            UpgradeGUIConfig.GenericEntry upgradeConfig = guiConfig.entries().upgrade();
            if (upgradeConfig.item().material() != null) {
                Material material = Material.getMaterial(upgradeConfig.item().material());
                if (material != null) {
                    ItemStack itemStack = ItemStack.of(material);
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    if (upgradeConfig.item().name() != null) {
                        itemMeta.displayName(FormatUtil.format(upgradeConfig.item().name()));
                    }

                    double displayRange = displayUpgrade.get().getKey();
                    double displayPrice = displayUpgrade.get().getValue();

                    List<TagResolver.Single> lorePlaceholders = List.of(
                            Placeholder.parsed("current", String.valueOf(skyHopper.maxSuctionRange())),
                            Placeholder.parsed("next", String.valueOf(displayRange)),
                            Placeholder.parsed("price", String.valueOf(displayPrice)));

                    List<Component> lore = upgradeConfig.item().lore().stream().map(line -> FormatUtil.format(line, lorePlaceholders)).toList();
                    itemMeta.lore(lore);

                    List<ItemFlag> flags = upgradeConfig.item().itemFlags().stream().map(ItemFlag::valueOf).toList();
                    flags.forEach(itemMeta::addItemFlags);

                    itemStack.setItemMeta(itemMeta);

                    upgradeBuilder.setItemStack(itemStack);

                    upgradeBuilder.setAction(inventoryClickEvent -> {
                        Player player = (Player) inventoryClickEvent.getWhoClicked();
                        Locale locale = localeManager.getLocale();
                        SkyHopper currentSkyHopper = hopperManager.getSkyHopper(location);
                        if (currentSkyHopper == null) return;

                        TreeMap<Integer, Double> upgrades = settingsManager.getSuctionRangeUpgrades();
                        if (upgrades == null) return;

                        Optional<Map.Entry<Integer, Double>> optionalUpgrade = getNextUpgrade(upgrades, skyHopper.maxSuctionRange());
                        if (optionalUpgrade.isEmpty()) {
                            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.upgradeMaxed()));
                            return;
                        }

                        int upgradeRange = optionalUpgrade.get().getKey();
                        double upgradePrice = optionalUpgrade.get().getValue();

                        if (plugin.getEconomy().getBalance(player) >= upgradePrice) {
                            if (currentSkyHopper.location() != null && currentSkyHopper.location().getBlock().getState(false) instanceof Hopper hopper) {
                                plugin.getEconomy().withdrawPlayer(player, upgradePrice);

                                List<TagResolver.Single> placeholders = List.of(
                                        Placeholder.parsed("current", String.valueOf(currentSkyHopper.suctionRange())),
                                        Placeholder.parsed("next", String.valueOf(upgradeRange)));

                                player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.suctionSpeedUpgrade(), placeholders));

                                SkyHopper updatedSkyHopper = new SkyHopper(
                                        currentSkyHopper.enabled(),
                                        currentSkyHopper.particles(),
                                        currentSkyHopper.owner(),
                                        currentSkyHopper.members(),
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
                                        upgradeRange,
                                        upgradeRange,
                                        currentSkyHopper.maxContainers(),
                                        currentSkyHopper.nextSuctionTime(),
                                        currentSkyHopper.nextTransferTime());

                                hopperManager.saveSkyHopperToBlockPDC(updatedSkyHopper, hopper);

                                hopperManager.cacheSkyHopper(location, updatedSkyHopper);

                                hopperManager.refreshViewersGUI(location);

                                update();
                            }
                        } else {
                            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.notEnoughMoney()));
                        }
                    });

                    setButton(upgradeConfig.slot(), upgradeBuilder.build());
                }
            }
        } else {
            UpgradeGUIConfig.GenericEntry upgradeConfig = guiConfig.entries().upgradeMax();
            if (upgradeConfig.item().material() != null) {
                Material material = Material.getMaterial(upgradeConfig.item().material());
                if (material != null) {
                    ItemStack itemStack = ItemStack.of(material);
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    if (upgradeConfig.item().name() != null) {
                        itemMeta.displayName(FormatUtil.format(upgradeConfig.item().name()));
                    }

                    List<Component> lore = upgradeConfig.item().lore().stream().map(FormatUtil::format).toList();
                    itemMeta.lore(lore);

                    List<ItemFlag> flags = upgradeConfig.item().itemFlags().stream().map(ItemFlag::valueOf).toList();
                    flags.forEach(itemMeta::addItemFlags);

                    itemStack.setItemMeta(itemMeta);

                    upgradeBuilder.setItemStack(itemStack);

                    setButton(upgradeConfig.slot(), upgradeBuilder.build());
                }
            }
        }
    }

    /**
     * Gets the next available upgrade.
     * @param upgrades The TreeMap of upgrades.
     * @param currentKey The current upgrade.
     * @return An Optional of the next available upgrade.
     */
    private Optional<Map.Entry<Integer, Double>> getNextUpgrade(TreeMap<Integer, Double> upgrades, Integer currentKey) {
        Map.Entry<Integer, Double> currentEntry = upgrades.floorEntry(currentKey);

        if(currentEntry == null) return Optional.empty();

        return Optional.ofNullable(upgrades.higherEntry(currentEntry.getKey()));
    }

    /**
     * Gets suction range larger than the current.
     * @param upgrades The TreeMap of upgrades.
     * @param currentKey The current upgrade.
     * @return An Optional of the larger suction range.
     */
    private Optional<Map.Entry<Integer, Double>> getLargerRange(TreeMap<Integer, Double> upgrades, Integer currentKey) {
        Map.Entry<Integer, Double> currentEntry = upgrades.floorEntry(currentKey);

        if(currentEntry == null) return Optional.empty();

        Map.Entry<Integer, Double> maxEntry = upgrades.lastEntry();

        if(maxEntry.getKey().equals(currentEntry.getKey())) return Optional.empty();

        return Optional.ofNullable(upgrades.higherEntry(currentEntry.getKey()));
    }

    /**
     * Gets suction range smaller than the current.
     * @param upgrades The TreeMap of upgrades.
     * @param currentKey The current upgrade.
     * @return An Optional of the smaller suction range.
     */
    private Optional<Map.Entry<Integer, Double>> getSmallerRange(TreeMap<Integer, Double> upgrades, Integer currentKey) {
        Map.Entry<Integer, Double> currentEntry = upgrades.floorEntry(currentKey);

        if (currentEntry == null) return Optional.empty();

        Map.Entry<Integer, Double> minEntry = upgrades.firstEntry();

        if(minEntry.getKey().equals(currentEntry.getKey())) return Optional.empty();

        return Optional.ofNullable(upgrades.lowerEntry(currentEntry.getKey()));
    }
}
