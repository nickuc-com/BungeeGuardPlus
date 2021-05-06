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

package me.lucko.bungeeguard.spigot.listener;

import me.lucko.bungeeguard.spigot.TokenStore;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Logger;

/**
 * An abstract handshake listener.
 */
public abstract class AbstractTokenListener {

    protected final TokenStore tokenStore;
    protected final Logger logger;
    protected final String noDataKickMessage, invalidTokenKickMessage;
    protected final boolean protectStatus;
    private long throttle;

    protected AbstractTokenListener(TokenStore tokenStore, Logger logger, ConfigurationSection config) {
        this.tokenStore = tokenStore;
        this.logger = logger;
        String prefix = "§c[nLogin - BungeeGuard]\n§r";
        this.noDataKickMessage = prefix + ChatColor.translateAlternateColorCodes('&', String.join("§r\n", config.getStringList("no-data-kick-message")));
        this.invalidTokenKickMessage = prefix + ChatColor.translateAlternateColorCodes('&', String.join("§r\n", config.getStringList("invalid-token-kick-message")));
        this.protectStatus = config.getBoolean("protect-status", true);
    }

    public boolean isThrottled() {
        long cur = System.currentTimeMillis();
        if (cur - throttle >= 1000) {
            throttle = cur;
            return false;
        }
        return true;
    }

}
