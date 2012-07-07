/* Client web sockets. */

use "connection.eh"

type Socket < StreamConnection;

def new_socket(host: String, port: Int): Socket;

def Socket.get_host(): String;
def Socket.get_port(): Int;
def Socket.get_localhost(): String;
def Socket.get_localport(): Int;
def Socket.get_delay(): Bool;
def Socket.set_delay(on: Bool);
def Socket.get_keepalive(): Bool;
def Socket.set_keepalive(on: Bool);
def Socket.get_linger(): Int;
def Socket.set_linger(linger: Int);
def Socket.get_sndbuf(): Int;
def Socket.set_sndbuf(size: Int);
def Socket.get_rcvbuf(): Int;
def Socket.set_rcvbuf(size: Int);

type SecureSocket < Socket;

def new_securesocket(host: String, port: Int): SecureSocket;

type SecInfo;
def SecureSocket.get_secinfo(): SecInfo;
