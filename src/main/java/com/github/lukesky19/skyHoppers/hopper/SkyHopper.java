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
package com.github.lukesky19.skyHoppers.hopper;

import org.bukkit.Location;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Contains the data for a SkyHopper.
 */
public class SkyHopper {
    // Settings
    private boolean enabled;
    private boolean particles;

    // Player Data
    private @Nullable UUID owner;
    private final @NotNull List<@NotNull UUID> members = new ArrayList<>();

    // Location
    private @Nullable Location location;

    // Linked Containers
    private final @NotNull List<@NotNull SkyContainer> linkedContainers = new ArrayList<>();

    // Filter
    private @NotNull FilterType filterType;
    private final @NotNull List<@NotNull ItemType> filterItems = new ArrayList<>();

    // Upgrades
    private double transferSpeed;
    private double maxTransferSpeed;
    private int transferAmount;
    private int maxTransferAmount;
    private double suctionSpeed;
    private double maxSuctionSpeed;
    private int suctionAmount;
    private int maxSuctionAmount;
    private int suctionRange;
    private int maxSuctionRange;
    private int maxContainers;

    // Next Task Times
    private long nextSuctionTime;
    private long nextTransferTime;

    /**
     * Constructor
     * @param enabled Is the SkyHopper enabled?
     * @param particles Are particles enabled for this SkyHopper?
     * @param owner The {@link UUID} of the owner of this SkyHopper.
     * @param members The {@link List} of {@link UUID} that can also access this SkyHopper.
     * @param location The {@link Location} of the SkyHopper.
     * @param linkedContainers The {@link List} of {@link SkyContainer}s that are linked to this SkyHopper.
     * @param filterType The {@link FilterType} of the SkyHopper.
     * @param filterItems The {@link List} of {@link ItemType} that are filtered.
     * @param transferSpeed The transfer speed of the SkyHopper.
     * @param maxTransferSpeed The max transfer speed of the SkyHopper.
     * @param transferAmount The transfer amount of the SkyHopper.
     * @param maxTransferAmount The max transfer amount of the SkyHopper.
     * @param suctionSpeed The suction speed of the SkyHopper.
     * @param maxSuctionSpeed The max suction speed of the SkyHopper.
     * @param suctionRange The suction range of the SkyHopper.
     * @param maxSuctionRange The max suction range of the SkyHopper.
     * @param maxContainers The max number of containers that can be linked.
     * @param nextSuctionTime When the next suction should occur.
     * @param nextTransferTime When the next transfer should occur.
     */
    public SkyHopper(
            boolean enabled,
            boolean particles,
            @Nullable UUID owner,
            @NotNull List<UUID> members,
            @Nullable Location location,
            @NotNull List<SkyContainer> linkedContainers,
            @NotNull FilterType filterType,
            @NotNull List<ItemType> filterItems,
            double transferSpeed,
            double maxTransferSpeed,
            int transferAmount,
            int maxTransferAmount,
            double suctionSpeed,
            double maxSuctionSpeed,
            int suctionAmount,
            int maxSuctionAmount,
            int suctionRange,
            int maxSuctionRange,
            int maxContainers,
            long nextSuctionTime,
            long nextTransferTime) {
        this.enabled = enabled;
        this.particles = particles;
        this.owner = owner;
        this.members.addAll(members);

        if(location != null) {
            this.location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        } else {
            this.location = null;
        }

        this.linkedContainers.addAll(linkedContainers);
        this.filterType = filterType;
        this.filterItems.addAll(filterItems);
        this.transferSpeed = transferSpeed;
        this.maxTransferSpeed = maxTransferSpeed;
        this.transferAmount = transferAmount;
        this.maxTransferAmount = maxTransferAmount;
        this.suctionSpeed = suctionSpeed;
        this.maxSuctionSpeed = maxSuctionSpeed;
        this.suctionAmount = suctionAmount;
        this.maxSuctionAmount = maxSuctionAmount;
        this.suctionRange = suctionRange;
        this.maxSuctionRange = maxSuctionRange;
        this.maxContainers = maxContainers;
        this.nextSuctionTime = nextSuctionTime;
        this.nextTransferTime = nextTransferTime;
    }

    /**
     * Is the SkyHopper enabled for suction and transfer?
     * @return true if enabled, otherwise false.
     */
    public boolean isSkyHopperEnabled() {
        return enabled;
    }

    /**
     * Toggle whether the SkyHopper is enabled or not.
     */
    public void toggleEnabled() {
        enabled = !enabled;
    }

