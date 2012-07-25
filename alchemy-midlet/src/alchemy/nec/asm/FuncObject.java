package alchemy.nec.asm;

public class FuncObject {
	final String value;

	public FuncObject(String value) {
		this.value = value;
	}

	public boolean equals(Object obj) {
		if (obj instanceof FuncObject) {
			return ((FuncObject)obj).value.equals(value);
		}
		return false;
	}
}

/* Assembler function. */
class AsmFunc extends FuncObject {
	boolean shared;
	int stacksize;
	int varcount;
	byte[] code;
	char[] relocs;

	public AsmFunc(String value) {
		super(value);
	}
}