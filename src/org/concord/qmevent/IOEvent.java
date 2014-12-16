package org.concord.qmevent;

import java.util.EventObject;

/**
 * @author Charles Xie
 * 
 */
public class IOEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	public final static byte FILE_INPUT = 0;
	public final static byte FILE_OUTPUT = 1;

	private byte type = FILE_INPUT;

	public IOEvent(byte type, Object source) {
		super(source);
		this.type = type;
	}

	public byte getType() {
		return type;
	}

}
