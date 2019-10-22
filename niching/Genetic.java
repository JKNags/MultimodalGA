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
	final int range = 1; 
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
        
        for (double x = 0; x <= 1; x += .01) {
        	Data d = new XYChart.Data(x, getFitness(x, 0));
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
        
		KeyFrame keyFrame;
		Data[][] points = new Data[numGenerations][populationSize];
        Individual[] population = initPopulation(populationSize);   // Current population
        Individual[] nextGenPopulation = new Individual[populationSize];   // Next generation population
        double[] rouletteWheel;
        Timeline timeline = new Timeline();
        Duration timepoint = Duration.ZERO;
        Duration pause = Duration.millis(50);
        Individual parent1, parent2;
        int parent1Idx, parent2Idx;
        double roll;  // stores random variables
        double value; // Decimal value of child
		int xP1, xP2, yP1, yP2;   // crossover points
		
		// Child 1 Variables
		BitSet childXBits1 = new BitSet(bitLength);
		BitSet childYBits1 = new BitSet(bitLength);
		double childXValue1, childYValue1, childX1, childY1;
		
		// Child 2 Variables
		BitSet childXBits2 = new BitSet(bitLength);
		BitSet childYBits2 = new BitSet(bitLength);
		double childXValue2, childYValue2, childX2, childY2;
		
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
	        	
	        	for (int idx = 0; idx < population.length; idx++) {
	        		Data individualPoint = new XYChart.Data(population[idx].getX(), population[idx].getFitness());
	        		Circle point = new Circle(5);
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
					
					///////////////////////////////
					// Cross parents into new child
					///////////////////////////////
					
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
						childXBits1.set(idx, parent2.getXBits().get(idx));  // C2 <- P2
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
					childX1 = childXValue1 / maxValue * range;
					childX2 = childXValue2 / maxValue * range;
					
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
						childYBits1.set(idx, parent2.getYBits().get(idx));  // C2 <- P2
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
					childY1 = childYValue1 / maxValue * range;
					childY2 = childYValue2 / maxValue * range;
					
					// Add children to next generation
					nextGenPopulation[childIdx] = new Individual(childXBits1, childYBits1, childX1, childY1, getFitness(childX1, childY1));
					nextGenPopulation[childIdx + 1] = new Individual(childXBits2, childYBits2, childX2, childY2, getFitness(childX2, childY2));
				}
				
				// Move to next generation
				population = nextGenPopulation;
				
				// Print statistics
				printStatistics(population);
				//printPopulation(population);
				//System.out.println("\n");
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
	
	/*
	 * Print Statistics
	 * Output Average, Min, and Max fitness of population
	 */
	void printStatistics(Individual[] population) {
		double sumFitness = 0, minFitness = Float.MAX_VALUE,
				maxFitness = Float.MIN_VALUE, fitness;
		for (int idx = 0; idx < population.length; idx++) {
			fitness = population[idx].getFitness();
			sumFitness += fitness;
			if (fitness < minFitness) minFitness = fitness;
			if (fitness > maxFitness) maxFitness = fitness;
		}
		//System.out.println("Avg Fit (" + (sumFitness / population.length) + "), Min Fit (" + minFitness + "), Max Fitness (" + maxFitness + ")");
		System.out.println((sumFitness / population.length) + "\t" + minFitness + "\t" + maxFitness);
	}
	
	/*
	 * Print Population
	 * Output coordinates and fitness of population
	 */
	void printPopulation(Individual[] population) {
        for (int idx = 0; idx < population.length; idx++) {
        	System.out.println(idx + ": (" + population[idx].getX() + ", " +  population[idx].getY() 
        			+ ") = " + population[idx].getFitness());
        }
	}
	
	/*
	 * Get Fitness
	 * Return fitness of individual for a certain function
	 */
	double getFitness(double x, double y) {
		return m1(x);
		//return m4(x);
		//return m6(x, y);
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
    
    double m6(double x, double y) {
    	double sumValue = 0;
    	for (int i = 0; i <= 24; i++) {
    		sumValue += 1 / ( 1 + i + Math.pow(x - 16 *((i % 5) - 2), 6) + Math.pow(y - 16 * (Math.floorDiv(i, 5) - 2), 6) );
    	}
    	return 500 - 1 / (0.002 + sumValue);
    }

	/*
	 * Get Roulette Wheel
	 */
	double[] getRouletteWheel(Individual[] population) {
		int size = population.length;
        double runningTotal = 0, sumFitness = 0;
		double[] wheel = new double[size];

        for (int idx = 0; idx < size; idx++) {
        	sumFitness += population[idx].getFitness();
        }
        
        for (int idx = 0; idx < size; idx++) {
        	runningTotal += population[idx].getFitness();
        	wheel[idx] = runningTotal / sumFitness;
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
			x = xValue / maxValue * range;
			y = yValue / maxValue * range;
			population[idx] = new Individual(xBits, yBits, x, y, getFitness(x, y));
		}
		
		return population;
    }
}
