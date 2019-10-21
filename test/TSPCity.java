package test;

import java.text.DecimalFormat;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class TSPCity {
	// Instance Variables
	private int cityNum;
	private double coordX, coordY, circleX, circleY;   // Coordinates
	private TSPCity nextCity;   // Next | successor
	private TSPCity prevCity;   // Previous | predecessor
	private Arrow nextArrow;  // Arrow facing this city
	private Circle circle;
	
	// Constructor
	public TSPCity(int cityNum, double coordX, double coordY, double circleX, double circleY) {
		this.cityNum = cityNum;
		this.coordX = coordX;
		this.coordY = coordY;
		this.circleX = circleX;
		this.circleY = circleY;
		this.circle = new Circle(circleX, circleY, 4);
	}
	
	// Getters
	public int getCityNum() {
		return this.cityNum;
	}
	
	public double getX() {
		return this.coordX;
	}
	
	public double getY() {
		return this.coordY;
	}
	
	public double getCircleX() {
		return this.circleX;
	}
	
	public double getCircleY() {
		return this.circleY;
	}
	
	public Circle getCircle() {
		return this.circle;
	}
	
	public TSPCity getNextCity() {
		return this.nextCity;
	}
	
	public TSPCity getPrevCity() {
		return this.prevCity;
	}
	
	public boolean hasNextCity() {
		return this.nextCity != null;
	}
	
	public boolean hasPrevCity() {
		return this.prevCity != null;
	}
	
	public Arrow getNextArrow() {
		return this.nextArrow;
	}
	
	// Setters
	public void setNextCity(TSPCity nextCity) {
		this.nextCity = nextCity;
		nextCity.setPrevCity(this);
		this.nextArrow = new Arrow(this.circleX, this.circleY, nextCity.getCircleX(), nextCity.getCircleY());
	}
	
	public void setPrevCity(TSPCity prevCity) {
		this.prevCity = prevCity;
	}
	
	public void setStart() {
		this.circle.setFill(Color.GOLD);;
	}
	
	// To String
	public String toString() {
		//DecimalFormat dblFormat = new DecimalFormat("#0.00");;
		//return "(" + this.cityNum + ": " + dblFormat.format(this.coordX) + ", " + dblFormat.format(this.coordY) + ")";
		return this.cityNum + "";
	}
	
	public String printCities() {
		DecimalFormat dblFormat = new DecimalFormat("#0.00");;
		return "(" + this.cityNum + ": " + dblFormat.format(this.coordX) + ", " + dblFormat.format(this.coordY) + ")";
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {

	    return super.clone();
	}

}
