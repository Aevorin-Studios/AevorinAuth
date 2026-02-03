# Changelog

All significant changes to AevorinAuth are documented in this file.

## [1.0.0-beta-1] - 2026-02-03

### Features Added

- **Premium Coordinate Relocation System**:
  - Implemented a +1,000,000 block coordinate offset for unauthenticated players.
  - Added packet-level spoofing for the F3 debug screen coordinates.
  - Synchronized world chunk translation to maintain visibility during relocation.
  - Integrated dynamic View-Center synchronization to ensure reliable chunk loading.
  - Developed an Independent Axis Normalization system to prevent movement synchronization errors.
- **Comprehensive Authentication System**:
  - Added `/register` and `/login` commands for player onboarding.
  - Implemented `/changepassword` and `/unregister` for account management.
  - Integrated robust session management with immediate de-authentication upon logout.
- **Robust Security Framework**:
  - Utilizes high-entropy password hashing for secure data protection.
  - Implemented strictly enforced movement, interaction, and chat restrictions for unauthenticated sessions.
  - Added a blindness potion overlay to obscure surroundings prior to authentication.
- **Multi-Storage Architecture**:
  - Native support for SQLite for localized, zero-configuration deployments.
  - Enterprise-grade MySQL support via the HikariCP high-performance connection pool.
- **Cross-Platform Compatibility**:
  - Fully optimized for the Folia regionized threading model.
  - Native support for Paper and Spigot versions 1.21.1 and higher.
- **Automated Update Monitoring**:
  - Integrated Modrinth API integration for real-time update notifications for administrators.
- **Administrative Tools**:
  - Added the `/auth reload` command for seamless configuration updates without server restarts.

---

Initial Beta Release
