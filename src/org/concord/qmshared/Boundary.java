package org.concord.qmshared;

/**
 * @author Charles Xie
 * 
 */
public interface Boundary {

	public final static byte DEFAULT_BOUNDARY_CONDITION = 0;
	public final static byte ABSORBING_BOUNDARY_CONDITION = 1;

	public void setDirection(char direction);

	public char getDirection();

	public String toXml();

}
