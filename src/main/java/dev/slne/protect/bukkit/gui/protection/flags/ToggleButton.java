package dev.slne.protect.bukkit.gui.protection.flags;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import dev.slne.protect.bukkit.gui.utils.ItemUtils;
import dev.slne.protect.bukkit.message.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ToggleButton extends GuiItem {
    private static final TextColor active = NamedTextColor.GREEN;
    private static final TextColor inactive = NamedTextColor.GRAY;

    private final Consumer<State> consumer;
    private final State toggleToState;
    private @Nullable State currentState;

    /**
     * Creates a new toggle button based on the item stack and action
     *
     * @param material      the material
     * @param displayName   the display name
     * @param currentState  the currentState state
     * @param toggleToState the state to toggle to
     * @param consumer      the consumer
     */
    public ToggleButton(Material material, Component displayName, @Nullable State currentState, State toggleToState, Consumer<@Nullable State> consumer) {
        super(ItemUtils.item(material, 1, 0, displayName, formLore(getCurrentToggleState(currentState, toggleToState), toggleToState).toArray(Component[]::new)));
        this.toggleToState = toggleToState;

        this.currentState = currentState;
        this.consumer = consumer;

        setAction(event -> setNextState((Player) event.getWhoClicked()));
    }

    /**
     * Gets the current state
     *
     * @param currentState  the currentState state
     * @param toggleToState the state to toggle to
     * @return the current state
     */
    @Contract(value = "!null, _ -> param1", pure = true)
    public static State getCurrentToggleState(@Nullable State currentState, State toggleToState) {
        return currentState != null ? currentState : toggleToState == State.DENY ? State.ALLOW : State.DENY;
    }

    /**
     * Forms the lore for the current toggle button
     *
     * @return the lore
     */
    public static @NotNull List<Component> formLore(@Nullable State currentState, State toggleToState) {
        List<Component> lore = new ArrayList<>();


        if (currentState == null) {
            if (toggleToState == State.ALLOW) {
                addAllowLore(lore);
            } else {
                addDenyLore(lore);
            }
        } else if (currentState == State.ALLOW) {
            addDenyLore(lore);
        } else {
            addAllowLore(lore);
        }

        List<Component> newLore = new ArrayList<>();

        for (Component loreComponent : lore) {
            newLore.add(loreComponent.decoration(TextDecoration.ITALIC, false));
        }

        return newLore;
    }

    /**
     * Adds the deny lore to the list
     *
     * @param lore the lore list to add to
     */
    private static void addDenyLore(@NotNull List<Component> lore) {
        lore.add(Component.text("[", active)
                .append(Component.text("✓", active))
                .append(Component.text("]", active))
                .append(Component.space())
                .append(Component.text(State.ALLOW.toString(), active)));

        lore.add(Component.text("[", inactive)
                .append(Component.text("✗", inactive))
                .append(Component.text("]", inactive))
                .append(Component.space())
                .append(Component.text(State.DENY.toString(), inactive)));
    }

    /**
     * Adds the allow lore to the list
     *
     * @param lore the lore list to add to
     */
    private static void addAllowLore(@NotNull List<Component> lore) {
        lore.add(Component.text("[", inactive)
                .append(Component.text("✗", inactive))
                .append(Component.text("]", inactive))
                .append(Component.space())
                .append(Component.text(State.ALLOW.toString(), inactive)));

        lore.add(Component.text("[", active)
                .append(Component.text("✓", active))
                .append(Component.text("]", active))
                .append(Component.space())
                .append(Component.text(State.DENY.toString(), active)));
    }

    /**
     * Sets the current state
     *
     * @param newState the new current state
     * @param player   the player
     */
    public void setCurrentState(@Nullable State newState, Player player) {
        State oldState = getCurrentToggleState(this.currentState, this.toggleToState);
        this.currentState = newState;

        ItemMeta itemMeta = getItem().getItemMeta();

        if (itemMeta == null) {
            return;
        }

        itemMeta.lore(formLore(newState, toggleToState));

        getItem().setItemMeta(itemMeta);

        Component displayName =
                getItem().getItemMeta().hasDisplayName() ? (getItem().getItemMeta().displayName() != null ? getItem().getItemMeta().displayName() : Component.text(getItem().getType().name())) : Component.text(getItem().getType().name());

        TextComponent.Builder builder = Component.text();

        builder.append(MessageManager.prefix());
        builder.append(Component.text("Der Status von ", MessageManager.SUCCESS));
        assert displayName != null;
        builder.append(displayName.color(MessageManager.VARIABLE_VALUE));
        builder.append(Component.text(" wurde von ", MessageManager.SUCCESS));
        builder.append(Component.text(oldState.toString(), MessageManager.VARIABLE_VALUE));
        builder.append(Component.text(" auf ", MessageManager.SUCCESS));
        builder.append(Component.text(getCurrentToggleState(newState, toggleToState).toString(), MessageManager.VARIABLE_VALUE));
        builder.append(Component.text(" geändert.", MessageManager.SUCCESS));

        player.sendMessage(builder.build());

        consumer.accept(newState);
    }

    /**
     * Gets the next state
     *
     * @return the next state
     */
    public @Nullable State getNextState() {
        // null -> toggleToState
        // toggleToState -> null

        if (currentState == null) {
            return toggleToState;
        } else if (currentState == toggleToState) {
            return null;
        } else {
            return currentState == State.ALLOW ? State.DENY : State.ALLOW;
        }
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
     * Gets the current state
     *
     * @return the current state
     */
    public @Nullable State getCurrentState() {
        return currentState;
    }

}
