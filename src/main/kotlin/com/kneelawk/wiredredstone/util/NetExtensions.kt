package com.kneelawk.wiredredstone.util

import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetIdSignalK

fun <T> NetIdSignalK<T>.setRecv(recv: T.(IMsgReadCtx) -> Unit): NetIdSignalK<T> {
    setReceiver { obj, ctx -> obj.recv(ctx) }
    return this
}
