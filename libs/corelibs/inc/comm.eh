/* COMM connections. */

use "io.eh"

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

def listCommPorts(): [String];

def Comm.new(port: String, cfg: CommCfg);
def Comm.getBaudRate(): Int;
def Comm.setBaudRate(baudrate: Int);
