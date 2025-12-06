package com.github.lukesky19.skyHoppers.util;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This record is used to store a location and uuid in a Map.
 * @param location The {@link Location}.
 * @param uuid The {@link UUID}.
 */
public record LocationUUIDKey(@NotNull Location location, @NotNull UUID uuid) {}
