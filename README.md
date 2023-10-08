## 1 Introduction

This is an implementation of an in-memory object-based storage architecture where objects are stored in key-value
pairs for simple insertion and retrieval. Essentially a string data type to byte array association is established where
the keys are strings used to identify which object corresponds to which key. The following three-step process was
used to implement this architecture:

```
(1) Designing data structures to make sure multiple clients are able to utilize efficient storage and retrieval of
objects.
```
```
(2) Making sure that reliable connection is established between the client and the server, accommodating clients
concurrently.
```
```
(3) Creating a protocol to enable coherent communication standards between the two end points of connection.
```
## 2 Data Structures

The main data structure that was used for this project was Concurrent Hash Map. This was used in order to ensure
storage of key-value pairs and to accommodate the needs of multiple clients to concurrently insert, remove, or retrieve
data from the object store.

For defining the protocol a protocol enumeration data structure was created which maps method names to numerical
identifiers. More details are provided in section 4 of the report.

## 3 Establishing a Connection

## 3.1 The Client

To establish a connection with the server, the client object, after instantiation, connects to the network using a
socket on the chosen port. When the socket is created and connected to the server, the necessary input and output
streams are opened to easily transfer information to and from the server. After the client is finished executing and
the disconnect method is called, it closes the connection and stops all incoming and outgoing communications with
the server.

## 3.2 The Server

In order for the server to handle multiple clients coming in concurrently, a straight-forward multi-threaded architec-
ture was employed. When the server object is instantiated, it immediately creates a socket, along with a dispatcher
thread and a thread pool which are meant to manage task delegation to worker threads. Then, when the server
is connected to the network, the dispatcher thread is executed, spawning new threads, adding them to the thread
pool, and accommodating client requests. When the disconnect method is invoked on the server-side, all of the client
sockets are closed, the worker threads are terminated, the dispatcher is interrupted, the thread pool is shut down,
and the server socket is closed.


## 4 The Protocol

The communication protocol for this client-server architecture assigns numerical values to each of the methods defined
on the client side. The method-to-numeric-value assignments, defined in the Protocol enumeration, are remembered
by both parties allowing the server to recognize which method the client is trying to invoke.

Each method on the client side first sends it’s own method identifier number to the server through the output
stream. When the server receives the identifier, it knows which method is meant to be called and awaits for the
needed arguments.

### 4.1 String[] list()

The identifier number for this method is enough for the client to send so that the server can successfully execute the
needed instructions. On the server side, the storage unit is iterated and each of the keys of the string data type is
stored in an array and sent back to the client as a list of keys.

### 4.2 byte[] get(String key)

For this method the client, in addition to the identifier number, sends the key as a string data type through the
socket. On the server side, the Hash Map is queried and the contents of the byte array corresponding to the key are
sent back to the client along with the number of bytes. If the Hash Map does not contain the queried key, the field
for the number of bytes is assigned-1letting the client know about the unsuccessful lookup.

### 4.3 int get(String key, String filepath)

This method is exactly the same asbyte[] get(String key)on the server side, but has a slight variation for the
client side. Instead of returning the retrieved bytes, the method writes them to the file specified by thefilepath
variable.

### 4.4 int put(String key, byte[] data)

After sending the identifier and the key, the client then awaits for a response from the server which checks whether
the key provided, already exists or not. The client is notified of this information and if the key exists, it returns 1 ,
otherwise it continues execution and sends thedataarray and its length to the server. Finally the server inserts the
key-value pair into the object store.

### 4.5 int put(String key, String filepath)

Again on the server side, this method is exactly the same asint put(String key, byte[] data), but is slightly
different for the client side. Instead of directly sending an array of bytes, here the client uses thefilepathparameter
to retrieve the bytes of the file which is then sent to the server.

### 4.6 int remove(String key)

On the client side, this method simply sends the identifier and the key to the server. If the key is not in storage, the
appropriate return value is sent to the client. Otherwise the server uses the key to remove the entry from the Hash
Map and sends a success code to the client.
