# Reporting a Vulnerability

Serious vulnerabilities should be reported in private, using the contact details [here](https://lucko.me/). You can encrypt your message using my PGP key if you feel inclined to do so.

# Security Patches

This page will be updated with any notices about security issues in BungeeGuard.

#### #001 - 7th June 2020
* `v1.2.0` released which fixes a security issue in the BungeeGuard Spigot plugin.
* The issue allowed malicious users to bypass BungeeGuard's authentication checks.
* All releases prior to `1.2` are affected.

#### #002 - 2nd June 2025
* `v1.4.0` released which fixes a security issue in the BungeeGuard BungeeCord plugin.
* An issue introduced in BungeeCord build 1756 caused the BungeeGuard token to be leaked to players using Minecraft 1.20.2 or higher via the LoginSuccess packet.
* This issue only affects BungeeGuard setups using BungeeCord, it does not affect Velocity proxies.
* Affected users are recommended to update to BungeeGuard `v1.4.0` or later on their proxy, and rotate their BungeeGuard tokens.
