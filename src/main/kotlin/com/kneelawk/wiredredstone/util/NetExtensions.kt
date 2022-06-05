package com.kneelawk.wiredredstone.util

import alexiil.mc.lib.net.*
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil

fun <T> NetIdSignalK<T>.setRecv(recv: T.(IMsgReadCtx) -> Unit): NetIdSignalK<T> {
    setReceiver { obj, ctx -> obj.recv(ctx) }
    return this
}

fun <T> NetIdDataK<T>.setRecv(recv: T.(NetByteBuf, IMsgReadCtx) -> Unit): NetIdDataK<T> {
    setReceiver { obj, buf, ctx -> obj.recv(buf, ctx) }
    return this
}

interface NetExtensions {
    fun <T> NetIdDataK<T>.sendToServer(encoder: (NetByteBuf, IMsgWriteCtx) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        send(CoreMinecraftNetUtil.getClientConnection(), this@NetExtensions as T) { _, buf, ctx -> encoder(buf, ctx) }
    }
}
