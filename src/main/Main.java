package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import data.DataController;
import data.Depot;
import data.Global;
import data.Location;
import data.Request;
import data.StrategyController;
import data.Tool;
import data.Vehicle;
import io.Reader;
import io.Writer;
import solver.BruteforceSolver;
import solver.CarmenSolverRebuild;
import solver.CarmenTryingSolver;
import solver.SimulatedAnnealingSolver;
import solver.Solver;
import solver.StupidSolver2;
import solver.StupidSolver3;
import solver.old1;
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
