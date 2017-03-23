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
import solver.StupidSolver2;
import solver.StupidSolver3;

public class Main {
	public static void main(String[] args) {
		
		String filename = "VeRoLog_r100d5_1.txt";
		if (args.length > 0) {
			filename = args[0]; 
		}
		Reader r = new Reader();
		File f = new File(filename);
		DataController data = r.readFile(f);
		
		StupidSolver3 stup = new StupidSolver3();
		StrategyController strat = stup.solve(data);
		
		Writer w = new Writer();
		w.write(data, strat, filename);
		
	}
}
