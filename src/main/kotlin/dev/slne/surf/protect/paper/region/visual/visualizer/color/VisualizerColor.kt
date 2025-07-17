package dev.slne.surf.protect.paper.region.visual.visualizer.color

import com.sk89q.worldguard.protection.regions.ProtectedRegion
import org.bukkit.block.BlockType
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player

enum class VisualizerColor(val blockData: BlockData) {
    OWNING(BlockType.LIME_STAINED_GLASS.createBlockData()),
    MEMBER(BlockType.LIGHT_BLUE_STAINED_GLASS.createBlockData()),
    NOT_OWNING(BlockType.RED_STAINED_GLASS.createBlockData());

    companion object {
        fun selectColor(player: Player, region: ProtectedRegion): VisualizerColor = when {
            region.owners.contains(player.uniqueId) -> OWNING
            region.members.contains(player.uniqueId) -> MEMBER
            else -> NOT_OWNING
        }
    }
}
