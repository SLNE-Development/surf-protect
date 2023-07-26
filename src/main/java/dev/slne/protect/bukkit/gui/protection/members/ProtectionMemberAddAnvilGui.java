package dev.slne.protect.bukkit.gui.protection.members;

import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import dev.slne.protect.bukkit.gui.protection.members.requirements.AnvilLengthRequirement;
import dev.slne.protect.bukkit.gui.protection.members.requirements.AnvilNoSpaceRequirement;
import dev.slne.protect.bukkit.gui.protection.members.requirements.AnvilRequirement;
import org.bukkit.entity.Player;

import java.util.List;

public class ProtectionMemberAddAnvilGui extends ProtectionMemberAnvilGui {

    /**
     * Creates a new anvil gui
     *
     * @param parentGui     the parent gui
     * @param viewingPlayer the viewing player
     */
    public ProtectionMemberAddAnvilGui(Gui parentGui, Player viewingPlayer) {
        super(parentGui, "Mitglied hinzufügen", viewingPlayer);
    }

    @Override
    public List<AnvilRequirement> getRequirements(List<AnvilRequirement> requirements) {
        requirements.add(new AnvilLengthRequirement(3, 16));
        requirements.add(new AnvilNoSpaceRequirement());

        return requirements;
    }

    @Override
    public void onSubmit(String input) {
        backToParentGui();

        getViewingPlayer().sendMessage("§aDu hast §e" + input + " §azu den Mitgliedern hinzugefügt.");
    }

    @Override
    public void onCancel(String input) {
        backToParentGui();

        getViewingPlayer().sendMessage("§cDu hast das Hinzufügen von §e" + input + " §cabgebrochen.");
    }
}
