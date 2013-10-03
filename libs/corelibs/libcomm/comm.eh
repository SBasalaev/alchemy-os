/* COMM connections. */

use "connection.eh"

type Comm < StreamConnection;

type CommCfg {
 baudrate: Int,
 bitsperchar: Int = 8,
 stopbits: Int = 1,
 parity: String = "none",
 blocking: Bool = true,
 autocts: Bool = true,
 autorts: Bool = true
}

def list_commports(): [String];

def new_comm(port: String, cfg: CommCfg): Comm;
const `Comm.new` = new_comm;

def Comm.get_baudrate(): Int;
def Comm.set_baudrate(baudrate: Int);
