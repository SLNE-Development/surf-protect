package dev.slne.protect.bukkitold.gui.anvil.requirement;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public interface AnvilRequirement {

  /**
   * Returns the description
   *
   * @param description  the description
   * @param stateColor   the state color
   * @param currentInput the current input
   * @return the description
   */
  List<Component> getDescription(List<Component> description, TextColor stateColor,
      String currentInput);

  /**
   * Returns if the input is met
   *
   * @param input the input
   * @return if the input is met
   */
  CompletableFuture<Boolean> isMet(String input);

}

