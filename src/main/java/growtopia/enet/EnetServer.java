package growtopia.enet;

import static growtopia.enet.Enet.enet_deinitialize;
import static growtopia.enet.Enet.enet_initialize;

public final class EnetServer {
    private EnetServer() {}

    public static void start(EventHandler eventHandler) {
        enet_initialize();
        try (final var host = Enet.Host.create(
                new Enet.Address(0, (short) 17091),
                1024,
                10,
                0,
                0
        )) {
            host.setChecksumCallbackToCRC32();
            host.compressWithRangeCoder();
            host.listenForEvents(eventHandler);
        } finally {
            enet_deinitialize();
        }
    }
}
