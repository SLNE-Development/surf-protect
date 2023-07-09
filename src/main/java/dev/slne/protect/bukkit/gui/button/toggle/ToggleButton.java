package dev.slne.protect.bukkit.gui.button.toggle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;

import dev.slne.protect.bukkit.gui.item.ItemStackUtils;
import dev.slne.protect.bukkit.message.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ToggleButton<T> extends GuiItem {

    private Consumer<T> consumer;

    private T currentState;
    private T[] states;

    /**
     * Creates a new toggle button based on the item stack and action
     *
     * @param material     the material
     * @param displayName  the display name
     * @param states       the states
     * @param initialState the initial state
     * @param consumer     the consumer
     */
    public ToggleButton(Material material, Component displayName, T[] states,
            T initialState, Consumer<T> consumer) {
        super(ItemStackUtils.getItem(material, 1, 0, displayName, formLore(states, initialState)));

        this.states = states;
        this.currentState = initialState;
        this.consumer = consumer;

        setAction(event -> setNextState((Player) event.getWhoClicked()));
    }

    /**
     * Forms the lore for the current toggle button
     *
     * @return the lore
     */
    public static <T> List<Component> formLore(T[] states, T currentState) {
        List<Component> lore = new ArrayList<>();

        TextColor active = NamedTextColor.GREEN;
        TextColor inactive = NamedTextColor.GRAY;

        for (T state : states) {
            if (state == currentState) {
                lore.add(Component.text("[", active).append(Component.text("✓", active))
                        .append(Component.text("]", active)).append(Component.space())
                        .append(Component.text(state.toString(), active)));
            } else {
                lore.add(Component.text("[", inactive).append(Component.text("✗", inactive))
                        .append(Component.text("]", inactive)).append(Component.space())
                        .append(Component.text(state.toString(), inactive)));
            }
        }

        List<Component> newLore = new ArrayList<>();

        for (Component loreComponent : lore) {
            newLore.add(loreComponent.decoration(TextDecoration.ITALIC, false));
        }

        return newLore;
    }

    /**
     * Gets the current state
     *
     * @return the current state
     */
    public T getCurrentState() {
        return currentState;
    }

    /**
     * Sets the current state
     *
     * @param currentState the new current state
     * @param player       the player
     */
    public void setCurrentState(T currentState, Player player) {
        T oldState = this.currentState;
        this.currentState = currentState;

        ItemMeta itemMeta = getItem().getItemMeta();

        if (itemMeta == null) {
            return;
        }

        itemMeta.lore(formLore(states, currentState));

        getItem().setItemMeta(itemMeta);

        TextComponent.Builder builder = Component.text();

        builder.append(MessageManager.prefix());
        builder.append(Component.text("Der Status von ", MessageManager.SUCCESS));
        builder.append(getItem().getItemMeta().displayName().color(MessageManager.VARIABLE_VALUE));
        builder.append(Component.text(" wurde von ", MessageManager.SUCCESS));
        builder.append(Component.text(oldState.toString(), MessageManager.VARIABLE_VALUE));
        builder.append(Component.text(" auf ", MessageManager.SUCCESS));
        builder.append(Component.text(currentState.toString(), MessageManager.VARIABLE_VALUE));
        builder.append(Component.text(" geändert.", MessageManager.SUCCESS));

        player.sendMessage(builder.build());

        consumer.accept(currentState);
    }

    /**
     * Gets the states
     *
     * @return the states
     */
    public T[] getStates() {
        return states;
    }

    /**
     * Sets the states
     *
     * @param states the new states
     */
    public void setStates(T[] states) {
        this.states = states;
    }

    /**
     * Gets the next state
     *
     * @return the next state
     */
    public T getNextState() {
        int index = 0;

        for (int i = 0; i < states.length; i++) {
            if (states[i] == currentState) {
                index = i;
                break;
            }
        }

        index++;

        if (index >= states.length) {
            index = 0;
        }

        return states[index];
    }

    /**
     * Gets the previous state
     *
     * @return the previous state
     */
    public T getPreviousState() {
        int index = 0;

        for (int i = 0; i < states.length; i++) {
            if (states[i] == currentState) {
                index = i;
                break;
            }
        }

        index--;

        if (index < 0) {
            index = states.length - 1;
        }

        return states[index];
    }

    /**
     * Sets the next state
     *
     * @param player the player
     */
    public void setNextState(Player player) {
        setCurrentState(getNextState(), player);
    }

    /**
     * Sets the previous state
     *
     * @param player the player
     */
    public void setPreviousState(Player player) {
        setCurrentState(getPreviousState(), player);
    }

}
