package com.github.lukesky19.skyHoppers.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * This record stores the data for a {@link Location} in an immutable format.
 * @param worldReference A {@link WeakReference} containing a {@link World}.
 * @param x The x coordinate.
 * @param y The y coordinate.
 * @param z The z coordinate.
 */
public record ImmutableLocation(@NotNull WeakReference<World> worldReference, int x, int y, int z) {
    /**
     * Constructor
     * @param world A {@link World}.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     */
    public ImmutableLocation(@NotNull World world, int x, int y, int z) {
        this(new WeakReference<>(world), x, y, z);
    }

    /**
     * Create an {@link ImmutableLocation} from a {@link Location}.
     * @param location The {@link Location} to create the {@link ImmutableLocation} from.
     * @return An {@link ImmutableLocation}.
     * @throws RuntimeException If the {@link Location}'s world is null.
     */
    public static @NotNull ImmutableLocation fromBukkitLocation(@NotNull Location location) {
        @Nullable World world = location.getWorld();
        if (world == null) {
            throw new RuntimeException("Unable to create ImmutableLocation. The world in the bukkit location provided is null.");
        }

        return new ImmutableLocation(world, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Create a {@link Location} from this {@link ImmutableLocation}.
     * @return A {@link Location}.
     * @throws RuntimeException if the world reference is no longer valid.
     */
    public @NotNull Location toBukkitLocation() {
        return new Location(getWorld(), x, y, z);
    }

    /**
     * Get the {@link World} for this location.
     * @return The {@link World} or null.
     * @throws RuntimeException if the world reference is no longer valid.
     */
    public @NotNull World getWorld() {
        @Nullable World world = worldReference.get();
        if (world == null) {
            throw new RuntimeException("World reference is invalid. Likely unloaded.");
        }
        return world;
    }

    /**
     * Get the x coordinate of the location.
     * @return The x coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Get the y coordinate of the location.
     * @return The y coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * Get the z coordinate of the location.
     * @return The z coordinate.
     */
    public int getZ() {
        return z;
    }

    /**
     * Get the {@link Block} at this location.
     * @return The {@link Block}.
     * @throws RuntimeException if the world reference is no longer valid.
     */
    public @NotNull Block getBlock() {
        return getWorld().getBlockAt(x, y, z);
    }

    /**
     * Get the {@link BlockState} at this location.
     * This returns the live state, not a snapshot of the {@link BlockState}.
     * @return The {@link BlockState}.
     * @throws RuntimeException if the world reference is no longer valid.
     */
    public @NotNull BlockState getBlockState() {
        return getWorld().getBlockAt(x, y, z).getState(false);
    }

    /**
     * Is the chunk at this location not loaded?
     * @return true if not loaded, false if loaded.
     * @throws RuntimeException if the world reference is no longer valid.
     */
    public boolean isChunkUnloaded() {
        return !this.getWorld().isChunkLoaded(x >> 4, z >> 4);
    }

    /**
     * Check if the object provided is equal to this ImmutableLocation.
     * @param obj The object to compare.
     * @return true if the object is an ImmutableLocation and both are equal according to their world names, x, y, and z coordinates.
     */
    @Override
    public boolean equals(@NotNull Object obj) {
        if(!(obj instanceof ImmutableLocation compareLocation)) return false;

        @Nullable World world = worldReference.get();
        if(world == null) {
            throw new RuntimeException("World reference is invalid. Likely unloaded.");
        }

        return compareLocation.getWorld().getName().equals(world.getName())
                && x == compareLocation.getX()
                && y == compareLocation.getY()
                && z == compareLocation.getZ();
    }

    /**
     * Create a hash code for this ImmutableLocation.
     * @return An int.
     * @throws RuntimeException if the world reference is invalid.
     */
    @Override
    public int hashCode() throws RuntimeException {
        @Nullable World world = worldReference.get();
        if(world == null) {
            throw new RuntimeException("World reference is invalid. Likely unloaded.");
        }

        int result = 7;
        result = 31 * result + world.getName().hashCode();
        result = 31 * result + Integer.hashCode(x);
        result = 31 * result + Integer.hashCode(y);
        result = 31 * result + Integer.hashCode(z);

        return result;
    }
}