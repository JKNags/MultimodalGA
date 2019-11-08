package niching;

public class GenerationData {
 
	private int timesAdded;
	private double minFitness, 
		avgFitness, 
		maxFitness;
	
	public GenerationData() {
		timesAdded = 0;
		minFitness = 0;
		avgFitness = 0;
		maxFitness = 0;
	}
	
	public void addData(double _minFitness, double _avgFitness, double _maxFitness) {
		timesAdded += 1;
		minFitness += _minFitness;
		avgFitness += _avgFitness;
		maxFitness += _maxFitness;
	}
	
	public String toString() {
		return (avgFitness / timesAdded) + "\t" + (minFitness / timesAdded) + "\t" + (maxFitness / timesAdded);
	}

}
