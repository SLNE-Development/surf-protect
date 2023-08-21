package dev.slne.protect.bukkit.region.flags;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import dev.slne.protect.bukkit.region.info.ProtectionFlagInfo;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the protection flag
 */
public class ProtectionFlag extends Flag<ProtectionFlagInfo> {

    /**
     * Construct a new protection flag
     *
     * @param name The name of the flag
     */
    public ProtectionFlag(String name) {
        super(name);
    }

    @Override
    public ProtectionFlagInfo parseInput(FlagContext context) {
        String userInput = context.getUserInput();

        // Form a new protection flag info from the user input. Currently only the name
        // is supported though
        return new ProtectionFlagInfo(userInput);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ProtectionFlagInfo unmarshal(@Nullable Object object) {
        // Check if the object is a map and if so it should be the
        // actual protection object
        if (object instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>) object;

            /**
             * Get values from the map
             */
            String name = (String) map.get("name");

            return new ProtectionFlagInfo(name);
        }

        return null;
    }

    @Override
    public Object marshal(ProtectionFlagInfo protectionFlagInfo) {
        Map<String, Object> map = new HashMap<>();

        // Put values into map
        map.put("name", protectionFlagInfo.name());

        return map;
    }

}
