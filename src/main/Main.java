package main;

import java.io.File;

import data.DataController;
import data.StrategyController;
import io.Reader;
import io.Writer;
import solver.Solver;
import solver.old2;

public class Main {
	public static void main(String[] args) {
		
		String filename = "data/ORTEC_Test/ORTEC_Test_05.txt";
		if (args.length > 0) {
			filename = args[0]; 
		}
		Reader r = new Reader();
		File f = new File(filename);
		DataController data = r.readFile(f);
		
		Solver stup = new old2();
		StrategyController strat = stup.solve(data);
		
		Writer w = new Writer();
		w.write(data, strat, filename);
		
	}
}
