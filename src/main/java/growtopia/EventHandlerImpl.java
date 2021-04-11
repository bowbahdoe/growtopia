package growtopia;

import growtopia.enet.Enet;
import growtopia.enet.EventHandler;

public final class EventHandlerImpl implements EventHandler {
    @Override
    public void onReceive(Enet.Event.Receive receiveEvent) {
        System.out.println(receiveEvent);
        System.out.println(receiveEvent.dataAsString());
    }

    @Override
    public void onConnect(Enet.Event.Connect connectEvent) {
        System.out.println(connectEvent);

        System.out.println(connectEvent.dataAsString());
    }

    @Override
    public void onDisconnect(Enet.Event.Disconnect disconnectEvent) {
        System.out.println(disconnectEvent);
    }
}
