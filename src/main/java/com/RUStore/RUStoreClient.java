package com.RUStore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.RUStore.Protocol.*;

public class RUStoreClient {
    private final String host;
    private final int port;
    DataOutputStream out;
    DataInputStream in;
    private Socket clientSocket = null;

    public RUStoreClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        if (clientSocket != null && !clientSocket.isClosed()) {
            throw new SocketException("Client already connected to the server running on port " + port + ".");
        }
        clientSocket = new Socket(host, port);
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new DataInputStream(clientSocket.getInputStream());
    }

    public int put(String key, byte[] data) throws IOException {
        out.writeInt(PUT.methodNumber());
        out.writeUTF(key);
        boolean keyExists = in.readBoolean();
        if (keyExists) return 1;
        out.writeBoolean(true);
        out.writeInt(data.length);
        out.write(data);
        return 0;
    }

    public int put(String key, String file_path) throws IOException {
        out.writeInt(PUT.methodNumber());
        out.writeUTF(key);
        boolean keyExists = in.readBoolean();
        if (keyExists) return 1;
        File file = new File(file_path);
        if (!file.exists()) {
            out.writeBoolean(false);
            throw new NoSuchFileException("Specified file path does not exist!");
        }
        out.writeBoolean(true);
        byte[] data = Files.readAllBytes(file.toPath());
        out.writeInt(data.length);
        out.write(data);
        return 0;
    }

    public byte[] get(String key) throws IOException {
        out.writeInt(GET.methodNumber());
        out.writeUTF(key);
        int length = in.readInt();
        if (length == -1) return null;
        return in.readNBytes(length);
    }

    public int get(String key, String file_path) throws IOException {
        out.writeInt(GET.methodNumber());
        out.writeUTF(key);
        int length = in.readInt();
        if (length == -1) return 1;
        Path path = Paths.get(file_path);
        if (!Files.exists(path)) Files.createFile(path);
        Files.write(path, in.readNBytes(length));
        return 0;
    }

    public int remove(String key) throws IOException {
        out.writeInt(REMOVE.methodNumber());
        out.writeUTF(key);
        return in.readInt();
    }

    public String[] list() throws IOException {
        out.writeInt(LIST.methodNumber());
        int arrayLength = in.readInt();
        if (arrayLength == 0) return null;
        String[] result = new String[arrayLength];
        for (int i = 0; i < arrayLength; i++) {
            result[i] = in.readUTF();
        }
        return result;
    }

    public void disconnect() throws IOException {
        if (clientSocket.isClosed()) throw new SocketException("Client already disconnected from the server!");
        clientSocket.close();
    }
}