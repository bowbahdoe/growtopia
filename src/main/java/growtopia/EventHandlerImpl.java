package growtopia;

import growtopia.enet.Enet;
import growtopia.enet.EventHandler;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

public final class EventHandlerImpl implements EventHandler {
    @Override
    public void onReceive(Enet.Event.Receive receiveEvent) {
        System.out.println(receiveEvent);
        System.out.println(receiveEvent.dataAsString());

        receiveEvent.peer().send(
                Enet.Packet.create(
                        "hello".getBytes(StandardCharsets.US_ASCII),
                        EnumSet.of(Enet.Packet.Flag.RELIABLE)
                )
        );
    }

    @Override
    public void onConnect(Enet.Event.Connect connectEvent) {
        System.out.println(connectEvent);
    }

    @Override
    public void onDisconnect(Enet.Event.Disconnect disconnectEvent) {
        System.out.println(disconnectEvent);
    }
}
