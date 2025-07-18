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
    displayName: SurfComponentBuilder.() -> Unit,
    val flag: StateFlag,
    val initialState: StateFlag.State?
) {
    CHEST_ACCESS(
        icon = ItemType.CHEST,
        displayName = { primary("Chest Access") },
        flag = Flags.CHEST_ACCESS,
        initialState = null
    ),
    USE(
        icon = ItemType.LEVER,
        displayName = { primary("Use") },
        flag = Flags.USE,
        initialState = null
    ),
    DAMAGE_ANIMALS(
        icon = ItemType.COOKED_BEEF,
        displayName = { primary("Damage Animals") },
        flag = Flags.DAMAGE_ANIMALS,
        initialState = null
    ),
    SLEEP(
        icon = ItemType.RED_BED,
        displayName = { primary("Sleep") },
        flag = Flags.SLEEP,
        initialState = null
    ),
    VEHICLE_PLACE(
        icon = ItemType.MINECART,
        displayName = { primary("Vehicle Place") },
        flag = Flags.PLACE_VEHICLE,
        initialState = null
    ),
    VEHICLE_DESTROY(
        icon = ItemType.TNT_MINECART,
        displayName = { primary("Vehicle Destroy") },
        flag = Flags.DESTROY_VEHICLE,
        initialState = null
    ),
    RIDE(
        icon = ItemType.SADDLE,
        displayName = { primary("Ride") },
        flag = Flags.RIDE,
        initialState = null
    ),
    ITEM_FRAME_ROTATION(
        icon = ItemType.ITEM_FRAME,
        displayName = { primary("ItemFrames Rotation") },
        flag = Flags.ITEM_FRAME_ROTATE,
        initialState = null
    ),
    USE_ANVIL(
        icon = ItemType.ANVIL,
        displayName = { primary("Anvil Usage") },
        flag = Flags.USE_ANVIL,
        initialState = null
    ),
    USE_DRIPLEAF(
        icon = ItemType.BIG_DRIPLEAF,
        displayName = { primary("Dripleaf Usage") },
        flag = Flags.USE_DRIPLEAF,
        initialState = null
    ),
    OTHER_EXPLOSION(
        icon = ItemType.TNT,
        displayName = { primary("Explosion Damage") },
        flag = Flags.OTHER_EXPLOSION,
        initialState = null
    ),
    ENDERMAN_GRIEF(
        icon = ItemType.ENDERMAN_SPAWN_EGG,
        displayName = { primary("Enderman Griefing") },
        flag = Flags.ENDER_BUILD,
        initialState = null
    ),
    FIRE_SPREAD(
        icon = ItemType.FLINT_AND_STEEL,
        displayName = { primary("Fire Spread") },
        flag = Flags.FIRE_SPREAD,
        initialState = StateFlag.State.DENY
    ),
    LEAF_DECAY(
        icon = ItemType.OAK_LEAVES,
        displayName = { primary("Leaf Decay") },
        flag = Flags.LEAF_DECAY,
        initialState = null
    ),
    GRASS_GROWTH(
        icon = ItemType.GRASS_BLOCK,
        displayName = { primary("Grass Growth") },
        flag = Flags.GRASS_SPREAD,
        initialState = null
    ),
    MYCELIUM_SPREAD(
        icon = ItemType.MYCELIUM,
        displayName = { primary("Mycelium Spread") },
        flag = Flags.MYCELIUM_SPREAD,
        initialState = null
    ),
    MUSHROOM_GROWTH(
        icon = ItemType.RED_MUSHROOM,
        displayName = { primary("Mushroom Growth") },
        flag = Flags.MUSHROOMS,
        initialState = null
    ),
    VINE_GROWTH(
        icon = ItemType.VINE,
        displayName = { primary("Vine Growth") },
        flag = Flags.VINE_GROWTH,
        initialState = null
    ),
    ROCK_GROWTH(
        icon = ItemType.DRIPSTONE_BLOCK,
        displayName = { primary("Rock Growth") },
        flag = Flags.ROCK_GROWTH,
        initialState = null
    ),
    SCULK_GROWTH(
        icon = ItemType.SCULK_SENSOR,
        displayName = { primary("Sculk Growth") },
        flag = Flags.SCULK_GROWTH,
        initialState = null
    ),
    CROP_GROWTH(
        icon = ItemType.WHEAT,
        displayName = { primary("Crop Growth") },
        flag = Flags.CROP_GROWTH,
        initialState = null
    ),
    CORAL_FADE(
        icon = ItemType.BRAIN_CORAL,
        displayName = { primary("Coral Fade") },
        flag = Flags.CORAL_FADE,
        initialState = null
    ),
    SNOWMAN_TRAILS(
        icon = ItemType.CARVED_PUMPKIN,
        displayName = { primary("Snowman Trails") },
        flag = Flags.SNOWMAN_TRAILS,
        initialState = null
    ),
    SNOW_FALL(
        icon = ItemType.SNOW,
        displayName = { primary("Snow Fall") },
        flag = Flags.SNOW_FALL,
        initialState = null
    ),
    SNOW_MELT(
        icon = ItemType.SNOW,
        displayName = { primary("Snow Melt") },
        flag = Flags.SNOW_MELT,
        initialState = null
    ),
    ICE_FORM(
        icon = ItemType.ICE,
        displayName = { primary("Ice Form") },
        flag = Flags.ICE_FORM,
        initialState = null
    ),
    ICE_MELT(
        icon = ItemType.ICE,
        displayName = { primary("Ice Melt") },
        flag = Flags.ICE_MELT,
        initialState = null
    );

    val displayName = SurfComponentBuilder(displayName)
}