package dev.slne.protect.bukkit.gui.protection.rename;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.gui.api.SurfGui;
import dev.slne.gui.api.anvil.SurfAnvilGui;
import dev.slne.gui.api.anvil.requirement.AnvilRequirement;
import dev.slne.gui.api.anvil.requirement.requirements.AnvilLengthRequirement;
import dev.slne.gui.api.anvil.requirement.requirements.AnvilNoSpaceRequirement;
import dev.slne.gui.api.anvil.requirement.requirements.AnvilNoSpecialCharacterRequirement;
import dev.slne.gui.api.confirmation.ConfirmationGui;
import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.info.ProtectionFlagInfo;
import dev.slne.protect.bukkit.region.info.RegionInfo;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.region.transaction.ProtectionRenameData;
import dev.slne.protect.bukkit.user.ProtectionUser;
import dev.slne.surf.surfapi.core.api.messages.Colors;
import dev.slne.transaction.api.TransactionApi;
import dev.slne.transaction.api.currency.Currency;
import dev.slne.transaction.api.transaction.result.TransactionAddResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static dev.slne.protect.bukkit.user.ProtectionUser.getProtectionUser;

public class RenameAnvilGUI extends SurfAnvilGui {

	private static final ComponentLogger LOGGER  = ComponentLogger.logger("RenameAnvilGUI");
	private final ProtectedRegion region;
	private boolean isProcessingTransaction = false;

	/**
	 * Creates a new anvil gui
	 *
	 * @param parentGui the parent gui
	 * @param region    the region to add the member to
	 */
	public RenameAnvilGUI(SurfGui parentGui, ProtectedRegion region) {
		super(parentGui, Component.text("Grundstück umbennnen"), MessageManager.prefix());

		this.region = region;
	}

	@Override
	public SurfAnvilGui copyGui() {
		return new RenameAnvilGUI(getParent(), region);
	}

	@Override
	public List<AnvilRequirement> getRequirements(List<AnvilRequirement> requirements) {
		requirements.add(new AnvilLengthRequirement(3, 16));
		requirements.add(new AnvilNoSpaceRequirement());
		requirements.add(new AnvilNoSpecialCharacterRequirement("A-Za-z0-9_"));

		return requirements;
	}

	@Override
	public List<AnvilGUI.ResponseAction> onSubmit(Player player, String input) {
		List<AnvilGUI.ResponseAction> responseActions = new ArrayList<>();

		Currency currency = TransactionApi.getCurrency("CastCoin").get();

		RegionInfo regionInfo = new RegionInfo(player.getWorld(), region);
		String oldName = regionInfo.getName();
		String newName = input;

		List<Component> questionLore = new ArrayList<>();
		questionLore.add(Component.text("Möchtest du das Grundstück wirklich in ", NamedTextColor.GRAY)
						.append(Component.text(newName, NamedTextColor.YELLOW)).append(Component.text(" umbenennen?", NamedTextColor.GRAY)));
		questionLore.add(Component.empty());
		questionLore.add(Component.text("Achtung:", NamedTextColor.RED)
						.append(Component.text(" Für diese Aktion wird eine Gebühr in Höhe von ", NamedTextColor.GRAY)
						.append(Component.text(ProtectionSettings.PROTECTION_RENAME_PRICE + " " + currency.getName(), NamedTextColor.YELLOW))
						.append(Component.text(" berechnet.", NamedTextColor.GRAY))));

		new BukkitRunnable() {
			@Override
			public void run() {
				new ConfirmationGui(getParent(),
						confirmEvent -> {
							ProtectionUser protectionUser = getProtectionUser(player);
							isProcessingTransaction = true;

							protectionUser.hasEnoughCurrency(BigDecimal.valueOf(ProtectionSettings.PROTECTION_RENAME_PRICE), currency).thenAcceptAsync(hasEnoughCurrency -> {
								if (!hasEnoughCurrency) {
									player.sendMessage(MessageManager.getTooExpensiveToBuyComponent());
									isProcessingTransaction = false;
								} else {
									protectionUser.addTransaction(
											null,
											BigDecimal.valueOf(ProtectionSettings.PROTECTION_RENAME_PRICE).negate(),
											currency,
											new ProtectionRenameData(Bukkit.getWorlds().get(0), region, oldName, newName)
									).thenAcceptAsync(transactionAddResult -> {
										if (transactionAddResult != null && transactionAddResult.equals(TransactionAddResult.SUCCESS)) {
											regionInfo.setProtectionInfoToRegion(new ProtectionFlagInfo(newName));
											player.sendMessage(Colors.PREFIX.append(Component.text("Du hast das Grundstück ", Colors.INFO))
													.append(Component.text(oldName, Colors.VARIABLE_VALUE))
													.append(Component.text(" in ", Colors.INFO))
													.append(Component.text(newName, Colors.VARIABLE_VALUE))
													.append(Component.text(" umbenannt.", Colors.INFO)));
										} else {
											protectionUser.sendMessage(MessageManager.getTooExpensiveToBuyComponent());
										}

										isProcessingTransaction = false;
									}).exceptionally(throwable -> {
										LOGGER.error("Error while buying protection", throwable);
										return null;
									});
								}

							}).exceptionally(throwable -> {
								LOGGER.error("Error while checking if user has enough currency", throwable);
								return null;
							});

							backToParent(player);
						}, cancelEvent -> {
					backToParent(player);
				}, Component.text("Möchtest du das Grundstück wirklich umbenennen?", Colors.WARNING), questionLore).show(player);
			}
		}.runTask(BukkitMain.getInstance());

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
