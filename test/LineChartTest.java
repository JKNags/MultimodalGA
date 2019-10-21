package test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.lang.Math;
 
public class LineChartTest extends Application {
 
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override public void start(Stage stage) {
        stage.setTitle("Line Chart Sample");
        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Number of Month");
        //creating the chart
        final LineChart<Number,Number> lineChart = 
                new LineChart<Number,Number>(xAxis,yAxis);
                
        lineChart.setTitle("M1");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("My portfolio");
        
        //populating the series with data
        //series.getData().add(new XYChart.Data(1, 23));
        
        for (double x = 0; x < 1; x += .01) {
        	Data d = new XYChart.Data(x, m1(x));
        	//d.getNode().setVisible(false);
        	Rectangle rect = new Rectangle(0, 0);
        	rect.setVisible(false);
        	d.setNode(rect);
        	series.getData().add(d);
        	System.out.println(x + ",  " + m1(x));
        }
        
        lineChart.getData().add(series);
        
        Scene scene  = new Scene(lineChart,800,600);
        stage.setScene(scene);
        stage.show();
    }
 
    double m1(double x) {
    	return Math.pow(Math.sin(5 * Math.PI * x), 6);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
