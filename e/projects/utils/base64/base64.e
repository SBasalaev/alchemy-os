/* base64 encoder/decoder
 * Version 1.0.2
 * (C) 2011-2012, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io"
use "string"

const CODING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

def enc(b: Int): Int = strchr(CODING, b)

def dec(ch: Int): Int = strindex(CODING, ch)

def encode() {
  var buf = new BArray(3)
  var len = readarray(buf, 0, 3)
  while (len > 0) {
    if (len == 1) {
      write(enc((buf[0]>>2) & 0x3f))
      write(enc((buf[0]<<4) & 0x3f))
      write('=')
      write('=')
    } else if (len == 2) {
      write(enc((buf[0]>>2) & 0x3f))
      write(enc((buf[0]<<4) & 0x30 | (buf[1]>>4) & 0x0f))
      write(enc((buf[1]<<2) & 0x3c))
      write('=')
    } else {
      write(enc((buf[0]>>2) & 0x3f))
      write(enc((buf[0]<<4) & 0x30 | (buf[1]>>4) & 0x0f))
      write(enc((buf[1]<<2) & 0x3c | (buf[2]>>6) & 0x03))
      write(enc(buf[2] & 0x3f))
    }
    len = readarray(buf, 0, 3)
  }
}

def decode() {
  var buf = new BArray(4)
  while (readarray(buf, 0, 4) == 4) {
    var b1 = dec(buf[0])
    var b2 = dec(buf[1])
    var b3 = dec(buf[2])
    var b4 = dec(buf[3])
    if ((b1 | b2) >= 0) {
     write((b1<<2) | ((b2>>4) & 0x03))
     if (b3 >= 0) {
      write((b2<<4) | ((b3>>2) & 0x0f))
      if (b4 >= 0) {
       write((b3<<6) | (b4 & 0x3f))
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
      setin(fopen_r(to_str(args[1])))
    }
    decode()
  } else {
    setin(fopen_r(to_str(args[0])))
    encode()
  }
  flush()
}
