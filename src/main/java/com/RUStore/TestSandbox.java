package com.RUStore;

import java.io.IOException;
import java.util.Arrays;

/**
 * This TestSandbox is meant for you to implement and extend to
 * test your object store as you slowly implement both the client and server.
 * <p>
 * If you need more information on how an RUStorageClient is used
 * take a look at the RUStoreClient.java source as well as
 * TestSample.java which includes sample usages of the client.
 */
public class TestSandbox {

    public static void main(String[] args) throws IOException, InterruptedException {

        // Create a new RUStoreClient
        RUStoreClient client = new RUStoreClient("localhost", 80);

        // Open a connection to a remote service
        System.out.println("Connecting to object server...");

        client.connect();
        System.out.println(Arrays.toString(client.list()));
        System.out.println(client.put("Nikoloz", new byte[]{43, 62, 3, 24, 8}));
        System.out.println(client.put("Chichua", new byte[]{20, 102, 31, 5, 2}));
        System.out.println(Arrays.toString(client.list()));
        System.out.println(Arrays.toString(client.get("Chichua")));
        client.disconnect();
    }

}
