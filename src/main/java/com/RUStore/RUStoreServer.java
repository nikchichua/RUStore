package com.RUStore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.RUStore.Protocol.*;

public class RUStoreServer {
    ConcurrentMap<String, byte[]> map = new ConcurrentHashMap<>();

    public static void main(String[] arguments) throws InterruptedException {
        if (arguments.length != 1) {
            System.out.println("Invalid number of arguments. You must provide a port number.");
            return;
        }
        int port = Integer.parseInt(arguments[0]);
        new RUStoreServer().run(port);
    }

    public void run(int port) throws InterruptedException {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);
        Thread serverThread = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                System.out.println("Waiting for clients to connect...");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    clientProcessingPool.submit(new ClientTask(clientSocket));
                }
            } catch (IOException e) {
                System.err.println("Unable to process client request");
                throw new RuntimeException(e);
            }
        });
        serverThread.start();
    }

    private class ClientTask implements Runnable {
        private final Socket clientSocket;
        private final DataInputStream in;
        private final DataOutputStream out;

        private ClientTask(Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            this.in = new DataInputStream(clientSocket.getInputStream());
            this.out = new DataOutputStream(clientSocket.getOutputStream());
        }

        private void handlePut(String key, byte[] data) throws IOException {
            System.out.println("Putting!!");
            if (map.containsKey(key)) {
                out.writeInt(1);
                return;
            }
            map.put(key, data);
            out.writeInt(0);
        }

        private void handleGet(String key) throws IOException {
            System.out.println("Getting!");
            if (!map.containsKey(key)) {
                out.writeInt(0);
                return;
            }
            byte[] data = map.get(key);
            out.writeInt(data.length);
            out.write(data);
        }

        private void handleList() throws IOException {
            List<String> strings = new ArrayList<>(map.keySet());
            out.writeInt(strings.size());
            for (String s : strings) out.writeUTF(s);
        }

        @Override
        public void run() {
            System.out.println("Got a client !");

            try {

                while (true) {
                    int methodNumber = in.readInt();

                    if (methodNumber == LIST.methodNumber()) {
                        handleList();
                        continue;
                    }


                    String key = in.readUTF();

                    if (methodNumber == GET.methodNumber()) {
                        handleGet(key);
                        continue;
                    }

                    int dataLength = in.readInt();
                    byte[] data = in.readNBytes(dataLength);

                    if (methodNumber == PUT.methodNumber()) {
                        handlePut(key, data);
                        continue;
                    }
                    System.out.println("Received: " + methodNumber);
                }

            } catch (EOFException e) {
                System.out.println("Client disconnected.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                clientSocket.close();
                System.out.println("Thread closed!");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
