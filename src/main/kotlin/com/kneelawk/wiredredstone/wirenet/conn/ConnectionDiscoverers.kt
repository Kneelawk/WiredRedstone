package com.kneelawk.wiredredstone.wirenet.conn

import com.kneelawk.wiredredstone.wirenet.FullBlockPartExt
import com.kneelawk.wiredredstone.wirenet.NetNode
import com.kneelawk.wiredredstone.wirenet.PartExt
import com.kneelawk.wiredredstone.wirenet.WirePartExt
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import kotlin.reflect.KClass



object ConnectionDiscoverers {
    val WIRE = connectionDiscoverer<WirePartExt, Direction> {
        // wires in same block
        connectionRule {
            forOutputs { Direction.values().filter { it.axis != self.data.ext.side.axis } }
            connect {
                findNode(pos,
                    Constraint(WirePartExt::class) { it.side == output && it.canConnectAt(world, pos, self.data.ext.side) }
                )
            }
        }

        // planar connections
        connectionRule {
            forOutputs { Direction.values().filter { it.axis != self.data.ext.side.axis } }
            connect {
                val otherPos = pos.offset(output)
                findNode(otherPos,
                    Constraint(WirePartExt::class) { it.side == self.data.ext.side && it.canConnectAt(world, otherPos, output.opposite) }
                )
            }
        }

        // machine connections
        connectionRule {
            forOutputs { Direction.values().filter { it != self.data.ext.side.opposite } }
            connect {
                findNode(
                    pos.offset(output),
                    Constraint(FullBlockPartExt::class)
                )
            }
        }

        // corner connections
        connectionRule {
            forOutputs { Direction.values().filter { it.axis != self.data.ext.side.axis } }
            connect {
                val otherPos = pos.offset(output).offset(self.data.ext.side)
                findNode(otherPos,
                    Constraint(WirePartExt::class) { it.side == output.opposite && it.canConnectAt(world, otherPos, self.data.ext.side.opposite) }
                )
            }
        }
    }

    private data class Edge(val side: Direction, val edge: Direction)

    private val edges = Direction.values().flatMap { side -> Direction.values().filter { edge -> edge.axis != side.axis }.map { edge -> Edge(side, edge) } }

    val FULL_BLOCK = connectionDiscoverer<FullBlockPartExt, Edge> {
        connectionRule {
            forOutputs { edges }
            connect {
                findNode(pos.offset(output.side),
                    Constraint(WirePartExt::class) { it.side == output.edge }
                )
            }
        }

        connectionRule {
            forOutputs { edges }
            connect {
                findNode(
                    pos.offset(output.side),
                    Constraint(FullBlockPartExt::class)
                )
            }
        }
    }


    private data class Constraint<T : Any>(val cls: KClass<T>, val check: (T) -> Boolean = { true }) {
        fun matches(node: NetNode) = cls.isInstance(node.data.ext) && check(node.data.ext as T)
    }

    private fun <E : PartExt, T> ConnectScope<E, T>.findNode(at: BlockPos, vararg constraints: Constraint<*>): List<NetNode> {
        return nv.getNodes(at).filter { node -> constraints.all { c -> c.matches(node) } }
    }

}
