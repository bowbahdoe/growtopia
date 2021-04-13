package growtopia;

import growtopia.enet.EnetServer;

public final class Main {
    public static void main(String[] args) {
        System.out.println("Starting server");
        EnetServer.start(new EventHandlerImpl());
    }
}
