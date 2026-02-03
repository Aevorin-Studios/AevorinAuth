# AevorinAuth

A professional-grade, lightweight authentication plugin for Minecraft 1.21.1 and higher, supporting Paper, Spigot, and Folia.

## Features

- **Premium Coordinate Spoofing**: Implements a packet-level world relocation system that hides sensitive coordinates on the F3 debug screen without causing the player to fall into the void.
- **Blindness Overlay**: Automatically applies a blindness potion effect to unauthenticated players to obscure their immediate surroundings.
- **Multi-Storage Support**: Offers native support for SQLite (default) and MySQL for robust and scalable data management.
- **Folia Compatibility**: Engineered to support regionized multi-threading on Folia-based servers.
- **Enhanced Security**: utilizes industry-standard password hashing and modern security protocols.
- **Automated Update Monitoring**: Integrated update checker to notify administrators of new releases via Modrinth.

## System Requirements

- **Minecraft Version**: 1.21.1 or higher.
- **ProtocolLib**: Version 5.3.0 or higher is required for coordinate spoofing functionality.

## Commands and Permissions

| Command                        | Description                      | Permission        |
| :----------------------------- | :------------------------------- | :---------------- |
| /login <password>              | Authenticate your session.       | None              |
| /register <password> <confirm> | Create a new user account.       | None              |
| /changepassword <old> <new>    | Modify your account password.    | None              |
| /unregister <password>         | Permanently delete your account. | None              |
| /auth reload                   | Reload the plugin configuration. | aevorinauth.admin |

## Configuration

The `config.yml` file provides granular control over plugin features:

- Enable or disable coordinate spoofing.
- Toggle movement restrictions for unauthenticated players.
- Manage inventory and interaction permissions.
- Configure MySQL connection parameters.

## Folia Support

AevorinAuth is fully optimized for Folia. It utilizes the modern region-aware scheduling API to ensure thread safety and performance across independent world regions.

---

© 2026 Aevorin Studios. All rights reserved.
