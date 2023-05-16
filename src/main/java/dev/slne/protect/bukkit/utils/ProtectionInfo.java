package dev.slne.protect.bukkit.utils;

public class ProtectionInfo {

	private final String name;

	public ProtectionInfo(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public ProtectionInfo copyWithNewName(String name) {
		return new ProtectionInfo(name);
	}
}
