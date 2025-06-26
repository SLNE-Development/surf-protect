package dev.slne.protect.bukkit.gui.anvil.requirement.requirements.player;

import dev.slne.protect.bukkit.KotlinConversationUtils;
import dev.slne.protect.bukkit.gui.anvil.requirement.AnvilRequirement;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class AnvilOfflinePlayerRequirement implements AnvilRequirement {

  @Override
  public List<Component> getDescription(List<Component> description, TextColor stateColor,
      String currentInput) {
    description.add(Component.text("Die Eingabe muss einem Spielernamen entsprechen.", stateColor));

    return description;
  }

  @Override
  public CompletableFuture<Boolean> isMet(String input) {
    input = input.trim().replace(" ", "");

    if (input.isEmpty()) {
      return CompletableFuture.completedFuture(false);
    }

    return KotlinConversationUtils.INSTANCE.getUuidAsync(input).thenApply(uuid -> {
      if (uuid == null) {
        return false;
      }

      OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
      player.getPlayerProfile().complete(false);

      return player.hasPlayedBefore();
    }).exceptionally(exception -> false);
  }
}
