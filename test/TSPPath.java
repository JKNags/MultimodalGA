package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import javafx.scene.layout.Pane;

public class TSPPath {
	private ArrayList<TSPCity> path;
	private double distance = -1;
	
	public TSPPath() {
		path = new ArrayList<TSPCity>();
	}
	
	public TSPPath(int size) {
		path = new ArrayList<TSPCity>(size);
	}
	
	public TSPPath(TSPCity[] path) {
		if (path.length < 1) return;
		this.path = new ArrayList<TSPCity>(Arrays.asList(path.clone()));
		this.path.add(path[0]);   // Return to start
	}
	
	// Return distance of path
	public void calculatePathDistance() {
		double distance = 0;	
		for (int cityIdx = 1; cityIdx < this.path.size(); cityIdx++) {
			distance += getPointDistance(this.path.get(cityIdx - 1).getX(), this.path.get(cityIdx - 1).getY(),
					this.path.get(cityIdx).getX(), this.path.get(cityIdx).getY());
		}
		this.distance = distance;
	}
	
	public double getPathDistance() {
		return this.distance;
	}
	
	public TSPCity get(int idx) {
		return this.path.get(idx);
	}
	
	public void set(int idx, TSPCity city) {
		this.path.set(idx, city);
	}
	
	public void add(TSPCity city) {
		this.path.add(city);
	}
	
	public boolean contains(TSPCity city) {
		return this.path.contains(city);
	}
	
	public int size() {
		return this.path.size();
	}
	
	public int indexOf(TSPCity city) {
		return this.path.indexOf(city);
	}
	
	// Calculate distance from two points
	public static double getPointDistance(double x1, double y1, double x2, double y2) {
		return Math.sqrt( Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}
	
	// Randomly shuffle elements in array (except first and last)
	public void shuffle(){
		Random rnd = new Random();			
		int toPosition;
		TSPCity temp;
		
		for (int idx = 1; idx < this.path.size() - 1; idx++) {
			toPosition = rnd.nextInt(this.path.size() - 2) + 1;
		    temp = this.path.get(idx);
		    this.path.set(idx, this.path.get(toPosition));
		    this.path.set(toPosition, temp);
		}
		
		calculatePathDistance();
	}
	
    // Swap cities in list by index
    public void swap(int i, int j) {
    	TSPCity temp = this.path.get(i);
    	this.path.set(i, this.path.get(j));
    	this.path.set(j, temp);
    }
    
    // To String
    public String toString() {
    	return this.path.toString();
    }
    
	// Print string of cities and their total distance
	public void printPath(Pane pane) {
		pane.getChildren().clear();
		
		for (int idx = 1; idx < this.path.size(); idx++) {
			Arrow arrow = new Arrow(this.path.get(idx - 1).getCircleX(), this.path.get(idx - 1).getCircleY(), this.path.get(idx).getCircleX(), this.path.get(idx).getCircleY());
			pane.getChildren().add(arrow);
		}
	}
}
