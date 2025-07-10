package dev.slne.protect.paper.gui.protection.members;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.paper.gui.SurfGui;
import dev.slne.protect.paper.gui.anvil.SurfAnvilGui;
import dev.slne.protect.paper.gui.anvil.requirement.AnvilRequirement;
import dev.slne.protect.paper.gui.anvil.requirement.requirements.AnvilLengthRequirement;
import dev.slne.protect.paper.gui.anvil.requirement.requirements.AnvilNoSpaceRequirement;
import dev.slne.protect.paper.gui.anvil.requirement.requirements.AnvilNoSpecialCharacterRequirement;
import dev.slne.protect.paper.gui.anvil.requirement.requirements.player.AnvilOfflinePlayerRequirement;
import dev.slne.protect.paper.message.MessageManager;
import dev.slne.surf.protect.paper.util.UtilKt;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;

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

    LocalPlayer memberPlayer = UtilKt.toLocalPlayer(input);
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
