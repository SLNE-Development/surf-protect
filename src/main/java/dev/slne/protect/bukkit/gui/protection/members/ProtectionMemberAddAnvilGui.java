package dev.slne.protect.bukkit.gui.protection.members;

import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.bukkit.gui.protection.members.requirements.AnvilLengthRequirement;
import dev.slne.protect.bukkit.gui.protection.members.requirements.AnvilNoSpaceRequirement;
import dev.slne.protect.bukkit.gui.protection.members.requirements.AnvilRequirement;
import dev.slne.protect.bukkit.user.ProtectionUserFinder;
import org.bukkit.entity.Player;

import java.util.List;

public class ProtectionMemberAddAnvilGui extends ProtectionMemberAnvilGui {

    private final ProtectedRegion region;

    /**
     * Creates a new anvil gui
     *
     * @param parentGui     the parent gui
     * @param viewingPlayer the viewing player
     * @param region        the region to add the member to
     */
    public ProtectionMemberAddAnvilGui(Gui parentGui, Player viewingPlayer, ProtectedRegion region) {
        super(parentGui, "Mitglied hinzufügen", viewingPlayer);

        this.region = region;
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

        LocalPlayer memberPlayer = ProtectionUserFinder.findLocalPlayer(input);
        region.getMembers().addPlayer(memberPlayer);
        getViewingPlayer().sendMessage("§aDu hast §e" + input + " §azu den Mitgliedern hinzugefügt.");
    }

    @Override
    public void onCancel(String input) {
        backToParentGui();

        getViewingPlayer().sendMessage("§cDu hast das Hinzufügen von §e" + input + " §cabgebrochen.");
    }

    /**
     * Gets the region
     *
     * @return the region
     */
    public ProtectedRegion getRegion() {
        return region;
    }
}
