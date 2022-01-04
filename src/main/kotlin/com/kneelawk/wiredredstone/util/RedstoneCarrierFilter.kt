package com.kneelawk.wiredredstone.util

import com.kneelawk.wiredredstone.wirenet.NetNode
import com.kneelawk.wiredredstone.wirenet.RedstoneCarrierPartExt
import com.kneelawk.wiredredstone.wirenet.conn.ConnectionFilter

object RedstoneCarrierFilter : ConnectionFilter {
    override fun accepts(self: NetNode, other: NetNode): Boolean {
        val d1 = self.data.ext as? RedstoneCarrierPartExt ?: return false
        val d2 = other.data.ext as? RedstoneCarrierPartExt ?: return false
        return d1.redstoneType.canConnect(d2.redstoneType)
    }
}
