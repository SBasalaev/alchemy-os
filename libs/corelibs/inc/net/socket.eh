/* Client web sockets. */

use "connection.eh"

type Socket < StreamConnection

def Socket.new(host: String, port: Int): Socket

def Socket.getHost(): String
def Socket.getPort(): Int
def Socket.getLocalHost(): String
def Socket.getLocalPort(): Int
def Socket.getDelay(): Bool
def Socket.setDelay(on: Bool)
def Socket.getKeepAlive(): Bool
def Socket.setKeepAlive(on: Bool)
def Socket.getLinger(): Int
def Socket.setLinger(linger: Int)
def Socket.getSndBuf(): Int
def Socket.setSndBuf(size: Int)
def Socket.getRcvBuf(): Int
def Socket.setRcvBuf(size: Int)

type SecureSocket < Socket

def SecureSocket.new(host: String, port: Int): SecureSocket

type SecInfo
def SecureSocket.getSecInfo(): SecInfo
