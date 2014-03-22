/* Server web sockets. */

use "connection.eh"

type ServerSocket < Connection
type Socket

const ANY_PORT = -1

def ServerSocket.new(port: Int = ANY_PORT): ServerSocket
def ServerSocket.getLocalHost(): String
def ServerSocket.getLocalPort(): Int
def ServerSocket.accept(): Socket
