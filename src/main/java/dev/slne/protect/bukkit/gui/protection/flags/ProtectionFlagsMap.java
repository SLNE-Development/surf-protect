package dev.slne.protect.bukkit.gui.protection.flags;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

public enum ProtectionFlagsMap {

    TNT(Material.TNT, Component.text("TNT", NamedTextColor.GOLD), Flags.TNT, State.DENY, State.DENY),
    CHEST_ACCESS(Material.CHEST, Component.text("Kisten", NamedTextColor.GOLD), Flags.CHEST_ACCESS, null, State.ALLOW),
    VEHICLE_PLACE(Material.MINECART, Component.text("Fahrzeuge setzen", NamedTextColor.GOLD), Flags.PLACE_VEHICLE, null, State.ALLOW),
    VEHICLE_DESTROY(Material.MINECART, Component.text("Fahrzeuge zerstören", NamedTextColor.GOLD), Flags.DESTROY_VEHICLE, null, State.ALLOW),
    RIDE(Material.SADDLE, Component.text("Reiten", NamedTextColor.GOLD), Flags.RIDE, null, State.ALLOW),
    ITEM_FRAME_ROTATION(Material.ITEM_FRAME, Component.text("Item Frames drehen", NamedTextColor.GOLD), Flags.ITEM_FRAME_ROTATE, null, State.ALLOW),
    BLOCK_TRAMPLING(Material.FARMLAND, Component.text("Block Trampling", NamedTextColor.GOLD), Flags.TRAMPLE_BLOCKS, null, State.ALLOW),
    USE_ANVIL(Material.ANVIL, Component.text("Anvils benutzen", NamedTextColor.GOLD), Flags.USE_ANVIL, null, State.ALLOW),
    USE_DRIPLEAF(Material.DRIPSTONE_BLOCK, Component.text("Dripleaf benutzen", NamedTextColor.GOLD), Flags.USE_DRIPLEAF, null, State.ALLOW),
    CREEPER_EXPLOSION(Material.CREEPER_HEAD, Component.text("Creeper Explosion", NamedTextColor.GOLD), Flags.CREEPER_EXPLOSION, State.DENY, State.DENY),
    GHAST_FIREBALL(Material.GHAST_SPAWN_EGG, Component.text("Ghast Fireball", NamedTextColor.GOLD), Flags.GHAST_FIREBALL, State.DENY, State.DENY),
    OTHER_EXPLOSION(Material.TNT, Component.text("Other Explosion", NamedTextColor.GOLD), Flags.OTHER_EXPLOSION, State.DENY, State.DENY),
    ENDERMAN_GRIEF(Material.ENDERMAN_SPAWN_EGG, Component.text("Enderman Grief", NamedTextColor.GOLD), Flags.ENDER_BUILD, State.DENY, State.DENY),
    SNOWMAN_TRAILS(Material.CARVED_PUMPKIN, Component.text("Snowman Trails", NamedTextColor.GOLD), Flags.SNOWMAN_TRAILS, State.DENY, State.DENY),
    RAVAGER_GRIEF(Material.RAVAGER_SPAWN_EGG, Component.text("Ravager Grief", NamedTextColor.GOLD), Flags.RAVAGER_RAVAGE, State.DENY, State.DENY),
    FIRE_SPREAD(Material.FLINT_AND_STEEL, Component.text("Fire Spread", NamedTextColor.GOLD), Flags.FIRE_SPREAD, State.DENY, State.DENY),
    PISTONS(Material.PISTON, Component.text("Pistons", NamedTextColor.GOLD), Flags.PISTONS, null, State.ALLOW),
    SNOW_FALL(Material.SNOW, Component.text("Snow Fall", NamedTextColor.GOLD), Flags.SNOW_FALL, null, State.DENY),
    SNOW_MELT(Material.SNOW, Component.text("Snow Melt", NamedTextColor.GOLD), Flags.SNOW_MELT, null, State.DENY),
    ICE_FORM(Material.ICE, Component.text("Ice Form", NamedTextColor.GOLD), Flags.ICE_FORM, null, State.DENY),
    ICE_MELT(Material.ICE, Component.text("Ice Melt", NamedTextColor.GOLD), Flags.ICE_MELT, null, State.DENY),
    LIGHTNING(Material.LIGHTNING_ROD, Component.text("Lightning", NamedTextColor.GOLD), Flags.LIGHTNING, null, State.DENY),
    MUSHROOM_GROWTH(Material.MUSHROOM_STEM, Component.text("Mushroom Growth", NamedTextColor.GOLD), Flags.MUSHROOMS, null, State.DENY),
    LEAF_DECAY(Material.OAK_LEAVES, Component.text("Leaf Decay", NamedTextColor.GOLD), Flags.LEAF_DECAY, null, State.DENY),
    GRASS_GROWTH(Material.GRASS_BLOCK, Component.text("Grass Growth", NamedTextColor.GOLD), Flags.GRASS_SPREAD, null, State.DENY),
    MYCELIUM_SPREAD(Material.MYCELIUM, Component.text("Mycelium Spread", NamedTextColor.GOLD), Flags.MYCELIUM_SPREAD, null, State.DENY),
    VINE_GROWTH(Material.VINE, Component.text("Vine Growth", NamedTextColor.GOLD), Flags.VINE_GROWTH, null, State.DENY),
    ROCK_GROWTH(Material.MOSS_BLOCK, Component.text("Rock Growth", NamedTextColor.GOLD), Flags.ROCK_GROWTH, null, State.DENY),
    SCULK_GROWTH(Material.SCULK_SENSOR, Component.text("Sculk Growth", NamedTextColor.GOLD), Flags.SCULK_GROWTH, null, State.DENY),
    CROP_GROWTH(Material.WHEAT, Component.text("Crop Growth", NamedTextColor.GOLD), Flags.CROP_GROWTH, null, State.DENY),
    CORAL_FADE(Material.BRAIN_CORAL, Component.text("Coral Fade", NamedTextColor.GOLD), Flags.CORAL_FADE, null, State.DENY);

    private final Material material;
    private final Component displayName;
    private final StateFlag flag;
    private final @Nullable State initialState;
    private final State toggleToState;

    ProtectionFlagsMap(Material material, Component displayName, StateFlag stateFlag, @Nullable State initialState, State toggleToState) {
        this.material = material;
        this.displayName = displayName;
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
