package dev.slne.protect.bukkit.regions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.slne.protect.bukkit.user.ProtectionUserFinder;
import dev.slne.protect.bukkit.utils.ProtectionInfo;
import dev.slne.protect.bukkit.utils.ProtectionSettings;
import dev.slne.protect.bukkit.utils.ProtectionUtils;

public class RegionInfo extends CompletableFuture<RegionInfo> {

	private ProtectionInfo info;
	private final ProtectedRegion region;

	private List<LocalPlayer> owners;
	private List<LocalPlayer> members;

	public RegionInfo(ProtectedRegion region) {
		this.region = region;
		this.info = region.getFlag(ProtectionSettings.SURVIVAL_PROTECT_FLAG);

		if (this.info == null) {
			this.info = new ProtectionInfo(this.region.getId());
		}

		fetchAll();
	}

	public CompletableFuture<Boolean> fetchAll() {
		return CompletableFuture
				.allOf(fetch(region.getOwners()).thenApplyAsync(owners -> this.owners = owners),
						fetch(region.getMembers()).thenApplyAsync(members -> this.members = members))
				.thenApplyAsync(v -> this.complete(this));
	}

	public long getArea() {
		return ProtectionUtils.getArea(this.region);
	}

	public float getPrice() {
		return getArea() * ProtectionSettings.PRICE_PER_BLOCK;
	}

	public float getRetailPrice() {
		return getPrice() * ProtectionSettings.RETAIL_MODIFIER;
	}

	public String getName() {
		return this.info != null ? this.info.getName() : this.region.getId();
	}

	public ProtectionInfo getInfo() {
		return info;
	}

	public void setInfo(ProtectionInfo info) {
		this.info = info;
		this.region.setFlag(ProtectionSettings.SURVIVAL_PROTECT_FLAG, this.info);
	}

	public Location getTeleportLocation() {
		return BukkitAdapter.adapt(region.getFlag(Flags.TELE_LOC));
	}

	public CompletableFuture<List<LocalPlayer>> fetch(DefaultDomain domain) {
		List<LocalPlayer> localPlayers = new ArrayList<>();
		Set<UUID> uuids = domain.getUniqueIds();

		for (UUID uuid : uuids) {
			LocalPlayer localPlayer = ProtectionUserFinder.findLocalPlayer(uuid);

			if (localPlayer != null) {
				localPlayers.add(localPlayer);
			}
		}

		return CompletableFuture.supplyAsync(() -> localPlayers);
	}

	public ProtectedRegion getRegion() {
		return region;
	}

	public List<LocalPlayer> getMembers() {
		return members;
	}

	public List<LocalPlayer> getOwners() {
		return owners;
	}

}
