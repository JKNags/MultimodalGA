package niching;

import java.util.BitSet;

public class Individual {
	private BitSet xBits;
	private BitSet yBits;
	private double x;
	private double y;
	private double fitness;
	
	public Individual(BitSet _xBits, BitSet _yBits, 
			double _x, double _y, double _fitness) {
		xBits = _xBits;
		yBits = _yBits;
		x = _x;
		y = _y;
		fitness= _fitness;
	}
	
	public BitSet getXBits() {
		return xBits;
	}
	
	public BitSet getYBits() {
		return yBits;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getFitness() {
		return fitness;
	}

}