    /**
     * Are particles enabled for this SkyHopper?
     * @return true if enabled, otherwise false.
     */
    public boolean isParticlesEnabled() {
        return particles;
    }

    /**
     * Toggle whether particles are enabled or not.
     */
    public void toggleParticles() {
        particles = !particles;
    }

    /**
     * Get the owner of the SkyHopper.
     * @return A {@link UUID} of the owner or null.
     */
    public @Nullable UUID getOwner() {
        return owner;
    }

    /**
     * Set the owner of the SkyHopper.
     * @param owner The {@link UUID} of the new owner.
     */
    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
    }

    /**
     * Add a {@link UUID} to the members list.
     * @param uuid The {@link UUID} to add.
     */
    public void addMember(@NotNull UUID uuid) {
        if(!members.contains(uuid)) {
            members.add(uuid);
        }
    }

    /**
     * Remove a {@link UUID} from the members list.
     * @param uuid The {@link UUID} to remove.
     */
    public void removeMember(@NotNull UUID uuid) {
        members.remove(uuid);
    }

    /**
     * Get a {@link List} of {@link UUID}s for the members of this SkyHopper.
     * @return A {@link List} of {@link UUID}s for the members of this SkyHopper.
     */
    public @NotNull List<UUID> getMembers() {
        return members;
    }

    /**
     * Get the {@link Location} of the SkyHopper.
     * @return A copy of the {@link Location} of the SkyHopper.
     */
    public @Nullable Location getLocation() {
        if(location == null) return null;

        return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Set the {@link Location} of the SkyHopper. Will create a copy of the {@link Location} provided before using,
     */
    public void setLocation(@Nullable Location location) {
        if(location != null) {
            this.location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
            return;
        }

        this.location = null;
    }

    /**
     * Add a {@link SkyContainer} to the linked containers.
     * @param skyContainer The {@link SkyContainer} to add.
     */
    public void addLinkedContainer(@NotNull SkyContainer skyContainer) {
        linkedContainers.add(skyContainer);
    }

    /**
     * Remove a {@link SkyContainer} from the linked containers.
     * @param skyContainer The {@link SkyContainer} to remove.
     */
    public void removeLinkedContainer(@NotNull SkyContainer skyContainer) {
        linkedContainers.remove(skyContainer);
    }

    /**
     * Get the {@link List} of {@link SkyContainer}s that are linked to the SkyHopper.
     * @return A {@link List} of {@link SkyContainer}s.
     */
    public @NotNull List<SkyContainer> getLinkedContainers() {
        return linkedContainers;
    }

    /**
     * Get the {@link FilterType} of the SkyHopper.
     * @return The {@link FilterType} of the SkyHopper.
     */
    public @NotNull FilterType getFilterType() {
        return filterType;
    }

    /**
     * Set the {@link FilterType} of the SkyHopper.
     * @param filterType The {@link FilterType} to set.
     */
    public void setFilterType(@NotNull FilterType filterType) {
        this.filterType = filterType;
    }

    /**
     * Add an {@link ItemType} to the filter items.
     * @param itemType The {@link ItemType} to add.
     */
    public void addFilterItem(@NotNull ItemType itemType) {
        if(!filterItems.contains(itemType)) {
            filterItems.add(itemType);
        }
    }

    /**
     * Remove an {@link ItemType} from the filter items.
     * @param itemType The {@link ItemType} to remove.
     */
    public void removeFilterItem(@NotNull ItemType itemType) {
        filterItems.remove(itemType);
    }

    /**
     * Get the {@link List} of {@link ItemType}s that are filtered.
     * @return A {@link List} of {@link ItemType}.
     */
    public @NotNull List<ItemType> getFilterItems() {
        return filterItems;
    }

    /**
     * Get the transfer speed of the SkyHopper.
     * @return The transfer speed of the SkyHopper.
     */
    public double getTransferSpeed() {
        return transferSpeed;
    }

    /**
     * Set the transfer speed of the SkyHopper.
     * @param transferSpeed The transfer speed to set.
     */
    public void setTransferSpeed(double transferSpeed) {
        this.transferSpeed = transferSpeed;
    }

    /**
     * Get the max transfer speed of the SkyHopper.
     * @return The max transfer speed of the SkyHopper.
     */
    public double getMaxTransferSpeed() {
        return maxTransferSpeed;
    }

    /**
     * Set the max transfer speed of the SkyHopper.
     * @param maxTransferSpeed The max transfer speed to set.
     */
    public void setMaxTransferSpeed(double maxTransferSpeed) {
        this.maxTransferSpeed = maxTransferSpeed;
    }

    /**
     * Get the transfer amount of the SkyHopper.
     * @return The transfer amount of the SkyHopper.
     */
    public int getTransferAmount() {
        return transferAmount;
    }

    /**
     * Set the transfer amount of the SkyHopper.
     * @param transferAmount The transfer amount to set.
     */
    public void setTransferAmount(int transferAmount) {
        this.transferAmount = transferAmount;
    }

    /**
     * Get the max transfer amount of the SkyHopper.
     * @return The max transfer amount of the SkyHopper.
     */
    public int getMaxTransferAmount() {
        return maxTransferAmount;
    }

    /**
     * Set the max transfer amount of the SkyHopper.
     * @param maxTransferAmount The max transfer amount to set.
     */
    public void setMaxTransferAmount(int maxTransferAmount) {
        this.maxTransferAmount = maxTransferAmount;
    }

    /**
     * Get the suction speed of the SkyHopper.
     * @return The suction speed of the SkyHopper.
     */
    public double getSuctionSpeed() {
        return suctionSpeed;
    }

    /**
     * Set the suction speed of the SkyHopper.
     * @param suctionSpeed The suction speed to set.
     */
    public void setSuctionSpeed(double suctionSpeed) {
        this.suctionSpeed = suctionSpeed;
    }

    /**
     * Get the max suction speed of the SkyHopper.
     * @return The max suction speed of the SkyHopper.
     */
    public double getMaxSuctionSpeed() {
        return maxSuctionSpeed;
    }

    /**
     * Set the max suction speed of the SkyHopper.
     * @param maxSuctionSpeed The max suction speed to set.
     */
    public void setMaxSuctionSpeed(double maxSuctionSpeed) {
        this.maxSuctionSpeed = maxSuctionSpeed;
    }

    /**
     * Get the suction amount of the SkyHopper.
     * @return The suction amount of the SkyHopper.
     */
    public int getSuctionAmount() {
        return suctionAmount;
    }

    /**
     * Set the suction amount of the SkyHopper.
     * @param suctionAmount The suction amount to set.
     */
    public void setSuctionAmount(int suctionAmount) {
        this.suctionAmount = suctionAmount;
    }

    /**
     * Get the max suction amount of the SkyHopper.
     * @return The max suction amount of the SkyHopper.
     */
    public int getMaxSuctionAmount() {
        return maxSuctionAmount;
    }

    /**
     * Set the max suction amount of the SkyHopper.
     * @param maxSuctionAmount The max suction amount to set.
     */
    public void setMaxSuctionAmount(int maxSuctionAmount) {
        this.maxSuctionAmount = maxSuctionAmount;
    }

    /**
     * Get the suction range of the SkyHopper.
     * @return The suction range of the SkyHopper.
     */
    public int getSuctionRange() {
        return suctionRange;
    }

    /**
     * Set the suction range of the SkyHopper.
     * @param suctionRange The suction range to set.
     */
    public void setSuctionRange(int suctionRange) {
        this.suctionRange = suctionRange;
    }

    /**
     * Get the max suction range of the SkyHopper.
     * @return The max suction range of the SkyHopper.
     */
    public int getMaxSuctionRange() {
        return maxSuctionRange;
    }

    /**
     * Set the max suction range of the SkyHopper.
     * @param maxSuctionRange The max suction range to set.
     */
    public void setMaxSuctionRange(int maxSuctionRange) {
        this.maxSuctionRange = maxSuctionRange;
    }

    /**
     * Get the maximum number of containers that can be linked to this SkyHopper.
     */
    public int getMaxContainers() {
        return maxContainers;
    }

    /**
     * Set the maximum number of containers that can be linked to this SkyHopper.
     * @param maxContainers The maximum number of containers that can be linked to this SkyHopper.
     */
    public void setMaxContainers(int maxContainers) {
        this.maxContainers = maxContainers;
    }

    public long getNextSuctionTime() {
        return nextSuctionTime;
    }

    public void setNextSuctionTime(long nextSuctionTime) {
        this.nextSuctionTime = nextSuctionTime;
    }

    public long getNextTransferTime() {
        return nextTransferTime;
    }

    public void setNextTransferTime(long nextTransferTime) {
        this.nextTransferTime = nextTransferTime;
    }
}
