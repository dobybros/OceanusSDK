package main;

import oceanus.apis.CoreException;
import oceanus.apis.Oceanus;
import oceanus.apis.OceanusBuilder;
import oceanus.apis.RPCManager;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import java.util.function.Function;

public class Main {
    public static void main(String... args) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, CoreException {
        OceanusBuilder oceanusBuilder = new OceanusBuilder();
        Oceanus oceanus = oceanusBuilder.build();

        oceanus.init(Main.class.getClassLoader()).thenAccept(unused -> {
            RPCManager rpcManager = oceanus.getRPCManager();
            String hello = null;
            try {
                hello = rpcManager.call("goldcentral", "CentralService", "hello", String.class);
            } catch (CoreException e) {
                e.printStackTrace();
            }
            System.out.println("hello " + hello);

            CentralService centralService = rpcManager.getService("goldcentral", CentralService.class);
            System.out.println("hello2 " + centralService.hello());

            System.out.println("Hello 3 " + MyController.instance.getCentralService().hello());
        }).exceptionally(throwable -> {
            System.out.println("hello failed " + throwable.getMessage());
            return null;
        });

    }
}
