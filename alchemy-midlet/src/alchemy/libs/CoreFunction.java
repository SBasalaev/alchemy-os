/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package alchemy.libs;

import alchemy.core.Context;
import alchemy.core.Function;
import alchemy.fs.File;
import alchemy.util.Util;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;
import javax.microedition.io.Connector;

/**
 * Core runtime function set.
 * @author Sergey Basalaev
 */
class CoreFunction extends Function {

	private final int index;
	private Random rnd = new Random(0);

	public CoreFunction(String name, int index) {
		super(name);
		this.index = index;
	}

	protected Object exec(Context c, Object[] args) throws Exception {
		switch (index) {
			case 0: //fname
				return ((File)args[0]).name();
			case 1: //fpath
				return ((File)args[0]).path();
			case 2: //fparent
				return ((File)args[0]).parent();
			case 3: //fcreate
				c.fs().create((File)args[0]);
				return null;
			case 4: //fremove
				c.fs().remove((File)args[0]);
				return null;
			case 5: //mkdir
				c.fs().mkdir((File)args[0]);
				return null;
			case 6: //fcopy
				c.fs().copy((File)args[0], (File)args[1]);
				return null;
			case 7: //fmove
				c.fs().copy((File)args[0], (File)args[1]);
				return null;
			case 8: //set_read
				c.fs().setRead((File)args[0], bval(args[1]));
				return null;
			case 9: //set_write
				c.fs().setWrite((File)args[0], bval(args[1]));
				return null;
			case 10: //set_exec
				c.fs().setExec((File)args[0], bval(args[1]));
				return null;
			case 11: //can_read
				return Ival(c.fs().canRead((File)args[0]));
			case 12: //can_write
				return Ival(c.fs().canRead((File)args[0]));
			case 13: //can_exec
				return Ival(c.fs().canRead((File)args[0]));
			case 14: //exists
				return Ival(c.fs().exists((File)args[0]));
			case 15: //is_dir
				return Ival(c.fs().isDirectory((File)args[0]));
			case 16: //fread
				return c.fs().read((File)args[0]);
			case 17: //fwrite
				return c.fs().write((File)args[0]);
			case 18: //fappend
				return c.fs().append((File)args[0]);
			case 19: //flist
				return c.fs().list((File)args[0]);
			case 20: //fmodified
				return Lval(c.fs().lastModified((File)args[0]));
			case 21: //fsize
				return Ival(c.fs().size((File)args[0]));
			case 22: //set_cwd
				c.setCurDir((File)args[0]);
				return null;
			case 23: //relpath
				return CoreLibrary.relPath(c, (File)args[0]);
			case 24: //strlen
				return Ival(((String)args[0]).length());
			case 25: //strchr
				return Ival(((String)args[0]).charAt(ival(args[1])));
			case 26: //strindex
				return Ival(((String)args[0]).indexOf(ival(args[1])));
			case 27: //strlindex
				return Ival(((String)args[0]).lastIndexOf(ival(args[1])));
			case 28: //strstr
				return Ival(((String)args[0]).indexOf((String)args[1]));
			case 29: //substr
				return ((String)args[0]).substring(ival(args[1]), ival(args[2]));
			case 30: //strucase
				return ((String)args[0]).toUpperCase();
			case 31: //strlcase
				return ((String)args[0]).toLowerCase();
			case 32: //strcat
				return ((String)args[0]).concat((String)args[1]);
			case 33: //strcmp
				return Ival(((String)args[0]).compareTo((String)args[1]));
			case 34: //strchars
				return ((String)args[0]).toCharArray();
			case 35: //strtrim
				return ((String)args[0]).trim();
			case 36: //to_file
				return c.toFile(((String)args[0]));
			case 37: //getenv
				return c.getEnv(((String)args[0]));
			case 38: //setenv
				c.setEnv(((String)args[0]), (String)args[1]);
				return null;
			case 39: //utfbytes
				return Util.utfEncode(((String)args[0]));
			case 40: //datestr
				return new Date(lval(args[0])).toString();
			case 41: { //year
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.YEAR));
			}
			case 42: { //month
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.MONTH));
			}
			case 43: { //day
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.DAY_OF_MONTH));
			}
			case 44: { //dow
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.DAY_OF_WEEK));
			}
			case 45: { //hour
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.HOUR_OF_DAY));
			}
			case 46: { //minute
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.MINUTE));
			}
			case 47: { //second
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.SECOND));
			}
			case 48: //sb_append
				return ((StringBuffer)args[0]).append(args[1]);
			case 49: //sb_addch
				return ((StringBuffer)args[0]).append((char)ival(args[1]));
			case 50: //sb_delete
				return ((StringBuffer)args[0]).delete(ival(args[1]), ival(args[2]));
			case 51: //sb_delch
				return ((StringBuffer)args[0]).deleteCharAt(ival(args[1]));
			case 52: //sb_insert
				return ((StringBuffer)args[0]).insert(ival(args[1]), args[2]);
			case 53: //sb_insch
				return ((StringBuffer)args[0]).insert(ival(args[1]), (char)ival(args[2]));
			case 54: //sb_setch
				((StringBuffer)args[0]).setCharAt(ival(args[1]), (char)ival(args[2]));
				return args[0];
			case 55: //sb_len
				return Ival(((StringBuffer)args[0]).length());
			case 56: //millis
				return Ival((int)(lval(args[0]) % 1000));
			case 57: //systime
				return Lval(System.currentTimeMillis());
			case 58: //get_cwd
				return c.getCurDir();
			case 59: //space_total
				return Lval(c.fs().spaceTotal());
			case 60: //space_free
				return Lval(c.fs().spaceFree());
			case 61: //space_used
				return Lval(c.fs().spaceUsed());
			case 62: //to_str
				return String.valueOf(args[0]);
			case 63: //new_sb
				return new StringBuffer();
			case 64: //close
				if (args[0] instanceof InputStream) {
					((InputStream)args[0]).close();
					return null;
				} else if (args[0] instanceof OutputStream) {
					((OutputStream)args[0]).close();
					return null;
				} else {
					throw new ClassCastException();
				}
			case 65: //read
				return Ival(((InputStream)args[0]).read());
			case 66: //readarray
				return Ival(((InputStream)args[0]).read((byte[])args[1], ival(args[2]), ival(args[3])));
			case 67: //available
				return Ival(((InputStream)args[0]).available());
			case 68: //skip
				return Lval(((InputStream)args[0]).skip(lval(args[1])));
			case 69: //write
				((OutputStream)args[0]).write(ival(args[1]));
				return null;
			case 70: //writearray
				((OutputStream)args[0]).write((byte[])args[1], ival(args[2]), ival(args[3]));
				return null;
			case 71: //flush
				((OutputStream)args[0]).flush();
				return null;
			case 72: { //exec
				String prog = (String)args[0];
				Object[] oargs = (Object[])args[1];
				Context cc = new Context(c);
				String[] sargs = new String[oargs.length];
				for (int i=oargs.length-1; i>=0; i--) {
					sargs[i] = String.valueOf(oargs[i]);
				}
				return Ival(cc.startAndWait(prog, sargs));
			}
			case 73: { //fork
				String prog = (String)args[0];
				Object[] oargs = (Object[])args[1];
				Context cc = new Context(c);
				String[] sargs = new String[oargs.length];
				for (int i=oargs.length-1; i>=0; i--) {
					sargs[i] = String.valueOf(oargs[i]);
				}
				cc.start(prog, sargs);
				return null;
			}
			case 74: //new_ba
				return new byte[ival(args[0])];
			case 75: //new_ca
				return new char[ival(args[0])];
			case 76: //new_ia
				return new int[ival(args[0])];
			case 77: //new_la
				return new long[ival(args[0])];
			case 78: //new_fa
				return new float[ival(args[0])];
			case 79: //new_da
				return new double[ival(args[0])];
			case 80: //new_aa
				return new Object[ival(args[0])];
			case 81: //baload
				return Ival(((byte[])args[0])[ival(args[1])]);
			case 82: //caload
				return Ival(((char[])args[0])[ival(args[1])]);
			case 83: //iaload
				return Ival(((int[])args[0])[ival(args[1])]);
			case 84: //laload
				return Lval(((long[])args[0])[ival(args[1])]);
			case 85: //faload
				return Fval(((float[])args[0])[ival(args[1])]);
			case 86: //daload
				return Dval(((double[])args[0])[ival(args[1])]);
			case 87: //aaload
				return ((Object[])args[0])[ival(args[1])];
			case 88: //bastore
				((byte[])args[0])[ival(args[1])] = (byte)ival(args[2]);
				return null;
			case 89: //castore
				((char[])args[0])[ival(args[1])] = (char)ival(args[2]);
				return null;
			case 90: //iastore
				((int[])args[0])[ival(args[1])] = ival(args[2]);
				return null;
			case 91: //lastore
				((long[])args[0])[ival(args[1])] = lval(args[2]);
				return null;
			case 92: //fastore
				((float[])args[0])[ival(args[1])] = fval(args[2]);
				return null;
			case 93: //dastore
				((double[])args[0])[ival(args[1])] = dval(args[2]);
				return null;
			case 94: //aastore
				((Object[])args[0])[ival(args[1])] = args[2];
				return null;
			case 95: //balen
				return Ival(((byte[])args[0]).length);
			case 96: //calen
				return Ival(((char[])args[0]).length);
			case 97: //ialen
				return Ival(((int[])args[0]).length);
			case 98: //lalen
				return Ival(((long[])args[0]).length);
			case 99: //falen
				return Ival(((float[])args[0]).length);
			case 100: //dalen
				return Ival(((double[])args[0]).length);
			case 101: //aalen
				return Ival(((Object[])args[0]).length);
			case 102: //bacopy
			case 103: //cacopy
			case 104: //iacopy
			case 105: //lacopy
			case 106: //facopy
			case 107: //dacopy
			case 108: //aacopy
				System.arraycopy(args[0], ival(args[1]), args[2], ival(args[3]), ival(args[4]));
				return null;
			case 109: //print
				((OutputStream)args[0]).write(Util.utfEncode(String.valueOf(args[1])));
				return args[0];
			case 110: //println
				((OutputStream)args[0]).write(Util.utfEncode(String.valueOf(args[1]).concat("\n")));
				return args[0];
			case 111: //stdin
				return c.stdin;
			case 112: //stdout
				return c.stdout;
			case 113: //stderr
				return c.stderr;
			case 114: //setin
				c.stdin = (InputStream)args[0];
				return null;
			case 115: //setout
				c.stdout = new PrintStream((OutputStream)args[0]);
				return null;
			case 116: //seterr
				c.stderr = new PrintStream((OutputStream)args[0]);
				return null;
			case 117: //pi
				return Dval(Math.PI);
			case 118: //abs
				return Dval(Math.abs(dval(args[0])));
			case 119: //sin
				return Dval(Math.sin(dval(args[0])));
			case 120: //cos
				return Dval(Math.cos(dval(args[0])));
			case 121: //tan
				return Dval(Math.tan(dval(args[0])));
			case 122: //sqrt
				return Dval(Math.sqrt(dval(args[0])));
			case 123: //ipow
				return Dval(CoreLibrary.ipow(dval(args[0]), ival(args[1])));
			case 124: //exp
				return Dval(CoreLibrary.exp(dval(args[0])));
			case 125: //log
				return Dval(CoreLibrary.log(dval(args[0])));
			case 126: //asin
				return Dval(CoreLibrary.asin(dval(args[0])));
			case 127: //acos
				return Dval(CoreLibrary.acos(dval(args[0])));
			case 128: //atan
				return Dval(CoreLibrary.atan(dval(args[0])));
			case 129: //ca2str
				return new String((char[])args[0]);
			case 130: //ba2utf
				return Util.utfDecode((byte[])args[0]);
			case 131: //ibits2f
				return Fval(Float.intBitsToFloat(ival(args[0])));
			case 132: //f2ibits
				return Ival(Float.floatToIntBits(fval(args[0])));
			case 133: //lbits2d
				return Dval(Double.longBitsToDouble(lval(args[0])));
			case 134: //d2lbits
				return Lval(Double.doubleToLongBits(dval(args[0])));
			case 135: //new_ht
				return new Hashtable();
			case 136: //ht_put
				return ((Hashtable)args[0]).put(args[1], args[2]);
			case 137: //ht_get
				return ((Hashtable)args[0]).get(args[1]);
			case 138: //ht_rm
				return ((Hashtable)args[0]).remove(args[1]);
			case 139: //hash
				return Ival(args[0].hashCode());
			case 140: //randomize
				rnd = new Random(lval(args[0]));
				return null;
			case 141: //rnd
				return Ival(rnd.nextInt(ival(args[0])));
			case 142: //rndint
				return Ival(rnd.nextInt());
			case 143: //rndlong
				return Lval(rnd.nextLong());
			case 144: //rndfloat
				return Fval(rnd.nextFloat());
			case 145: //rnddouble
				return Dval(rnd.nextDouble());
			case 146: { //netread
				String addr = args[0].toString();
				if (!addr.startsWith("http:")&&!addr.startsWith("https:"))
					throw new IOException("Unknown protocol in netread()");
				return Connector.openInputStream(addr);
			}
		}
		return null;
	}
	
	public String toString() {
		return "libcore:"+signature;
	}
}
