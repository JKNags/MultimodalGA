package niching;

import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Genetic extends Application {

    /*
     * Global Variables
     */
	final int bitLength = 32;   // Amount of bits to represent the chromosomes
	final double maxValue = Math.pow(2, bitLength);   // Max value for the given bit length
	final double minRange = 0;   // Range of chromosomes - depends on function
	final double maxRange = 1;
	final double range = Math.abs(maxRange - minRange);
	final double SHARED_SIGMA = 0.1;   // niching radius
	final double SHARED_ALPHA = 6;   // adjusts fitness decrease if in radius
	final double SEQUENTIAL_SIGMA = 2;   // niching radius
	final double SEQUENTIAL_ALPHA = 0.75;   // amount to decrease fitness if in radius
	final boolean showTimeline = false;
	final int numRuns = 1;   // For experimentation to collect averages
	final int functionNumber = 1;  // 1,4,6
	final boolean useY = functionNumber == 6;  // Only M6 uses y
	Random rand = new Random();
	
    /*
     * Start
     * Entry point to application using JavaFX
     */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void start(Stage primaryStage) throws Exception {
		// Variables
		DecimalFormat decimalFormat = new DecimalFormat("#0.0000");
		int populationSize = 100;
		float mutationRate = 0.02f;
		float crossoverRate = 0.9f;
		int crossoverIndividuals = (int) (populationSize * crossoverRate);
		int numGenerations = 500;
		
		// These must be even
		if (populationSize % 2 != 0) populationSize = populationSize + 1;
		if (crossoverIndividuals % 2 != 0) crossoverIndividuals = crossoverIndividuals + 1;

		// Niching methods - either sharing or sequential
		boolean useSharing = false;
		int sequentialRuns = 1;
		boolean useSequential = sequentialRuns > 1;
		
		// Setup GUI
		primaryStage.setTitle("Genetic Algorithm");
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("x");
        yAxis.setLabel("Fitness");
        //creating the chart
        final LineChart<Number,Number> lineChart = 
        		new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setTitle("Population=" + populationSize + ", Generations=" + numGenerations);
        lineChart.setAnimated(false);
        
        // Plot Function
        XYChart.Series series = new XYChart.Series();
        series.setName("Function");
        XYChart.Series pointSeries = new XYChart.Series();
        pointSeries.setName("Individuals");
        
        for (double x = 0; x <= 1; x += .01) {
        	Data d = new XYChart.Data(x, calculateFitness(x, 0));
        	Rectangle rect = new Rectangle(0, 0);
        	rect.setVisible(false);
        	d.setNode(rect);
        	series.getData().add(d);
        }
        
        lineChart.getData().add(series);
        lineChart.getData().add(pointSeries);   // Add individuals to chart
        Node line = pointSeries.getNode().lookup(".chart-series-line");
        line.setStyle("-fx-stroke: rgba(0.0, 0.0, 0.0, 0.0);");   // Set line as transparent to just show points
        
        ////////////////////
        // Run GA
        ////////////////////
        
        double[] sequentialSolutionsX = new double[sequentialRuns];
        double[] sequentialSolutionsY = new double[sequentialRuns];
        
        // Stores avg, min, and max fitness for each generation to calculate averages across runs
        GenerationData[] generationData = new GenerationData[numGenerations+1];
        for (int idx = 0; idx < generationData.length; idx++) {
        	generationData[idx] = new GenerationData();
        }
        
        for (int runIdx = 0; runIdx < numRuns; runIdx++) {   // Loop for experiment
        	
        	for (int sequenceIdx = 0; sequenceIdx < sequentialRuns; sequenceIdx++) {   // Loop for sequential niching
        		
            	KeyFrame keyFrame;
        		Data[][] points = new Data[numGenerations][populationSize];
                Individual[] population = initPopulation(populationSize, useSharing, useSequential, sequentialSolutionsX, sequentialSolutionsY);   // Current population
                Individual[] nextGenPopulation = population; //new Individual[populationSize];   // Next generation population
                double[] rouletteWheel;
                Timeline timeline = new Timeline();
                Duration timepoint = Duration.ZERO;
                Duration pause = Duration.millis(5);
                Individual parent1, parent2;
                int parent1Idx, parent2Idx;
                double roll;  // stores random variables
                double value; // Decimal value of child
        		int xP1, xP2, yP1, yP2;   // crossover points
        		double childXValue1, childYValue1, childX1, childY1;
        		double childXValue2, childYValue2, childX2, childY2;
        		
        		// For plotting individuals
                double colorMin = 0.2;
                double colorMax = 1;
                double colorRange = colorMax - colorMin;
                double colorStep = colorRange / (sequentialRuns - 1);   
                    
                // Print population
                //printPopulation(population);
                //printStatistics(population, 0);
                storeStatistics(population, generationData, 0);
                
                try {
        	        
                	// Reproduce population
        	        for (int genIdx = 0; genIdx < numGenerations; genIdx++) {
        	        	
        	        	rouletteWheel = getRouletteWheel(population);   // Fill Wheel
        	        	
        	        	// Add points to graph
        	        	if (showTimeline) {
        		        	keyFrame = new KeyFrame(timepoint, e -> pointSeries.getData().clear());
        		        	timeline.getKeyFrames().add(keyFrame);
        		        	
        		        	for (int idx = 0; idx < population.length; idx++) {
        		        		Data individualPoint = new XYChart.Data(population[idx].getX(), population[idx].getActualFitness());
        		        		Circle point = new Circle(5);
        		        		individualPoint.setNode(point);
        		        		points[genIdx][idx] = individualPoint;
        		        	}
        		        	Data[] genPoints = points[genIdx];
        		        	keyFrame = new KeyFrame(timepoint, e -> pointSeries.getData().addAll(genPoints));
        		        	timeline.getKeyFrames().add(keyFrame);
        		        	timepoint = timepoint.add(pause);
        	        	}
        	        	
        				// Replace crossoverIndividuals in population with children
        	        	int startingIndividual = (populationSize - crossoverIndividuals) < 1 ? 0 : rand.nextInt(populationSize - crossoverIndividuals) + 1;
        				for (int childIdx = startingIndividual; childIdx < (startingIndividual + crossoverIndividuals); childIdx += 2) {
        					parent1Idx = parent2Idx = 0;
        					
        					// Choose first parent
        					roll = rand.nextDouble();
        					do {
        						if (roll <= rouletteWheel[parent1Idx]) break;
        						parent1Idx++;
        					} while (parent1Idx < populationSize);
        					parent1 = population[parent1Idx];
        					
        					// Choose second parent
        					roll = rand.nextDouble();
        					do {
        						if (roll <= rouletteWheel[parent2Idx]) break;
        						parent2Idx++;
        					} while(parent2Idx < populationSize);
        					if (parent1Idx == parent2Idx)   // Choose the next one if same
        						parent2Idx = (parent2Idx >= populationSize - 1) ? 0 : parent2Idx + 1;
        					parent2 = population[parent2Idx];
        					
        					///////////////////////////////
        					// Cross parents into new child
        					///////////////////////////////
        					
        					BitSet childXBits1 = new BitSet(bitLength);
        					BitSet childYBits1 = new BitSet(bitLength);
        					BitSet childXBits2 = new BitSet(bitLength);
        					BitSet childYBits2 = new BitSet(bitLength);
        					
        					// Crossover X
        					
        					// Generate two random points
        					xP1 = rand.nextInt(32);
        					xP2 = rand.nextInt(32);
        					if (xP1 > xP2) {
        						int tmp = xP1;
        						xP1 = xP2;
        						xP2 = tmp;
        					}
        				
        					// First section
        					for(int idx = 0; idx < xP1; idx++) {
        						childXBits1.set(idx, parent1.getXBits().get(idx));  // C1 <- P1
        						childXBits2.set(idx, parent2.getXBits().get(idx));  // C2 <- P2
        					}

        					// Second section
        					for(int idx = xP1; idx < xP2; idx++) {
        						childXBits1.set(idx, parent2.getXBits().get(idx));  // C1 <- P2
        						childXBits2.set(idx, parent1.getXBits().get(idx));  // C2 <- P1
        					}
        					
        					// Third section
        					for(int idx = xP2; idx < bitLength; idx++) {
        						childXBits1.set(idx, parent1.getXBits().get(idx));  // C1 <- P1
        						childXBits2.set(idx, parent2.getXBits().get(idx));  // C2 <- P2
        					}
        					
        					// Mutate children
        					for (int idx = 0; idx < bitLength; idx++) {
        						roll = rand.nextDouble();
        						if (roll < mutationRate)
        							childXBits1.flip(idx);
        						
        						roll = rand.nextDouble();
        						if (roll < mutationRate)
        							childXBits2.flip(idx);
        					}
        					
        					childXValue1 = childXBits1.toLongArray() == null ? 0 : childXBits1.toLongArray()[0];
        					childXValue2 = childXBits2.toLongArray() == null ? 0 : childXBits2.toLongArray()[0];
        					childX1 = translateValue(childXValue1);
        					childX2 = translateValue(childXValue2);
        					
        					// Crossover Y
        					
        					// Generate two random points
        					yP1 = rand.nextInt(32);
        					yP2 = rand.nextInt(32);
        					if (yP1 > yP2) {
        						int tmp = yP1;
        						yP1 = yP2;
        						yP2 = tmp;
        					}
        				
        					// First section
        					for(int idx = 0; idx < yP1; idx++) {
        						childYBits1.set(idx, parent1.getYBits().get(idx));  // C1 <- P1
        						childYBits2.set(idx, parent2.getYBits().get(idx));  // C2 <- P2
        					}

        					// Second section
        					for(int idx = yP1; idx < yP2; idx++) {
        						childYBits1.set(idx, parent2.getYBits().get(idx));  // C1 <- P2
        						childYBits2.set(idx, parent1.getYBits().get(idx));  // C2 <- P1
        					}
        					
        					// Third section
        					for(int idx = yP2; idx < bitLength; idx++) {
        						childYBits1.set(idx, parent1.getYBits().get(idx));  // C1 <- P1
        						childYBits2.set(idx, parent2.getYBits().get(idx));  // C2 <- P2
        					}
        					
        					// Mutate children
        					for (int idx = 0; idx < bitLength; idx++) {
        						roll = rand.nextDouble();
        						if (roll < mutationRate)
        							childYBits1.flip(idx);
        						
        						roll = rand.nextDouble();
        						if (roll < mutationRate)
        							childYBits2.flip(idx);
        					}
        					
        					childYValue1 = childYBits1.toLongArray() == null ? 0 : childYBits1.toLongArray()[0];
        					childYValue2 = childYBits2.toLongArray() == null ? 0 : childYBits2.toLongArray()[0];
        					childY1 = translateValue(childYValue1);
        					childY2 = translateValue(childYValue2);
        					
        					// Add children to next generation
        					nextGenPopulation[childIdx] = new Individual(childXBits1, childYBits1, childX1, childY1, calculateFitness(childX1, childY1));
        					nextGenPopulation[childIdx + 1] = new Individual(childXBits2, childYBits2, childX2, childY2, calculateFitness(childX2, childY2));
        				
        				}   // End crossover
        				
        				// Assign fitness if sharing is used
        				if (useSharing)
        					for (int idx = 0; idx < populationSize; idx++)
        					{
        						nextGenPopulation[idx].setEffectiveFitness(calculateFitnessSharing(nextGenPopulation[idx], nextGenPopulation));
        					}
        				if (useSequential)
        					for (int idx = 0; idx < populationSize; idx++)
        					{
        						population[idx].setEffectiveFitness(calculateFitnessSequential(nextGenPopulation[idx], sequentialSolutionsX, sequentialSolutionsY));
        					}
        				
        				// Move to next generation
        				population = nextGenPopulation;
        				
        				// Print statistics
        				//printStatistics(population, genIdx+1);
        				storeStatistics(population, generationData, genIdx+1);
        				     				
        	        }   // End generations
        	
        	        if (showTimeline) {
        	        	//timeline.setOnFinished(e -> printPopulation(population));
        	        	timeline.play();
        	        } else { //if (sequentialRuns == 1) {
        	        	for (int idx = 0; idx < population.length; idx++) {
        	        		Data individualPoint = new XYChart.Data(population[idx].getX(), population[idx].getActualFitness());
        	        		Circle point = new Circle(5);
        	            	double colorValue = colorMin + colorStep * sequenceIdx;
        	            	point.setFill(new Color(colorValue, 0, colorValue, 1));
        	        		individualPoint.setNode(point);
        	        		pointSeries.getData().add(individualPoint);
        	        	}
        	        }
        		} catch (RuntimeException e ) {
        			e.printStackTrace();
        		}
                
                // For sequential - find max fitness and get X and Y
            	Individual apex = null;
            	double maxFitness = Double.MIN_VALUE;
            	
            	for (int idx = 0; idx < populationSize; idx++) {
            		if (population[idx].getActualFitness() > maxFitness)
            			apex = population[idx];
            	}

            	sequentialSolutionsX[sequenceIdx] = apex.getX();
            	sequentialSolutionsY[sequenceIdx] = apex.getY();
                
            }    // End sequence loop
        }   // End experiment runs   
        
        // Output run data
        for (int generationIdx = 0; generationIdx < generationData.length; generationIdx++) {
        	System.out.println(generationIdx + "\t" + generationData[generationIdx]);
        }
   
        // Show Application Window
        Scene scene  = new Scene(lineChart, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
	}
	
	/*
	 * Translate Range
	 * Return value [0:1] translated within given range 
	 */
	double translateValue(double v) {
		return (v / maxValue) * range + minRange;
	}
	
	/*
	 * Get Fitness
	 * Return fitness of individual for a certain function
	 */
	double calculateFitness(double x, double y) {
		switch (functionNumber) {
			case 1: return m1(x);
			case 4: return m4(x);
			case 6: return m6(x,y);
		}
		return m1(x);
	}
	
	/*
	 * Get Fitness Sharing
	 * Returns fitness using the niching algorithm Sharing
	 * Depresses fitness for a greater amount of neighboring individuals
	 */
	double calculateFitnessSharing(Individual individual, Individual[] population) {
		double denominator = 0, distanceSquared;
		double sigmaSquared = Math.pow(SHARED_SIGMA, 2);

		for (int idx = 0; idx < population.length; idx++) {
			distanceSquared = Math.pow(population[idx].getX() - individual.getX(), 2) + Math.pow(population[idx].getY() - individual.getY(), 2);
			if (distanceSquared < sigmaSquared) 
				denominator += 1 - Math.pow(distanceSquared / sigmaSquared, SHARED_ALPHA / 2);
		}
		
		if (denominator == 0)
			denominator = 1;
		return individual.getActualFitness() / denominator;
	}
	
	/*
	 * Get Fitness Sequential
	 * Returns fitness using the niching algorithm Sequential
	 * Depresses fitness if individual near a prior solution
	 */
	double calculateFitnessSequential(Individual individual, double[] sequentialSolutionsX, double[] sequentialSolutionsY) {
		double fitness = individual.getActualFitness();
		double X, Y;
		
		for (int idx = 0; idx < sequentialSolutionsX.length; idx++) {
			X = sequentialSolutionsX[idx];
			Y = sequentialSolutionsY[idx];
			
			// Depress if near X solution
			if (individual.getX() > (X - SEQUENTIAL_SIGMA)
					&& individual.getX() < (X + SEQUENTIAL_SIGMA)) {
				fitness *= SEQUENTIAL_ALPHA;
			}
			
			// Depress if near Y solution
			if (useY && individual.getY() > (Y - SEQUENTIAL_SIGMA)
					&& individual.getY() < (Y + SEQUENTIAL_SIGMA)) {
				fitness *= SEQUENTIAL_ALPHA;
			}
		}
		
		return fitness;
	}
	
	/*
	 * Fitness Functions
	 * Should only be called by getFitness()
	 */
    double m1(double x) {
    	return Math.pow(Math.sin(5 * Math.PI * x), 6);
    }
    
    double m4(double x) {
    	return Math.pow(Math.E, -2 * Math.log(2) * Math.pow((x - 0.08) / 0.854, 2))
    			* Math.pow(Math.sin(5 * Math.PI * (Math.pow(x, 0.75) - 0.05)), 6);
    }
    
    double m6(double x, double y) {
    	double sumValue = 0;
    	for (int i = 0; i <= 24; i++) {
    		sumValue += 1 / ( 1 + i + Math.pow(x - 16 *((i % 5) - 2), 6) + Math.pow(y - 16 * (Math.floorDiv(i, 5) - 2), 6) );
    	}
    	return 500 - 1 / (0.002 + sumValue);
    }

	/*
	 * Get Roulette Wheel
	 * Return cumulative distribution weighted by fitness
	 */
	double[] getRouletteWheel(Individual[] population) {
		int size = population.length;
        double runningTotal = 0, sumFitness = 0;
		double[] wheel = new double[size];

        for (int idx = 0; idx < size; idx++) {
        	sumFitness += population[idx].getEffectiveFitness();
        }
        
        for (int idx = 0; idx < size; idx++) {
        	runningTotal += population[idx].getEffectiveFitness();
        	wheel[idx] = runningTotal / sumFitness;
        }
        
        return wheel;
	}
    
    /*
     * Initialize Population
     * Returns BitSet array for population
     */
    Individual[] initPopulation(int populationSize, boolean useSharing, boolean useSequential, double[] sequentialSolutionsX, double[] sequentialSolutionsY) {
		final double top = Math.pow(2, bitLength); 
		Individual[] population = new Individual[populationSize];
		double xValue, yValue, x, y;
		
		for (int idx = 0; idx < populationSize; idx++) {
			BitSet xBits = new BitSet(bitLength);   // initializes to all 0
			BitSet yBits = new BitSet(bitLength);
			for (int i = 0; i < bitLength; i++) {
			 	if (rand.nextDouble() < 0.5) xBits.flip(i);   // randomly flip bits
			 	if (rand.nextDouble() < 0.5) yBits.flip(i);
			}
			xValue = xBits.toLongArray() == null ? 0 : xBits.toLongArray()[0];
			yValue = yBits.toLongArray() == null ? 0 : yBits.toLongArray()[0];
			x = translateValue(xValue);
			y = useY ? translateValue(yValue) : 0;
			
			// Create individual
			population[idx] = new Individual(xBits, yBits, x, y, calculateFitness(x, y));
		}
		
		// Assign fitness if sharing is used
		if (useSharing)
			for (int idx = 0; idx < populationSize; idx++)
			{
				population[idx].setEffectiveFitness(calculateFitnessSharing(population[idx], population));
			}
		if (useSequential)
			for (int idx = 0; idx < populationSize; idx++)
			{
				population[idx].setEffectiveFitness(calculateFitnessSequential(population[idx], sequentialSolutionsX, sequentialSolutionsY));
			}
		
		return population;
    }
    
	/*
	 * Print Statistics
	 * Output Average, Min, and Max fitness of population
	 */
	void printStatistics(Individual[] population, int generationIdx) {
		double sumFitness = 0, minFitness = Float.MAX_VALUE,
				maxFitness = Float.MIN_VALUE, fitness;
		for (int idx = 0; idx < population.length; idx++) {
			fitness = population[idx].getActualFitness();
			sumFitness += fitness;
			if (fitness < minFitness) minFitness = fitness;
			if (fitness > maxFitness) maxFitness = fitness;
		}
		//System.out.println("Avg Fit (" + (sumFitness / population.length) + "), Min Fit (" + minFitness + "), Max Fitness (" + maxFitness + ")");
		System.out.println(generationIdx + "\t" + (sumFitness / population.length) + "\t" + minFitness + "\t" + maxFitness);
	}
	
	/*
	 * Store Statistics
	 * Store Average, Min, and Max fitness of population into array
	 */
	void storeStatistics(Individual[] population, GenerationData[] generationData, int generationIdx) {
		double sumFitness = 0, minFitness = Float.MAX_VALUE,
				maxFitness = Float.MIN_VALUE, fitness;
		for (int idx = 0; idx < population.length; idx++) {
			fitness = population[idx].getActualFitness();
			sumFitness += fitness;
			if (fitness < minFitness) minFitness = fitness;
			if (fitness > maxFitness) maxFitness = fitness;
		}
		
		generationData[generationIdx].addData(minFitness, (sumFitness / population.length), maxFitness);
	}
	
	/*
	 * Print Population
	 * Output coordinates and fitness of population
	 */
	void printPopulation(Individual[] population) {
        for (int idx = 0; idx < population.length; idx++) {
        	System.out.println(idx + ": (" + population[idx].getX() + ", " +  population[idx].getY() 
        			+ ") = " + population[idx].getActualFitness());
        }
	}
}
