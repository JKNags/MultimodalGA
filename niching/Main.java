package niching;

import javafx.application.Application;

public class Main {

	public static void main(String[] args) {
		int populationSize = 10;
		float mutationRate = 0.05f;
		int numGenerations = 100;
		
		Application.launch(Genetic.class, 
				new String[] {populationSize + "", mutationRate + "", numGenerations + ""});
	}

}
