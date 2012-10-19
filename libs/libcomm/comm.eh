/* COMM connections. */

use "connection.eh"

type Comm < StreamConnection;

type CommCfg {
 baudrate: Int,
 bitsperchar: Int,
 stopbits: Int,
 parity: String,
 blocking: Bool,
 autocts: Bool,
 autorts: Bool
}

def list_commports(): [String];

def new_comm(port: String, cfg: CommCfg): Comm;

def Comm.get_baudrate(): Int;
def Comm.set_baudrate(baudrate: Int);
