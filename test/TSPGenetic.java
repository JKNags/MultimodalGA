package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Scanner;
import java.text.DecimalFormat;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class TSPGenetic extends Application {

	/*******************************
	 *  Global Variables
	 *******************************/
	static String folderName = "./src/ProjectFolder/";  // Configure to your tsp file location
	Pane paneCities;   // Pane to hold city dots
	Pane paneArrows;   // Pane to hold arrows between cities
	TSPCity[] cityList;   // List of all city objects
	double[][] cityDistances;   // Distance lookup table
	ListView<String> lvFileNames;
	TextField tfMutationRate;
	TextField tfNumGenerations;
	TextField tfElitism;
	TextField tfDistance;
	ToggleGroup rbCrossoverGroup;
	boolean reset = false;
	DecimalFormat decimalFormat = new DecimalFormat("#0.00");
	
	/*******************************
	 *  Main Functions
	 *******************************/
	public static void main(String[] args) {	
		launch(args);
	}
	
	public void start(Stage primaryStage)  {	
		// Create the control buttons
		Button btnStart = new Button("Start");
		btnStart.setOnAction(this::btnStartOnAction);
		btnStart.setLayoutX(20);
		btnStart.setLayoutY(415);
		
		Button btnTest = new Button("Test 10");
		btnTest.setOnAction(this::btnTestOnAction);
		btnTest.setLayoutX(80);
		btnTest.setLayoutY(415);
		
		// Set pane of cities
		paneCities = new Pane();
		paneCities.setTranslateX(150);
		paneCities.setTranslateY(10);
		paneCities.setPrefSize(840, 775);
		paneCities.setStyle("-fx-background-color: LIGHTGRAY");
		
		// Set pane of arrows between cities
		paneArrows = new Pane();
		paneArrows.setTranslateX(150);
		paneArrows.setTranslateY(10);
		paneArrows.setPrefSize(840, 775);
		paneArrows.setOpacity(50);
		
		// Create axis of graphs for visualization
		Line lineXMarker = new Line();
		lineXMarker.setStyle("-fx-stroke: DARKGRAY;");
		Line lineYMarker = new Line();
		lineYMarker.setStyle("-fx-stroke: DARKGRAY;");
		paneCities.toBack();
		
		// Set list view controls
		lvFileNames = new ListView<String>(getFileNames());
		lvFileNames.setTranslateX(10);
		lvFileNames.setTranslateY(10);
		lvFileNames.setMaxHeight(430);
		lvFileNames.setPrefHeight(400);
		lvFileNames.setMinWidth(130);
		lvFileNames.setMaxWidth(130);
		
		// Text field for mutation rate
		Label lblMutationRate = new Label("Mutation Rate (%)");
		lblMutationRate.setLayoutX(10);
		lblMutationRate.setLayoutY(450);
		tfMutationRate = new TextField("1");
		tfMutationRate.setLayoutX(10);
		tfMutationRate.setLayoutY(470);
		tfMutationRate.setMinWidth(130);
		tfMutationRate.setMaxWidth(130);
		
		// Text field for number for number of generations
		Label lblNumGenerations = new Label("# Generations");
		lblNumGenerations.setLayoutX(10);
		lblNumGenerations.setLayoutY(510);
		tfNumGenerations = new TextField("1000");
		tfNumGenerations.setLayoutX(10);
		tfNumGenerations.setLayoutY(530);
		tfNumGenerations.setMinWidth(130);
		tfNumGenerations.setMaxWidth(130);
		
		// Text field for elitism
		Label lblElitism = new Label("Elitism (%)");
		lblElitism.setLayoutX(10);
		lblElitism.setLayoutY(570);
		tfElitism  = new TextField("10");
		tfElitism.setLayoutX(10);
		tfElitism.setLayoutY(590);
		tfElitism.setMinWidth(130);
		tfElitism.setMaxWidth(130);
		
		// Radio buttons for crossover method
		Label lblCrossover = new Label("Crossover");
		lblCrossover.setLayoutX(10);
		lblCrossover.setLayoutY(630);   ///////  
		rbCrossoverGroup = new ToggleGroup();
		RadioButton rbMPX = new RadioButton("MPX");
		RadioButton rbSCX = new RadioButton("SCX");
		rbMPX.setToggleGroup(rbCrossoverGroup);
		rbMPX.setLayoutX(10);
		rbMPX.setLayoutY(650); //////
		rbSCX.setToggleGroup(rbCrossoverGroup);
		rbSCX.setLayoutX(80);
		rbSCX.setLayoutY(650); ////////
		rbSCX.setSelected(true);
		
		// Text field for result
		tfDistance = new TextField();
		tfDistance.setLayoutX(10);
		tfDistance.setLayoutY(700);
		tfDistance.setMinWidth(130);
		tfDistance.setMaxWidth(130);
		tfDistance.setEditable(false);
		
		// Add elements to scene
        Group root = new Group(btnStart, lvFileNames, paneCities, paneArrows, lblNumGenerations, tfNumGenerations, lblElitism, tfElitism,
        		lineXMarker, lineYMarker, lblMutationRate, tfMutationRate, lblCrossover, rbMPX, rbSCX, btnTest, tfDistance);  
        Scene scene = new Scene(root, 1000, 800);
        
        primaryStage.setTitle("Genetic TSP");
        primaryStage.setScene(scene);
        primaryStage.show(); 
        
        // Show first city in list
		lvFileNames.getSelectionModel().selectedItemProperty().addListener(
                this::lvFileNamesSelector);
		lvFileNames.getSelectionModel().select(0);
		
		// Setting the axes position must be done after pane is initialized
		lineXMarker.setStartX(paneCities.getWidth() / 2 + paneCities.getTranslateX());
		lineXMarker.setEndX(paneCities.getWidth() / 2 + paneCities.getTranslateX());
		lineXMarker.setStartY(paneCities.getTranslateY() + paneCities.getHeight());
		lineXMarker.setEndY(paneCities.getTranslateY() + paneCities.getHeight() - paneCities.getHeight());
		
		lineYMarker.setStartX(paneCities.getTranslateX());
		lineYMarker.setEndX(paneCities.getTranslateX() + paneCities.getWidth());
		lineYMarker.setStartY(paneCities.getHeight() / 2 + paneCities.getTranslateY());
		lineYMarker.setEndY(paneCities.getHeight() / 2 + paneCities.getTranslateY());
	
	}
	
	/*******************************
	 *  Listeners
	 *******************************/
	
	// When start button is clicked, perform search
	private void btnStartOnAction(ActionEvent event) {
		// Test to reset graph
		if (reset = true) {
	    	File file = new File(folderName + lvFileNames.getSelectionModel().getSelectedItem());
	    	getCitiesFromFile(file);
			reset = false;
		}
		
		double populationPct = 1.0;
		tfDistance.setText(" ");
		
		// Perform Search
		long startTime = System.nanoTime();
		geneticSearch(true, populationPct, Integer.parseInt(tfNumGenerations.getText()), ((RadioButton) rbCrossoverGroup.getSelectedToggle()).getText(), 
				Double.parseDouble(tfMutationRate.getText()) / 100.0, Double.parseDouble(tfElitism.getText()) / 100.0);
		long stopTime = System.nanoTime();
		//System.out.println("Duration:  " + ((int)((stopTime - startTime) / (Math.pow(10,7))))/Math.pow(10,2) + "s,  " + (stopTime - startTime) + "ns");
		//System.out.println("Duration=" + (stopTime - startTime));
		
		System.out.println("Duration:  " + ((int)((stopTime - startTime) / (Math.pow(10,7))))/Math.pow(10,2) + "s");
	}
	
	// When test button is clicked, perform search on 10
	private void btnTestOnAction(ActionEvent event) {
		// Test to reset graph
		if (reset = true) {
	    	File file = new File(folderName + lvFileNames.getSelectionModel().getSelectedItem());
	    	getCitiesFromFile(file);
			reset = false;
		}
		
		double populationPct = 1.0;
		tfDistance.setText(" ");
		
		System.out.println("------------------------------------------------------");
		
		// Perform 10 Searches
		for (int testNum = 0; testNum < 10; testNum++) {
			long startTime = System.nanoTime();
			System.out.print(testNum + ":  ");
			geneticSearch(false, populationPct, Integer.parseInt(tfNumGenerations.getText()), ((RadioButton) rbCrossoverGroup.getSelectedToggle()).getText(), 
					Double.parseDouble(tfMutationRate.getText()) / 100.0, Double.parseDouble(tfElitism.getText()) / 100.0);
			long stopTime = System.nanoTime();
			//System.out.println("Duration:  " + ((int)((stopTime - startTime) / (Math.pow(10,7))))/Math.pow(10,2) + "s,  " + (stopTime - startTime) + "ns");
			System.out.println((stopTime - startTime));
		}
	}

	// When a new file in list view is selected, update graph
    public void lvFileNamesSelector(ObservableValue<? extends String> val, String oldValue, String newValue)
    {
    	File file = new File(folderName + newValue);
    	reset = true;
    	getCitiesFromFile(file);
    }
    
	/*******************************
	 *  Helper Functions
	 *******************************/
	
	// Return all .tsp files in folder in a format compatible for a ListView
	public ObservableList<String> getFileNames() {
		File folder = new File(folderName);
		File[] fileList = folder.listFiles();
		ArrayList<String> strFileList = new ArrayList<String>();
		String[] strSortedFileList = null;
		ObservableList<String> obFileList = FXCollections.observableArrayList();
		
		try {
			for (File file : fileList) {
				if (file.getName().substring(file.getName().length() - 4, file.getName().length()).equals(".tsp")) {
					strFileList.add(file.getName());
				}
			}
			
			// Sort files by path/name
			strSortedFileList = strFileList.toArray(new String[strFileList.size()]);
			Arrays.sort(strSortedFileList, (f1, f2) -> {
				return f1.compareTo(f2);
			});
			
	        obFileList.addAll(strSortedFileList);
	        
		} catch (NullPointerException e) {
			System.err.println("Error: " + e.getMessage());
		}
		
		return obFileList;
	}
	
	// Genetic Algorithm solution to the TSP
	public void geneticSearch(boolean displayGraphics, double populationRatio, int numGenerations, String crossover, double mutationRate, double elitismRatio) {
		int numCities = cityList.length;
		int populationSize, parent1Idx, parent2Idx, numElite;
		double roll;
		double[] rouletteWheel;
		Random rand = new Random();
		ArrayList<TSPPath> population = new ArrayList<TSPPath>();
		ArrayList<TSPPath> nextGenPopulation = new ArrayList<TSPPath>();
		TSPPath child = new TSPPath();

		try {
			// Initialization //
			populationSize = Math.max((int)(numCities * populationRatio), 10);   // Set population Size
			numElite = Math.min(populationSize, (int) (elitismRatio * cityList.length + 0.5));

			for (int individualIdx = 0; individualIdx < populationSize; individualIdx++) {   // For each individual ...	
				population.add(new TSPPath(this.cityList));
				population.get(individualIdx).shuffle();
			}
			
			nextGenPopulation = population;

			// Reproduce Population //
			for (int genIdx = 0; genIdx < numGenerations; genIdx++) {
				
				rouletteWheel = getRouletteWheel(population, numElite);   // Fill Wheel
				
				//Add elite individuals - shortest path of population
				for (int idx = 0; idx < numElite; idx++) {
					nextGenPopulation.set(idx, population.get(idx));
				}
				
				// Replace population with children (with elitism)
				for (int childIdx = numElite; childIdx < populationSize; childIdx++) {
					parent1Idx = parent2Idx = 0;
					
					// Choose first parent
					roll = 100 * rand.nextDouble();
					do {
						if (roll <= rouletteWheel[parent1Idx]) break;
						parent1Idx++;
					} while(parent1Idx < populationSize);
					
					// Choose second parent
					roll = 100 * rand.nextDouble();
					do {
						if (roll <= rouletteWheel[parent2Idx]) break;
						parent2Idx++;
					} while(parent2Idx < populationSize);
					if (parent1Idx == parent2Idx)   // Choose the next one if same
						parent2Idx = (parent2Idx >= populationSize - 1) ? 0 : parent2Idx + 1;

					// Cross parents into new child
					int limit = 0;
					if (crossover.equals("MPX") || (numGenerations > 100 && (genIdx + 1) % (int)(numGenerations * 0.2) == 0 )) {
						// Perform MPX until a better child is found or a limit is reached
						do {
							child = crossoverMPX(population.get(parent1Idx), population.get(parent2Idx), mutationRate);   // MPX
						} while (limit++ < 3 && child.getPathDistance() > Math.min(population.get(parent1Idx).getPathDistance(), population.get(parent2Idx).getPathDistance()));
					}
					else if (crossover.equals("SCX")) { 
						child = crossoverSCX(population.get(parent1Idx), population.get(parent2Idx), mutationRate);
					}
					
					nextGenPopulation.set(childIdx, child);
				}
				
				population = nextGenPopulation;   // Replace old generation with new
				
				// Sort population by distance
				Collections.sort(population, new Comparator<TSPPath>() {
					public int compare(TSPPath p1, TSPPath p2) {
						if (p1.getPathDistance() > p2.getPathDistance()) return 1; 
						if (p1.getPathDistance() < p2.getPathDistance()) return -1;
						return 0;
					}
				});
				
			}
			
			// Display Results //
			System.out.print(crossover + ", C=" + numCities + ", P=" + populationSize + ", G=" + numGenerations 
					+ ", M=" + mutationRate + "%, E=" + numElite + "\t");
			
			if (displayGraphics) {   // Normal Run
				System.out.print("Distance: " + decimalFormat.format(population.get(0).getPathDistance()) + "\t");
				tfDistance.setText("" + decimalFormat.format(population.get(0).getPathDistance()));
				population.get(0).printPath(this.paneArrows);
			} else {   // Test Run
				double totalDistance = 0;
				for (TSPPath p : population) { totalDistance += p.getPathDistance(); }
				double mean = (totalDistance / populationSize);
				double std = 0;
				for (TSPPath p : population) { std += Math.pow(p.getPathDistance() - mean, 2); }
				std = Math.pow(std / populationSize, .5);
				// Best, Mean, Max, standard deviation
				System.out.print("Best: " + decimalFormat.format(population.get(0).getPathDistance()) + "\t" + decimalFormat.format(mean) + "\t" 
						+ decimalFormat.format(population.get(populationSize-1).getPathDistance()) + "\t" + decimalFormat.format(std) + "\t");
				tfDistance.setText(" ");
			}
			
		} catch (RuntimeException e ) {
			e.printStackTrace();
		}
		
		return;
	} 
	
	// Perform Sequential Constructive Crossover and return a child
	public TSPPath crossoverSCX(TSPPath parent1, TSPPath parent2, double mutationRate) {
		int parent1Idx, parent2Idx;
		double mutate;   // mutation roll
		Random rand = new Random();   // Random number generator
		TSPPath child = new TSPPath();   // child path
		TSPCity parent1Next = null, parent2Next = null;
		
		child.add(parent1.get(0));   // added first element (same for both)
		
		for (int idx = 1; idx < parent1.size() - 1; idx++) {   // For each city in the individual

			// Get next of both parents for last added city //
			parent1Idx = parent1.indexOf(child.get(idx - 1)) + 1;
			if (parent1Idx >= parent1.size() || child.contains(parent1.get(parent1Idx))) {
				// Next city for Parent1 is out of range -> chose first available from sequential list
				for (TSPCity city : this.cityList) {
					if (!child.contains(city)) {
						parent1Next = city;
						break;
					}
				}
			} else {
				parent1Next = parent1.get(parent1.indexOf(child.get(idx - 1)) + 1);
			}
			
			parent2Idx = parent2.indexOf(child.get(idx - 1)) + 1;
			if (parent2Idx >= parent2.size() || child.contains(parent2.get(parent2Idx))) {
				// Next city for Parent1 is out of range -> chose first available from sequential list
				parent2Next = null;
				for (TSPCity city : this.cityList) {
					if (!child.contains(city) && city != parent1Next) {
						parent2Next = city;
						break;
					}
				}
				if (parent2Next == null) parent2Next = parent1Next;
			} else {
				parent2Next = parent2.get(parent2.indexOf(child.get(idx - 1)) + 1);
			}
			
			// Determine if they are equal, else choose shorter distance
			if (parent1Next.equals(parent2Next)) {
				// Equal -> append
				child.add(parent1Next);
			} else {
				// Not equal -> Determine cost to next
				if (this.cityDistances[child.get(idx - 1).getCityNum() - 1][parent1Next.getCityNum() - 1] 
						< this.cityDistances[child.get(idx - 1).getCityNum() - 1][parent2Next.getCityNum() - 1])
				{
					// Parent1 next shorter
					child.add(parent1Next);
				} else {
					// Parent2 next shorter
					child.add(parent2Next);
				}
			}
		}
		
		// Return to start and calculate distance
		child.add(child.get(0));	

		// Mutate Child // 
		for (int idx = 1; idx < child.size() - 1; idx++) {
			mutate = rand.nextDouble();
			if (mutate < mutationRate) {
				child.swap(idx, rand.nextInt(child.size() - 2) + 1);
			}
		}
		
		child.calculatePathDistance();
		
		return child;
	}
	
	// Perform Multiple Point Crossover and return a child
	public TSPPath crossoverMPX(TSPPath parent1, TSPPath parent2, double mutationRate) {
		int slice1, slice2;
		int beginIdx, endIdx;
		double mutate;   // mutation roll
		Random rand = new Random();   // Random number generator
		TSPPath child = new TSPPath();   // child path

		// Select slices
		slice1 = rand.nextInt(parent1.size() - 2) + 1;
		slice2 = rand.nextInt(parent1.size() - 2) + 1;
		
		beginIdx = Math.min(slice1, slice2);
		endIdx = Math.max(slice1, slice2);
		
		for (int idx = 0; idx < parent1.size(); idx++) {
			child.add(null);
		}
		
		// Add cities within slices
		for (int idx = beginIdx; idx < endIdx; idx++) {
			child.set(idx, parent1.get(idx));
		}
		
		// Add all other cities not between slices\
		int parentIdx = 0;
		for (int idx = 0; idx < child.size() - 1; idx++) {
			if (child.get(idx) == null) {
				while (child.contains(parent2.get(parentIdx))) parentIdx++;
				child.set(idx, parent2.get(parentIdx));
			}
		}
		child.set(child.size() - 1, child.get(0));   // Add start
		
		// Mutate Child // 
		for (int idx = 1; idx < child.size() - 1; idx++) {
			mutate = rand.nextDouble(); 
			if (mutate < mutationRate) {
				child.swap(idx, rand.nextInt(child.size() - 2) + 1);
			}
		}
		
		child.calculatePathDistance();
		
		return child;
	}
	
	// Return weighted distances, preferring the lowest
	public static double[] getRouletteWheel(ArrayList<TSPPath> population, int numElite) {
		int size = population.size() - numElite;
        double runningTotal = 0, total = 0;
		double[] wheel = new double[size];
        double[] distanceInverses = new double[size];

        for (int idx = 0; idx < size; idx++) {
        	distanceInverses[idx] = 1.0 / population.get(idx).getPathDistance();
        	total += distanceInverses[idx];
        }
        for (int idx = 0; idx < size; idx++) {
        	runningTotal += distanceInverses[idx];
        	wheel[idx] = 100 * runningTotal / total;
        }
        
        return wheel;
	}

    // Read file to set global list of cities and mark points on graph
	private void getCitiesFromFile(File file) {
		int numCities;
		double coordX, coordY, circleX, circleY;
		String fileLine;
		String[] fileLineSplit;
		Scanner fileScanner = null;
		ArrayList<TSPCity> cityArrayList = new ArrayList<TSPCity>(); 
		
		try {
			paneCities.getChildren().clear();  // Clear existing cities
			paneArrows.getChildren().clear();  // Clear existing arrows
			
			fileScanner = new Scanner(file);
			
			// Skip scanner to coordinate section
			while (!fileScanner.nextLine().equals("NODE_COORD_SECTION")) {}
			 
			// Get cities from file
			while (fileScanner.hasNextLine()) {
				 fileLine = fileScanner.nextLine();
				 fileLineSplit = fileLine.split(" ");   //each line:  <city#> X Y
				 if (fileLineSplit.length != 3) {
					 throw new RuntimeException("\nError parsing file - coordinate line not formatted properly");
				 }
				 
				 coordX = Double.parseDouble(fileLineSplit[1]);   // X of given city
				 coordY = Double.parseDouble(fileLineSplit[2]);   // Y of given city
				 circleX = (coordX / 100) * (paneCities.getWidth());   // X of graph circle
				 circleY = paneCities.getHeight() - ((coordY / 100) * (paneCities.getHeight()));   // Y of graph circle
				 
				 TSPCity newCity = new TSPCity(Integer.parseInt(fileLineSplit[0]), coordX, coordY, circleX, circleY);
				 cityArrayList.add(newCity);   // New city
				 
				 // Add city to graph
				 paneCities.getChildren().add(newCity.getCircle());
				 
				 // Set city number by circle
				 Text tb = new Text("" + newCity.getCityNum());
				 tb.setX(newCity.getCircleX() + 10);
				 tb.setY(newCity.getCircleY() + 5);
				 paneCities.getChildren().add(tb);
		     }
			
			this.cityList = cityArrayList.toArray(new TSPCity[cityArrayList.size()]);
			
			// Initialize Distance Lookup Table
			numCities = this.cityList.length;   // Number of cities
			cityDistances = new double[numCities][numCities];   // Initialize table
			
			for(int sourceIdx = 0; sourceIdx < numCities; sourceIdx++) {
				for(int destinationIdx = 0; destinationIdx < numCities; destinationIdx++) {
					cityDistances[sourceIdx][destinationIdx] = getPointDistance(cityList[sourceIdx].getX(), cityList[sourceIdx].getY(), 
																		cityList[destinationIdx].getX(), cityList[destinationIdx].getY());
				}
			}
			
		} catch (FileNotFoundException | RuntimeException e) {
			e.printStackTrace();
		} finally {
			fileScanner.close();
		}		
	
		
		//System.out.println("\n**********************************");
		//printCities(this.cityList);   //DEBUG
	}
	
	// Calculate distance from two points
	public static double getPointDistance(double x1, double y1, double x2, double y2) {
		return Math.sqrt( Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}
		
}
