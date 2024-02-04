package dev.slne.protect.bukkit.listener.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import dev.slne.protect.bukkit.region.flags.ProtectionFlagsRegistry;
import dev.slne.protect.bukkit.region.info.ProtectionFlagInfo;
import dev.slne.surf.surfapi.core.api.messages.Colors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;

import java.util.Set;
public class BorderCrossingHandler extends Handler {

	public static final Factory FACTORY = new Factory();
	public BorderCrossingHandler(Session session) {
		super(session);
	}

	@Override
	public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet,
								   Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {

		// Check if player is entering a region
		for(ProtectedRegion region : entered) {
			handleEntryOrExitMessage(player, region, true);
		}

		// Check if player is leaving a region
		for(ProtectedRegion region : exited) {
			handleEntryOrExitMessage(player, region, false);
		}

		return true;
	}

	public void handleEntryOrExitMessage(LocalPlayer player, ProtectedRegion region, boolean entered) {
		ProtectionFlagInfo protectionFlagInfo = region.getFlag(ProtectionFlagsRegistry.SURF_PROTECT_FLAG);
		StateFlag.State surfProtectionFlag = region.getFlag(ProtectionFlagsRegistry.SURF_PROTECTION);

		// Check if it is a player protection
		if(surfProtectionFlag == null || surfProtectionFlag == StateFlag.State.DENY || protectionFlagInfo == null) {
			return;
		}

		Player bukkitPlayer = BukkitAdapter.adapt(player);

		TextComponent.Builder builder = Component.text();
		builder.append(Colors.PREFIX);
		builder.append(Component.text("Du hast das Grundstück ", Colors.INFO));
		builder.append(Component.text(protectionFlagInfo.name(), Colors.VARIABLE_VALUE));
		builder.append(Component.text(entered ? " betreten." : " verlassen.", Colors.INFO));

		bukkitPlayer.sendMessage(builder.build());
	}

	public static class Factory extends Handler.Factory<BorderCrossingHandler> {
		@Override
		public BorderCrossingHandler create(Session session) {
			return new BorderCrossingHandler(session);
		}
	}
}
