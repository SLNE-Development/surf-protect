package dev.slne.protect.bukkitold.gui.anvil.requirement.requirements;

import com.google.common.base.MoreObjects;
import dev.slne.protect.bukkitold.gui.anvil.requirement.AnvilRequirement;
import dev.slne.protect.bukkitold.message.MessageManager;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

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
  public List<Component> getDescription(List<Component> description, TextColor stateColor,
      String currentInput) {
    TextComponent.Builder builder = Component.text();
    builder.append(Component.text("Die Eingabe muss zwischen ", stateColor));
    builder.append(Component.text(minLength, MessageManager.VARIABLE_VALUE));
    builder.append(Component.text(" und ", stateColor));
    builder.append(Component.text(maxLength, MessageManager.VARIABLE_VALUE));
    builder.append(Component.text(" Zeichen lang sein.", stateColor));

    description.add(builder.build());

    return description;
  }

  @Override
  public CompletableFuture<Boolean> isMet(String input) {
    input = input.trim();

    return CompletableFuture.completedFuture(
        input.length() >= minLength && input.length() <= maxLength);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("minLength", minLength)
        .add("maxLength", maxLength)
        .toString();
  }
}
