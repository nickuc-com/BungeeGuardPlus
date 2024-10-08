/*
 * This file is part of BungeeGuard, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.bungeeguard.backend.listener;

import me.lucko.bungeeguard.backend.BungeeGuardBackend;
import me.lucko.bungeeguard.backend.TokenStore;

import java.util.concurrent.atomic.AtomicLong;

/**
 * An abstract handshake listener.
 */
public abstract class AbstractHandshakeListener {
    protected final BungeeGuardBackend plugin;
    protected final TokenStore tokenStore;

    protected final String noDataKickMessage;
    protected final String invalidTokenKickMessage;

    private final AtomicLong throttle = new AtomicLong();

    protected AbstractHandshakeListener(BungeeGuardBackend plugin, TokenStore tokenStore) {
        this.plugin = plugin;
        this.tokenStore = tokenStore;
        this.noDataKickMessage = plugin.getKickMessage("no-data-kick-message");
        this.invalidTokenKickMessage = plugin.getKickMessage("invalid-token-kick-message");
    }

    public boolean isRateLimitAllowed() {
        long current = System.currentTimeMillis();
        return current - this.throttle.getAndSet(current) >= 1000;
    }
}
