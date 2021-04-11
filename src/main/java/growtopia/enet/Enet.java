package growtopia.enet;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.ref.Cleaner;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;

import jdk.incubator.foreign.*;

import static jdk.incubator.foreign.CLinker.*;

/**
 *
 */
public final class Enet {
    private Enet() {
    }

    private static final Cleaner CLEANER = Cleaner.create();

    private static final LibraryLookup LIBRARY_LOOKUP = lookup();

    private static LibraryLookup lookup() {
        try {
            return LibraryLookup.ofPath(Paths.get("libenet.so").toAbsolutePath());
        } catch (Throwable t) {
            return LibraryLookup.ofPath(
                    Paths.get("/", "usr", "local", "lib", "libenet.dylib")
            );
        }
    }
    /* enet_initialize */

    private static final MethodHandle ENET_INITIALIZE = CLinker.getInstance().downcallHandle(
            LIBRARY_LOOKUP.lookup("enet_initialize").get(),
            MethodType.methodType(int.class),
            FunctionDescriptor.of(C_INT)
    );

    public static int enet_initialize() {
        try {
            return (int) ENET_INITIALIZE.invoke();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    /* enet_deinitialize */

    private static final MethodHandle ENET_DEINITIALIZE = CLinker.getInstance().downcallHandle(
            LIBRARY_LOOKUP.lookup("enet_deinitialize").get(),
            MethodType.methodType(int.class),
            FunctionDescriptor.of(C_INT)
    );

    public static int enet_deinitialize() {
        try {
            return (int) ENET_DEINITIALIZE.invoke();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    /* enet_host_compress_with_range_coder */
    private static final MethodHandle ENET_HOST_COMPRESS_WITH_RANGE_CODER = CLinker.getInstance().downcallHandle(
            LIBRARY_LOOKUP.lookup("enet_host_compress_with_range_coder").get(),
            MethodType.methodType(int.class, MemoryAddress.class),
            FunctionDescriptor.of(C_INT, C_POINTER)
    );

    /* enet_host_destroy */
    private static final MethodHandle ENET_HOST_DESTROY = CLinker.getInstance().downcallHandle(
            LIBRARY_LOOKUP.lookup("enet_host_destroy").get(),
            MethodType.methodType(void.class, MemoryAddress.class),
            FunctionDescriptor.ofVoid(C_POINTER)
    );

    /* enet_host_create */

    private static final MethodHandle ENET_HOST_CREATE = CLinker.getInstance().downcallHandle(
            LIBRARY_LOOKUP.lookup("enet_host_create").get(),
            MethodType.methodType(MemoryAddress.class, MemoryAddress.class, long.class, long.class, int.class, int.class),
            FunctionDescriptor.of(C_POINTER, C_POINTER, C_LONG, C_LONG, C_INT, C_INT)
    );

    /* enet_host_service */
    private static final MethodHandle ENET_HOST_SERVICE = CLinker.getInstance().downcallHandle(
            LIBRARY_LOOKUP.lookup("enet_host_service").get(),
            MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class, int.class),
            FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_INT)
    );

    /**
     * The address for a connected client.
     */
    public record Address(int host, short port) {
        private static final int ENET_HOST_ANY = 0;

        static final MemoryLayout LAYOUT = MemoryLayout.ofStruct(
                C_INT.withName("host"),
                C_SHORT.withName("port"),
                MemoryLayout.ofPaddingBits(16L)
        );

        private static final VarHandle HOST_HANDLE = LAYOUT.varHandle(
                int.class,
                MemoryLayout.PathElement.groupElement("host")
        );

        private static final VarHandle PORT_HANDLE = LAYOUT.varHandle(
                short.class,
                MemoryLayout.PathElement.groupElement("port")
        );

        private MemorySegment asStruct() {
            final var segment = MemorySegment.allocateNative(LAYOUT);
            HOST_HANDLE.set(segment, this.host());
            PORT_HANDLE.set(segment, this.port());
            return segment;
        }
    }

    public static final class Host implements AutoCloseable {
        private final MemoryAddress hostAddress;

        private Host(MemoryAddress hostAddress) {
            this.hostAddress = hostAddress;
        }

        private static final MemoryLayout LAYOUT = MemoryLayout.ofStruct(
                CLinker.C_INT.withName("socket"),
                Address.LAYOUT.withName("address"),
                CLinker.C_INT.withName("incomingBandwidth"),
                CLinker.C_INT.withName("outgoingBandwidth"),
                CLinker.C_INT.withName("bandwidthThrottleEpoch"),
                CLinker.C_INT.withName("mtu"),
                CLinker.C_INT.withName("randomSeed"),
                CLinker.C_INT.withName("recalculateBandwidthLimits"),
                MemoryLayout.ofPaddingBits(32L),
                CLinker.C_POINTER.withName("peers"),
                CLinker.C_LONG.withName("peerCount"),
                CLinker.C_LONG.withName("channelLimit"),
                CLinker.C_INT.withName("serviceTime"),
                MemoryLayout.ofPaddingBits(32L),
                MemoryLayout.ofStruct(
                        MemoryLayout.ofStruct(
                                CLinker.C_POINTER.withName("next"),
                                CLinker.C_POINTER.withName("previous")
                        ).withName("sentinel")
                ).withName("dispatchQueue"),
                CLinker.C_INT.withName("continueSending"),
                MemoryLayout.ofPaddingBits(32L),
                CLinker.C_LONG.withName("packetSize"),
                CLinker.C_SHORT.withName("headerFlags"),
                MemoryLayout.ofSequence(
                        32L,
                        MemoryLayout.ofUnion(
                                MemoryLayout.ofStruct(
                                        CLinker.C_CHAR.withName("command"),
                                        CLinker.C_CHAR.withName("channelID"),
                                        CLinker.C_SHORT.withName("reliableSequenceNumber")
                                ).withName("header"),
                                MemoryLayout.ofStruct(
                                        MemoryLayout.ofStruct(
                                                CLinker.C_CHAR.withName("command"),
                                                CLinker.C_CHAR.withName("channelID"),
                                                CLinker.C_SHORT.withName("reliableSequenceNumber")
                                        ).withName("header"),
                                        CLinker.C_SHORT.withName("receivedReliableSequenceNumber"),
                                        CLinker.C_SHORT.withName("receivedSentTime")
                                ).withName("acknowledge"),
                                MemoryLayout.ofStruct(
                                        MemoryLayout.ofStruct(
                                                CLinker.C_CHAR.withName("command"),
                                                CLinker.C_CHAR.withName("channelID"),
                                                CLinker.C_SHORT.withName("reliableSequenceNumber")
                                        ).withName("header"),
                                        CLinker.C_SHORT.withName("outgoingPeerID"),
                                        CLinker.C_CHAR.withName("incomingSessionID"),
                                        CLinker.C_CHAR.withName("outgoingSessionID"),
                                        CLinker.C_INT.withName("mtu"),
                                        CLinker.C_INT.withName("windowSize"),
                                        CLinker.C_INT.withName("channelCount"),
                                        CLinker.C_INT.withName("incomingBandwidth"),
                                        CLinker.C_INT.withName("outgoingBandwidth"),
                                        CLinker.C_INT.withName("packetThrottleInterval"),
                                        CLinker.C_INT.withName("packetThrottleAcceleration"),
                                        CLinker.C_INT.withName("packetThrottleDeceleration"),
                                        CLinker.C_INT.withName("connectID"),
                                        CLinker.C_INT.withName("data")
                                ).withName("connect"),
                                MemoryLayout.ofStruct(
                                        MemoryLayout.ofStruct(
                                                CLinker.C_CHAR.withName("command"),
                                                CLinker.C_CHAR.withName("channelID"),
                                                CLinker.C_SHORT.withName("reliableSequenceNumber")
                                        ).withName("header"),
                                        CLinker.C_SHORT.withName("outgoingPeerID"),
                                        CLinker.C_CHAR.withName("incomingSessionID"),
                                        CLinker.C_CHAR.withName("outgoingSessionID"),
                                        CLinker.C_INT.withName("mtu"),
                                        CLinker.C_INT.withName("windowSize"),
                                        CLinker.C_INT.withName("channelCount"),
                                        CLinker.C_INT.withName("incomingBandwidth"),
                                        CLinker.C_INT.withName("outgoingBandwidth"),
                                        CLinker.C_INT.withName("packetThrottleInterval"),
                                        CLinker.C_INT.withName("packetThrottleAcceleration"),
                                        CLinker.C_INT.withName("packetThrottleDeceleration"),
                                        CLinker.C_INT.withName("connectID")
                                ).withName("verifyConnect"),
                                MemoryLayout.ofStruct(
                                        MemoryLayout.ofStruct(
                                                CLinker.C_CHAR.withName("command"),
                                                CLinker.C_CHAR.withName("channelID"),
                                                CLinker.C_SHORT.withName("reliableSequenceNumber")
                                        ).withName("header"),
                                        CLinker.C_INT.withName("data")
                                ).withName("disconnect"),
                                MemoryLayout.ofStruct(
                                        MemoryLayout.ofStruct(
                                                CLinker.C_CHAR.withName("command"),
                                                CLinker.C_CHAR.withName("channelID"),
                                                CLinker.C_SHORT.withName("reliableSequenceNumber")
                                        ).withName("header")).withName("ping"),
                                MemoryLayout.ofStruct(
                                        MemoryLayout.ofStruct(
                                                CLinker.C_CHAR.withName("command"),
                                                CLinker.C_CHAR.withName("channelID"),
                                                CLinker.C_SHORT.withName("reliableSequenceNumber")
                                        ).withName("header"),
                                        CLinker.C_SHORT.withName("dataLength")
                                ).withName("sendReliable"),
                                MemoryLayout.ofStruct(
                                        MemoryLayout.ofStruct(
                                                CLinker.C_CHAR.withName("command"),
                                                CLinker.C_CHAR.withName("channelID"),
                                                CLinker.C_SHORT.withName("reliableSequenceNumber")
                                        ).withName("header"),
                                        CLinker.C_SHORT.withName("unreliableSequenceNumber"),
                                        CLinker.C_SHORT.withName("dataLength")).withName("sendUnreliable"),
                                MemoryLayout.ofStruct(
                                        MemoryLayout.ofStruct(
                                                CLinker.C_CHAR.withName("command"),
                                                CLinker.C_CHAR.withName("channelID"),
                                                CLinker.C_SHORT.withName("reliableSequenceNumber")
                                        ).withName("header"), CLinker.C_SHORT.withName("unsequencedGroup"),
                                        CLinker.C_SHORT.withName("dataLength")).withName("sendUnsequenced"),
                                MemoryLayout.ofStruct(
                                        MemoryLayout.ofStruct(
                                                CLinker.C_CHAR.withName("command"),
                                                CLinker.C_CHAR.withName("channelID"),
                                                CLinker.C_SHORT.withName("reliableSequenceNumber")
                                        ).withName("header"),
                                        CLinker.C_SHORT.withName("startSequenceNumber"),
                                        CLinker.C_SHORT.withName("dataLength"),
                                        CLinker.C_INT.withName("fragmentCount"),
                                        CLinker.C_INT.withName("fragmentNumber"),
                                        CLinker.C_INT.withName("totalLength"),
                                        CLinker.C_INT.withName("fragmentOffset")
                                ).withName("sendFragment"),
                                MemoryLayout.ofStruct(
                                        MemoryLayout.ofStruct(
                                                CLinker.C_CHAR.withName("command"),
                                                CLinker.C_CHAR.withName("channelID"),
                                                CLinker.C_SHORT.withName("reliableSequenceNumber")
                                        ).withName("header"),
                                        CLinker.C_INT.withName("incomingBandwidth"),
                                        CLinker.C_INT.withName("outgoingBandwidth")
                                ).withName("bandwidthLimit"),
                                MemoryLayout.ofStruct(
                                        MemoryLayout.ofStruct(
                                                CLinker.C_CHAR.withName("command"),
                                                CLinker.C_CHAR.withName("channelID"),
                                                CLinker.C_SHORT.withName("reliableSequenceNumber")
                                        ).withName("header"),
                                        CLinker.C_INT.withName("packetThrottleInterval"),
                                        CLinker.C_INT.withName("packetThrottleAcceleration"),
                                        CLinker.C_INT.withName("packetThrottleDeceleration")
                                ).withName("throttleConfigure")
                        ).withName("_ENetProtocol")
                ).withName("commands"),
                MemoryLayout.ofPaddingBits(48L),
                CLinker.C_LONG.withName("commandCount"),
                MemoryLayout.ofSequence(
                        65L,
                        MemoryLayout.ofStruct(
                                CLinker.C_POINTER.withName("data"),
                                CLinker.C_LONG.withName("dataLength")
                        )
                ).withName("buffers"),
                CLinker.C_LONG.withName("bufferCount"),
                CLinker.C_POINTER.withName("checksum"),
                MemoryLayout.ofStruct(
                        CLinker.C_POINTER.withName("context"),
                        CLinker.C_POINTER.withName("compress"),
                        CLinker.C_POINTER.withName("decompress"),
                        CLinker.C_POINTER.withName("destroy")
                ).withName("compressor"),
                MemoryLayout.ofSequence(
                        2L,
                        MemoryLayout.ofSequence(4096L, CLinker.C_CHAR)
                ).withName("packetData"),
                MemoryLayout.ofStruct(
                        CLinker.C_INT.withName("host"),
                        CLinker.C_SHORT.withName("port"),
                        MemoryLayout.ofPaddingBits(16L)
                ).withName("receivedAddress"),
                CLinker.C_POINTER.withName("receivedData"),
                CLinker.C_LONG.withName("receivedDataLength"),
                CLinker.C_INT.withName("totalSentData"),
                CLinker.C_INT.withName("totalSentPackets"),
                CLinker.C_INT.withName("totalReceivedData"),
                CLinker.C_INT.withName("totalReceivedPackets"),
                CLinker.C_POINTER.withName("intercept"),
                CLinker.C_LONG.withName("connectedPeers"),
                CLinker.C_LONG.withName("bandwidthLimitedPeers"),
                CLinker.C_LONG.withName("duplicatePeers"),
                CLinker.C_LONG.withName("maximumPacketSize"),
                CLinker.C_LONG.withName("maximumWaitingData")
        ).withName("_ENetHost");

        private static final VarHandle CHECKSUM_HANDLE = LAYOUT.varHandle(
                long.class,
                MemoryLayout.PathElement.groupElement("checksum")
        );

        public static Host create(Address address, long peerCount, long channelLimit, int incomingBandwidth, int outgoingBandwidth) {
            try (final var addressPointer = address.asStruct()) {
                final var hostAddress = (MemoryAddress) ENET_HOST_CREATE.invoke(
                        addressPointer.address(),
                        peerCount,
                        channelLimit,
                        incomingBandwidth,
                        outgoingBandwidth
                );

                if (hostAddress.equals(MemoryAddress.NULL)) {
                    throw new RuntimeException("Got null from enet_host_create");
                }

                return new Host(hostAddress);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        public Address address() {
            return new Address(
                    MemoryAccess.getIntAtOffset(
                            this.hostAddress.asSegmentRestricted(LAYOUT.byteSize()),
                            LAYOUT.byteOffset(
                                    MemoryLayout.PathElement.groupElement("address"),
                                    MemoryLayout.PathElement.groupElement("host")
                            )
                    ),
                    MemoryAccess.getShortAtOffset(
                            this.hostAddress.asSegmentRestricted(LAYOUT.byteSize()),
                            LAYOUT.byteOffset(
                                    MemoryLayout.PathElement.groupElement("address"),
                                    MemoryLayout.PathElement.groupElement("port")
                            )
                    )
            );
        }

        void setChecksumCallbackToCRC32() {
            final var callback = LIBRARY_LOOKUP.lookup("enet_crc32")
                    .get()
                    .address().asSegmentRestricted(C_POINTER.byteSize());

            CHECKSUM_HANDLE.set(
                    this.hostAddress.asSegmentRestricted(LAYOUT.byteSize()),
                    callback.address().toRawLongValue()
            );
        }

        void compressWithRangeCoder() {
            try {
                ENET_HOST_COMPRESS_WITH_RANGE_CODER.invoke(this.hostAddress);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        void listenForEvents(EventHandler callback) {
            final var eventSegment = MemorySegment
                    .allocateNative(Event.LAYOUT)
                    .registerCleaner(CLEANER);
            while (true) {
                try {
                    final var status = (int) ENET_HOST_SERVICE.invoke(this.hostAddress, eventSegment.address(), 1000);
                    if (status == 0) {
                        // no event, pass
                    }
                    else if (status < 0) {
                        // error status
                        throw new RuntimeException("Error status back from enet_host_service. " + status);
                    }
                    else {
                        final var ev = Event.fromUnsafe(eventSegment);
                        if (ev instanceof Event.Receive receive) {
                            callback.onReceive(receive);
                        }
                        else if (ev instanceof Event.Connect connect) {
                            callback.onConnect(connect);
                        }
                        else if (ev instanceof Event.Disconnect disconnect) {
                            callback.onDisconnect(disconnect);
                        }
                    }
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }
        }

        @Override
        public void close() {
            try {
                ENET_HOST_DESTROY.invoke(this.hostAddress);
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }
    }

    public static abstract class Event {
        static final MemoryLayout LAYOUT = MemoryLayout.ofStruct(
                CLinker.C_INT.withName("type"),
                MemoryLayout.ofPaddingBits(32L),
                CLinker.C_POINTER.withName("peer"),
                CLinker.C_CHAR.withName("channelID"),
                MemoryLayout.ofPaddingBits(24L),
                CLinker.C_INT.withName("data"),
                CLinker.C_POINTER.withName("packet")
        ).withName("_ENetEvent");

        protected final MemorySegment event;

        private Event(MemorySegment event) {
            this.event = event;
        }

        /**
         * Constructs an event from a memory segment owned by the caller. The lifetime of the Event is bounded
         * on when the caller wants to release that memory.
         */
        static Event fromUnsafe(MemorySegment event) {
            final var type = MemoryAccess.getIntAtOffset(event, LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("type")));
            return switch (type) {
                case 0 -> new None(event);
                case 1 -> new Connect(event);
                case 2 -> new Disconnect(event);
                case 3 -> new Receive(event);
                default -> throw new RuntimeException("Unknown event type - int value " + type);
            };
        }

        public Peer peer() {
            final var peerAddr = MemoryAccess.getAddressAtOffset(
                this.event,
                    LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("peer"))
            );


            return null;
        }

        public static final class None extends Event {
            private None(MemorySegment event) {
                super(event);
            }
        }

        public static final class Connect extends Event {
            private Connect(MemorySegment event) {
                super(event);
            }
        }

        public static final class Receive extends Event {
            private Receive(MemorySegment event) {
                super(event);
            }


            public String dataAsString() {
                MemoryAddress packet = MemoryAccess.getAddressAtOffset(
                        this.event,
                        LAYOUT.byteOffset(
                                MemoryLayout.PathElement.groupElement("packet")
                        )
                );

                MemorySegment packetData = packet.asSegmentRestricted(Packet.LAYOUT.byteSize());
                final var packetLength = MemoryAccess.getLongAtOffset(
                        packetData,
                        Packet.LAYOUT.byteOffset(
                                MemoryLayout.PathElement.groupElement("dataLength")
                        )
                );
                final var packetDataPtr =  MemoryAccess.getAddressAtOffset(
                        packetData,
                        Packet.LAYOUT.byteOffset(
                                MemoryLayout.PathElement.groupElement("data")
                        )
                );
                final var packetDataArray = packetDataPtr.asSegmentRestricted(
                        MemoryLayout.ofSequence(packetLength, C_CHAR).byteSize()
                );

                byte[] data = new byte[(int) packetLength];
                for (int i = 0; i < packetLength; i++) {
                    data[i] = MemoryAccess.getByteAtOffset(packetDataArray, C_CHAR.byteSize() * i);
                }

                return new String(data, StandardCharsets.US_ASCII);
            }
        }

        public static final class Disconnect extends Event {
            private Disconnect(MemorySegment event) {
                super(event);
            }
        }
    }

    public static final class Packet {
        static final MemoryLayout LAYOUT = MemoryLayout.ofStruct(
                CLinker.C_LONG.withName("referenceCount"),
                CLinker.C_INT.withName("flags"),
                MemoryLayout.ofPaddingBits(32L),
                CLinker.C_POINTER.withName("data"),
                CLinker.C_LONG.withName("dataLength"),
                CLinker.C_POINTER.withName("freeCallback"),
                CLinker.C_POINTER.withName("userData")
        ).withName("_ENetPacket");

        public enum Flag {
            RELIABLE, UNSEQUENCED;

            int bit() {
                return switch (this) {
                    case RELIABLE -> 0x01;
                    case UNSEQUENCED -> 0x10;
                };
            }
        }

        private final byte[] data;
        private final EnumSet<Flag> flags;

        private Packet(byte[] data, EnumSet<Flag> flags) {
            this.data = data;
            this.flags = flags;
        }

        public static Packet create(byte[] data, EnumSet<Flag> flags) {
            return new Packet(data, flags);
        }

        public byte[] data() {
            return Arrays.copyOf(data, data.length);
        }

        public EnumSet<Flag> flags() {
            return EnumSet.copyOf(this.flags);
        }
    }

    public static final class Peer implements AutoCloseable {
        private static final MemoryLayout LAYOUT = MemoryLayout.ofStruct();

        private Peer() {}


        public void disconnectLater() {}

        public void send(Packet packet) {
            final var bitFlags = packet.flags()
                    .stream()
                    .reduce(0, (a, b) -> a & b.bit(), (a, b) -> a & b);
            final var data = packet.data();
        }

        @Override
        public void close() {

        }
    }
}
