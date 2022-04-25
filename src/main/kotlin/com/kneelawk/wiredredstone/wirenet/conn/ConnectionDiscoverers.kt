package com.kneelawk.wiredredstone.wirenet.conn

import com.kneelawk.wiredredstone.util.ConnectionType.*
import com.kneelawk.wiredredstone.wirenet.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import kotlin.reflect.KClass

// Most of this is copied from HTCM-Base.
// Some modifications have been made to check for things in the way of wire connections.

object ConnectionDiscoverers {
    val WIRE = connectionDiscoverer<ConnectablePartExt, Direction> {
        // wires in same block
        connectionRule {
            forOutputs { Direction.values().asSequence().filter { it.axis != self.data.ext.side.axis } }
            connect {
                findNode(pos, Constraint(ConnectablePartExt::class) { otherNode ->
                    val other = otherNode.data.ext
                    other.side == output && other.canConnectAt(world, pos, self.data.ext.side, INTERNAL)
                })
            }
        }

        // planar connections
        connectionRule {
            forOutputs { Direction.values().asSequence().filter { it.axis != self.data.ext.side.axis } }
            connect {
                val otherPos = pos.offset(output)
                findNode(otherPos, Constraint(ConnectablePartExt::class) { otherNode ->
                    val other = otherNode.data.ext
                    other.side == self.data.ext.side && other.canConnectAt(world, otherPos, output.opposite, EXTERNAL)
                })
            }
        }

        // machine connections
        connectionRule {
            forOutputs { Direction.values().asSequence().filter { it != self.data.ext.side.opposite } }
            connect {
                findNode(pos.offset(output), Constraint(FullBlockPartExt::class))
            }
        }

        // corner connections
        connectionRule {
            forOutputs { Direction.values().asSequence().filter { it.axis != self.data.ext.side.axis } }
            connect {
                val otherPos = pos.offset(output).offset(self.data.ext.side)
                findNode(otherPos, Constraint(ConnectablePartExt::class) { otherNode ->
                    val other = otherNode.data.ext
                    other.side == output.opposite && other.canConnectAt(
                        world, otherPos, self.data.ext.side.opposite, CORNER
                    )
                })
            }
        }
    }

    private data class Edge(val side: Direction, val edge: Direction)

    private val edges = Direction.values().flatMap { side ->
        Direction.values().filter { edge -> edge.axis != side.axis }.map { edge -> Edge(side, edge) }
    }

    val FULL_BLOCK = connectionDiscoverer<FullBlockPartExt, Edge> {
        connectionRule {
            forOutputs { edges.asSequence() }
            connect {
                findNode(pos.offset(output.side), Constraint(ConnectablePartExt::class) { it.data.ext.side == output.edge })
            }
        }

        connectionRule {
            forOutputs { edges.asSequence() }
            connect {
                findNode(
                    pos.offset(output.side), Constraint(FullBlockPartExt::class)
                )
            }
        }
    }


    private data class Constraint<T : PartExt>(val cls: KClass<T>, val check: (TNetNode<T>) -> Boolean = { true }) {
        fun matches(node: NetNode) = cls.isInstance(node.data.ext) && check(node as TNetNode<T>)
    }

    private fun <E : PartExt, T> ConnectScope<E, T>.findNode(
        at: BlockPos, vararg constraints: Constraint<*>
    ): Sequence<NetNode> {
        return nv.getNodes(at).filter { node -> constraints.all { c -> c.matches(node) } }
    }
}
