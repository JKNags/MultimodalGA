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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Genetic extends Application {

    /*
     * Global Variables
     */
	final int bitLength = 32;
	final double maxValue = Math.pow(2, bitLength);
	Random rand = new Random();
	
    /*
     * start
     * Entry point to application
     */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void start(Stage primaryStage) throws Exception {
		// Variables
		DecimalFormat decimalFormat = new DecimalFormat("#0.0000");
		int populationSize, numGenerations;
		double mutationRate;
		
		// Parameters
		Parameters parameters = getParameters();
		populationSize = Integer.parseInt(parameters.getRaw().get(0));
		mutationRate = Double.parseDouble(parameters.getRaw().get(1));
		numGenerations = Integer.parseInt(parameters.getRaw().get(2));
		if (populationSize % 2 != 0) populationSize = populationSize + 1;
		
		// Setup GUI
		primaryStage.setTitle("Line Chart Sample");
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("x");
        yAxis.setLabel("y");
        //creating the chart
        final LineChart<Number,Number> lineChart = 
        		new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setTitle("Genetic");
        lineChart.setAnimated(false);
        
        // Plot Function
        XYChart.Series series = new XYChart.Series();
        series.setName("Function");
        XYChart.Series pointSeries = new XYChart.Series();
        pointSeries.setName("Individuals");
        
        for (double x = 0; x < 1; x += .01) {
        	Data d = new XYChart.Data(x, m1(x));
        	Rectangle rect = new Rectangle(0, 0);
        	rect.setVisible(false);
        	d.setNode(rect);
        	series.getData().add(d);
        	//System.out.println(x + ",  " + m1(x));
        }
        
        lineChart.getData().add(series);
        lineChart.getData().add(pointSeries);   // Add individuals to chart
        Node line = pointSeries.getNode().lookup(".chart-series-line");
        line.setStyle("-fx-stroke: rgba(0.0, 0.0, 0.0, 0.0);");   // Set line as transparent to just show points
        
        ////////////////////
        // Run GA
        ////////////////////
        
        Individual[] population = initPopulation(populationSize);   // Current population
        Individual[] nextGenPopulation = new Individual[populationSize];   // Next generation population
        double[] rouletteWheel;
        Timeline timeline = new Timeline();
        Duration timepoint = Duration.ZERO;
        Duration pause = Duration.millis(10);
        Individual parent1, parent2;
        int parent1Idx, parent2Idx;
        double roll;  // stores random variables
        double value; // Decimal value of child
		int p1, p2;   // crossover points
		KeyFrame keyFrame;
		Data[][] points = new Data[numGenerations][populationSize];
		
        // Print population
        printPopulation(population);
        printStatistics(population);
        
        try {
	        // Reproduce population
	        for (int genIdx = 0; genIdx < numGenerations; genIdx++) {
	        	
	        	rouletteWheel = getRouletteWheel(population);   // Fill Wheel
	        	
	        	// Add points to graph
	        	//timepoint = timepoint.add(pause);
	        	keyFrame = new KeyFrame(timepoint, e -> pointSeries.getData().clear());
	        	timeline.getKeyFrames().add(keyFrame);
	        	
	        	//getDataPoints(population, points);
	        	for (int idx = 0; idx < population.length; idx++) {
	        		Data individualPoint = new XYChart.Data(population[idx].getX(), population[idx].getY());
	        		Circle point = new Circle(4);
	        		individualPoint.setNode(point);
	        		points[genIdx][idx] = individualPoint;
	        	}
	        	//timepoint = timepoint.add(pause);
	        	Data[] genPoints = points[genIdx];
	        	keyFrame = new KeyFrame(timepoint, e -> pointSeries.getData().addAll(genPoints));
	        	timeline.getKeyFrames().add(keyFrame);
	        	timepoint = timepoint.add(pause);
	        	
				// Replace population with children
				for (int childIdx = 0; childIdx < populationSize; childIdx += 2) {
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
					
					// Cross parents into new child
					BitSet childBits1 = new BitSet(bitLength);
					BitSet childBits2 = new BitSet(bitLength);
					
					// Generate two random points
					p1 = rand.nextInt(32);
					p2 = rand.nextInt(32);
					if (p1 > p2) {
						int tmp = p1;
						p1 = p2;
						p2 = tmp;
					}
					
					//System.out.println("\nParent1 (idx=" + parent1Idx + ") = " + parent1.getBits());
					//System.out.println("Parent2 (idx=" + parent2Idx + ") = " + parent2.getBits());
					//System.out.println("P1: " + p1 + ", P2: " + p2);
					
					// First section
					for(int idx = 0; idx < p1; idx++) {
						childBits1.set(idx, parent1.getBits().get(idx));  // C1 <- P1
						childBits2.set(idx, parent2.getBits().get(idx));  // C2 <- P2
					}

					// Second section
					for(int idx = p1; idx < p2; idx++) {
						childBits1.set(idx, parent2.getBits().get(idx));  // C1 <- P2
						childBits2.set(idx, parent1.getBits().get(idx));  // C2 <- P1
					}
					
					// Third section
					for(int idx = p2; idx < bitLength; idx++) {
						childBits1.set(idx, parent1.getBits().get(idx));  // C1 <- P1
						childBits2.set(idx, parent2.getBits().get(idx));  // C2 <- P2
					}
					
					// Mutate children
					for (int idx = 0; idx < bitLength; idx++) {
						roll = rand.nextDouble();
						if (roll < mutationRate)
							childBits1.flip(idx);
						
						roll = rand.nextDouble();
						if (roll < mutationRate)
							childBits2.flip(idx);
					}
					
					// Add children to next generation
					//System.out.println("childBits1: " + childBits1);
					value = childBits1.toLongArray() == null ? 0 : childBits1.toLongArray()[0];
					nextGenPopulation[childIdx] = new Individual(childBits1, value / maxValue, getFitness(value));
					
					//System.out.println("childBits2: " + childBits2);
					value = childBits2.toLongArray() == null ? 0 : childBits2.toLongArray()[0];
					nextGenPopulation[childIdx + 1] = new Individual(childBits2, value / maxValue, getFitness(value));
				}
				
				// Move to next generation
				population = nextGenPopulation;
				
				// Print statistics
				printStatistics(population);
	        }
	
	        //timeline.setOnFinished(e -> printPopulation(population));
	        timeline.play();
		} catch (RuntimeException e ) {
			e.printStackTrace();
		}
        
        // Show Application Window
        Scene scene  = new Scene(lineChart, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void getDataPoints(Individual[] population, Data[] points) {
    	for (int idx = 0; idx < population.length; idx++) {
    		Data individualPoint = new XYChart.Data(population[idx].getX(), population[idx].getY());
    		Circle point = new Circle(4);
    		individualPoint.setNode(point);
    		points[idx] = individualPoint;
    	}
	}
	
	void printStatistics(Individual[] population) {
		double sumFitness = 0, minFitness = Float.MAX_VALUE,
				maxFitness = Float.MIN_VALUE, fitness;
		for (int idx = 0; idx < population.length; idx++) {
			fitness = population[idx].getY();
			sumFitness += fitness;
			if (fitness < minFitness) minFitness = fitness;
			if (fitness > maxFitness) maxFitness = fitness;
		}
		//System.out.println("Avg Fit (" + (sumFitness / population.length) + "), Min Fit (" + minFitness + "), Max Fitness (" + maxFitness + ")");
		System.out.println((sumFitness / population.length) + "\t" + minFitness + "\t" + maxFitness);
	}
	
	void printPopulation(Individual[] population) {
        for (int idx = 0; idx < population.length; idx++) {
        	System.out.println(idx + ": " + population[idx].getX() + " = " +  population[idx].getY());
        }
	}
	
	/*
	 * Get Fitness
	 * Return fitness [0:1] of individual for a certain function
	 */
	double getFitness(double value) {
		return m1(value / maxValue);
	}
	
	double getFitness(Individual individual) {
		return m1(individual.getX());
	}
	
	/*
	 * Fitness Functions
	 */
    double m1(double x) {
    	return Math.pow(Math.sin(5 * Math.PI * x), 6);
    }
    
    double m4(double x) {
    	return Math.pow(Math.E, -2 * Math.log(2) * Math.pow((x - 0.08) / 0.854, 2))
    			* Math.pow(Math.sin(5 * Math.PI * (Math.pow(x, 0.75) - 0.05)), 6);
    }

	/*
	 * Get Roulette Wheel
	 */
	double[] getRouletteWheel(Individual[] population) {
		int size = population.length;
        double runningTotal = 0, total = 0;
		double[] wheel = new double[size];
        double[] fitnessInverses = new double[size];

        for (int idx = 0; idx < size; idx++) {
        	fitnessInverses[idx] = population[idx].getY();
        	total += population[idx].getY();
        }
        
        for (int idx = 0; idx < size; idx++) {
        	runningTotal += fitnessInverses[idx];
        	wheel[idx] = runningTotal / total;
        }
        
        return wheel;
	}
    
    /*
     * Initialize Population
     * Returns BitSet array for population
     */
    Individual[] initPopulation(int populationSize) {
		final double top = Math.pow(2, bitLength); 
		Individual[] population = new Individual[populationSize];
		
		for (int idx = 0; idx < populationSize; idx++) {
			BitSet bits = new BitSet(bitLength);   // initializes to all 0
			for (int i = 0; i < bitLength; i++)
			 	if (rand.nextDouble() < 0.5) bits.flip(i);   // randomly flip bits
			
			long value = bits.toLongArray() == null ? 0 : bits.toLongArray()[0];
			population[idx] = new Individual(bits, value / maxValue, getFitness(value));
		}
		
		return population;
    }
}
