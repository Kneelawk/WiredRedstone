package com.kneelawk.wiredredstone.mixin.api;

import java.net.SocketAddress;

import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import com.kneelawk.wiredredstone.mixin.impl.ServerLoginNetworkHandlerAccessor;
import com.kneelawk.wiredredstone.mixin.impl.ServerPlayNetworkHandlerAccessor;

public final class NetworkHelper {
    private NetworkHelper() {
    }

    public static SocketAddress getAddress(ServerLoginNetworkHandler handler) {
        return ((ServerLoginNetworkHandlerAccessor) handler).getConnection().getAddress();
    }

    public static SocketAddress getAddress(ServerPlayNetworkHandler handler) {
        return ((ServerPlayNetworkHandlerAccessor) handler).getConnection().getAddress();
    }
}
