package dev.slne.protect.bukkit.gui.anvil.requirement.requirements;

import dev.slne.protect.bukkit.gui.anvil.requirement.AnvilRequirement;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class AnvilNoSpaceRequirement implements AnvilRequirement {

  @Override
  public List<Component> getDescription(List<Component> description, TextColor stateColor,
      String currentInput) {
    description.add(Component.text("Die Eingabe darf keine Leerzeichen enthalten.", stateColor));

    return description;
  }

  @Override
  public CompletableFuture<Boolean> isMet(String input) {
    return CompletableFuture.completedFuture(!input.contains(" "));
  }
}
