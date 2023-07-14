package dev.slne.protect.bukkit.gui;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import dev.slne.protect.bukkit.gui.utils.GuiUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ProtectionGui extends ChestGui {

    private final ProtectionGui parent;
    private final Player viewingPlayer;

    /**
     * Creates a new shop gui.
     *
     * @param parent        the parent gui
     * @param rows          the amount of rows this gui should have
     * @param title         the title of this gui
     * @param viewingPlayer the player viewing the shop
     */
    protected ProtectionGui(ProtectionGui parent, int rows, String title, Player viewingPlayer) {
        this(parent, rows, title, viewingPlayer, true, true, true, true);
    }

    /**
     * Creates a new shop gui.
     *
     * @param parent            the parent gui
     * @param rows              the amount of rows this gui should have
     * @param title             the title of this gui
     * @param viewingPlayer     the player viewing the shop
     * @param cancelTopClick    if the top inventory click should be cancelled
     * @param cancelTopDrag     if the top inventory drag should be cancelled
     * @param cancelBottomClick if the bottom inventory click should be cancelled
     * @param cancelBottomDrag  if the bottom inventory drag should be cancelled
     */
    @SuppressWarnings("java:S107")
    protected ProtectionGui(ProtectionGui parent, int rows, String title, Player viewingPlayer, boolean cancelTopClick,
                            boolean cancelTopDrag, boolean cancelBottomClick, boolean cancelBottomDrag) {
        super(rows, title);

        if (rows < 2) {
            throw new IllegalArgumentException("rows must be at least 2");
        }

        this.parent = parent;
        this.viewingPlayer = viewingPlayer;

        setOnTopClick(event -> event.setCancelled(cancelTopClick));
        setOnTopDrag(event -> event.setCancelled(cancelTopDrag));
        setOnBottomClick(event -> event.setCancelled(cancelBottomClick));
        setOnBottomDrag(event -> event.setCancelled(cancelBottomDrag));
        setOnOutsideClick(event -> event.setCancelled(true));

        addPane(GuiUtils.getOutline(0));
        addPane(GuiUtils.getOutline(rows - 1));
        addPane(GuiUtils.getNavigation(this, rows - 1));
    }

    /**
     * Shows the parent gui to the player.
     *
     * @param player the player to show the gui to
     */
    public void showParent(Player player) {
        if (parent == null) {
            return;
        }

        parent.show(player);
        parent.update();
    }

    /**
     * Returns if the gui has a parent.
     *
     * @return if the gui has a parent
     */
    public boolean hasParent() {
        return parent != null;
    }

    /**
     * Walks through the parents of this gui and returns them.
     *
     * @return the parents of this gui
     */
    public List<ProtectionGui> walkParents() {
        List<ProtectionGui> parents = new ArrayList<>();

        ProtectionGui parentGui = getParent();
        while (parentGui != null) {
            parents.add(parentGui);
            parentGui = parentGui.getParent();
        }

        return parents;
    }

    /**
     * @return the parent
     */
    public ProtectionGui getParent() {
        return parent;
    }

    /**
     * @return the viewingPlayer
     */
    public Player getViewingPlayer() {
        return viewingPlayer;
    }

}

