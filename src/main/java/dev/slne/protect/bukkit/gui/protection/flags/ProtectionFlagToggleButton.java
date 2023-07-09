package dev.slne.protect.bukkit.gui.protection.flags;

import java.util.Arrays;
import java.util.function.Consumer;

import org.bukkit.Material;

import com.sk89q.worldguard.protection.flags.StateFlag.State;

import dev.slne.protect.bukkit.gui.button.toggle.ToggleButton;
import net.kyori.adventure.text.Component;

public class ProtectionFlagToggleButton extends ToggleButton<String> {

    /**
     * Creates a new toggle button based on the item stack and action
     *
     * @param material     the material
     * @param displayName  the display name
     * @param initialState the initial state
     * @param parentGui    the parent gui
     */
    public ProtectionFlagToggleButton(Material material, Component displayName,
            State initialState, Consumer<String> consumer) {
        super(material, displayName,
                Arrays.asList(State.values()).stream().map(Enum::name).toArray(String[]::new),
                initialState.name(), consumer);
    }

    /**
     * Gets the current state
     *
     * @return the current state
     */
    public State getCurrentToggleState() {
        return State.valueOf(getCurrentState());
    }

}
