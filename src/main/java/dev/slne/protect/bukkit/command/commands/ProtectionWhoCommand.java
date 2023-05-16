package dev.slne.protect.bukkit.command.commands;

import org.bukkit.Location;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.jorel.commandapi.CommandAPICommand;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.regions.RegionInfo;
import dev.slne.protect.bukkit.utils.ProtectionSettings;
import dev.slne.protect.bukkit.utils.ProtectionUtils;
import net.kyori.adventure.text.Component;

public class ProtectionWhoCommand extends CommandAPICommand {

	public ProtectionWhoCommand() {
		super("pwho");

		withPermission("survival.protect.pwho");

		executesPlayer((player, args) -> {
			Location playerLocation = player.getLocation();
			ProtectedRegion protectedRegion = ProtectionUtils.getProtectedRegionByLocation(playerLocation);

			if (protectedRegion == null
					|| protectedRegion.getFlags().containsKey(ProtectionSettings.SURVIVAL_PROTECT)) {
				player.sendMessage(MessageManager.prefix().append(Component
						.text("Du stehst in keiner von einem Spieler gesicherten Region.", MessageManager.ERROR)));
				return;
			}

			new RegionInfo(protectedRegion).thenAcceptAsync(regionInfo -> {
				String regionName = regionInfo.getInfo() != null ? regionInfo.getInfo().getName() : null;
				String regionId = regionInfo.getRegion().getId();

				boolean existsAndDifferent = regionName != null && !regionName.equals(regionId);

				Component regionOwnersMembers = ProtectionUtils.getRegionOwnersMembersComponent(regionInfo);

				player.sendMessage(MessageManager.prefix()
						.append(Component.text("Du befindest dich aktuell in der Region ", MessageManager.INFO))
						.append(!existsAndDifferent
								? Component.text(regionName + " (" + regionId + ")", MessageManager.VARIABLE_VALUE)
								: Component
										.text(regionId, MessageManager.VARIABLE_VALUE))
						.append(Component.text(". ", MessageManager.INFO)).append(regionOwnersMembers));
			});
		});

		register();
	}

}
