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
package com.github.lukesky19.skyHoppers.command;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.manager.LocaleManager;
import com.github.lukesky19.skyHoppers.config.manager.SettingsManager;
import com.github.lukesky19.skyHoppers.config.record.Locale;
import com.github.lukesky19.skyHoppers.config.record.Settings;
import com.github.lukesky19.skyHoppers.hopper.FilterType;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skylib.format.FormatUtil;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class handles the creation of the SkyHoppers command.
 */
public class SkyHopperCommand {
    private final SkyHoppers plugin;
    private final LocaleManager localeManager;
    private final HopperManager hopperManager;
    private final SettingsManager settingsManager;

    /**
     * Constructor
     * @param plugin The SkyHoppers plugin.
     * @param localeManager A LocaleManager instance.
     * @param hopperManager A HopperManager instance.
     * @param settingsManager A SettingsManager instance.
     */
    public SkyHopperCommand(SkyHoppers plugin, LocaleManager localeManager, HopperManager hopperManager, SettingsManager settingsManager) {
        this.plugin = plugin;
        this.localeManager = localeManager;
        this.hopperManager = hopperManager;
        this.settingsManager = settingsManager;
    }

    /**
     * Creates a command to be passed into the LifeCycleAPI.
     * @return A LiteralCommandNode of a CommandSourceStack.
     */
    @SuppressWarnings("UnstableApiUsage")
    public LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("skyhoppers")
                .requires(ctx -> ctx.getSender().hasPermission("skyhoppers.commands.skyhoppers"));
        
        builder.then(Commands.literal("reload")
            .requires(ctx -> ctx.getSender().hasPermission("skyhoppers.commands.skyhoppers.reload"))
            .executes(ctx -> {
                Locale locale = localeManager.getLocale();
                
                plugin.reload();
                
                ctx.getSource().getSender().sendMessage(FormatUtil.format(locale.prefix() + locale.reload()));
                
                return 1;
            })
        );
        
        builder.then(Commands.literal("help")
            .requires(ctx -> ctx.getSender().hasPermission("skyhoppers.commands.skyhoppers.help"))
            .executes(ctx -> {
                Locale locale = localeManager.getLocale();
                CommandSender sender = ctx.getSource().getSender();

                for (String msg : locale.help()) {
                    sender.sendMessage(FormatUtil.format(msg));
                }
                
                return 1;
            })
        );

