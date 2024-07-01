package dev.slne.protect.bukkit.region.flag;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import dev.slne.protect.bukkit.region.flag.info.ProtectionFlagInfo;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

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
    return new ProtectionFlagInfo(context.getUserInput());
  }

  @Override
  @SuppressWarnings("unchecked")
  public ProtectionFlagInfo unmarshal(@Nullable Object object) {
    if (object instanceof Map<?, ?>) {
      Map<String, Object> map = (Map<String, Object>) object;

      return new ProtectionFlagInfo((String) map.get("name"));
    }

    return null;
  }

  @Override
  public Object marshal(ProtectionFlagInfo protectionFlagInfo) {
    Map<String, Object> map = new HashMap<>();

    map.put("name", protectionFlagInfo.getName());

    return map;
  }

}
