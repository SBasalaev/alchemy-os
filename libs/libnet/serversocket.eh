/* Server web sockets. */

use "connection.eh"

type ServerSocket < Connection;
type Socket;

const ANY_PORT = -1;

def new_serversocket(port: Int = ANY_PORT): ServerSocket;
const `ServerSocket.new` = new_serversocket;

def ServerSocket.get_localhost(): String;
def ServerSocket.get_localport(): Int;
def ServerSocket.accept(): Socket;
