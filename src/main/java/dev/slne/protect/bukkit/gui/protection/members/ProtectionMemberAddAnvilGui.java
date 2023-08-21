package dev.slne.protect.bukkit.gui.protection.members;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.gui.api.SurfGui;
import dev.slne.gui.api.anvil.SurfAnvilGui;
import dev.slne.gui.api.anvil.requirement.AnvilRequirement;
import dev.slne.gui.api.anvil.requirement.requirements.AnvilLengthRequirement;
import dev.slne.gui.api.anvil.requirement.requirements.AnvilNoSpaceRequirement;
import dev.slne.gui.api.anvil.requirement.requirements.AnvilNoSpecialCharacterRequirement;
import dev.slne.gui.api.anvil.requirement.requirements.player.AnvilOfflinePlayerRequirement;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.user.ProtectionUserFinder;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ProtectionMemberAddAnvilGui extends SurfAnvilGui {

    private final ProtectedRegion region;

    /**
     * Creates a new anvil gui
     *
     * @param parentGui the parent gui
     * @param region    the region to add the member to
     */
    public ProtectionMemberAddAnvilGui(SurfGui parentGui, ProtectedRegion region) {
        super(parentGui, Component.text("Mitglied hinzufügen"), MessageManager.prefix());

        this.region = region;
    }

    @Override
    public SurfAnvilGui copyGui() {
        return new ProtectionMemberAddAnvilGui(getParent(), region);
    }

    @Override
    public List<AnvilRequirement> getRequirements(List<AnvilRequirement> requirements) {
        requirements.add(new AnvilLengthRequirement(3, 16));
        requirements.add(new AnvilNoSpaceRequirement());
        requirements.add(new AnvilNoSpecialCharacterRequirement("A-Za-z0-9_"));
        requirements.add(new AnvilOfflinePlayerRequirement());

        return requirements;
    }

    @Override
    public List<AnvilGUI.ResponseAction> onSubmit(Player player, String input) {
        List<AnvilGUI.ResponseAction> responseActions = new ArrayList<>();

        LocalPlayer memberPlayer = ProtectionUserFinder.findLocalPlayer(input);
        region.getMembers().addPlayer(memberPlayer);

        player.sendMessage(MessageManager.prefix().append(Component.text("Du hast ",
                        MessageManager.SUCCESS)).append(Component.text(input, MessageManager.VARIABLE_VALUE))
                .append(Component.text(" zu den Mitgliedern hinzugefügt.", MessageManager.SUCCESS)));

        backToParent(player);

        return responseActions;
    }

    @Override
    public List<AnvilGUI.ResponseAction> onCancel(Player player, String input) {
        List<AnvilGUI.ResponseAction> responseActions = new ArrayList<>();

        backToParent(player);

        return responseActions;
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
