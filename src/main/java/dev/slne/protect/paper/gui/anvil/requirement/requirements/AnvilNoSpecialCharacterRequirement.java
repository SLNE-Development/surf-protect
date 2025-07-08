package dev.slne.protect.paper.gui.anvil.requirement.requirements;

import dev.slne.protect.paper.gui.anvil.requirement.AnvilRequirement;
import dev.slne.protect.paper.message.MessageManager;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class AnvilNoSpecialCharacterRequirement implements AnvilRequirement {

  private final String allowedCharacters;

  /**
   * Creates a new AnvilNoSpecialCharacterRequirement.
   *
   * @param allowedCharacters The characters that are allowed to be entered.
   */
  public AnvilNoSpecialCharacterRequirement(String allowedCharacters) {
    this.allowedCharacters = allowedCharacters;
  }

  @Override
  public List<Component> getDescription(List<Component> description, TextColor stateColor,
      String currentInput) {
    description.add(
        Component.text("Die Eingabe darf nur die folgenden Zeichen enthalten:", stateColor));
    description.add(Component.text(allowedCharacters, MessageManager.VARIABLE_VALUE));

    return description;
  }

  @Override
  public CompletableFuture<Boolean> isMet(String input) {
    return CompletableFuture.completedFuture(input.matches("[" + allowedCharacters + "]+"));
  }
}
