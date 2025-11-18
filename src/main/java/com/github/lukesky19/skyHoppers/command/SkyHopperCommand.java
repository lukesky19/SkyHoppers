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
package com.github.lukesky19.skyHoppers.command;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.LocaleManager;
import com.github.lukesky19.skyHoppers.config.SettingsManager;
import com.github.lukesky19.skyHoppers.config.data.Locale;
import com.github.lukesky19.skyHoppers.config.data.Settings;
import com.github.lukesky19.skyHoppers.skyhopper.SkyHopperManager;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * This class handles the creation of the SkyHoppers command.
 */
public class SkyHopperCommand {
    private final @NotNull SkyHoppers plugin;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull SkyHopperManager hopperManager;

    /**
     * Constructor
     * @param plugin A {@link SkyHoppers} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param hopperManager A {@link SkyHopperManager} instance.
     */
    public SkyHopperCommand(
            @NotNull SkyHoppers plugin,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull SkyHopperManager hopperManager) {
        this.plugin = plugin;
        this.localeManager = localeManager;
        this.hopperManager = hopperManager;
        this.settingsManager = settingsManager;
    }

    /**
     * Creates a command to be passed into the LifeCycleAPI.
     * @return A LiteralCommandNode of a CommandSourceStack.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("skyhoppers")
                .requires(ctx -> ctx.getSender().hasPermission("skyhoppers.commands.skyhoppers"));
        
        builder.then(Commands.literal("reload")
            .requires(ctx -> ctx.getSender().hasPermission("skyhoppers.commands.skyhoppers.reload"))
            .executes(ctx -> {
                Locale locale = localeManager.getLocale();
                
                plugin.reload();
                
                ctx.getSource().getSender().sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.reload()));
                
                return 1;
            })
        );
        
        builder.then(Commands.literal("help")
            .requires(ctx -> ctx.getSender().hasPermission("skyhoppers.commands.skyhoppers.help"))
            .executes(ctx -> {
                Locale locale = localeManager.getLocale();
                CommandSender sender = ctx.getSource().getSender();

                for (String msg : locale.help()) {
                    sender.sendMessage(AdventureUtil.deserialize(msg));
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
                                                CommandSender sender = ctx.getSource().getSender();
                                                Locale locale = localeManager.getLocale();

                                                @Nullable Settings settings = settingsManager.getSettings();
                                                if(settings == null) {
                                                    sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.skyhopperCreationFailed()));
                                                    sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.invalidSettings()));

                                                    return 0;
                                                }

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

                                                SkyHopper skyHopper = new SkyHopper(settings);

                                                skyHopper.setSuctionSpeed(suctionSpeed);
                                                skyHopper.setMaxSuctionSpeed(suctionSpeed);
                                                skyHopper.setSuctionAmount(suctionAmount);
                                                skyHopper.setMaxSuctionAmount(suctionAmount);
                                                skyHopper.setSuctionRange(suctionRange);
                                                skyHopper.setMaxSuctionRange(suctionRange);
                                                skyHopper.setTransferSpeed(transferSpeed);
                                                skyHopper.setMaxTransferSpeed(transferSpeed);
                                                skyHopper.setTransferAmount(transferAmount);
                                                skyHopper.setMaxTransferAmount(transferAmount);
                                                skyHopper.setMaxContainers(maxContainers);

                                                skyHopper.setNextSuctionTime(nextSuction);
                                                skyHopper.setNextTransferTime(nextTransfer);

                                                ItemStack itemStack = hopperManager.getSkyHopperCreator().createSkyHopperItemStack(skyHopper, amount);

                                                if(itemStack != null) {
                                                    target.getInventory().addItem(itemStack);

                                                    target.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.hopperGiven()));

                                                    sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.hopperGivenTo(), List.of(Placeholder.parsed("player", target.getName()))));

                                                    return 1;
                                                } else {
                                                    sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.skyhopperCreationFailed()));

                                                    return 0;
                                                }
                                            }))
                                        .executes(ctx -> {
                                            CommandSender sender = ctx.getSource().getSender();
                                            Locale locale = localeManager.getLocale();

                                            @Nullable Settings settings = settingsManager.getSettings();
                                            if(settings == null) {
                                                sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.skyhopperCreationFailed()));
                                                sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.invalidSettings()));

                                                return 0;
                                            }

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

                                            SkyHopper skyHopper = new SkyHopper(settings);

                                            skyHopper.setSuctionSpeed(suctionSpeed);
                                            skyHopper.setMaxSuctionSpeed(suctionSpeed);
                                            skyHopper.setSuctionAmount(suctionAmount);
                                            skyHopper.setMaxSuctionAmount(suctionAmount);
                                            skyHopper.setSuctionRange(suctionRange);
                                            skyHopper.setMaxSuctionRange(suctionRange);
                                            skyHopper.setTransferSpeed(transferSpeed);
                                            skyHopper.setMaxTransferSpeed(transferSpeed);
                                            skyHopper.setTransferAmount(transferAmount);
                                            skyHopper.setMaxTransferAmount(transferAmount);

                                            skyHopper.setNextSuctionTime(nextSuction);
                                            skyHopper.setNextTransferTime(nextTransfer);

                                            ItemStack itemStack = hopperManager.getSkyHopperCreator().createSkyHopperItemStack(skyHopper, amount);

                                            if(itemStack != null) {
                                                target.getInventory().addItem(itemStack);

                                                target.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.hopperGiven()));

                                                sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.hopperGivenTo(), List.of(Placeholder.parsed("player", target.getName()))));

                                                return 1;
                                            } else {
                                                sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.skyhopperCreationFailed()));

                                                return 0;
                                            }
                                        }))
                                    .executes(ctx -> {
                                        Locale locale = localeManager.getLocale();
                                        CommandSender sender = ctx.getSource().getSender();

                                        Settings settings = settingsManager.getSettings();
                                        if(settings == null) {
                                            sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.skyhopperCreationFailed()));
                                            sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.invalidSettings()));
                                            return 0;
                                        }

                                        PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player name", PlayerSelectorArgumentResolver.class);
                                        Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                        int amount = ctx.getArgument("amount",  int.class);
                                        double suctionSpeed = ctx.getArgument("suction speed", double.class);
                                        int suctionAmount = ctx.getArgument("suction amount", int.class);
                                        int suctionRange = ctx.getArgument("suction range", int.class);
                                        double transferSpeed = ctx.getArgument("transfer speed", double.class);

                                        long nextSuction = (long) (System.currentTimeMillis() + (suctionSpeed * 1000));
                                        long nextTransfer = (long) (System.currentTimeMillis() + (transferSpeed * 1000));

                                        SkyHopper skyHopper = new SkyHopper(settings);

                                        skyHopper.setSuctionSpeed(suctionSpeed);
                                        skyHopper.setMaxSuctionSpeed(suctionSpeed);
                                        skyHopper.setSuctionAmount(suctionAmount);
                                        skyHopper.setMaxSuctionAmount(suctionAmount);
                                        skyHopper.setSuctionRange(suctionRange);
                                        skyHopper.setMaxSuctionRange(suctionRange);
                                        skyHopper.setTransferSpeed(transferSpeed);
                                        skyHopper.setMaxTransferSpeed(transferSpeed);

                                        skyHopper.setNextSuctionTime(nextSuction);
                                        skyHopper.setNextTransferTime(nextTransfer);

                                        ItemStack itemStack = hopperManager.getSkyHopperCreator().createSkyHopperItemStack(skyHopper, amount);

                                        if(itemStack != null) {
                                            target.getInventory().addItem(itemStack);

                                            target.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.hopperGiven()));

                                            sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.hopperGivenTo(), List.of(Placeholder.parsed("player", target.getName()))));

                                            return 1;
                                        } else {
                                            sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.skyhopperCreationFailed()));

                                            return 0;
                                        }
                                    }))
                                .executes(ctx -> {
                                    Locale locale = localeManager.getLocale();
                                    CommandSender sender = ctx.getSource().getSender();

                                    Settings settings = settingsManager.getSettings();
                                    if(settings == null) {
                                        sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.skyhopperCreationFailed()));
                                        sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.invalidSettings()));

                                        return 0;
                                    }
                                    Settings.SkyHopperConfig skyHopperConfigSettings = settings.skyHopperConfig();

                                    PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player name", PlayerSelectorArgumentResolver.class);
                                    Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                    int amount = ctx.getArgument("amount",  int.class);
                                    double suctionSpeed = ctx.getArgument("suction speed", double.class);
                                    int suctionAmount = ctx.getArgument("suction amount", int.class);
                                    int suctionRange = ctx.getArgument("suction range", int.class);

                                    long nextSuction = (long) (System.currentTimeMillis() + (suctionSpeed * 1000));
                                    long nextTransfer = (long) (System.currentTimeMillis() + (skyHopperConfigSettings.startingTransferSpeed() * 1000));

                                    SkyHopper skyHopper = new SkyHopper(settings);

                                    skyHopper.setSuctionSpeed(suctionSpeed);
                                    skyHopper.setMaxSuctionSpeed(suctionSpeed);
                                    skyHopper.setSuctionAmount(suctionAmount);
                                    skyHopper.setMaxSuctionAmount(suctionAmount);
                                    skyHopper.setSuctionRange(suctionRange);
                                    skyHopper.setMaxSuctionRange(suctionRange);

                                    skyHopper.setNextSuctionTime(nextSuction);
                                    skyHopper.setNextTransferTime(nextTransfer);

                                    ItemStack itemStack = hopperManager.getSkyHopperCreator().createSkyHopperItemStack(skyHopper, amount);

                                    if(itemStack != null) {
                                        target.getInventory().addItem(itemStack);

                                        target.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.hopperGiven()));

                                        sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.hopperGivenTo(), List.of(Placeholder.parsed("player", target.getName()))));

                                        return 1;
                                    } else {
                                        sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.skyhopperCreationFailed()));

                                        return 0;
                                    }
                                }))
                            .executes(ctx -> {
                                Locale locale = localeManager.getLocale();
                                CommandSender sender = ctx.getSource().getSender();

                                Settings settings = settingsManager.getSettings();
                                if(settings == null) {
                                    sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.skyhopperCreationFailed()));
                                    sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.invalidSettings()));

                                    return 0;
                                }
                                Settings.SkyHopperConfig skyHopperConfigSettings = settings.skyHopperConfig();

                                PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player name", PlayerSelectorArgumentResolver.class);
                                Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                int amount = ctx.getArgument("amount",  int.class);
                                double suctionSpeed = ctx.getArgument("suction speed", double.class);
                                int suctionAmount = ctx.getArgument("suction amount", int.class);

                                long nextSuction = (long) (System.currentTimeMillis() + (suctionSpeed * 1000));
                                long nextTransfer = (long) (System.currentTimeMillis() + (skyHopperConfigSettings.startingTransferSpeed() * 1000));

                                SkyHopper skyHopper = new SkyHopper(settings);

                                skyHopper.setSuctionSpeed(suctionSpeed);
                                skyHopper.setMaxSuctionSpeed(suctionSpeed);
                                skyHopper.setSuctionAmount(suctionAmount);
                                skyHopper.setMaxSuctionAmount(suctionAmount);

                                skyHopper.setNextSuctionTime(nextSuction);
                                skyHopper.setNextTransferTime(nextTransfer);

                                ItemStack itemStack = hopperManager.getSkyHopperCreator().createSkyHopperItemStack(skyHopper, amount);

                                if(itemStack != null) {
                                    target.getInventory().addItem(itemStack);

                                    target.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.hopperGiven()));

                                    sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.hopperGivenTo(), List.of(Placeholder.parsed("player", target.getName()))));

                                    return 1;
                                } else {
                                    sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.skyhopperCreationFailed()));

                                    return 0;
                                }
                            }))
                        .executes(ctx -> {
                            Locale locale = localeManager.getLocale();
                            CommandSender sender = ctx.getSource().getSender();

                            Settings settings = settingsManager.getSettings();
                            if(settings == null) {
                                sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.skyhopperCreationFailed()));
                                sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.invalidSettings()));

                                return 0;
                            }
                            Settings.SkyHopperConfig skyHopperConfigSettings = settings.skyHopperConfig();

                            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player name", PlayerSelectorArgumentResolver.class);
                            Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                            int amount = ctx.getArgument("amount",  int.class);
                            double suctionSpeed = ctx.getArgument("suction speed", double.class);

                            long nextSuction = (long) (System.currentTimeMillis() + (suctionSpeed * 1000));
                            long nextTransfer = (long) (System.currentTimeMillis() + (skyHopperConfigSettings.startingTransferSpeed() * 1000));

                            SkyHopper skyHopper = new SkyHopper(settings);

                            skyHopper.setSuctionSpeed(suctionSpeed);
                            skyHopper.setMaxSuctionSpeed(suctionSpeed);

                            skyHopper.setNextSuctionTime(nextSuction);
                            skyHopper.setNextTransferTime(nextTransfer);

                            ItemStack itemStack = hopperManager.getSkyHopperCreator().createSkyHopperItemStack(skyHopper, amount);

                            if(itemStack != null) {
                                target.getInventory().addItem(itemStack);

                                target.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.hopperGiven()));

                                sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.hopperGivenTo(), List.of(Placeholder.parsed("player", target.getName()))));

                                return 1;
                            } else {
                                sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.skyhopperCreationFailed()));

                                return 0;
                            }
                        }))
                    .executes(ctx -> {
                        Locale locale = localeManager.getLocale();
                        CommandSender sender = ctx.getSource().getSender();

                        Settings settings = settingsManager.getSettings();
                        if(settings == null) {
                            sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.skyhopperCreationFailed()));
                            sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.invalidSettings()));

                            return 0;
                        }
                        Settings.SkyHopperConfig skyHopperConfigSettings = settings.skyHopperConfig();

                        PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player name", PlayerSelectorArgumentResolver.class);
                        Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                        int amount = ctx.getArgument("amount",  int.class);

                        long nextSuction = (long) (System.currentTimeMillis() + (skyHopperConfigSettings.startingSuctionSpeed() * 1000));
                        long nextTransfer = (long) (System.currentTimeMillis() + (skyHopperConfigSettings.startingTransferSpeed() * 1000));

                        SkyHopper skyHopper = new SkyHopper(settings);

                        skyHopper.setNextSuctionTime(nextSuction);
                        skyHopper.setNextTransferTime(nextTransfer);

                        ItemStack itemStack = hopperManager.getSkyHopperCreator().createSkyHopperItemStack(skyHopper, amount);

                        if(itemStack != null) {
                            target.getInventory().addItem(itemStack);

                            target.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.hopperGiven()));

                            sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.hopperGivenTo(), List.of(Placeholder.parsed("player", target.getName()))));

                            return 1;
                        } else {
                            sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.skyhopperCreationFailed()));

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
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.itemNotSkyHopper()));
                        return 0;
                    }

                    SkyHopper skyHopper = hopperManager.getSkyHopperProcessor().loadSkyHopper(null, itemStack.getItemMeta().getPersistentDataContainer());
                    if(skyHopper == null) return 0;

                    if(skyHopper.getOwner() != null) {
                        if(skyHopper.getOwner().equals(playerUUID) || skyHopper.getMembers().contains(playerUUID) || player.hasPermission("skyhoppers.admin")) {
                            ItemStack newStack = hopperManager.getSkyHopperCreator().createSkyHopperItemStack(skyHopper, 1);
                            player.getInventory().setItemInMainHand(newStack);

                            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.newOwner(), List.of(Placeholder.parsed("player_name", newOwner.getName()))));
                            return 1;
                        } else {
                            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.noAccessOwnerChange()));
                            return 0;
                        }
                    } else {
                        if(skyHopper.getMembers().contains(playerUUID) || player.hasPermission("skyhoppers.admin")) {
                            ItemStack newStack = hopperManager.getSkyHopperCreator().createSkyHopperItemStack(skyHopper, 1);
                            player.getInventory().setItemInMainHand(newStack);

                            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.newOwner(), List.of(Placeholder.parsed("player_name", newOwner.getName()))));
                            return 1;
                        } else {
                            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.noAccessOwnerChange()));
                            return 0;
                        }
                    }
            }))
        );

        builder.then(Commands.literal("load")
            .requires(ctx -> ctx.getSender().hasPermission("skyhoppers.commands.skyhoppers.load"))
            .executes(ctx -> {
                Locale locale = localeManager.getLocale();
                CommandSender sender = ctx.getSource().getSender();

                hopperManager.getSkyHopperProcessor().queueLoadedChunks();

                sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.skyhoppersLoaded()));

                return 1;
            })
        );
        
         builder.then(Commands.literal("pause")
            .requires(ctx -> ctx.getSender().hasPermission("skyhoppers.commands.skyhoppers.pause"))
            .executes(ctx -> {
                Locale locale = localeManager.getLocale();
                CommandSender sender = ctx.getSource().getSender();
                
                plugin.pauseSkyHoppers();

                sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.skyHoppersPaused()));

                return 1;
            })
         );
         
          builder.then(Commands.literal("unpause")
              .requires(ctx -> ctx.getSender().hasPermission("skyhoppers.commands.skyhoppers.unpause"))
              .executes(ctx -> {
                  Locale locale = localeManager.getLocale();
                  CommandSender sender = ctx.getSource().getSender();
                
                  plugin.unPauseSkyHoppers();

                  sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.skyhoppersUnpaused()));

                  return 1;
            })
         );

          builder.then(Commands.literal("removeowner")
                  .requires(ctx -> ctx.getSender().hasPermission("skyhoppers.commands.skyhoppers.removeowner") && ctx.getSender() instanceof Player)
                  .executes(ctx -> {
                      Locale locale = localeManager.getLocale();
                      Player player = (Player) ctx.getSource().getSender();
                      ItemStack itemStack = player.getInventory().getItemInMainHand();

                      if(!hopperManager.isItemStackSkyHopper(itemStack)) {
                          player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.itemNotSkyHopper()));
                          return 0;
                      }

                      SkyHopper skyHopper = hopperManager.getSkyHopperProcessor().loadSkyHopper(null, itemStack.getItemMeta().getPersistentDataContainer());
                      if(skyHopper == null) {
                          player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.itemNotSkyHopper()));
                          return 0;
                      }

                      skyHopper.setOwner(null);

                      ItemStack newStack = hopperManager.getSkyHopperCreator().createSkyHopperItemStack(skyHopper, 1);

                      player.getInventory().setItemInMainHand(newStack);

                      return 1;
                }));

          return builder.build();
    }
}