        builder.then(Commands.literal("give")
            .requires(ctx -> ctx.getSender().hasPermission("skyhoppers.commands.skyhoppers.give"))
            .then(Commands.argument("player name", ArgumentTypes.player())
                .then(Commands.argument("amount", IntegerArgumentType.integer())
                    .then(Commands.argument("suction speed", DoubleArgumentType.doubleArg())
                        .then(Commands.argument("suction amount", IntegerArgumentType.integer())
                            .then(Commands.argument("suction range", IntegerArgumentType.integer())
                                .then(Commands.argument("transfer speed", DoubleArgumentType.doubleArg())
                                    .then(Commands.argument("transfer amount", IntegerArgumentType.integer())
                                        .then(Commands.argument("max containers", IntegerArgumentType.integer())
                                            .executes(ctx -> {
                                                Locale locale = localeManager.getLocale();
                                                CommandSender sender = ctx.getSource().getSender();

                                                PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player name", PlayerSelectorArgumentResolver.class);
                                                Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                                int amount = ctx.getArgument("amount",  int.class);
                                                double suctionSpeed = ctx.getArgument("suction speed", double.class);
                                                int suctionAmount = ctx.getArgument("suction amount", int.class);
                                                int suctionRange = ctx.getArgument("suction range", int.class);
                                                double transferSpeed = ctx.getArgument("transfer speed", double.class);
                                                int transferAmount = ctx.getArgument("transfer amount", int.class);
                                                int maxContainers = ctx.getArgument("max containers", int.class);

                                                long nextSuction = (long) (System.currentTimeMillis() + (suctionSpeed * 1000));
                                                long nextTransfer = (long) (System.currentTimeMillis() + (transferSpeed * 1000));

                                                SkyHopper skyHopper = new SkyHopper(
                                                        true,
                                                        true,
                                                        target.getUniqueId(),
                                                        new ArrayList<>(),
                                                        null,
                                                        new ArrayList<>(),
                                                        FilterType.NONE,
                                                        new ArrayList<>(),
                                                        transferSpeed,
                                                        transferSpeed,
                                                        transferAmount,
                                                        transferAmount,
                                                        suctionSpeed,
                                                        suctionSpeed,
                                                        suctionAmount,
                                                        suctionAmount,
                                                        suctionRange,
                                                        suctionRange,
                                                        maxContainers,
                                                        nextSuction,
                                                        nextTransfer);

                                                ItemStack itemStack = hopperManager.createItemStackFromSkyHopper(skyHopper, amount);

                                                if(itemStack != null) {
                                                    target.getInventory().addItem(itemStack);

                                                    target.sendMessage(FormatUtil.format(locale.prefix() + locale.hopperGiven()));

                                                    sender.sendMessage(FormatUtil.format(locale.prefix() + locale.hopperGivenTo(), List.of(Placeholder.parsed("player", target.getName()))));

                                                    return 1;
                                                } else {
                                                    sender.sendMessage(FormatUtil.format(locale.prefix() + locale.skyhopperCreationFailed()));

                                                    return 0;
                                                }
                                            }))
                                        .executes(ctx -> {
                                            Locale locale = localeManager.getLocale();
                                            CommandSender sender = ctx.getSource().getSender();

                                            Settings settings = settingsManager.getSettings();
                                            if(settings == null) {
                                                sender.sendMessage(FormatUtil.format(locale.prefix() + locale.skyhopperCreationFailed()));
                                                sender.sendMessage(FormatUtil.format(locale.prefix() + locale.invalidSettings()));
                                                return 0;
                                            }

                                            Settings.SkyHopper skyHopperSettings = settings.skyhopper();

                                            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player name", PlayerSelectorArgumentResolver.class);
                                            Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                            int amount = ctx.getArgument("amount",  int.class);
                                            double suctionSpeed = ctx.getArgument("suction speed", double.class);
                                            int suctionAmount = ctx.getArgument("suction amount", int.class);
                                            int suctionRange = ctx.getArgument("suction range", int.class);
                                            double transferSpeed = ctx.getArgument("transfer speed", double.class);
                                            int transferAmount = ctx.getArgument("transfer amount", int.class);

                                            long nextSuction = (long) (System.currentTimeMillis() + (suctionSpeed * 1000));
                                            long nextTransfer = (long) (System.currentTimeMillis() + (transferSpeed * 1000));

                                            SkyHopper skyHopper = new SkyHopper(
                                                    true,
                                                    true,
                                                    target.getUniqueId(),
                                                    new ArrayList<>(),
                                                    null,
                                                    new ArrayList<>(),
                                                    FilterType.NONE,
                                                    new ArrayList<>(),
                                                    transferSpeed,
                                                    transferSpeed,
                                                    transferAmount,
                                                    transferAmount,
                                                    suctionSpeed,
                                                    suctionSpeed,
                                                    suctionAmount,
                                                    suctionAmount,
                                                    suctionRange,
                                                    suctionRange,
                                                    skyHopperSettings.startingMaxContainers(),
                                                    nextSuction,
                                                    nextTransfer);

                                            ItemStack itemStack = hopperManager.createItemStackFromSkyHopper(skyHopper, amount);

                                            if(itemStack != null) {
                                                target.getInventory().addItem(itemStack);

                                                target.sendMessage(FormatUtil.format(locale.prefix() + locale.hopperGiven()));

                                                sender.sendMessage(FormatUtil.format(locale.prefix() + locale.hopperGivenTo(), List.of(Placeholder.parsed("player", target.getName()))));

                                                return 1;
                                            } else {
                                                sender.sendMessage(FormatUtil.format(locale.prefix() + locale.skyhopperCreationFailed()));

                                                return 0;
                                            }
                                        }))
                                    .executes(ctx -> {
                                        Locale locale = localeManager.getLocale();
                                        CommandSender sender = ctx.getSource().getSender();

                                        Settings settings = settingsManager.getSettings();
                                        if(settings == null) {
                                            sender.sendMessage(FormatUtil.format(locale.prefix() + locale.skyhopperCreationFailed()));
                                            sender.sendMessage(FormatUtil.format(locale.prefix() + locale.invalidSettings()));

                                            return 0;
                                        }
                                        Settings.SkyHopper skyHopperSettings = settings.skyhopper();

                                        PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player name", PlayerSelectorArgumentResolver.class);
                                        Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                        int amount = ctx.getArgument("amount",  int.class);
                                        double suctionSpeed = ctx.getArgument("suction speed", double.class);
                                        int suctionAmount = ctx.getArgument("suction amount", int.class);
                                        int suctionRange = ctx.getArgument("suction range", int.class);
                                        double transferSpeed = ctx.getArgument("transfer speed", double.class);

                                        long nextSuction = (long) (System.currentTimeMillis() + (suctionSpeed * 1000));
                                        long nextTransfer = (long) (System.currentTimeMillis() + (transferSpeed * 1000));

                                        SkyHopper skyHopper = new SkyHopper(
                                                true,
                                                true,
                                                target.getUniqueId(),
                                                new ArrayList<>(),
                                                null,
                                                new ArrayList<>(),
                                                FilterType.NONE,
                                                new ArrayList<>(),
                                                transferSpeed,
                                                transferSpeed,
                                                skyHopperSettings.startingTransferAmount(),
                                                skyHopperSettings.startingTransferAmount(),
                                                suctionSpeed,
                                                suctionSpeed,
                                                suctionAmount,
                                                suctionAmount,
                                                suctionRange,
                                                suctionRange,
                                                skyHopperSettings.startingMaxContainers(),
                                                nextSuction,
                                                nextTransfer);

                                        ItemStack itemStack = hopperManager.createItemStackFromSkyHopper(skyHopper, amount);

                                        if(itemStack != null) {
                                            target.getInventory().addItem(itemStack);

                                            target.sendMessage(FormatUtil.format(locale.prefix() + locale.hopperGiven()));

                                            sender.sendMessage(FormatUtil.format(locale.prefix() + locale.hopperGivenTo(), List.of(Placeholder.parsed("player", target.getName()))));

                                            return 1;
                                        } else {
                                            sender.sendMessage(FormatUtil.format(locale.prefix() + locale.skyhopperCreationFailed()));

                                            return 0;
                                        }
                                    }))
                                .executes(ctx -> {
                                    Locale locale = localeManager.getLocale();
                                    CommandSender sender = ctx.getSource().getSender();

                                    Settings settings = settingsManager.getSettings();
                                    if(settings == null) {
                                        sender.sendMessage(FormatUtil.format(locale.prefix() + locale.skyhopperCreationFailed()));
                                        sender.sendMessage(FormatUtil.format(locale.prefix() + locale.invalidSettings()));

                                        return 0;
                                    }
                                    Settings.SkyHopper skyHopperSettings = settings.skyhopper();

                                    PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player name", PlayerSelectorArgumentResolver.class);
                                    Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                    int amount = ctx.getArgument("amount",  int.class);
                                    double suctionSpeed = ctx.getArgument("suction speed", double.class);
                                    int suctionAmount = ctx.getArgument("suction amount", int.class);
                                    int suctionRange = ctx.getArgument("suction range", int.class);

                                    long nextSuction = (long) (System.currentTimeMillis() + (suctionSpeed * 1000));
                                    long nextTransfer = (long) (System.currentTimeMillis() + (skyHopperSettings.startingTransferSpeed() * 1000));

                                    SkyHopper skyHopper = new SkyHopper(
                                            true,
                                            true,
                                            target.getUniqueId(),
                                            new ArrayList<>(),
                                            null,
                                            new ArrayList<>(),
                                            FilterType.NONE,
                                            new ArrayList<>(),
                                            skyHopperSettings.startingTransferSpeed(),
                                            skyHopperSettings.startingTransferSpeed(),
                                            skyHopperSettings.startingTransferAmount(),
                                            skyHopperSettings.startingTransferAmount(),
                                            suctionSpeed,
                                            suctionSpeed,
                                            suctionAmount,
                                            suctionAmount,
                                            suctionRange,
                                            suctionRange,
                                            skyHopperSettings.startingMaxContainers(),
                                            nextSuction,
                                            nextTransfer);

                                    ItemStack itemStack = hopperManager.createItemStackFromSkyHopper(skyHopper, amount);

                                    if(itemStack != null) {
                                        target.getInventory().addItem(itemStack);

                                        target.sendMessage(FormatUtil.format(locale.prefix() + locale.hopperGiven()));

                                        sender.sendMessage(FormatUtil.format(locale.prefix() + locale.hopperGivenTo(), List.of(Placeholder.parsed("player", target.getName()))));

                                        return 1;
                                    } else {
                                        sender.sendMessage(FormatUtil.format(locale.prefix() + locale.skyhopperCreationFailed()));

                                        return 0;
                                    }
                                }))
                            .executes(ctx -> {
                                Locale locale = localeManager.getLocale();
                                CommandSender sender = ctx.getSource().getSender();

                                Settings settings = settingsManager.getSettings();
                                if(settings == null) {
                                    sender.sendMessage(FormatUtil.format(locale.prefix() + locale.skyhopperCreationFailed()));
                                    sender.sendMessage(FormatUtil.format(locale.prefix() + locale.invalidSettings()));

                                    return 0;
                                }
                                Settings.SkyHopper skyHopperSettings = settings.skyhopper();

                                PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player name", PlayerSelectorArgumentResolver.class);
                                Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                int amount = ctx.getArgument("amount",  int.class);
                                double suctionSpeed = ctx.getArgument("suction speed", double.class);
                                int suctionAmount = ctx.getArgument("suction amount", int.class);

                                long nextSuction = (long) (System.currentTimeMillis() + (suctionSpeed * 1000));
                                long nextTransfer = (long) (System.currentTimeMillis() + (skyHopperSettings.startingTransferSpeed() * 1000));

                                SkyHopper skyHopper = new SkyHopper(
                                        true,
                                        true,
                                        target.getUniqueId(),
                                        new ArrayList<>(),
                                        null,
                                        new ArrayList<>(),
                                        FilterType.NONE,
                                        new ArrayList<>(),
                                        skyHopperSettings.startingTransferSpeed(),
                                        skyHopperSettings.startingTransferSpeed(),
                                        skyHopperSettings.startingTransferAmount(),
                                        skyHopperSettings.startingTransferAmount(),
                                        suctionSpeed,
                                        suctionSpeed,
                                        suctionAmount,
                                        suctionAmount,
                                        skyHopperSettings.startingSuctionRange(),
                                        skyHopperSettings.startingSuctionRange(),
                                        skyHopperSettings.startingMaxContainers(),
                                        nextSuction,
                                        nextTransfer);

                                ItemStack itemStack = hopperManager.createItemStackFromSkyHopper(skyHopper, amount);

                                if(itemStack != null) {
                                    target.getInventory().addItem(itemStack);

                                    target.sendMessage(FormatUtil.format(locale.prefix() + locale.hopperGiven()));

                                    sender.sendMessage(FormatUtil.format(locale.prefix() + locale.hopperGivenTo(), List.of(Placeholder.parsed("player", target.getName()))));

                                    return 1;
                                } else {
                                    sender.sendMessage(FormatUtil.format(locale.prefix() + locale.skyhopperCreationFailed()));

                                    return 0;
                                }
                            }))
                        .executes(ctx -> {
                            Locale locale = localeManager.getLocale();
                            CommandSender sender = ctx.getSource().getSender();

                            Settings settings = settingsManager.getSettings();
                            if(settings == null) {
                                sender.sendMessage(FormatUtil.format(locale.prefix() + locale.skyhopperCreationFailed()));
                                sender.sendMessage(FormatUtil.format(locale.prefix() + locale.invalidSettings()));

                                return 0;
                            }
                            Settings.SkyHopper skyHopperSettings = settings.skyhopper();

                            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player name", PlayerSelectorArgumentResolver.class);
                            Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                            int amount = ctx.getArgument("amount",  int.class);
                            double suctionSpeed = ctx.getArgument("suction speed", double.class);

                            long nextSuction = (long) (System.currentTimeMillis() + (suctionSpeed * 1000));
                            long nextTransfer = (long) (System.currentTimeMillis() + (skyHopperSettings.startingTransferSpeed() * 1000));

                            SkyHopper skyHopper = new SkyHopper(
                                    true,
                                    true,
                                    target.getUniqueId(),
                                    new ArrayList<>(),
                                    null,
                                    new ArrayList<>(),
                                    FilterType.NONE,
                                    new ArrayList<>(),
                                    skyHopperSettings.startingTransferSpeed(),
                                    skyHopperSettings.startingTransferSpeed(),
                                    skyHopperSettings.startingTransferAmount(),
                                    skyHopperSettings.startingTransferAmount(),
                                    suctionSpeed,
                                    suctionSpeed,
                                    skyHopperSettings.startingSuctionAmount(),
                                    skyHopperSettings.startingSuctionAmount(),
                                    skyHopperSettings.startingSuctionRange(),
                                    skyHopperSettings.startingSuctionRange(),
                                    skyHopperSettings.startingMaxContainers(),
                                    nextSuction,
                                    nextTransfer);

                            ItemStack itemStack = hopperManager.createItemStackFromSkyHopper(skyHopper, amount);

                            if(itemStack != null) {
                                target.getInventory().addItem(itemStack);

                                target.sendMessage(FormatUtil.format(locale.prefix() + locale.hopperGiven()));

                                sender.sendMessage(FormatUtil.format(locale.prefix() + locale.hopperGivenTo(), List.of(Placeholder.parsed("player", target.getName()))));

                                return 1;
                            } else {
                                sender.sendMessage(FormatUtil.format(locale.prefix() + locale.skyhopperCreationFailed()));

                                return 0;
                            }
                        }))
                    .executes(ctx -> {
                        Locale locale = localeManager.getLocale();
                        CommandSender sender = ctx.getSource().getSender();

                        Settings settings = settingsManager.getSettings();
                        if(settings == null) {
                            sender.sendMessage(FormatUtil.format(locale.prefix() + locale.skyhopperCreationFailed()));
                            sender.sendMessage(FormatUtil.format(locale.prefix() + locale.invalidSettings()));

                            return 0;
                        }
                        Settings.SkyHopper skyHopperSettings = settings.skyhopper();

                        PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player name", PlayerSelectorArgumentResolver.class);
                        Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                        int amount = ctx.getArgument("amount",  int.class);

                        long nextSuction = (long) (System.currentTimeMillis() + (skyHopperSettings.startingSuctionSpeed() * 1000));
                        long nextTransfer = (long) (System.currentTimeMillis() + (skyHopperSettings.startingTransferSpeed() * 1000));

                        SkyHopper skyHopper = new SkyHopper(
                                true,
                                true,
                                target.getUniqueId(),
                                new ArrayList<>(),
                                null,
                                new ArrayList<>(),
                                FilterType.NONE,
                                new ArrayList<>(),
                                skyHopperSettings.startingTransferSpeed(),
                                skyHopperSettings.startingTransferSpeed(),
                                skyHopperSettings.startingTransferAmount(),
                                skyHopperSettings.startingTransferAmount(),
                                skyHopperSettings.startingSuctionSpeed(),
                                skyHopperSettings.startingSuctionSpeed(),
                                skyHopperSettings.startingSuctionAmount(),
                                skyHopperSettings.startingSuctionAmount(),
                                skyHopperSettings.startingSuctionRange(),
                                skyHopperSettings.startingSuctionRange(),
                                skyHopperSettings.startingMaxContainers(),
                                nextSuction,
                                nextTransfer);

                        ItemStack itemStack = hopperManager.createItemStackFromSkyHopper(skyHopper, amount);

                        if(itemStack != null) {
                            target.getInventory().addItem(itemStack);

                            target.sendMessage(FormatUtil.format(locale.prefix() + locale.hopperGiven()));

                            sender.sendMessage(FormatUtil.format(locale.prefix() + locale.hopperGivenTo(), List.of(Placeholder.parsed("player", target.getName()))));

                            return 1;
                        } else {
                            sender.sendMessage(FormatUtil.format(locale.prefix() + locale.skyhopperCreationFailed()));

                            return 0;
                        }
                    })
                )
            )
        );
        
