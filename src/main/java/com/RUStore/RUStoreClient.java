package com.RUStore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static com.RUStore.Protocol.*;

public class RUStoreClient {
    private final String host;
    private final int port;
    DataOutputStream out;
    DataInputStream in;
    private Socket clientSocket;

    /**
     * RUStoreClient Constructor, initializes default values
     * for class members
     *
     * @param host host url
     * @param port port number
     */
    public RUStoreClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Opens a socket and establish a connection to the object store server
     * running on a given host and port.
     *
     * @return n/a, however throw an exception if any issues occur
     */
    public void connect() throws IOException {
        clientSocket = new Socket(host, port);
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new DataInputStream(clientSocket.getInputStream());
    }

    /**
     * Sends an arbitrary data object to the object store server. If an
     * object with the same key already exists, the object should NOT be
     * overwritten
     *
     * @param key  key to be used as the unique identifier for the object
     * @param data byte array representing arbitrary data object
     * @return 0 upon success
     * 1 if key already exists
     * Throw an exception otherwise
     */
    public int put(String key, byte[] data) throws IOException {
        out.writeInt(PUT.methodNumber());

        out.writeUTF(key);
        out.writeInt(data.length);
        out.write(data);
        return in.readInt();
    }

    /**
     * Sends an arbitrary data object to the object store server. If an
     * object with the same key already exists, the object should NOT
     * be overwritten.
     *
     * @param key       key to be used as the unique identifier for the object
     * @param file_path path of file data to transfer
     * @return 0 upon success
     * 1 if key already exists
     * Throw an exception otherwise
     */
    public int put(String key, String file_path) {

        // Implement here
        return -1;

    }

    /**
     * Downloads arbitrary data object associated with a given key
     * from the object store server.
     *
     * @param key key associated with the object
     * @return object data as a byte array, null if key doesn't exist.
     * Throw an exception if any other issues occur.
     */
    public byte[] get(String key) throws IOException {
        out.writeInt(GET.methodNumber());
        out.writeUTF(key);

        int arrayLength = in.readInt();

        if (arrayLength <= 0) return null;

        return in.readNBytes(arrayLength);
    }

    /**
     * Downloads arbitrary data object associated with a given key
     * from the object store server and places it in a file.
     *
     * @param key       key associated with the object
     * @param file_path output file path
     * @return 0 upon success
     * 1 if key doesn't exist
     * Throw an exception otherwise
     */
    public int get(String key, String file_path) {

        // Implement here
        return -1;

    }

    /**
     * Removes data object associated with a given key
     * from the object store server. Note: No need to download the data object,
     * simply invoke the object store server to remove object on server side
     *
     * @param key key associated with the object
     * @return 0 upon success
     * 1 if key doesn't exist
     * Throw an exception otherwise
     */
    public int remove(String key) {

        // Implement here
        return -1;

    }

    /**
     * Retrieves of list of object keys from the object store server
     *
     * @return List of keys as string array, null if there are no keys.
     * Throw an exception if any other issues occur.
     */
    public String[] list() throws IOException, InterruptedException {
        out.writeInt(LIST.methodNumber());

        int arrayLength = in.readInt();

        if (arrayLength == 0) return null;

        String[] result = new String[arrayLength];
        for (int i = 0; i < arrayLength; i++) {
            result[i] = in.readUTF();
        }

        return result;
    }

    /**
     * Signals to server to close connection before closes
     * the client socket.
     *
     * @return n/a, however throw an exception if any issues occur
     */
    public void disconnect() throws IOException {
        clientSocket.close();
        System.out.println("Client Closed!");
    }

}
