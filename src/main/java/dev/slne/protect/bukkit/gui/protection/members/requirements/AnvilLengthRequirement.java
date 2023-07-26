package dev.slne.protect.bukkit.gui.protection.members.requirements;

import com.google.common.base.MoreObjects;
import dev.slne.protect.bukkit.message.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

import java.util.concurrent.CompletableFuture;

public class AnvilLengthRequirement implements AnvilRequirement {

    private final int minLength;
    private final int maxLength;

    /**
     * Creates a new anvil length requirement
     *
     * @param minLength the minimum length
     * @param maxLength the maximum length
     */
    public AnvilLengthRequirement(int minLength, int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public Component getDescription(TextColor stateColor, String currentInput) {
        TextComponent.Builder builder = Component.text();

        builder.append(Component.text("Die Eingabe muss zwischen ", stateColor));
        builder.append(Component.text(minLength, MessageManager.VARIABLE_VALUE));
        builder.append(Component.text(" und ", stateColor));
        builder.append(Component.text(maxLength, MessageManager.VARIABLE_VALUE));
        builder.append(Component.text(" Zeichen lang sein.", stateColor));

        return builder.build();
    }

    @Override
    public CompletableFuture<Boolean> isMet(String input) {
        input = input.trim();

        return CompletableFuture.completedFuture(input.length() >= minLength && input.length() <= maxLength);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("minLength", minLength)
                .add("maxLength", maxLength)
                .toString();
    }
}
