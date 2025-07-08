package dev.slne.protect.paper.gui.anvil;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui;
import dev.slne.protect.paper.PaperMain;
import dev.slne.protect.paper.gui.SurfGui;
import dev.slne.protect.paper.gui.anvil.requirement.AnvilRequirement;
import dev.slne.protect.paper.gui.utils.sound.GuiSound;
import dev.slne.protect.paper.message.MessageManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SurfAnvilGui extends NamedGui implements SurfGui, AnvilGUI.ClickHandler {

  private final SurfGui parent;
  private final AnvilGUI.Builder anvilGuiBuilder;
  private final String defaultInput;
  private final Component title;
  private final Component pluginPrefix;
  private final List<HumanEntity> viewers;

  private boolean willinglyClosed;
  private boolean built;

  /**
   * Creates a new anvil gui.
   *
   * @param parent       the parent gui
   * @param title        the title of this gui
   * @param pluginPrefix the prefix of the plugin
   * @param defaultInput the default input
   */
  public SurfAnvilGui(@Nullable SurfGui parent, @NotNull String defaultInput,
      @NotNull Component title, @Nullable Component pluginPrefix) {
    super(ComponentHolder.of(title));

    this.parent = parent;
    this.defaultInput = defaultInput;
    this.title = title;
    this.pluginPrefix = pluginPrefix;
    this.viewers = new ArrayList<>();

    this.anvilGuiBuilder = new AnvilGUI.Builder();
  }

  private void build() {
    if (built) {
      return;
    }

    anvilGuiBuilder.onClose(closeStateSnapshot -> {
      if (!willinglyClosed) {
        onCancel(closeStateSnapshot.getPlayer(), closeStateSnapshot.getText());
      }
    });

    anvilGuiBuilder.jsonTitle(GsonComponentSerializer.gson().serialize(title));
    anvilGuiBuilder.plugin(PaperMain.getInstance());

    anvilGuiBuilder.onClickAsync(this);

    Map<AnvilRequirement, CompletableFuture<Boolean>> requirements = getMetRequirements("");
    Map<AnvilRequirement, Boolean> requirementBooleanMap = requirements.entrySet().stream()
        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().join()),
            HashMap::putAll);

    anvilGuiBuilder.text(defaultInput);

    List<Component> inputLore = buildLore(requirementBooleanMap, "", false);
    anvilGuiBuilder.itemLeft(buildItem(Material.PAPER, inputLore));

    List<Component> outputLore = buildLore(requirementBooleanMap, "", true);
    anvilGuiBuilder.itemOutput(buildItem(Material.NAME_TAG, outputLore));

    built = true;
  }

  /**
   * Creates a new anvil gui.
   *
   * @param parent       the parent gui
   * @param title        the title of this gui
   * @param pluginPrefix the prefix of the plugin
   */
  public SurfAnvilGui(@Nullable SurfGui parent, @NotNull Component title,
      @Nullable Component pluginPrefix) {
    this(parent, " ", title, pluginPrefix);
  }

  /**
   * Creates a new anvil gui.
   *
   * @param parent the parent gui
   * @param title  the title of this gui
   */
  public SurfAnvilGui(@Nullable SurfGui parent, @NotNull Component title) {
    this(parent, title, null);
  }

  /**
   * Creates a new anvil gui.
   *
   * @param parent       the parent gui
   * @param title        the title of this gui
   * @param defaultInput the default input
   */
  public SurfAnvilGui(@Nullable SurfGui parent, @NotNull String defaultInput,
      @NotNull Component title) {
    this(parent, defaultInput, title, null);
  }

  /**
   * Builds the itemstack
   *
   * @param material the material
   * @param lore     the lore
   * @return the item stack
   */
  private ItemStack buildItem(Material material, List<Component> lore) {
    ItemStack itemStack = new ItemStack(material);
    ItemMeta itemMeta = itemStack.getItemMeta();

    itemMeta.lore(lore);
    itemStack.setItemMeta(itemMeta);

    return itemStack;
  }

  @Override
  public void show(@NotNull HumanEntity humanEntity) {
    build();

    if (humanEntity instanceof Player player) {
      anvilGuiBuilder.open(player);
      viewers.add(player);

      return;
    }

    throw new IllegalArgumentException("HumanEntity must be a player");
  }

  @Override
  public @NotNull Gui copy() {
    build();

    return copyGui().getGui();
  }

  @Override
  public void click(@NotNull InventoryClickEvent event) {
    build();
    // not used
  }

  @Override
  public boolean isPlayerInventoryUsed() {
    return false;
  }

  @Override
  public int getViewerCount() {
    return 1;
  }

  @Override
  public @NotNull List<HumanEntity> getViewers() {
    return viewers;
  }

  /**
   * Returns the copied gui
   *
   * @return the copied gui
   */
  public abstract SurfAnvilGui copyGui();

  /**
   * Returns the requirements
   *
   * @param requirements the requirements
   * @return the requirements
   */
  public abstract List<AnvilRequirement> getRequirements(List<AnvilRequirement> requirements);

  /**
   * Called when the player submits the anvil gui
   *
   * @param player the player
   * @param input  the input
   */
  public abstract List<AnvilGUI.ResponseAction> onSubmit(Player player, String input);

  /**
   * Called when the player cancels the anvil gui
   *
   * @param player the player
   * @param input  the input
   */
  public abstract List<AnvilGUI.ResponseAction> onCancel(Player player, String input);

  /**
   * Returns the met requirements
   *
   * @param newInput the new input
   * @return the met requirements
   */
  private Map<AnvilRequirement, CompletableFuture<Boolean>> getMetRequirements(String newInput) {
    Map<AnvilRequirement, CompletableFuture<Boolean>> requirementsMet = new HashMap<>();
    List<AnvilRequirement> requirements = getRequirements(new ArrayList<>());

    for (AnvilRequirement requirement : requirements) {
      requirementsMet.put(requirement, requirement.isMet(newInput));
    }

    return requirementsMet;
  }

  /**
   * Returns if all requirements are met
   *
   * @param requirementMap the requirement map
   * @return if all requirements are met
   */
  private boolean allRequirementsMet(Map<AnvilRequirement, Boolean> requirementMap) {
    for (boolean met : requirementMap.values()) {
      if (!met) {
        return false;
      }
    }

    return true;
  }

  /**
   * Builds the item
   *
   * @param requirementsMet the requirements met
   * @param newInput        the new input
   * @param result          the result
   * @return the item
   */
  private List<Component> buildLore(Map<AnvilRequirement, Boolean> requirementsMet, String newInput,
      boolean result) {
    List<Component> components = new ArrayList<>();

    if (!requirementsMet.isEmpty()) {
      components.add(Component.empty());
      components.add(Component.text("Voraussetzungen:", NamedTextColor.GRAY));
      components.add(Component.empty());
    }

    for (Map.Entry<AnvilRequirement, Boolean> requirementEntry : requirementsMet.entrySet()) {
      AnvilRequirement requirement = requirementEntry.getKey();
      boolean met = requirementEntry.getValue();

//            String icon = met ? "✔" : "✖";
      String icon = "•";
//            TextColor stateColor = met ? GuiMessageManager.SUCCESS : GuiMessageManager.ERROR;
      TextColor stateColor = MessageManager.SPACER;

      List<Component> requirementLore =
          requirement.getDescription(new ArrayList<>(), stateColor, newInput);

      boolean first = true;
      for (Component requirementComponent : requirementLore) {
        TextComponent.Builder builder = Component.text();
        if (first) {
          builder.append(Component.text(icon, stateColor));
          builder.append(Component.space());
        } else {
          for (int i = 0; i < 2; i++) {
            builder.append(Component.space());
          }
        }
        builder.append(requirementComponent);

        components.add(builder.build());
        first = false;
      }
    }

    components.add(Component.empty());
    if (result) {
      components.add(Component.text("Klicke, um zu bestätigen.", NamedTextColor.GRAY));
    } else {
      components.add(Component.text("Klicke, um abzubrechen.", NamedTextColor.GRAY));
    }
    components.add(Component.empty());

    components =
        components.stream().map(component -> component.decoration(TextDecoration.ITALIC, false))
            .collect(
                Collectors.toList());

    return components;
  }

  @Override
  public @Nullable SurfGui getParent() {
    return parent;
  }

  @Override
  public @NotNull NamedGui getGui() {
    return this;
  }

  /**
   * Handles the click
   *
   * @param slot     the slot
   * @param snapshot the snapshot
   * @return the response action
   */
  @Override
  public CompletableFuture<List<AnvilGUI.ResponseAction>> apply(Integer slot,
      AnvilGUI.StateSnapshot snapshot) {
    build();

    CompletableFuture<List<AnvilGUI.ResponseAction>> future = new CompletableFuture<>();

    if (slot == AnvilGUI.Slot.INPUT_LEFT || slot == AnvilGUI.Slot.INPUT_RIGHT) {
      this.willinglyClosed = true;
      future.complete(this.onCancel(snapshot.getPlayer(), snapshot.getText()));
      GuiSound.BACK.playSound(snapshot.getPlayer(), .5f, 1);
      return future;
    }

    String newInput = snapshot.getText();
    Map<AnvilRequirement, CompletableFuture<Boolean>> requirementsMet = getMetRequirements(
        newInput);

    CompletableFuture.allOf(requirementsMet.values().toArray(CompletableFuture[]::new))
        .thenAcceptAsync(unused -> {
          Map<AnvilRequirement, Boolean> requirementMap = new HashMap<>();

          for (Map.Entry<AnvilRequirement, CompletableFuture<Boolean>> entry : requirementsMet.entrySet()) {
            requirementMap.put(entry.getKey(), entry.getValue().join());
          }

          Map<AnvilRequirement, Boolean> notMet = requirementMap.entrySet().stream()
              .filter(entry -> !entry.getValue())
              .collect(Collectors.toMap(Map.Entry::getKey, entry -> false));

          if (!allRequirementsMet(requirementMap)) {
            for (Map.Entry<AnvilRequirement, Boolean> entry : notMet.entrySet()) {
              List<Component> descriptionComponent = entry.getKey()
                  .getDescription(new ArrayList<>(),
                      NamedTextColor.RED,
                      snapshot.getText());

              TextComponent.Builder builder = Component.text();
              builder.append(Component.text("✘ ", MessageManager.ERROR));
              for (Component component : descriptionComponent) {
                builder.append(component);
              }
              Component description = builder.build();

              if (pluginPrefix != null) {
                description = pluginPrefix.append(description);
              }

              snapshot.getPlayer().sendMessage(description);
              GuiSound.DENY_ACTION.playSound(snapshot.getPlayer(), .5f, 1);
            }

            future.complete(List.of());
            return;
          }

          this.willinglyClosed = true;
          future.complete(this.onSubmit(snapshot.getPlayer(), newInput));
          GuiSound.CONFIRM_ACTION.playSound(snapshot.getPlayer(), .5f, 1);
        });

    return future;
  }
}

