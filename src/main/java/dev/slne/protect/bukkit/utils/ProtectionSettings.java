package dev.slne.protect.bukkit.utils;

import org.bukkit.HeightMap;
import org.bukkit.Material;

import com.sk89q.worldguard.protection.flags.StateFlag;

public class ProtectionSettings {
	public static final int MARKERS = 8;
	public static final int REGION_CREATION_COOLDOWN = 30;
	public static final int PRICE_PER_BLOCK = 5;

	public static final StateFlag SURVIVAL_PROTECT = new StateFlag("can-survival-protect", false);
	public static final StateFlag SURVIVAL_CAN_SELL_FLAG = new StateFlag("can-survival-protect-sell", true);
	public static final ProtectionFlag SURVIVAL_PROTECT_FLAG = new ProtectionFlag("survival-protect");

	public static final String METADATA_KEY = "MTIFCAD", MARKER_KEY = "SAFIUZdfx",
			ITEMSTACKMARKER_KEY = "HGFGHJIOIZTRFGB", RAW_SIGN = "FSOSA", OBJECTED_SIGN = "SFDSADA";
	public static final float RETAIL_MODIFIER = 0.5f;

	public static final int AREA_MAX_BLOCKS = Integer.MAX_VALUE;
	public static final int AREA_MIN_BLOCKS = 250;

	public static final int MIN_Y_WORLD = -64;
	public static final int MAX_Y_WORLD = 319;
	public static final double MAX_EXPAND_DISTANCE = 20;

	public static final HeightMap PROTECTION_HEIGHTMAP = HeightMap.MOTION_BLOCKING_NO_LEAVES;
	public static final Material TRAIL_MATERIAL = Material.STONE_BRICK_SLAB;

}
