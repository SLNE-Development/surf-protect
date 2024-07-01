package dev.slne.protect.bukkit.gui.anvil.requirement.requirements.player;

import dev.slne.protect.bukkit.gui.anvil.requirement.AnvilRequirement;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AnvilPlayerRequirement implements AnvilRequirement {

  @Override
  public List<Component> getDescription(List<Component> description, TextColor stateColor,
      String currentInput) {
    description.add(Component.text("Die Eingabe muss einem Spielernamen entsprechen,", stateColor));
    description.add(Component.text("welcher sich aktuell auf dem Server befindet.", stateColor));

    return description;
  }

  @Override
  public CompletableFuture<Boolean> isMet(String input) {
    input = input.trim().replace(" ", "");

    if (input.isEmpty()) {
      return CompletableFuture.completedFuture(false);
    }

    Player player = Bukkit.getPlayer(input);
    return CompletableFuture.completedFuture(player != null && player.isOnline());
  }
}

