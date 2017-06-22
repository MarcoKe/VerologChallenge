package main;

import java.io.File;

import data.DataController;
import data.StrategyController;
import io.Reader;
import io.Writer;
import solver.SimulatedAnnealingSolver;
import solver.Solver;
import solver.old2;

public class Main {
	public static void main(String[] args) {
		
		String filename = "data/ORTEC_Test/ORTEC_Test_01.txt";
		if (args.length > 0) {
			filename = args[0]; 
		}
		Reader r = new Reader();		
		DataController data = r.readFile(filename);
		
		Solver stup = new SimulatedAnnealingSolver(filename);
		StrategyController strat = stup.solve(data);
		
		Writer w = new Writer();
		w.write(data, strat, filename);
		
	}
}
