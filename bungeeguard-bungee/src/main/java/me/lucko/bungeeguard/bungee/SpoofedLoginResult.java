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

import net.md_5.bungee.ServerConnector;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.data.Property;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Extension of {@link LoginResult} which returns a modified Property array when
 * {@link #getProperties()} is called by the ServerConnector implementation.
 *
 * To achieve this, the stack trace is analyzed. This is kinda crappy, but is the only way
 * to modify the properties without leaking the token to other clients via the tablist.
 */
class SpoofedLoginResult extends LoginResult {
    private static final Field PROFILE_FIELD;

    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    static {
        try {
            PROFILE_FIELD = InitialHandler.class.getDeclaredField("loginProfile");
            PROFILE_FIELD.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static void inject(InitialHandler handler, String token) {
        LoginResult profile = handler.getLoginProfile();
        LoginResult newProfile;

        // profile is null for offline mode servers
        if (profile == null) {
            newProfile = new SpoofedLoginResult(token);
        } else {
            newProfile = new SpoofedLoginResult(profile, token);
        }

        try {
            PROFILE_FIELD.set(handler, newProfile);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private final Property bungeeGuardToken;
    private final Property[] bungeeGuardTokenArray;
    private final boolean offline;

    // online mode constructor
    protected SpoofedLoginResult(LoginResult oldProfile, String bungeeGuardToken) {
        super(oldProfile.getId(), oldProfile.getName(), oldProfile.getProperties());
        this.bungeeGuardToken = new Property("bungeeguard-token", bungeeGuardToken, "");
        this.bungeeGuardTokenArray = new Property[]{this.bungeeGuardToken};
        this.offline = false;
    }

    // offline mode constructor
    protected SpoofedLoginResult(String bungeeGuardToken) {
        super(null, null, new Property[0]);
        this.bungeeGuardToken = new Property("bungeeguard-token", bungeeGuardToken, "");
        this.bungeeGuardTokenArray = new Property[]{this.bungeeGuardToken};
        this.offline = true;
    }

    @Override
    public Property[] getProperties() {
        StackWalker.StackFrame frame = STACK_WALKER.walk(s ->
                // find the first frame that starts with "net.md_5.bungee"
                s.dropWhile(f -> !f.getClassName().startsWith("net.md_5.bungee"))
                        .findFirst()
                        .orElse(null)
        );

        // if the getProperties method is being called by the server connector, include our token in the properties
        if (frame != null && frame.getDeclaringClass() == ServerConnector.class && frame.getMethodName().equals("connected")) {
            return addTokenProperty(super.getProperties());
        } else {
            return super.getProperties();
        }
    }

    private Property[] addTokenProperty(Property[] properties) {
        if (properties.length == 0) {
            return this.bungeeGuardTokenArray;
        }

        Property[] newProperties = Arrays.copyOf(properties, properties.length + 1);
        newProperties[properties.length] = this.bungeeGuardToken;
        return newProperties;
    }

    @Override
    public String getId() {
        // some plugins may require this method even with offline-mode. By default, we will return the value of the original class.
        /*
        if (this.offline) {
            throw new RuntimeException("getId called for offline variant of SpoofedLoginResult");
        }
         */
        return super.getId();
    }

    @Override
    public String getName() {
        // some plugins may require this method even with offline-mode. By default, we will return the value of the original class.
        /*
        if (this.offline) {
            throw new RuntimeException("getId called for offline variant of SpoofedLoginResult");
        }
         */
        return super.getId();
    }
}
