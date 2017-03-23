package solver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import data.DataController;
import data.Request;
import io.Reader;
import data.Global;


// hacked together, do not use as a reference 
public class StupidSolver implements Solver {
	private static final String INPUT_FOLDER = "AllTimeBest/";



	public StupidSolver() {

	}

	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		StupidSolver solver = new StupidSolver();

		File folder = new File(INPUT_FOLDER);
		File[] files = folder.listFiles();

		for (File file : files) {
			Reader r = new Reader();
			File f = new File(INPUT_FOLDER + "" + file.getName());
			DataController data = r.readFile(f);
			String solution = solver.solve(data);

			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(INPUT_FOLDER + "" + file.getName() + ".sol"), "utf-8"))) {   // writes .txt.sol files, should write sol.txt files
				writer.write(solution);
			}
		}
	}

	public String solve(DataController data) {
		StringBuilder solution = new StringBuilder();    // should not build the output string directly in the actual algorithm
		Global g = data.getGlobal();
		List<Request> requests = data.getRequestList();		
		Collections.sort(requests, (o1, o2) -> Integer.compare(o1.getStartTime(), o2.getStartTime()));

		int minDays = findMinDays(requests); 

		System.out.println(minDays);
		int maxVehicles = 0; 
		int numVehicleDays = 0; 
		int distance = 0;
		int[] vehiclesUsed = new int[minDays];

		List<Request> pickups = new ArrayList<>();

		for (int i = 0; i < minDays; i++) {
			solution.append("DAY = " + Integer.toString(i+1) + "\n");
			StringBuilder partial = new StringBuilder(); 

			// deliver all requests for the day 
			List<Request> delivered = new ArrayList<>();
			int trucksUsed = 0; 
			for (Request request : requests) {
				if (request.getStartTime() == i+1) {
					// 
					delivered.add(request); 	
					trucksUsed++;
					distance += 2.0* (g.computeDistance(data.getLocationList().get(0), request.getLocation()));
					partial.append(Integer.toString(trucksUsed) + " R 0 " + Integer.toString(request.getId()) + " 0\n");

				}

			}

			// pick up any tools from customers scheduled for the day 
			// I'm abusing the Request class to hold Pickups here, not a great idea 
			List<Request> completedPickups = new ArrayList<>();
			for (Request p : pickups) {
				if (p.getStartTime() + p.getUsageTime() == i+1) {
					trucksUsed++; 
					completedPickups.add(p);
					distance += 2.0* (g.computeDistance(data.getLocationList().get(0), p.getLocation()));
					partial.append(Integer.toString(trucksUsed) + " R 0 " + Integer.toString(-p.getId()) + " 0\n");    

				}

			}

			if (trucksUsed > maxVehicles) {
				maxVehicles = trucksUsed; 
			}

			numVehicleDays += trucksUsed; 

			solution.append("NUMBER_OF_VEHICLES = " + Integer.toString(trucksUsed) + "\n");
			solution.append(partial.toString() + "\n");

			vehiclesUsed[i] = trucksUsed; 
			trucksUsed = 0; 
			pickups.removeAll(completedPickups);
			pickups.addAll(delivered);
			requests.removeAll(delivered);

		}

		return formatSolution(g, solution.toString());
	}



	// compute min # days needed to serve all requests and pick tools up again
	public int findMinDays(List<Request> requests) {
		int minDays = 0;   
		for (Request request : requests) {
			int sum = request.getStartTime() + request.getUsageTime()+1; 

			if (sum > minDays) {
				minDays = sum; 
			}
		}

		return minDays-1; 
	}

	public String formatSolution(Global g, String partialSolution) {
		//PrintWriter pw = new PrintWriter(new File("solution.txt"));
		StringBuilder sb = new StringBuilder();
		sb.append("DATASET = " + g.getDataSet() + "\n");
		sb.append("NAME = " + g.getName() + "\n\n");

		sb.append(partialSolution);
		return sb.toString();
	}
}