        builder.then(Commands.literal("transfer")
            .requires(ctx -> ctx.getSender().hasPermission("skyhoppers.commands.skyhoppers.transfer") && ctx.getSender() instanceof Player)
            .then(Commands.argument("player name", ArgumentTypes.player())
                .executes(ctx -> {
                    Locale locale = localeManager.getLocale();

                    Player player = (Player) ctx.getSource().getSender();
                    UUID playerUUID = player.getUniqueId();
                    ItemStack itemStack = player.getInventory().getItemInMainHand();

                    PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player name", PlayerSelectorArgumentResolver.class);
                    Player newOwner = targetResolver.resolve(ctx.getSource()).getFirst();

                    if(!hopperManager.isItemStackSkyHopper(itemStack)) {
                        player.sendMessage(FormatUtil.format(locale.prefix() + locale.itemNotSkyHopper()));
                        return 0;
                    }

                    SkyHopper skyHopper = hopperManager.getSkyHopperFromPDC(null, itemStack.getItemMeta().getPersistentDataContainer());
                    if(skyHopper == null) return 0;

                    if(!skyHopper.owner().equals(playerUUID)) {
                        if(!skyHopper.members().contains(playerUUID)) {
                            player.sendMessage(FormatUtil.format(locale.prefix() + locale.noAccessOwnerChange()));
                            return 0;
                        }
                    }

                    SkyHopper updatedSkyHopper = new SkyHopper(
                            skyHopper.enabled(),
                            skyHopper.particles(),
                            newOwner.getUniqueId(),
                            skyHopper.members(),
                            skyHopper.location(),
                            skyHopper.containers(),
                            skyHopper.filterType(),
                            skyHopper.filterItems(),
                            skyHopper.transferSpeed(),
                            skyHopper.maxTransferSpeed(),
                            skyHopper.transferAmount(),
                            skyHopper.maxTransferAmount(),
                            skyHopper.suctionSpeed(),
                            skyHopper.maxSuctionSpeed(),
                            skyHopper.suctionAmount(),
                            skyHopper.maxSuctionAmount(),
                            skyHopper.suctionRange(),
                            skyHopper.maxSuctionRange(),
                            skyHopper.maxContainers(),
                            skyHopper.nextSuctionTime(),
                            skyHopper.nextTransferTime());

                    ItemStack newStack = hopperManager.createItemStackFromSkyHopper(updatedSkyHopper, 1);
                    player.getInventory().setItemInMainHand(newStack);

                    player.sendMessage(FormatUtil.format(locale.prefix() + locale.newOwner(), List.of(Placeholder.parsed("player_name", newOwner.getName()))));
                    return 1;
            }))
        );

