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
    description: SurfComponentBuilder.() -> Unit,
    val flag: StateFlag,
    val initialState: StateFlag.State?
) {
    CHEST_ACCESS(
        icon = ItemType.CHEST,
        displayName = { primary("Kistenzugriff") },
        description = { info("Legt fest, ob Spieler Inventare wie Kisten, Fässer oder Shulkerboxen öffnen dürfen.") },
        flag = Flags.CHEST_ACCESS,
        initialState = null
    ),
    USE(
        icon = ItemType.LEVER,
        displayName = { primary("Blockbenutzung") },
        description = { info("Steuert die Nutzung interaktiver Blöcke wie bspw. Türen, Hebel oder Knöpfe.") },
        flag = Flags.USE,
        initialState = null
    ),
    DAMAGE_ANIMALS(
        icon = ItemType.COOKED_BEEF,
        displayName = { primary("Tiere verletzen") },
        description = { info("Legt fest, ob Spieler Tiere verletzen dürfen.") },
        flag = Flags.DAMAGE_ANIMALS,
        initialState = null
    ),
    SLEEP(
        icon = ItemType.RED_BED,
        displayName = { primary("Schlafen ") },
        description = { info("Legt fest, ob Spieler in Betten schlafen dürfen.") },
        flag = Flags.SLEEP,
        initialState = null
    ),
    VEHICLE_PLACE(
        icon = ItemType.MINECART,
        displayName = { primary("Fahrzeuge platzieren") },
        description = { info("Legt fest, ob Spieler  Boote oder Loren platzieren dürfen.") },
        flag = Flags.PLACE_VEHICLE,
        initialState = null
    ),
    VEHICLE_DESTROY(
        icon = ItemType.TNT_MINECART,
        displayName = { primary("Fahrzeuge zerstören") },
        description = { info("Bestimmt, ob Fahrzeuge zerstört werden dürfen.") },
        flag = Flags.DESTROY_VEHICLE,
        initialState = null
    ),
    RIDE(
        icon = ItemType.SADDLE,
        displayName = { primary("Reiten ") },
        description = { info("Erlaubt oder verhindert das Reiten von Tieren und Fahrzeugen.") },
        flag = Flags.RIDE,
        initialState = null
    ),
    ITEM_FRAME_ROTATION(
        icon = ItemType.ITEM_FRAME,
        displayName = { primary("Gegenstandsrahmen drehen") },
        description = { info("Legt fest, ob Gegenstände in Rahmen gedreht werden dürfen.") },
        flag = Flags.ITEM_FRAME_ROTATE,
        initialState = null
    ),
    USE_ANVIL(
        icon = ItemType.ANVIL,
        displayName = { primary("Amboss benutzen") },
        description = { info("Legt fest, ob Spieler einen Amboss benutzen dürfen.") },
        flag = Flags.USE_ANVIL,
        initialState = null
    ),
    USE_DRIPLEAF(
        icon = ItemType.BIG_DRIPLEAF,
        displayName = { primary("Tropfblatt Nutzung") },
        description = { info("Bestimmt, ob große Tropfblätter von Spielern ausgelöst werden dürfen.") },
        flag = Flags.USE_DRIPLEAF,
        initialState = null
    ),
    OTHER_EXPLOSION(
        icon = ItemType.TNT,
        displayName = { primary("Explosionsschaden") },
        description = { info("Regelt, ob Explosionen Schaden verursachen.") },

        flag = Flags.OTHER_EXPLOSION,
        initialState = null
    ),
    ENDERMAN_GRIEF(
        icon = ItemType.ENDERMAN_SPAWN_EGG,
        displayName = { primary("Enderman‑Griefing") },
        description = { info("Steuert, ob Endermänner Blöcke aufnehmen und platzieren dürfen.") },
        flag = Flags.ENDER_BUILD,
        initialState = null
    ),
    FIRE_SPREAD(
        icon = ItemType.FLINT_AND_STEEL,
        displayName = { primary("Feuerausbreitung") },
        description = { info("Bestimmt, ob sich Feuer natürlich ausbreiten kann.") },
        flag = Flags.FIRE_SPREAD,
        initialState = StateFlag.State.DENY
    ),
    LEAF_DECAY(
        icon = ItemType.OAK_LEAVES,
        displayName = { primary("Blattverfall") },
        description = { info("Regelt, ob Blätter natürlich verfallen.") },
        flag = Flags.LEAF_DECAY,
        initialState = null
    ),
    GRASS_GROWTH(
        icon = ItemType.GRASS_BLOCK,
        displayName = { primary("Graswachstum") },
        description = { info("Steuert, ob Gras sich auf Erde ausbreiten darf.") },
        flag = Flags.GRASS_SPREAD,
        initialState = null
    ),
    MYCELIUM_SPREAD(
        icon = ItemType.MYCELIUM,
        displayName = { primary("Myzelwachstum") },
        description = { info("Bestimmt, ob Myzel sich auf Erde ausbreiten darf.") },
        flag = Flags.MYCELIUM_SPREAD,
        initialState = null
    ),
    MUSHROOM_GROWTH(
        icon = ItemType.RED_MUSHROOM,
        displayName = { primary("Pilzwachstum") },
        description = { info("Regelt, ob Pilze wachsen oder sich ausbreiten dürfen.") },
        flag = Flags.MUSHROOMS,
        initialState = null
    ),
    VINE_GROWTH(
        icon = ItemType.VINE,
        displayName = { primary("Rankenwachstum") },
        description = { info("Steuert, ob Ranken und Seetang wachsen oder sich ausbreiten dürfen.") },
        flag = Flags.VINE_GROWTH,
        initialState = null
    ),
    ROCK_GROWTH(
        icon = ItemType.DRIPSTONE_BLOCK,
        displayName = { primary("Tropfsteinwachstum") },
        description = { info("Bestimmt, ob Tropfstein wächst oder sich verlängert.") },
        flag = Flags.ROCK_GROWTH,
        initialState = null
    ),
    SCULK_GROWTH(
        icon = ItemType.SCULK_SENSOR,
        displayName = { primary("Sculk-Ausbreitung") },
        description = { info("Regelt, ob Sculk sich ausbreiten oder wachsen darf.") },
        flag = Flags.SCULK_GROWTH,
        initialState = null
    ),
    CROP_GROWTH(
        icon = ItemType.WHEAT,
        displayName = { primary("Pflanzenwachstum") },
        description = { info("Steuert das Wachstum von Nutzpflanzen.") },
        flag = Flags.CROP_GROWTH,
        initialState = null
    ),
    CORAL_FADE(
        icon = ItemType.BRAIN_CORAL,
        displayName = { primary("Korallenverblassen") },
        description = { info("Bestimmt, ob Korallen ohne Wasser ausbleichen.") },
        flag = Flags.CORAL_FADE,
        initialState = null
    ),
    SNOWMAN_TRAILS(
        icon = ItemType.CARVED_PUMPKIN,
        displayName = { primary("Schneegolem-Spuren") },
        description = { info("Regelt, ob Schneegolems Schneespuren hinterlassen.") },
        flag = Flags.SNOWMAN_TRAILS,
        initialState = null
    ),
    SNOW_FALL(
        icon = ItemType.SNOW,
        displayName = { primary("Schneefall") },
        description = { info("Steuert, ob Schnee natürlich fallen und Blöcke bedecken kann.") },
        flag = Flags.SNOW_FALL,
        initialState = null
    ),
    SNOW_MELT(
        icon = ItemType.SNOW,
        displayName = { primary("Schneeschmelze") },
        description = { info("Bestimmt, ob Schnee durch Licht oder Wärme schmelzen darf.") },
        flag = Flags.SNOW_MELT,
        initialState = null
    ),
    ICE_FORM(
        icon = ItemType.ICE,
        displayName = { primary("Eisbildung") },
        description = { info("Regelt, ob Wasser zu Eis gefrieren kann.") },
        flag = Flags.ICE_FORM,
        initialState = null
    ),
    ICE_MELT(
        icon = ItemType.ICE,
        displayName = { primary("Eisschmelze") },
        description = { info("Steuert, ob Eis durch Licht oder Wärme schmelzen darf.") },
        flag = Flags.ICE_MELT,
        initialState = null
    );

    val displayName = SurfComponentBuilder(displayName)
    val description = SurfComponentBuilder(description)
}