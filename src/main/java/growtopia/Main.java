package growtopia;

import growtopia.enet.EnetServer;
import java.nio.file.Path;
import jdk.incubator.foreign.LibraryLookup;

public final class Main {
    public static void main(String[] args) {
        System.out.println("Starting server");
        EnetServer.start(new EventHandlerImpl());
    }
}
