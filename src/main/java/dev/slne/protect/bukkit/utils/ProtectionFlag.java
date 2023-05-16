package dev.slne.protect.bukkit.utils;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;

public class ProtectionFlag extends Flag<ProtectionInfo> {

	protected ProtectionFlag(String name) {
		super(name);
	}

	@Override
	public ProtectionInfo parseInput(FlagContext context) throws InvalidFlagFormat {
		String userInput = context.getUserInput();
		return new ProtectionInfo(userInput);
	}

	@Override
	public ProtectionInfo unmarshal(
			@Nullable Object object) {
		if (object instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>) object;
			String name = (String) map.get("name");
			return new ProtectionInfo(name);
		}
		return null;
	}

	@Override
	public Object marshal(ProtectionInfo o) {
		Map<String, Object> map = new HashMap<>();
		map.put("name", o.getName());
		return map;
	}

}
