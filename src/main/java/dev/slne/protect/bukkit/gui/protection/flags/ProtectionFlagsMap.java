package dev.slne.protect.bukkit.gui.protection.flags;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;

import dev.slne.protect.bukkit.message.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

public enum ProtectionFlagsMap {

    CHEST_ACCESS(Material.CHEST, Component.text("Chest Access", NamedTextColor.GOLD), "Erlaube/Verbiete den Kistenzugang für alle Spieler", Flags.CHEST_ACCESS, null, State.ALLOW),
    USE(Material.LEVER, Component.text("Use", NamedTextColor.GOLD), "Erlaube/Verbiete das Verwenden von Hebeln, Türen und so weiter für alle Spieler", Flags.USE, null, State.ALLOW),
    DAMAGE_ANIMALS(Material.COOKED_BEEF, Component.text("Damage Animals", NamedTextColor.GOLD), "Erlaube/Verbiete das Töten von Tieren für alle Spieler", Flags.DAMAGE_ANIMALS, null, State.ALLOW),
    SLEEP(Material.RED_BED, Component.text("Sleep", NamedTextColor.GOLD), "Erlaube/Verbiete das Verwenden von Betten für alle Spieler", Flags.SLEEP, null, State.ALLOW),
    VEHICLE_PLACE(Material.MINECART, Component.text("Vehicle Place", NamedTextColor.GOLD), "Erlaube/Verbiete das Platzieren von Fahrzeugen für alle Spieler", Flags.PLACE_VEHICLE, null, State.ALLOW),
    VEHICLE_DESTROY(Material.TNT_MINECART, Component.text("Vehicle Destroy", NamedTextColor.GOLD), "Erlaube/Verbiete das Zerstören von Fahrzeugen für alle Spieler", Flags.DESTROY_VEHICLE, null, State.ALLOW),
    RIDE(Material.SADDLE, Component.text("Ride", NamedTextColor.GOLD), "Erlaube/Verbiete das Reiten von Tieren für alle Spieler", Flags.RIDE, null, State.ALLOW),
    ITEM_FRAME_ROTATION(Material.ITEM_FRAME, Component.text("ItemFrames Rotation", NamedTextColor.GOLD), "Erlaube/Verbiete das Drehen von ItemFrames für alle Spieler", Flags.ITEM_FRAME_ROTATE, null, State.ALLOW),
    USE_ANVIL(Material.ANVIL, Component.text("Anvil Usage", NamedTextColor.GOLD), "Erlaube/Verbiete das Benutzen von Anvils für alle Spieler", Flags.USE_ANVIL, null, State.ALLOW),
    USE_DRIPLEAF(Material.DRIPSTONE_BLOCK, Component.text("Dripleaf Usage", NamedTextColor.GOLD), "Erlaube/Verbiete das Benutzen von Dripleaf für alle Spieler",Flags.USE_DRIPLEAF, null, State.ALLOW),
    OTHER_EXPLOSION(Material.TNT, Component.text("Explosion Damage", NamedTextColor.GOLD), "Aktiviere/Deaktiviere den Blockschaden von Explosionen", Flags.OTHER_EXPLOSION, null, State.ALLOW),
    ENDERMAN_GRIEF(Material.ENDERMAN_SPAWN_EGG, Component.text("Enderman Griefing", NamedTextColor.GOLD), "Aktiviere/Deaktiviere das Enderman Griefing", Flags.ENDER_BUILD, null, State.DENY),
    FIRE_SPREAD(Material.FLINT_AND_STEEL, Component.text("Fire Spread", NamedTextColor.GOLD), "Aktiviere/Deaktiviere das Ausbreiten von Feuer", Flags.FIRE_SPREAD, State.DENY, State.DENY),
    LEAF_DECAY(Material.OAK_LEAVES, Component.text("Leaf Decay", NamedTextColor.GOLD),"Aktiviere/Deaktiviere das natürliche Verschwinden von Blättern", Flags.LEAF_DECAY, null, State.DENY),
    GRASS_GROWTH(Material.GRASS_BLOCK, Component.text("Grass Growth", NamedTextColor.GOLD),"Aktiviere/Deaktiviere das Verbreiten von Gras", Flags.GRASS_SPREAD, null, State.DENY),
    MYCELIUM_SPREAD(Material.MYCELIUM, Component.text("Mycelium Spread", NamedTextColor.GOLD),"Aktiviere/Deaktiviere das Verbreiten von Myzel",Flags.MYCELIUM_SPREAD, null, State.DENY),
    MUSHROOM_GROWTH(Material.RED_MUSHROOM, Component.text("Mushroom Growth", NamedTextColor.GOLD),"Aktiviere/Deaktiviere das Wachstum von Pilzen", Flags.MUSHROOMS, null, State.DENY),
    VINE_GROWTH(Material.VINE, Component.text("Vine Growth", NamedTextColor.GOLD), "Aktiviere/Deaktiviere das Wachstum von Ranken", Flags.VINE_GROWTH, null, State.DENY),
    ROCK_GROWTH(Material.DRIPSTONE_BLOCK, Component.text("Rock Growth", NamedTextColor.GOLD),"Aktiviere/Deaktiviere das Wachstum von Dripstones", Flags.ROCK_GROWTH, null, State.DENY),
    SCULK_GROWTH(Material.SCULK_SENSOR, Component.text("Sculk Growth", NamedTextColor.GOLD), "Aktiviere/Deaktiviere das Verbreiten von Sculk", Flags.SCULK_GROWTH, null, State.DENY),
    CROP_GROWTH(Material.WHEAT, Component.text("Crop Growth", NamedTextColor.GOLD), "Aktiviere/Deaktiviere das Wachstum von Nutzpflanzen", Flags.CROP_GROWTH, null, State.DENY),
    CORAL_FADE(Material.BRAIN_CORAL, Component.text("Coral Fade", NamedTextColor.GOLD), "Aktiviere/Deaktiviere das Absterben von Korallen", Flags.CORAL_FADE, null, State.DENY),
    SNOWMAN_TRAILS(Material.CARVED_PUMPKIN, Component.text("Snowman Trails", NamedTextColor.GOLD), "Aktiviere/Deaktiviere ob Snowmans Schnee hinterlassen", Flags.SNOWMAN_TRAILS, null, State.DENY),
    SNOW_FALL(Material.SNOW, Component.text("Snow Fall", NamedTextColor.GOLD), "Aktiviere/Deaktiviere den Schneefall", Flags.SNOW_FALL, null, State.DENY),
    SNOW_MELT(Material.SNOW, Component.text("Snow Melt", NamedTextColor.GOLD), "Aktiviere/Deaktiviere das Schmelzen von Schnee", Flags.SNOW_MELT, null, State.DENY),
    ICE_FORM(Material.ICE, Component.text("Ice Form", NamedTextColor.GOLD), "Aktiviere/Deaktiviere das Formen von Eis", Flags.ICE_FORM, null, State.DENY),
    ICE_MELT(Material.ICE, Component.text("Ice Melt", NamedTextColor.GOLD),"Aktiviere/Deaktiviere das Schmelzen von Eis", Flags.ICE_MELT, null, State.DENY),
    LIGHTNING(Material.LIGHTNING_ROD, Component.text("Lightning", NamedTextColor.GOLD),"Aktiviere/Deaktiviere Blitzeinschläge", Flags.LIGHTNING, null, State.DENY);


    private final Material material;
    private final Component displayName;
    private final Component description;
    private final StateFlag flag;
    private final @Nullable State initialState;
    private final State toggleToState;

    ProtectionFlagsMap(Material material, Component displayName, String description, StateFlag stateFlag, @Nullable State initialState, State toggleToState) {
        this.material = material;
        this.displayName = displayName;
        this.description = Component.text(description, MessageManager.INFO).appendNewline().appendNewline();
        this.flag = stateFlag;
        this.initialState = initialState;
        this.toggleToState = toggleToState;
    }

    /**
     * @return the displayName
     */
    public Component getDisplayName() {
        return displayName;
    }

    /**
     * @return the description
     */
    public Component getDescription() {
        return description;
    }

    /**
     * @return the flag
     */
    public StateFlag getFlag() {
        return flag;
    }

    /**
     * @return the initialState
     */
    public @Nullable State getInitialState() {
        return initialState;
    }

    /**
     * @return the material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * @return the toggleToState
     */
    public State getToggleToState() {
        return toggleToState;
    }
}
