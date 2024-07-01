package dev.slne.protect.bukkit.region.flag.info;

/**
 * A ProtectionFlagInfo is used on the actual WorldGuard region to store attributes like the name
 * and other values
 */
public class ProtectionFlagInfo {

  private String name;

  /**
   * Instantiates a new Protection flag info.
   *
   * @param name the name
   */
  public ProtectionFlagInfo(String name) {
    this.name = name;
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets name.
   *
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }
}
