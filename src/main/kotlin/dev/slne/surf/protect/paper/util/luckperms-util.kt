package dev.slne.surf.protect.paper.util

import dev.slne.surf.api.core.luckperms.LuckPermsAccess

/**
 * Gets the prefix of a LuckPerms group, or `null` if the group does not exist or has no prefix.
 */
fun getGroupPrefix(groupName: String): String? = LuckPermsAccess.luckperms.groupManager
    .getGroup(groupName)
    ?.cachedData
    ?.metaData
    ?.prefix
