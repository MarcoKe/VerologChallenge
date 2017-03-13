package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import data.DataContoller;
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

public class Main {
	public static void main(String[] args) {
			
		Reader r = new Reader();
		File f = new File("VeRoLog_r100d5_1.txt");
		DataContoller data = r.readFile(f);
		
		StupidSolver2 stup = new StupidSolver2();
		StrategyController strat = stup.solve(data);
		
		Writer w = new Writer();
		w.write(data, strat);
		
	}
}
