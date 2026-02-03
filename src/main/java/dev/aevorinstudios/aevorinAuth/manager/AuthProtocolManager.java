package dev.aevorinstudios.aevorinAuth.manager;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import dev.aevorinstudios.aevorinAuth.AevorinAuth;
import dev.aevorinstudios.aevorinAuth.utils.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Professional-grade Coordinate Relocation Manager.
 * Handles secure world-shifting and seamless session resynchronization.
 */
public class AuthProtocolManager {

    private final AevorinAuth plugin;
    private final AuthManager authManager;
    private PacketType viewCenterType = null;

    private static final double RELOCATION_OFFSET_X = 1_000_000.0;
    private static final double RELOCATION_OFFSET_Z = 1_000_000.0;
    private static final int CHUNK_OFFSET_X = 62500;
    private static final int CHUNK_OFFSET_Z = 62500;

    // Threshold used to determine if a client coordinate is within the spoofed
    // range (1,000,000).
    private static final double COORDINATE_NORMALIZATION_THRESHOLD = 500_000.0;

    public AuthProtocolManager(AevorinAuth plugin, AuthManager authManager) {
        this.plugin = plugin;
        this.authManager = authManager;
    }

    public void register() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            plugin.getLogger()
                    .warning("ProtocolLib dependency is absent. Coordinate relocation functionality is disabled.");
            return;
        }

        plugin.getLogger().info("Initializing professional session synchronization and world relocation protocols...");

        // Outgoing Synchronized Relocation (Server -> Client)
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.POSITION) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (authManager.isAuthenticated(event.getPlayer().getUniqueId()))
                            return;
                        applyRelocationToPacket(event.getPacket(), RELOCATION_OFFSET_X, 0, RELOCATION_OFFSET_Z);
                    }
                });

        // Outgoing World Map Translation (Server -> Client)
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.MAP_CHUNK) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (authManager.isAuthenticated(event.getPlayer().getUniqueId()))
                            return;
                        translateChunkCoordinates(event.getPacket(), CHUNK_OFFSET_X, CHUNK_OFFSET_Z);
                    }
                });

        // Dynamic View-Center Protocol Discovery
        discoverAndRegisterViewCenterProtocols();

        // Incoming Movement Normalization (Client -> Server)
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.HIGHEST,
                        PacketType.Play.Client.POSITION,
                        PacketType.Play.Client.POSITION_LOOK) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        normalizeIncomingPacket(event.getPacket());
                    }
                });
    }

    private void discoverAndRegisterViewCenterProtocols() {
        for (Field field : PacketType.Play.Server.class.getFields()) {
            String name = field.getName();
            if (name.equals("SET_CHUNK_CACHE_CENTER") || name.equals("VIEW_CENTRE") ||
                    name.equals("UPDATE_VIEW_POSITION") || name.equals("VIEW_CENTER")) {
                try {
                    this.viewCenterType = (PacketType) field.get(null);
                    break;
                } catch (Exception ignored) {
                }
            }
        }

        if (viewCenterType != null) {
            ProtocolLibrary.getProtocolManager().addPacketListener(
                    new PacketAdapter(plugin, ListenerPriority.HIGHEST, viewCenterType) {
                        @Override
                        public void onPacketSending(PacketEvent event) {
                            if (authManager.isAuthenticated(event.getPlayer().getUniqueId()))
                                return;
                            translateChunkCoordinates(event.getPacket(), CHUNK_OFFSET_X, CHUNK_OFFSET_Z);
                        }
                    });
        }
    }

    private void applyRelocationToPacket(PacketContainer packet, double x, double y, double z) {
        // Modern 1.21.1+ Record-based position modification
        StructureModifier<Object> modifier = packet.getModifier();
        if (modifier.size() > 1) {
            Object record = modifier.read(1);
            if (record != null && record.getClass().isRecord()) {
                try {
                    rebuildPositionRecord(packet, record, x, y, z);
                    return;
                } catch (Exception ignored) {
                }
            }
        }

        // Standard coordinate modification fallback
        StructureModifier<Double> doubles = packet.getDoubles();
        if (doubles.size() >= 3) {
            doubles.write(0, doubles.read(0) + x);
            doubles.write(1, doubles.read(1) + y);
            doubles.write(2, doubles.read(2) + z);
        }
    }

    private void normalizeIncomingPacket(PacketContainer packet) {
        StructureModifier<Double> doubles = packet.getDoubles();
        if (doubles.size() >= 3) {
            double x = doubles.read(0);
            double z = doubles.read(2);

            // Independent Axis Normalization: Ensures that trailing client packets do not
            // corrupt
            // the server-side position during the session authentication transition.
            if (Math.abs(x) > COORDINATE_NORMALIZATION_THRESHOLD)
                doubles.write(0, x - RELOCATION_OFFSET_X);
            if (Math.abs(z) > COORDINATE_NORMALIZATION_THRESHOLD)
                doubles.write(2, z - RELOCATION_OFFSET_Z);
        }
    }

    private void translateChunkCoordinates(PacketContainer packet, int chunkX, int chunkZ) {
        StructureModifier<Integer> integers = packet.getIntegers();
        if (integers.size() >= 2) {
            integers.write(0, integers.read(0) + chunkX);
            integers.write(1, integers.read(1) + chunkZ);
        }
    }

    public void cleanupPlayer(UUID uuid) {
        // Session lifecycle managed through authentication state events.
    }

    /**
     * Executes a robust multi-phase resynchronization of the player's world state.
     * This sequence enforces a client-side chunk cache purge and forces immediate
     * rendering.
     */
    public void refreshPlayerPosition(Player player) {
        if (player == null || !player.isOnline())
            return;

        SchedulerUtils.runEntitySync(plugin, player, () -> {
            Location loc = player.getLocation();

            // Override view-center immediately to steer client-side chunk loading
            if (viewCenterType != null) {
                try {
                    PacketContainer viewPacket = ProtocolLibrary.getProtocolManager().createPacket(viewCenterType);
                    viewPacket.getIntegers().write(0, loc.getBlockX() >> 4);
                    viewPacket.getIntegers().write(1, loc.getBlockZ() >> 4);
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, viewPacket);
                } catch (Exception ignored) {
                }
            }

            // Calibrated absolute teleportation sequence to ensure world visibility
            player.teleport(loc);

            SchedulerUtils.runEntityLater(plugin, player, () -> {
                if (player.isOnline())
                    player.teleport(player.getLocation());
            }, 3L);

            SchedulerUtils.runEntityLater(plugin, player, () -> {
                if (player.isOnline()) {
                    // Final position verification and world reload confirmation
                    player.teleport(player.getLocation());
                }
            }, 12L);
        });
    }

    private void rebuildPositionRecord(PacketContainer packet, Object record, double offX, double offY, double offZ)
            throws Exception {
        Class<?> recordClass = record.getClass();
        java.lang.reflect.RecordComponent[] components = recordClass.getRecordComponents();

        Object[] values = new Object[components.length];
        Class<?>[] types = new Class<?>[components.length];

        for (int i = 0; i < components.length; i++) {
            types[i] = components[i].getType();
            values[i] = components[i].getAccessor().invoke(record);
        }

        Object oldPos = values[0];
        double curX = 0, curY = 0, curZ = 0;
        int found = 0;
        for (Field f : oldPos.getClass().getDeclaredFields()) {
            if (f.getType() == double.class) {
                f.setAccessible(true);
                if (found == 0)
                    curX = f.getDouble(oldPos);
                else if (found == 1)
                    curY = f.getDouble(oldPos);
                else if (found == 2)
                    curZ = f.getDouble(oldPos);
                found++;
            }
        }

        Class<?> vec3Class = oldPos.getClass();
        Constructor<?> vec3Constructor = vec3Class.getConstructor(double.class, double.class, double.class);
        Object newPos = vec3Constructor.newInstance(curX + offX, curY + offY, curZ + offZ);
        values[0] = newPos;

        Constructor<?> recordConstructor = recordClass.getDeclaredConstructor(types);
        recordConstructor.setAccessible(true);
        Object newRecord = recordConstructor.newInstance(values);

        packet.getModifier().write(1, newRecord);
    }
}
