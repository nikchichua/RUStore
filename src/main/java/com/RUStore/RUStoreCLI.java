package com.RUStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RUStoreCLI {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String WHITE = "\u001B[37m";
    public static final String BOLD = "\033[1;37m";
    public static final String RED_BOLD = "\033[1;31m";
    public static final String BLUE_BOLD = "\033[1;34m";
    public static final String PURPLE_BOLD = "\033[1;35m";
    private static RUStoreClient client;
    private static List<String> arguments;

    public static void printMessage(String color, String message) {
        System.out.println(color + message + RESET);
    }

    public static void printWelcome() {
        printMessage(BOLD, "\nThis little client utilizes a uses an RUClient to allow\n" +
                "you to send and store strings within an object store.");
    }

    public static void printUsage() {
        printMessage(BLUE, "\nUsage:");
        printMessage(BLUE, "   connect  <host> <port>");
        printMessage(BLUE, "   put      <key> <string>");
        printMessage(BLUE, "   put_file <key> <file_path>");
        printMessage(BLUE, "   get      <key>");
        printMessage(BLUE, "   get_file <key> <file_path>");
        printMessage(BLUE, "   remove   <key>");
        printMessage(BLUE, "   list");
        printMessage(BLUE, "   disconnect");
        printMessage(BLUE, "   exit\n");
    }

    static void connect() {
        if (arguments.size() != 3) {
            printMessage(RED, "Bad arguments. Host and port must be specified.");
            printUsage();
            return;
        }
        if (client == null) {
            int port;
            try {
                port = Integer.parseInt(arguments.get(2));
            } catch (NumberFormatException e) {
                printMessage(RED, "Port field needs to be a number.");
                return;
            }
            client = new RUStoreClient(arguments.get(1), port);
        }
        System.out.println(YELLOW + "Connecting to server at " + arguments.get(1) + ":" + arguments.get(2) + "..." + RESET);
        try {
            client.connect();
            printMessage(GREEN, "Connection established.");
        } catch (SocketException socketException) {
            printMessage(RED, socketException.getMessage() + "!");
            client = null;
        } catch (IOException e) {
            printMessage(RED, "Failure: unable to connect to the client");
            client = null;
        }
    }

    static void put() throws IOException {
        int ret;
        if (arguments.size() != 3) {
            printMessage(RED, "Bad arguments. Key and String must be specified.");
            printUsage();
            return;
        }
        printMessage(YELLOW, "Attempting a put request of key: \"" + arguments.get(1) + "\"...");
        try {
            ret = client.put(arguments.get(1), arguments.get(2).getBytes());
        } catch (IOException e) {
            printMessage(RED_BOLD, "Unexpected Error: connection terminating!");
            disconnect();
            return;
        }
        if (ret == 0) {
            printMessage(GREEN, "Successful put request.");
        } else {
            printMessage(WHITE, "Failure: " + "key already exists!");
        }
    }

    static void put_file() throws IOException {
        int ret;
        if (arguments.size() != 3) {
            printMessage(RED, "Bad arguments. Key and String must be specified.");
            printUsage();
            return;
        }
        printMessage(YELLOW, "Attempting a put request of key: \"" + arguments.get(1) + "\"...");
        try {
            ret = client.put(arguments.get(1), arguments.get(2));
        } catch (NoSuchFileException e) {
            printMessage(RED, e.getMessage());
            return;
        }
        if (ret == 0) {
            printMessage(GREEN, "Successful put request.");
        } else {
            printMessage(WHITE, "Failure: " + "key already exists!");
        }
    }

    static void get() throws IOException {
        if (arguments.size() != 2) {
            printMessage(RED, "Bad arguments. Key must be specified.");
            printUsage();
            return;
        }
        byte[] bytes;
        printMessage(YELLOW, "Attempting a get request of key: \"" + arguments.get(1) + "\"...");
        try {
            bytes = client.get(arguments.get(1));
        } catch (IOException e) {
            printMessage(RED_BOLD, "Unexpected Error: connection terminating!");
            disconnect();
            return;
        }
        if (bytes != null) {
            printMessage(GREEN, new String(bytes));
        } else {
            printMessage(WHITE, "Failure: no such key!");
        }
    }

    static void get_file() {
        int ret;
        if (arguments.size() != 3) {
            printMessage(RED, "Bad arguments. Key and String must be specified.");
            printUsage();
            return;
        }
        printMessage(YELLOW, "Attempting a get request of key: \"" + arguments.get(1) + "\"...");
        try {
            ret = client.get(arguments.get(1), arguments.get(2));
        } catch (IOException e) {
            printMessage(RED, e.getMessage());
            return;
        }
        if (ret == 0) {
            printMessage(GREEN, "Successful get request.");
        } else {
            printMessage(WHITE, "Failure: no such key!");
        }
    }

    static void remove() throws IOException {
        int ret;
        if (arguments.size() != 2) {
            printMessage(WHITE, "Bad arguments. Key must be specified.");
            printUsage();
            return;
        }
        printMessage(YELLOW, "Attempting a remove request of key \"" + arguments.get(1) + "\"...");
        try {
            ret = client.remove(arguments.get(1));
        } catch (IOException e) {
            printMessage(RED_BOLD, "Unexpected Error: connection terminating!");
            disconnect();
            return;
        }
        if (ret == 0) {
            printMessage(GREEN, "Successful remove request.");
        } else {
            printMessage(WHITE, "Failure: no such key!");
        }
    }

    static void list() throws IOException {
        printMessage(YELLOW, "Attempting a list request...");
        String[] keys;
        int index;
        try {
            keys = client.list();
        } catch (IOException e) {
            printMessage(RED_BOLD, "Unexpected Error: connection terminating!");
            disconnect();
            return;
        }
        if (keys != null) {
            for (index = 0; index < keys.length; index++) {
                printMessage(GREEN, keys[index]);
            }
        } else {
            printMessage(WHITE, "No available keys.");
        }
    }

    public static void disconnect() throws IOException {
        printMessage(BLUE_BOLD, "Disconnecting from the server...");
        try {
            client.disconnect();
            client = null;
            printMessage(PURPLE_BOLD, "Connection terminated.");
        } catch (SocketException e) {
            printMessage(RED, e.getMessage());
        } catch (IOException e) {
            printMessage(RED, "Unexpected Error: connection terminating...");
            client.disconnect();
            client = null;
        }
    }

    public static void main(String[] args) throws Exception {
        client = null;
        BufferedReader userdata = new BufferedReader(new InputStreamReader(System.in));
        printWelcome();
        printUsage();
        while (true) {
            System.out.print("> ");
            String line = userdata.readLine();
            arguments = new ArrayList<>();
            Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(line);
            while (m.find())
                arguments.add(m.group(1).replace("\"", ""));
            if (arguments.isEmpty()) continue;
            String command = arguments.get(0);
            if (command.equals("connect")) {
                connect();
            } else if (command.equals("exit")) {
                break;
            } else {
                if (client == null) {
                    printMessage(RED, "Client not connected to the server!");
                    continue;
                }
                switch (command) {
                    case "put":
                        put();
                        break;
                    case "put_file":
                        put_file();
                        break;
                    case "get":
                        get();
                        break;
                    case "get_file":
                        get_file();
                        break;
                    case "remove":
                        remove();
                        break;
                    case "list":
                        list();
                        break;
                    case "disconnect":
                        disconnect();
                        break;
                    default:
                        printMessage(WHITE, "Invalid Command.");
                        printUsage();
                        break;
                }
            }
        }
        userdata.close();
    }
}