package dev.slne.protect.bukkit.gui.protection.members.requirements;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.concurrent.CompletableFuture;

public class AnvilNoSpaceRequirement implements AnvilRequirement {

    @Override
    public Component getDescription(TextColor stateColor, String currentInput) {
        return Component.text("Die Eingabe darf keine Leerzeichen enthalten.", stateColor);
    }

    @Override
    public CompletableFuture<Boolean> isMet(String input) {
        return CompletableFuture.completedFuture(!input.contains(" "));
    }
}
