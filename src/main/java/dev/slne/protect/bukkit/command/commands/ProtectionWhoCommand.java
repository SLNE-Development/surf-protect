package dev.slne.protect.bukkit.command.commands;

import java.util.Set;

import org.bukkit.Location;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.jorel.commandapi.CommandAPICommand;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.info.RegionInfo;

public class ProtectionWhoCommand extends CommandAPICommand {

	/**
	 * The {@link ProtectionWhoCommand}
	 */
	public ProtectionWhoCommand() {
		super("pwho");

		withPermission("surf.protect.pwho");

		executesPlayer((player, args) -> {
			Location playerLocation = player.getLocation();

			Set<ProtectedRegion> regions = ProtectionUtils.getProtectedRegionsByLocation(playerLocation);

			if (regions.isEmpty()) {
				player.sendMessage(MessageManager.getNoPlayerDefinedRegionComponent());
			}

			for (ProtectedRegion protectedRegion : regions) {
				RegionInfo regionInfo = new RegionInfo(protectedRegion);
				player.sendMessage(MessageManager.getPWhoComponent(regionInfo));
			}
		});

		register();
	}

}
