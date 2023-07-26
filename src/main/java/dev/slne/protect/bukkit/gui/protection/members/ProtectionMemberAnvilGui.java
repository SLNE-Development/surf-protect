package dev.slne.protect.bukkit.gui.protection.members;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.gui.protection.members.requirements.AnvilRequirement;
import dev.slne.protect.bukkit.gui.utils.ItemUtils;
import dev.slne.protect.bukkit.message.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class ProtectionMemberAnvilGui extends AnvilGui {

    private final Player viewingPlayer;
    private final Gui parentGui;
    private final StaticPane firstItemPane;
    private final StaticPane secondItemPane;
    private final StaticPane resultItemPane;
    private boolean isSuccess;
    private List<AnvilRequirement> requirements;

    /**
     * Creates a new anvil gui
     *
     * @param parentGui     the parent gui
     * @param title         the title of the anvil gui
     * @param viewingPlayer the viewing player
     */
    public ProtectionMemberAnvilGui(Gui parentGui, String title, Player viewingPlayer) {
        super(title);

        this.parentGui = parentGui;
        this.viewingPlayer = viewingPlayer;

        this.requirements = new ArrayList<>();
        this.requirements = getRequirements(requirements);

        setCost((short) 0);
        setOnNameInputChanged(this::handleInput);
        setOnClose(event -> {
            if (!isSuccess) {
                this.onCancel(getRenameText());
            }
        });

        setOnGlobalClick(event -> event.setCancelled(true));
        setOnGlobalDrag(event -> event.setCancelled(true));

        this.firstItemPane = new StaticPane(0, 0, 1, 1);
        this.secondItemPane = new StaticPane(0, 0, 1, 1);
        this.resultItemPane = new StaticPane(0, 0, 1, 1);

        getFirstItemComponent().addPane(firstItemPane);
        getSecondItemComponent().addPane(secondItemPane);
        getResultComponent().addPane(resultItemPane);

        handleInput(getRenameText());
    }

    /**
     * Returns the requirements
     *
     * @param requirements the requirements
     *
     * @return the requirements
     */
    public abstract List<AnvilRequirement> getRequirements(List<AnvilRequirement> requirements);

    /**
     * Called when the player submits the anvil gui
     *
     * @param input the input
     */
    public abstract void onSubmit(String input);

    /**
     * Called when the player cancels the anvil gui
     *
     * @param input the input
     */
    public abstract void onCancel(String input);

    /**
     * Handles the input
     *
     * @param input the input
     */
    private void handleInput(String input) {
        Map<AnvilRequirement, Boolean> requirementsMet = new HashMap<>();
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        for (AnvilRequirement requirement : requirements) {
            futures.add(requirement.isMet(input));
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenAcceptAsync(v -> {
            for (int i = 0; i < requirements.size(); i++) {
                boolean result = futures.get(i).join();

                requirementsMet.put(requirements.get(i), result);
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    updateItems(requirementsMet);
                }
            }.runTask(BukkitMain.getInstance());
        }).exceptionally(throwable -> {
            throwable.printStackTrace();

            return null;
        });
    }

    /**
     * Updates the items
     */
    private void updateItems(Map<AnvilRequirement, Boolean> requirementsMet) {
        boolean allMet = allRequirementsMet(requirementsMet);

        this.firstItemPane.addItem(buildItem(requirementsMet, false), 0, 0);

        if (allMet) {
            this.resultItemPane.addItem(buildItem(requirementsMet, true), 0, 0);
        } else {
            this.resultItemPane.removeItem(0, 0);
        }

        update();
    }

    /**
     * Returns if all requirements are met
     *
     * @param requirementsMet the requirements met
     *
     * @return if all requirements are met
     */
    private boolean allRequirementsMet(Map<AnvilRequirement, Boolean> requirementsMet) {
        for (boolean met : requirementsMet.values()) {
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
     * @param result          if result
     *
     * @return the item
     */
    private GuiItem buildItem(Map<AnvilRequirement, Boolean> requirementsMet, boolean result) {
        List<Component> lore = buildItemLore(requirementsMet, result);
        Component[] loreArray = lore.toArray(Component[]::new);

        String renameText = getRenameText();
        Material material = result ? Material.NAME_TAG : Material.PAPER;

        return new GuiItem(ItemUtils.item(material, 1, 0,
                Component.text(renameText), loreArray), event -> {
            if (result && allRequirementsMet(requirementsMet)) {
                this.isSuccess = true;
                onSubmit(getRenameText());
            } else {
                getViewingPlayer().closeInventory();
            }
        });
    }

    /**
     * Builds the item lore
     *
     * @param requirementsMet the requirements met
     * @param result          if result
     *
     * @return the item lore
     */
    private List<Component> buildItemLore(Map<AnvilRequirement, Boolean> requirementsMet, boolean result) {
        List<Component> components = new ArrayList<>();

        if (requirementsMet.size() > 0) {
            components.add(Component.empty());
            components.add(Component.text("Voraussetzungen:", NamedTextColor.GRAY));
            components.add(Component.empty());
        }

        for (Map.Entry<AnvilRequirement, Boolean> requirementEntry : requirementsMet.entrySet()) {
            AnvilRequirement requirement = requirementEntry.getKey();
            boolean met = requirementEntry.getValue();

            String icon = met ? "✔" : "✖";
            TextColor stateColor = met ? MessageManager.SUCCESS : MessageManager.ERROR;

            TextComponent.Builder builder = Component.text();
            builder.append(Component.text(icon, stateColor));
            builder.append(Component.space());
            builder.append(requirement.getDescription(stateColor, getRenameText()));

            components.add(builder.build());
        }

        components.add(Component.empty());
        if (result) {
            components.add(Component.text("Klicke, um zu bestätigen.", NamedTextColor.GRAY));
        } else {
            components.add(Component.text("Klicke, um den Vorgang abzubrechen.", NamedTextColor.GRAY));
        }
        components.add(Component.empty());

        return components;
    }

    /**
     * Returns the parent gui
     *
     * @return the parent gui
     */
    public Gui getParentGui() {
        return parentGui;
    }

    /**
     * Returns if the current gui has a parent gui
     *
     * @return if the current gui has a parent gui
     */
    public boolean hasParentGui() {
        return getParentGui() != null;
    }

    /**
     * Gets the viewing player
     *
     * @return the viewing player
     */
    public Player getViewingPlayer() {
        return viewingPlayer;
    }

    /**
     * Returns to the parent gui
     */
    public void backToParentGui() {
        if (hasParentGui()) {
            getParentGui().show(getViewingPlayer());
            getParentGui().update();
            return;
        }

        getViewingPlayer().closeInventory();
    }
}
