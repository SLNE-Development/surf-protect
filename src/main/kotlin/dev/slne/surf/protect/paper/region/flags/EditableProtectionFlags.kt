package dev.slne.surf.protect.paper.region.flags

import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import org.bukkit.inventory.ItemType

/**
 * @see <a href="https://github.com/SLNE-Development/surf-protect/blob/18c4108a688c2e048d406203365ede81b801e0c1/src/main/java/dev/slne/protect/bukkit/gui/protection/flags/ProtectionFlagsMap.java">ProtectionFlagsMap</a>
 */
@Suppress("UnstableApiUsage")
enum class EditableProtectionFlags(
    val icon: ItemType,
    val displayName: String,
    val flag: StateFlag,
    val initialState: StateFlag.State?
) {
    CHEST_ACCESS(
        icon = ItemType.CHEST,
        displayName = "Chest Access",
        flag = Flags.CHEST_ACCESS,
        initialState = null
    ),
    USE(
        icon = ItemType.LEVER,
        displayName = "Use",
        flag = Flags.USE,
        initialState = null
    ),
    DAMAGE_ANIMALS(
        icon = ItemType.COOKED_BEEF,
        displayName = "Damage Animals",
        flag = Flags.DAMAGE_ANIMALS,
        initialState = null
    ),
    SLEEP(
        icon = ItemType.RED_BED,
        displayName = "Sleep",
        flag = Flags.SLEEP,
        initialState = null
    ),
    VEHICLE_PLACE(
        icon = ItemType.MINECART,
        displayName = "Vehicle Place",
        flag = Flags.PLACE_VEHICLE,
        initialState = null
    ),
    VEHICLE_DESTROY(
        icon = ItemType.TNT_MINECART,
        displayName = "Vehicle Destroy",
        flag = Flags.DESTROY_VEHICLE,
        initialState = null
    ),
    RIDE(
        icon = ItemType.SADDLE,
        displayName = "Ride",
        flag = Flags.RIDE,
        initialState = null
    ),
    ITEM_FRAME_ROTATION(
        icon = ItemType.ITEM_FRAME,
        displayName = "ItemFrames Rotation",
        flag = Flags.ITEM_FRAME_ROTATE,
        initialState = null
    ),
    USE_ANVIL(
        icon = ItemType.ANVIL,
        displayName = "Anvil Usage",
        flag = Flags.USE_ANVIL,
        initialState = null
    ),
    USE_DRIPLEAF(
        icon = ItemType.BIG_DRIPLEAF,
        displayName = "Dripleaf Usage",
        flag = Flags.USE_DRIPLEAF,
        initialState = null
    ),
    OTHER_EXPLOSION(
        icon = ItemType.TNT,
        displayName = "Explosion Damage",
        flag = Flags.OTHER_EXPLOSION,
        initialState = null
    ),
    ENDERMAN_GRIEF(
        icon = ItemType.ENDERMAN_SPAWN_EGG,
        displayName = "Enderman Griefing",
        flag = Flags.ENDER_BUILD,
        initialState = null
    ),
    FIRE_SPREAD(
        icon = ItemType.FLINT_AND_STEEL,
        displayName = "Fire Spread",
        flag = Flags.FIRE_SPREAD,
        initialState = StateFlag.State.DENY
    ),
    LEAF_DECAY(
        icon = ItemType.OAK_LEAVES,
        displayName = "Leaf Decay",
        flag = Flags.LEAF_DECAY,
        initialState = null
    ),
    GRASS_GROWTH(
        icon = ItemType.GRASS_BLOCK,
        displayName = "Grass Growth",
        flag = Flags.GRASS_SPREAD,
        initialState = null
    ),
    MYCELIUM_SPREAD(
        icon = ItemType.MYCELIUM,
        displayName = "Mycelium Spread",
        flag = Flags.MYCELIUM_SPREAD,
        initialState = null
    ),
    MUSHROOM_GROWTH(
        icon = ItemType.RED_MUSHROOM,
        displayName = "Mushroom Growth",
        flag = Flags.MUSHROOMS,
        initialState = null
    ),
    VINE_GROWTH(
        icon = ItemType.VINE,
        displayName = "Vine Growth",
        flag = Flags.VINE_GROWTH,
        initialState = null
    ),
    ROCK_GROWTH(
        icon = ItemType.DRIPSTONE_BLOCK,
        displayName = "Rock Growth",
        flag = Flags.ROCK_GROWTH,
        initialState = null
    ),
    SCULK_GROWTH(
        icon = ItemType.SCULK_SENSOR,
        displayName = "Sculk Growth",
        flag = Flags.SCULK_GROWTH,
        initialState = null
    ),
    CROP_GROWTH(
        icon = ItemType.WHEAT,
        displayName = "Crop Growth",
        flag = Flags.CROP_GROWTH,
        initialState = null
    ),
    CORAL_FADE(
        icon = ItemType.BRAIN_CORAL,
        displayName = "Coral Fade",
        flag = Flags.CORAL_FADE,
        initialState = null
    ),
    SNOWMAN_TRAILS(
        icon = ItemType.CARVED_PUMPKIN,
        displayName = "Snowman Trails",
        flag = Flags.SNOWMAN_TRAILS,
        initialState = null
    ),
    SNOW_FALL(
        icon = ItemType.SNOW,
        displayName = "Snow Fall",
        flag = Flags.SNOW_FALL,
        initialState = null
    ),
    SNOW_MELT(
        icon = ItemType.SNOW,
        displayName = "Snow Melt",
        flag = Flags.SNOW_MELT,
        initialState = null
    ),
    ICE_FORM(
        icon = ItemType.ICE,
        displayName = "Ice Form",
        flag = Flags.ICE_FORM,
        initialState = null
    ),
    ICE_MELT(
        icon = ItemType.ICE,
        displayName = "Ice Melt",
        flag = Flags.ICE_MELT,
        initialState = null
    );

    val component = SurfComponentBuilder { primary(displayName) }
}