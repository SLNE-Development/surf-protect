package dev.slne.surf.protect.paper.flags

import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.flags.StateFlag.State
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import net.kyori.adventure.text.Component
import org.bukkit.Material

enum class ProtectionFlags(
    val material: Material,
    val displayName: Component,
    val descriptionString: String,
    val flat: StateFlag,
    val initialState: State?,
    val toggleToState: State
) {
    CHEST_ACCESS(
        Material.CHEST, buildText {
            primary("Chest Access")
        },
        "Erlaube/Verbiete den Kistenzugang für alle Spieler", Flags.CHEST_ACCESS, null, State.ALLOW
    ),
    USE(
        Material.LEVER,
        buildText {
            primary("Use")
        },
        "Erlaube/Verbiete das Verwenden von Hebeln, Türen und so weiter für alle Spieler",
        Flags.USE,
        null,
        State.ALLOW
    ),
    DAMAGE_ANIMALS(
        Material.COOKED_BEEF,
        buildText {
            primary("Damage Animals")
        },
        "Erlaube/Verbiete das Töten von Tieren für alle Spieler",
        Flags.DAMAGE_ANIMALS,
        null,
        State.ALLOW
    ),
    SLEEP(
        Material.RED_BED, buildText {
            primary("Sleep")
        },
        "Erlaube/Verbiete das Verwenden von Betten für alle Spieler", Flags.SLEEP, null, State.ALLOW
    ),
    VEHICLE_PLACE(
        Material.MINECART,
        buildText {
            primary("Vehicle Place")
        },
        "Erlaube/Verbiete das Platzieren von Fahrzeugen für alle Spieler",
        Flags.PLACE_VEHICLE,
        null,
        State.ALLOW
    ),
    VEHICLE_DESTROY(
        Material.TNT_MINECART,
        buildText {
            primary("Vehicle Destroy")
        },
        "Erlaube/Verbiete das Zerstören von Fahrzeugen für alle Spieler",
        Flags.DESTROY_VEHICLE,
        null,
        State.ALLOW
    ),
    RIDE(
        Material.SADDLE, buildText {
            primary("Ride")
        },
        "Erlaube/Verbiete das Reiten von Tieren für alle Spieler", Flags.RIDE, null, State.ALLOW
    ),
    ITEM_FRAME_ROTATION(
        Material.ITEM_FRAME,
        buildText {
            primary("ItemFrames Rotation")
        },
        "Erlaube/Verbiete das Drehen von ItemFrames für alle Spieler",
        Flags.ITEM_FRAME_ROTATE,
        null,
        State.ALLOW
    ),
    USE_ANVIL(
        Material.ANVIL,
        buildText {
            primary("Anvil Usage")
        },
        "Erlaube/Verbiete das Benutzen von Anvils für alle Spieler",
        Flags.USE_ANVIL,
        null,
        State.ALLOW
    ),
    USE_DRIPLEAF(
        Material.BIG_DRIPLEAF,
        buildText {
            primary("Dripleaf Usage")
        },
        "Erlaube/Verbiete das Benutzen von Dripleaf für alle Spieler",
        Flags.USE_DRIPLEAF,
        null,
        State.ALLOW
    ),
    OTHER_EXPLOSION(
        Material.TNT,
        buildText {
            primary("Explosion Damage")
        },
        "Aktiviere/Deaktiviere den Blockschaden von Explosionen",
        Flags.OTHER_EXPLOSION,
        null,
        State.ALLOW
    ),
    ENDERMAN_GRIEF(
        Material.ENDERMAN_SPAWN_EGG, buildText {
            primary("Enderman Griefing")
        },
        "Aktiviere/Deaktiviere das Enderman Griefing", Flags.ENDER_BUILD, null, State.DENY
    ),
    FIRE_SPREAD(
        Material.FLINT_AND_STEEL, buildText {
            primary("Fire Spread")
        },
        "Aktiviere/Deaktiviere das Ausbreiten von Feuer", Flags.FIRE_SPREAD, State.DENY, State.DENY
    ),
    LEAF_DECAY(
        Material.OAK_LEAVES,
        buildText {
            primary("Leaf Decay")
        },
        "Aktiviere/Deaktiviere das natürliche Verschwinden von Blättern",
        Flags.LEAF_DECAY,
        null,
        State.DENY
    ),
    GRASS_GROWTH(
        Material.GRASS_BLOCK, buildText {
            primary("Grass Growth")
        },
        "Aktiviere/Deaktiviere das Verbreiten von Gras", Flags.GRASS_SPREAD, null, State.DENY
    ),
    MYCELIUM_SPREAD(
        Material.MYCELIUM, buildText {
            primary("Mycelium Spread")
        },
        "Aktiviere/Deaktiviere das Verbreiten von Myzel", Flags.MYCELIUM_SPREAD, null, State.DENY
    ),
    MUSHROOM_GROWTH(
        Material.RED_MUSHROOM, buildText {
            primary("Mushroom Growth")
        },
        "Aktiviere/Deaktiviere das Wachstum von Pilzen", Flags.MUSHROOMS, null, State.DENY
    ),
    VINE_GROWTH(
        Material.VINE, buildText {
            primary("Vine Growth")
        },
        "Aktiviere/Deaktiviere das Wachstum von Ranken", Flags.VINE_GROWTH, null, State.DENY
    ),
    ROCK_GROWTH(
        Material.DRIPSTONE_BLOCK, buildText {
            primary("Rock Growth")
        },
        "Aktiviere/Deaktiviere das Wachstum von Dripstones", Flags.ROCK_GROWTH, null, State.DENY
    ),
    SCULK_GROWTH(
        Material.SCULK_SENSOR, buildText {
            primary("Sculk Growth")
        },
        "Aktiviere/Deaktiviere das Verbreiten von Sculk", Flags.SCULK_GROWTH, null, State.DENY
    ),
    CROP_GROWTH(
        Material.WHEAT, buildText {
            primary("Crop Growth")
        },
        "Aktiviere/Deaktiviere das Wachstum von Nutzpflanzen", Flags.CROP_GROWTH, null, State.DENY
    ),
    CORAL_FADE(
        Material.BRAIN_CORAL, buildText {
            primary("Modify Selected Code")
        },
        "Aktiviere/Deaktiviere das Absterben von Korallen", Flags.CORAL_FADE, null, State.DENY
    ),
    SNOWMAN_TRAILS(
        Material.CARVED_PUMPKIN,
        buildText {
            primary("Snowman Trails")
        },
        "Aktiviere/Deaktiviere ob Snowmans Schnee hinterlassen",
        Flags.SNOWMAN_TRAILS,
        null,
        State.DENY
    ),
    SNOW_FALL(
        Material.SNOW, buildText {
            primary("Snow Fall")
        },
        "Aktiviere/Deaktiviere den Schneefall",
        Flags.SNOW_FALL, null, State.DENY
    ),
    SNOW_MELT(
        Material.SNOW, buildText {
            primary("Snow Melt")
        },
        "Aktiviere/Deaktiviere das Schmelzen von Schnee", Flags.SNOW_MELT, null, State.DENY
    ),
    ICE_FORM(
        Material.ICE, buildText {
            primary("Ice Form")
        },
        "Aktiviere/Deaktiviere das Formen von Eis",
        Flags.ICE_FORM, null, State.DENY
    ),
    ICE_MELT(
        Material.ICE, buildText {
            primary("Ice Melt")
        },
        "Aktiviere/Deaktiviere das Schmelzen von Eis", Flags.ICE_MELT, null, State.DENY
    );
}