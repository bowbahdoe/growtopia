package growtopia.enet;

public interface EventHandler {
    void onReceive(Enet.Event.Receive receiveEvent);
    void onConnect(Enet.Event.Connect connectEvent);
    void onDisconnect(Enet.Event.Disconnect disconnectEvent);
}
