use "io"
use "string"

def enc(b: Int): Int = strchr("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", b)

def dec(ch: Int): Int = strindex("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", ch)

def encode() {
  var in = stdin()
  var out = stdout()
  var buf = new BArray(3)
  var len = readarray(in, buf, 0, 3)
  while (len > 0) {
    if (len == 1) {
      write(out, enc((buf[0]>>2) & 0x3f))
      write(out, enc((buf[0]<<4) & 0x3f))
      write(out, '=')
      write(out, '=')
    } else if (len == 2) {
      write(out, enc((buf[0]>>2) & 0x3f))
      write(out, enc((buf[0]<<4) & 0x30 | (buf[1]>>4) & 0x0f))
      write(out, enc((buf[1]<<2) & 0x3c))
      write(out, '=')
    } else {
      write(out, enc((buf[0]>>2) & 0x3f))
      write(out, enc((buf[0]<<4) & 0x30 | (buf[1]>>4) & 0x0f))
      write(out, enc((buf[1]<<2) & 0x3c | (buf[2]>>6) & 0x03))
      write(out, enc(buf[2] & 0x3f))
    }
    len = readarray(in,buf,0,3)
  }
}

def decode() {
  var in = stdin()
  var out = stdout()
  var buf = new BArray(4)
  while (readarray(in,buf,0,4) == 4) {
    var b1 = dec(buf[0])
    var b2 = dec(buf[1])
    var b3 = dec(buf[2])
    var b4 = dec(buf[3])
    if ((b1 | b2) >= 0) {
    write(out, (b1<<2) | ((b2>>4) & 0x03))
    if (b3 >= 0) {
    write(out, (b2<<4) | ((b3>>2) & 0x0f))
    if (b4 >= 0) {
    write(out, (b3<<6) | (b4 & 0x3f))
    }
    }
    }
  }
}

def main(args: Array) {
  if (args.len == 0) {
    encode()
  } else if (args[0] == "-d") {
    if (args.len != 1) {
      setin(fread(to_file(to_str(args[1]))))
    }
    decode()
  } else {
    setin(fread(to_file(to_str(args[0]))))
    encode()
  }
  flush(stdout())
}
