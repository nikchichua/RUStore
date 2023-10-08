\documentclass{article}
\usepackage[utf8]{inputenc}
\usepackage{enumitem}
\usepackage{listings}
\usepackage{multirow}
\usepackage{array}
\usepackage[margin=0.75in]{geometry}
\usepackage{tikz}
\usetikzlibrary{arrows.meta}

\title{Project 1 Report}
\author{Nikoloz Chichua}
\date{October 8, 2023}

\begin{document}

\maketitle

\newcommand{\x}[2][0]{\ifnum#1=1\overline{x}_{#2}\else x_{#2}\fi}
\newtheorem{theorem}{Theorem}[section]

\section{Introduction}

This is an implementation of an in-memory object-based storage architecture where objects are stored in key-value pairs for simple insertion and retrieval. Essentially a string data type to byte array association is established where the keys are strings used to identify which object corresponds to which key. The following three-step process was used to implement this architecture:
\begin{enumerate}[label=(\arabic*)]
    \item Designing data structures to make sure multiple clients are able to utilize efficient storage and retrieval of objects.
    \item Making sure that reliable connection is established between the client and the server, accommodating clients concurrently.
    \item Creating a protocol to enable coherent communication standards between the two end points of connection.
\end{enumerate}

\noindent

\section{Data Structures}
The main data structure that was used for this project was Concurrent Hash Map. This was used in order to ensure storage of key-value pairs and to accommodate the needs of multiple clients to concurrently insert, remove, or retrieve data from the object store.
\\
\\
\noindent
For defining the protocol a protocol enumeration data structure was created which maps method names to numerical identifiers. More details are provided in section 4 of the report.

\section{Establishing a Connection}
\subsection{The Client}
To establish a connection with the server, the client object, after instantiation, connects to the network using a socket on the chosen port. When the socket is created and connected to the server, the necessary input and output streams are opened to easily transfer information to and from the server. After the client is finished executing and the disconnect method is called, it closes the connection and stops all incoming and outgoing communications with the server.

\subsection{The Server}
In order for the server to handle multiple clients coming in concurrently, a straight-forward multi-threaded architecture was employed. When the server object is instantiated, it immediately creates a socket, along with a dispatcher thread and a thread pool which are meant to manage task delegation to worker threads. Then, when the server is connected to the network, the dispatcher thread is executed, spawning new threads, adding them to the thread pool, and accommodating client requests. When the disconnect method is invoked on the server-side, all of the client sockets are closed, the worker threads are terminated, the dispatcher is interrupted, the thread pool is shut down, and the server socket is closed.

\section{The Protocol}
The communication protocol for this client-server architecture assigns numerical values to each of the methods defined on the client side. The method-to-numeric-value assignments, defined in the Protocol enumeration, are remembered by both parties allowing the server to recognize which method the client is trying to invoke.
\\
\\
\noindent
Each method on the client side first sends it's own method identifier number to the server through the output stream. When the server receives the identifier, it knows which method is meant to be called and awaits for the needed arguments.

% the next argument to arrive, unless the identifier corresponds to the \texttt{list()} method, in which case it can immediately execute and await for the next potential command. 

\subsection{\texttt{String[] list()}}
The identifier number for this method is enough for the client to send so that the server can successfully execute the needed instructions. On the server side, the storage unit is iterated and each of the keys of the string data type is stored in an array and sent back to the client as a list of keys.

\subsection{\texttt{byte[] get(String key)}}
For this method the client, in addition to the identifier number, sends the key as a string data type through the socket. On the server side, the Hash Map is queried and the contents of the byte array corresponding to the key are sent back to the client along with the number of bytes. If the Hash Map does not contain the queried key, the field for the number of bytes is assigned \texttt{-1} letting the client know about the unsuccessful lookup.

\subsection{\texttt{int get(String key, String file\_path)}}
This method is exactly the same as \texttt{byte[] get(String key)} on the server side, but has a slight variation for the client side. Instead of returning the retrieved bytes, the method writes them to the file specified by the \texttt{file\_path} variable.

\subsection{\texttt{int put(String key, byte[] data)}}
After sending the identifier and the key, the client then awaits for a response from the server which checks whether the key provided, already exists or not. The client is notified of this information and if the key exists, it returns \texttt{1}, otherwise it continues execution and sends the \texttt{data} array and its length to the server. Finally the server inserts the key-value pair into the object store.

\subsection{\texttt{int put(String key, String file\_path)}}
Again on the server side, this method is exactly the same as \texttt{int put(String key, byte[] data)}, but is slightly different for the client side. Instead of directly sending an array of bytes, here the client uses the \texttt{file\_path} parameter to retrieve the bytes of the file which is then sent to the server.

\subsection{\texttt{int remove(String key)}}
On the client side, this method simply sends the identifier and the key to the server. If the key is not in storage, the appropriate return value is sent to the client. Otherwise the server uses the key to remove the entry from the Hash Map and sends a success code to the client. 

\section{What could be better?}
There are numerous implementation details that could have been done to enhance the project including better synchronization when writing to files with the same name, a more advanced protocol for handling more complicated methods, and more efficient ways of checking for connections and sending data. The implementation in its current state is also very well designed and written, supporting scaling for multiple clients as well as giving the freedom to easily add new methods and widen the protocol to include other operations in case the program needs to be expanded.

\end{document}
