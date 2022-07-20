package com.kneelawk.wiredredstone.logic.phantom

import com.kneelawk.graphlib.world.UnloadingRegionBasedStorage
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.ChunkSectionPos
import java.nio.file.Path

class PhantomRedstoneStorage(world: ServerWorld, path: Path, syncChunkWrites: Boolean) : AutoCloseable {
    private var closed = false

    private val chunks = UnloadingRegionBasedStorage<PhantomRedstoneChunk>(
        world, path, syncChunkWrites, ::PhantomRedstoneChunk, ::PhantomRedstoneChunk
    )

    fun onWorldChunkLoad(pos: ChunkPos) = chunks.onWorldChunkLoad(pos)

    fun onWorldChunkUnload(pos: ChunkPos) = chunks.onWorldChunkUnload(pos)

    fun tick() = chunks.tick()

    fun saveChunk(pos: ChunkPos) = chunks.saveChunk(pos)

    fun saveAll() = chunks.saveAll()

    override fun close() {
        if (closed) return
        closed = true

        chunks.close()
    }

    fun get(pos: BlockPos): Set<PhantomRedstoneRef> {
        return chunks.getIfExists(ChunkSectionPos.from(pos))?.get(pos) ?: emptySet()
    }

    fun add(pos: BlockPos, ref: PhantomRedstoneRef) {
        chunks.getOrCreate(ChunkSectionPos.from(pos)).add(pos, ref)
    }

    fun remove(pos: BlockPos, ref: PhantomRedstoneRef): Boolean {
        return chunks.getIfExists(ChunkSectionPos.from(pos))?.remove(pos, ref) ?: false
    }
}
