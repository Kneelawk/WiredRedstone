package com.kneelawk.wiredredstone.util

// Copied from HTCM-Base.

// Copyright (c) 2017-2020 2xsaiko
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
// documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
// Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
// WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
// COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
// OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

class Graph<N, L> {
    var nodes: Set<Node<N, L>> = emptySet()
        private set

    fun add(data: N): Node<N, L> {
        val node = Node<N, L>(data)
        nodes.forEach { it.onAdded(node) }
        nodes += node
        return node
    }

    fun remove(node: Node<N, L>) {
        if (node in nodes) {
            nodes -= node
            nodes.forEach { it.onRemoved(node) }
        }
    }

    private fun moveBulkUnchecked(into: Graph<N, L>, nodes: Set<Node<N, L>>) {
        this.nodes -= nodes
        into.nodes += nodes
    }

    /**
     * Removes unconnected parts of the graph and returns them as new graphs.
     */
    fun split(): Set<Graph<N, L>> {
        val result = mutableSetOf<Graph<N, L>>()
        val toBeChecked = nodes.toMutableSet()
        while (toBeChecked.isNotEmpty()) {
            val connected = mutableSetOf<Node<N, L>>()
            fun descend(node: Node<N, L>) {
                connected += node
                toBeChecked -= node
                for (link in node.connections) {
                    val a = link.other(node)
                    if (a in toBeChecked) {
                        descend(a)
                    }
                }
            }
            descend(toBeChecked.first())

            if (toBeChecked.isNotEmpty()) {
                val net = Graph<N, L>()
                moveBulkUnchecked(net, connected)
                result += net
            }
        }
        return result
    }

    fun join(other: Graph<N, L>) {
        this.nodes += other.nodes
        other.nodes = emptySet()
    }

    fun link(node1: Node<N, L>, node2: Node<N, L>, data: L): Link<N, L> {
        val link = Link(node1, node2, data)
        node1.onLink(link)
        node2.onLink(link)
        return link
    }

    operator fun contains(node: Node<N, L>) = node in nodes
}

data class Node<N, L>(val data: N) {
    var connections: Set<Link<N, L>> = emptySet()
        @JvmSynthetic internal set

    fun onAdded(node: Node<N, L>) {}

    fun onRemoved(node: Node<N, L>) {
        connections = connections.filter { node !in it }.toSet()
    }

    fun onLink(link: Link<N, L>) {
        connections += link
    }
}

data class Link<N, L>(val first: Node<N, L>, val second: Node<N, L>, val data: L) {
    operator fun contains(node: Node<N, L>) = node == first || node == second

    fun other(node: Node<N, L>) = if (node == second) first else second
}
