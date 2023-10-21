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

package me.lucko.bungeeguard.bungee;

import com.google.common.base.Preconditions;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BungeeCord plugin which injects a special authentication token into a players
 * profile properties when they connect to a backend server.
 */
public class BungeeGuardProxyPlugin extends Plugin implements Listener {

    // characters to use to build a token
    private static final String TOKEN_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Randomly generates a new token
     *
     * @param length the length of the token
     * @return a new token
     */
    private static String generateToken(int length) {
        Preconditions.checkArgument(length > 0);
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            sb.append(TOKEN_CHARS.charAt(random.nextInt(TOKEN_CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * The auth token to inject into the property map
     */
    private String token = null;

    @Override
    public void onEnable() {

        // verify outdated BungeeCord version
        try {
            Class.forName("net.md_5.bungee.protocol.Property");
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
            Logger logger = getLogger();
            logger.log(Level.SEVERE, "*--------------------------------------------------------------*");
            logger.log(Level.SEVERE, "*      You are using an outdated version of BungeeCord!        *");
            logger.log(Level.SEVERE, "*  Please update it at https://papermc.io/downloads#Waterfall  *");
            logger.log(Level.SEVERE, "*--------------------------------------------------------------*");
            try {
                Thread.sleep(15_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        if (!getProxy().getConfig().isIpForward()) {
            getLogger().severe("*-----------------------------------------------------------*");
            getLogger().severe("'ip_forward' is set to false in BungeeCord's config.yml.");
            getLogger().severe("");
            getLogger().severe("BungeeGuard will probably not work unless this property is set to true.");
            getLogger().severe("*-----------------------------------------------------------*");
            try {
                Thread.sleep(15_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // load a token from the config, if present
        ConfigurationProvider provider = ConfigurationProvider.getProvider(YamlConfiguration.class);

        File pluginFolder = getDataFolder();
        if (!pluginFolder.exists() && !pluginFolder.mkdirs()) {
            getLogger().log(Level.SEVERE, "Could not create BungeeGuard folder. Shutting down the server...");
            getProxy().stop();
            return;
        }

        File tokenFile = new File(pluginFolder, "token.yml");
        if (tokenFile.exists()) {
            try {
                Configuration configuration = provider.load(tokenFile);
                this.token = configuration.getString("token", null);
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Unable to load token from config", e);
            }
        }

        if (this.token == null || this.token.isEmpty()) {
            this.token = generateToken(64);

            Configuration configuration = new Configuration();
            configuration.set("token", this.token);

            try {
                provider.save(configuration, tokenFile);
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Unable to save token in the config", e);
            }
        }

        getProxy().getPluginManager().registerListener(this, this);
    }

    // In some skin plugins, the Property[] value of the player profile can be overridden
    // To prevent the BungeeGuard property from being lost, we will use a high priority.
    @EventHandler(priority = Byte.MAX_VALUE - 1)
    public void onLogin(LoginEvent e) {
        // inject our spoofed loginresult instance into the initial handler
        InitialHandler con = (InitialHandler) e.getConnection();
        SpoofedLoginResult.inject(con, this.token);
    }

}
