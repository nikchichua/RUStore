package com.RUStore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.RUStore.Protocol.*;

public class RUStoreServer {
    private final Thread dispatcher;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final ServerSocket serverSocket;
    private final Set<Socket> connectedSockets = ConcurrentHashMap.newKeySet();
    private final Map<String, byte[]> map = new ConcurrentHashMap<>();

    public RUStoreServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        dispatcher = new Thread(() -> {
            try {
                while (true) {
                    System.out.println("There are " + this.getActiveConnections() + " connection(s) to this server.");
                    Socket clientSocket = serverSocket.accept();
                    connectedSockets.add(clientSocket);
                    threadPool.submit(new ClientTask(clientSocket));
                }
            } catch (IOException ignored) {
                System.out.println("Dispatcher terminated!");
            }
        });
    }

    public int getActiveConnections() {
        return connectedSockets.size();
    }

    public void connect() {
        System.out.println("Waiting for clients to connect...");
        dispatcher.start();
    }

    public void disconnect() throws IOException, InterruptedException {
        connectedSockets.forEach(socket -> {
            System.out.println("Closing socket coming from port " + socket.getPort());
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        threadPool.shutdownNow();
        serverSocket.close();
        dispatcher.join();
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

        private void put(String key) throws IOException {
            if (map.containsKey(key)) {
                out.writeBoolean(true);
                System.out.println("Key `" + key + "` already exists.");
                return;
            }
            out.writeBoolean(false);
            boolean dataExists = in.readBoolean();
            if (!dataExists) {
                System.out.println("Client could not retrieve the data.");
                return;
            }
            int dataLength = in.readInt();
            byte[] data = in.readNBytes(dataLength);
            map.put(key, data);
            System.out.println("Entry `" + key + "` successfully inserted.");
        }

        private void get(String key) throws IOException {
            if (!map.containsKey(key)) {
                out.writeInt(-1);
                System.out.println("Key `" + key + "` does not exist.");
                return;
            }
            byte[] data = map.get(key);
            out.writeInt(data.length);
            out.write(data);
            System.out.println("Value retrieved successfully!");
        }

        private void remove(String key) throws IOException {
            if (!map.containsKey(key)) {
                out.writeInt(1);
                System.out.println("Key `" + key + "` does not exist.");
                return;
            }
            map.remove(key);
            out.writeInt(0);
            System.out.println("Entry `" + key + "` successfully removed.");
        }

        private void list() throws IOException {
            List<String> strings = new ArrayList<>(map.keySet());
            System.out.println("Keys: " + strings);
            out.writeInt(strings.size());
            for (String s : strings) out.writeUTF(s);
        }

        @Override
        public void run() {
            System.out.println("Connected to client from port " + clientSocket.getPort() + ".");
            try {
                while (true) {
                    int method = in.readInt();
                    if (LIST.equals(method)) {
                        list();
                        continue;
                    }
                    String key = in.readUTF();
                    if (GET.equals(method)) {
                        get(key);
                        continue;
                    }
                    if (REMOVE.equals(method)) {
                        remove(key);
                        continue;
                    }
                    if (PUT.equals(method)) {
                        put(key);
                    }
                }
            } catch (EOFException e) {
                System.out.println("Client from port " + clientSocket.getPort() + " disconnected!");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                clientSocket.close();
                connectedSockets.remove(clientSocket);
                System.out.println("Thread closed!");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void runServer(int port) throws IOException, InterruptedException {
        RUStoreServer server = new RUStoreServer(port);
        System.out.println("Type exit to terminate the server.");
        server.connect();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("exit")) {
                server.disconnect();
                scanner.close();
                return;
            }
        }
    }

    public static void main(String[] arguments) throws IOException, InterruptedException {
        if (arguments.length != 1) {
            System.out.println("Invalid number of arguments. You must provide a port number.");
            return;
        }
        int port = Integer.parseInt(arguments[0]);
        runServer(port);
    }
}