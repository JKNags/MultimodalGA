package test;

import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.Random;

public class Test {

	public static void main(String[] args) {
		DecimalFormat decimalFormat = new DecimalFormat("#0.0000");
		
		//String bits = "00000000000000000000000000000101";
		//double top = Math.pow(2, 32);
		//int i = Integer.valueOf(bits, 2);
		
		//System.out.println(top + " / " + i + ",  " + decimalFormat.format(i / top) + "=" + i/top);
		
		final int bitLength = 10;
		final double top = Math.pow(2, bitLength); 
		BitSet bits = new BitSet(bitLength);
		BitSet child = new BitSet(bitLength);
		Random rand = new Random();
		for (int i = 0; i < bitLength; i++)
		 	if (rand.nextDouble() < 0.6) bits.flip(i);
		
		System.out.println("top: " + top);
		System.out.println("Parent: " + bits  + "\n" + bits.toLongArray()[0]);
		System.out.println("Child: " + child);
		//for(int idx = 2; idx < 6; idx++) {
		//	child.set(idx, bits.get(idx));
		//}
		System.out.println("Child1: " + child);
	}

}
