package niching;

import java.util.BitSet;
import java.util.Random;

public class Individual {
	private BitSet bits;
	private double x;
	private double y;
	
	public Individual(BitSet _bits, double _x, double _y) {
		bits = _bits;
		x = _x;
		y = _y;
	}
	
	public BitSet getBits() {
		return bits;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public void update(BitSet _bits, double _x, double _y) {
		bits = _bits;
		x = _x;
		y = _y;
	}
}
