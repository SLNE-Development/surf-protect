package dev.slne.surf.protect.paper.pdc

import io.papermc.paper.math.BlockPosition
import io.papermc.paper.math.Position
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

object BlockPositionPersistentDataType : PersistentDataType<IntArray, BlockPosition> {
    override fun getPrimitiveType() = IntArray::class.java
    override fun getComplexType() = BlockPosition::class.java

    override fun toPrimitive(
        complex: BlockPosition,
        context: PersistentDataAdapterContext
    ) = intArrayOf(complex.blockX(), complex.blockY(), complex.blockZ())

    override fun fromPrimitive(
        primitive: IntArray,
        context: PersistentDataAdapterContext
    ): BlockPosition {
        require(primitive.size == 3) { "Primitive array must have exactly 3 elements." }
        return Position.block(primitive[0], primitive[1], primitive[2])
    }
}