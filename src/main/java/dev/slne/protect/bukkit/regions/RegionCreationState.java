package dev.slne.protect.bukkit.regions;

public enum RegionCreationState {
	MORE_MARKERS_NEEDED,
	OVERLAPPING,

	TOO_EXPENSIVE,
	TOO_LARGE,
	TOO_SMALL,

	SUCCESS;
}
