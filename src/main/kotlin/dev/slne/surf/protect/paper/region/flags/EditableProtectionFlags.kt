package dev.slne.surf.protect.paper.region.flags

import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import org.bukkit.inventory.ItemType

@Suppress("UnstableApiUsage")
enum class EditableProtectionFlags(
    val icon: ItemType,
    val displayName: String,
    val description: String,
    val flag: StateFlag,
    val initialState: StateFlag.State?,
    val isPlayerRelated: Boolean = false
) {
    CHEST_ACCESS(
        icon = ItemType.CHEST,
        displayName = "Kistenzugriff",
        description = "Legt fest, ob fremde Spieler Inventare wie Kisten, Fässer oder Shulkerboxen öffnen dürfen.",
        flag = Flags.CHEST_ACCESS,
        initialState = StateFlag.State.DENY,
        isPlayerRelated = true
    ),
    USE(
        icon = ItemType.LEVER,
        displayName = "Blockbenutzung",
        description = "Legt fest, ob fremde Spieler interaktive Blöcke wie Türen, Hebel oder Knöpfe nutzen dürfen.",
        flag = Flags.USE,
        initialState = StateFlag.State.DENY,
        isPlayerRelated = true
    ),
    DAMAGE_ANIMALS(
        icon = ItemType.COOKED_BEEF,
        displayName = "Tiere verletzen",
        description = "Legt fest, ob fremde Spieler Tiere verletzen dürfen.",
        flag = Flags.DAMAGE_ANIMALS,
        initialState = StateFlag.State.DENY,
        isPlayerRelated = true
    ),
    SLEEP(
        icon = ItemType.RED_BED,
        displayName = "Schlafen",
        description = "Legt fest, ob fremde Spieler in Betten schlafen dürfen.",
        flag = Flags.SLEEP,
        initialState = StateFlag.State.DENY,
        isPlayerRelated = true
    ),
    VEHICLE_PLACE(
        icon = ItemType.MINECART,
        displayName = "Fahrzeuge platzieren",
        description = "Legt fest, ob fremde Spieler Fahrzeuge wie bspw. Boote oder Loren platzieren dürfen.",
        flag = Flags.PLACE_VEHICLE,
        initialState = StateFlag.State.DENY,
        isPlayerRelated = true
    ),
    VEHICLE_DESTROY(
        icon = ItemType.TNT_MINECART,
        displayName = "Fahrzeuge zerstören",
        description = "Legt fest, ob Fahrzeuge zerstören dürfen.",
        flag = Flags.DESTROY_VEHICLE,
        initialState = StateFlag.State.DENY,
        isPlayerRelated = true
    ),
    RIDE(
        icon = ItemType.SADDLE,
        displayName = "Reiten",
        description = "Legt fest, ob fremde Spieler auf Tieren oder in Fahrzeugen reiten dürfen.",
        flag = Flags.RIDE,
        initialState = StateFlag.State.DENY,
        isPlayerRelated = true
    ),
    ITEM_FRAME_ROTATION(
        icon = ItemType.ITEM_FRAME,
        displayName = "Gegenstandsrahmen drehen",
        description = "Legt fest, ob fremde Spieler Gegenstände in Rahmen drehen dürfen.",
        flag = Flags.ITEM_FRAME_ROTATE,
        initialState = StateFlag.State.DENY,
        isPlayerRelated = true
    ),
    USE_ANVIL(
        icon = ItemType.ANVIL,
        displayName = "Amboss benutzen",
        description = "Legt fest, ob fremde Spieler einen Amboss benutzen dürfen.",
        flag = Flags.USE_ANVIL,
        initialState = StateFlag.State.DENY,
        isPlayerRelated = true
    ),
    USE_DRIPLEAF(
        icon = ItemType.BIG_DRIPLEAF,
        displayName = "Tropfblatt Nutzung",
        description = "Legt fest, ob fremde Spieler große Tropfblätter auslösen dürfen.",
        flag = Flags.USE_DRIPLEAF,
        initialState = StateFlag.State.DENY,
        isPlayerRelated = true
    ),

    OTHER_EXPLOSION(
        icon = ItemType.END_CRYSTAL,
        displayName = "Explosionsschaden",
        description = "Legt fest, ob Explosionen auf dem Grundstück Schaden verursachen dürfen.",
        flag = Flags.OTHER_EXPLOSION,
        initialState = StateFlag.State.DENY
    ),
    ENDERMAN_GRIEF(
        icon = ItemType.ENDERMAN_SPAWN_EGG,
        displayName = "Enderman-Griefing",
        description = "Legt fest, ob Endermänner Blöcke aufnehmen oder platzieren dürfen.",
        flag = Flags.ENDER_BUILD,
        initialState = StateFlag.State.DENY
    ),
    FIRE_SPREAD(
        icon = ItemType.FLINT_AND_STEEL,
        displayName = "Feuerausbreitung",
        description = "Legt fest, ob sich Feuer auf dem Grundstück natürlich ausbreiten darf.",
        flag = Flags.FIRE_SPREAD,
        initialState = StateFlag.State.DENY
    ),
    TNT(
        icon = ItemType.TNT,
        displayName = "TNT-Schaden",
        description = "Legt fest, ob TNT auf dem Grundstück Schaden verursachen darf.",
        flag = Flags.TNT,
        initialState = StateFlag.State.DENY
    ),
    LEAF_DECAY(
        icon = ItemType.OAK_LEAVES,
        displayName = "Blattverfall",
        description = "Legt fest, ob Blätter auf dem Grundstück natürlich verfallen dürfen.",
        flag = Flags.LEAF_DECAY,
        initialState = null
    ),
    GRASS_GROWTH(
        icon = ItemType.GRASS_BLOCK,
        displayName = "Graswachstum",
        description = "Legt fest, ob sich Gras natürlich auf Erde ausbreiten darf.",
        flag = Flags.GRASS_SPREAD,
        initialState = null
    ),
    MYCELIUM_SPREAD(
        icon = ItemType.MYCELIUM,
        displayName = "Myzelwachstum",
        description = "Legt fest, ob sich Myzel natürlich auf Erde ausbreiten darf.",
        flag = Flags.MYCELIUM_SPREAD,
        initialState = null
    ),
    MUSHROOM_GROWTH(
        icon = ItemType.RED_MUSHROOM,
        displayName = "Pilzwachstum",
        description = "Legt fest, ob Pilze auf dem Grundstück wachsen oder sich ausbreiten dürfen.",
        flag = Flags.MUSHROOMS,
        initialState = null
    ),
    VINE_GROWTH(
        icon = ItemType.VINE,
        displayName = "Rankenwachstum",
        description = "Legt fest, ob Ranken und Seetang wachsen oder sich ausbreiten dürfen.",
        flag = Flags.VINE_GROWTH,
        initialState = null
    ),
    ROCK_GROWTH(
        icon = ItemType.DRIPSTONE_BLOCK,
        displayName = "Tropfsteinwachstum",
        description = "Legt fest, ob Tropfstein natürlich wachsen oder sich verlängern darf.",
        flag = Flags.ROCK_GROWTH,
        initialState = null
    ),
    SCULK_GROWTH(
        icon = ItemType.SCULK_SENSOR,
        displayName = "Sculk-Ausbreitung",
        description = "Legt fest, ob sich Sculk auf dem Grundstück ausbreiten oder wachsen darf.",
        flag = Flags.SCULK_GROWTH,
        initialState = null
    ),
    CROP_GROWTH(
        icon = ItemType.WHEAT,
        displayName = "Pflanzenwachstum",
        description = "Legt fest, ob Nutzpflanzen auf dem Grundstück wachsen dürfen.",
        flag = Flags.CROP_GROWTH,
        initialState = null
    ),
    CORAL_FADE(
        icon = ItemType.BRAIN_CORAL,
        displayName = "Korallenverblassen",
        description = "Legt fest, ob Korallen ohne Wasser ausbleichen dürfen.",
        flag = Flags.CORAL_FADE,
        initialState = null
    ),
    CONCRETE_FORM(
        icon = ItemType.WHITE_CONCRETE_POWDER,
        displayName = "Beton-Verfestigung",
        description = "Legt fest, ob sich Beton auf deinem Grundstück durch Wasser verfestigt.",
        flag = ProtectionFlagsRegistry.CONCRETE_FORM,
        initialState = StateFlag.State.ALLOW
    ),
    SNOWMAN_TRAILS(
        icon = ItemType.CARVED_PUMPKIN,
        displayName = "Schneegolem-Spuren",
        description = "Legt fest, ob Schneegolems Schneespuren auf dem Boden hinterlassen dürfen.",
        flag = Flags.SNOWMAN_TRAILS,
        initialState = null
    ),
    SNOW_FALL(
        icon = ItemType.SNOW,
        displayName = "Schneefall",
        description = "Legt fest, ob fallender Schnee Blöcke auf dem Grundstück bedecken darf.",
        flag = Flags.SNOW_FALL,
        initialState = null
    ),
    SNOW_MELT(
        icon = ItemType.SNOW,
        displayName = "Schneeschmelze",
        description = "Legt fest, ob Schnee durch Licht oder Wärme schmelzen darf.",
        flag = Flags.SNOW_MELT,
        initialState = null
    ),
    ICE_FORM(
        icon = ItemType.ICE,
        displayName = "Eisbildung",
        description = "Legt fest, ob Wasser auf dem Grundstück zu Eis gefrieren darf.",
        flag = Flags.ICE_FORM,
        initialState = null
    ),
    ICE_MELT(
        icon = ItemType.ICE,
        displayName = "Eisschmelze",
        description = "Legt fest, ob Eis durch Licht oder Wärme auf dem Grundstück schmelzen darf.",
        flag = Flags.ICE_MELT,
        initialState = null
    ),
    BLOCK_GRAVITY(
        icon = ItemType.GRAVEL,
        displayName = "Blockgravitation",
        description = "Legt fest, ob Blöcke auf dem Grundstück durch Gravitation fallen dürfen.",
        flag = ProtectionFlagsRegistry.SURF_BLOCK_GRAVITY,
        initialState = true
    );

    val displayNameComponent = SurfComponentBuilder { primary(displayName) }
    val descriptionComponent = SurfComponentBuilder { info(description) }
}