        builder.then(Commands.literal("load")
            .requires(ctx -> ctx.getSender().hasPermission("skyhoppers.commands.skyhoppers.load"))
                .then(Commands.argument("force", BoolArgumentType.bool()).executes(ctx -> {
                    Locale locale = localeManager.getLocale();
                    CommandSender sender = ctx.getSource().getSender();
                    
                    boolean force = ctx.getArgument("force", boolean.class);

                    hopperManager.loadSkyHoppers(force);
                    
                    if(force) {
                        sender.sendMessage(FormatUtil.format(locale.prefix() + locale.skyhoppersForceLoaded()));
                    } else {
                        sender.sendMessage(FormatUtil.format(locale.prefix() + locale.skyhoppersLoaded()));
                    }

                    return 1;
                })
            )
        );
        
         builder.then(Commands.literal("pause")
            .requires(ctx -> ctx.getSender().hasPermission("skyhoppers.commands.skyhoppers.pause"))
            .executes(ctx -> {
                Locale locale = localeManager.getLocale();
                CommandSender sender = ctx.getSource().getSender();
                
                plugin.pauseSkyHoppers();

                sender.sendMessage(FormatUtil.format(locale.prefix() + locale.skyHoppersPaused()));

                return 1;
            })
         );
         
          builder.then(Commands.literal("unpause")
              .requires(ctx -> ctx.getSender().hasPermission("skyhoppers.commands.skyhoppers.unpause"))
              .executes(ctx -> {
                  Locale locale = localeManager.getLocale();
                  CommandSender sender = ctx.getSource().getSender();
                
                  plugin.unPauseSkyHoppers();

                  sender.sendMessage(FormatUtil.format(locale.prefix() + locale.skyhoppersUnpaused()));

                  return 1;
            })
         );

        return builder.build();
    }
}