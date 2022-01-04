package com.kneelawk.wiredredstone.wirenet

import com.kneelawk.wiredredstone.util.Graph
import com.kneelawk.wiredredstone.util.Link
import com.kneelawk.wiredredstone.util.Node

// This is almost completely copied from 2xsaiko's HCTM-Base.

typealias NetNode = Node<NetworkPart<out PartExt>, Nothing?>
typealias NetGraph = Graph<NetworkPart<out PartExt>, Nothing?>
typealias NetLink = Link<NetworkPart<out PartExt>, Nothing?>

typealias TNetNode<T> = Node<NetworkPart<T>, Nothing>
