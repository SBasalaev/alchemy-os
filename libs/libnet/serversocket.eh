/* Server web sockets. */

use "connection.eh"

type ServerSocket < Connection;
type Socket;

const ANY_PORT = -1;

def new_serversocket(port: Int): ServerSocket;

def ServerSocket.get_localhost(): String;
def ServerSocket.get_localport(): Int;
def ServerSocket.accept(): Socket;
