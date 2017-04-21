package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import data.DataController;
import data.Depot;
import data.Global;
import data.Location;
import data.Request;
import data.Tool;
import data.Vehicle;

public class Reader {

	private static final String DATASET_VAR = "DATASET = ";
	private static final String NAME_VAR = "NAME = ";
	private static final String DAYS_VAR = "DAYS = ";
	private static final String CAPACITY_VAR = "CAPACITY = ";
	private static final String MAX_DISTANCE_VAR = "MAX_TRIP_DISTANCE = ";
	private static final String DEPOT_LOCATION_VAR = "DEPOT_COORDINATE = ";
	private static final String COST_PER_VEHICLE_VAR = "VEHICLE_COST = ";
	private static final String COST_PER_DAY_VAR = "VEHICLE_DAY_COST = ";
	private static final String COST_PER_DISTANCE_VAR = "DISTANCE_COST = ";
	private static final String TOOLS_VAR = "TOOLS = ";
	private static final String LOCATION_VAR = "COORDINATES = ";
	private static final String REQUESTS_VAR = "REQUESTS = ";
	
	private static final String DISTANCE_KEY = "DISTANCE";

	private static final int TOOL_MATRIX_DIMENSION = 4;
	private static final int LOCATION_MATRIX_DIMENSION = 3;
	private static final int REQUEST_MATRIX_DIMENSION = 7;

	private static final int VEHICLE_DEFAULT_ID = -1;

	HashMap<Integer, Tool> mapTool = null;
	HashMap<Integer, Location> mapLocation = null;

	/**
	 * 
	 */
	public Reader() {
		mapTool = new HashMap<>();
		mapLocation = new HashMap<>();
	}

	/**
	 * 
	 * @param f
	 * @return
	 */
	public DataController readFile(File f) {
		DataController ret = null;
		if (!f.exists()) {
			System.out.println("File doesnt exist");
			return ret;
		}

		Global global = null;
		Depot depot = null;
		Vehicle vehicle = null;
		List<Location> locList = new ArrayList<>();
		List<Request> reqList = new ArrayList<>();
		List<Tool> toolList = new ArrayList<>();

		FileReader in = null;
		BufferedReader reader = null;
		try {
			in = new FileReader(f);
			reader = new BufferedReader(in);
			// Data set
			String dataset = reader.readLine().substring(DATASET_VAR.length());
			// Name
			String name = reader.readLine().substring(NAME_VAR.length());
			// ----------------------------------------
			reader.readLine();

			// Days
			int days = Integer.parseInt(reader.readLine().substring(DAYS_VAR.length()));

			// Vehicle capacity
			int capacity = Integer.parseInt(reader.readLine().substring(CAPACITY_VAR.length()));

			// Max distance
			int maxDistance = Integer.parseInt(reader.readLine().substring(MAX_DISTANCE_VAR.length()));

			// Depot Coordinate ID
			int depotLocationId = Integer.parseInt(reader.readLine().substring(DEPOT_LOCATION_VAR.length()));

			// ------------------------------------------
			reader.readLine();
			// Vehicle Cost
			int costPerVehicle = Integer.parseInt(reader.readLine().substring(COST_PER_VEHICLE_VAR.length()));

			// Vehicle day cost
			int costPerDay = Integer.parseInt(reader.readLine().substring(COST_PER_DAY_VAR.length()));

			// Vehicle distance Cost
			int costPerDistance = Integer.parseInt(reader.readLine().substring(COST_PER_DISTANCE_VAR.length()));

			// ------------------------------------------
			reader.readLine();

			// --------- TOOLS ---------------------------

			readTools(reader, toolList);
			HashMap<Tool,Integer> depotMap = new HashMap<>(); 
			for (Tool t : toolList) {
				depotMap.put(t,0);
			}
			// -------------------------------------------
			reader.readLine();

			// --------- LOCATION ------------------------
			readLocation(reader, locList);
			Location depotLocation = mapLocation.get(depotLocationId);
			
			
			// ------------------------------------------
			reader.readLine();

			// --------- REQUESTS ------------------------
			readRequest(reader, reqList);

			// --------- DISTANCE ----------------------
			int[][] distMatrix = null;			
			String lastLine = reader.readLine();
			while(lastLine != null){
				if(lastLine.equals(DISTANCE_KEY)){
					distMatrix = readDistance(reader,locList.size());			
				}
				lastLine = reader.readLine();
			}
						
			vehicle = new Vehicle(capacity, maxDistance, costPerVehicle, costPerDay, costPerDistance,
					VEHICLE_DEFAULT_ID);
			depot = new Depot(depotLocation,depotMap);
			global = new Global(dataset,name,days, distMatrix);
				
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			clearMap();
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		ret = new DataController(depot, global, locList, reqList, toolList, vehicle);

		return ret;
	}
	
	/**
	 * 
	 */
	private void clearMap() {
		mapTool.clear();
		mapLocation.clear();
	}
	
	/**
	 * 
	 * @param reader
	 * @param toolList
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private void readTools(BufferedReader reader, List<Tool> toolList) throws NumberFormatException, IOException {

		int toolCount = Integer.parseInt(reader.readLine().substring(TOOLS_VAR.length()));

		for (int i = 0; i < toolCount; ++i) {
			String toolStr = reader.readLine();
			String[] split = toolStr.split("\\s");

			if (split.length != TOOL_MATRIX_DIMENSION) {
				throw new RuntimeException("TOOL MATRIX DIMENSION IS NOT " + TOOL_MATRIX_DIMENSION + ": AT " + i);
			}

			int id = Integer.parseInt(split[0]);
			int size = Integer.parseInt(split[1]);
			int maxAvailable = Integer.parseInt(split[2]);
			int cost = Integer.parseInt(split[3]);

			Tool tool = new Tool(size, cost, id, maxAvailable);
			mapTool.put(id, tool);
			toolList.add(tool);
		}

	}
	
	/**
	 * 
	 * @param reader
	 * @param locList
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private void readLocation(BufferedReader reader, List<Location> locList) throws NumberFormatException, IOException {
		int locCount = Integer.parseInt(reader.readLine().substring(LOCATION_VAR.length()));

		for (int i = 0; i < locCount; ++i) {
			String locStr = reader.readLine();
			String[] split = locStr.split("\\s");
			if (split.length != LOCATION_MATRIX_DIMENSION) {
				throw new RuntimeException(
						"COORDINATES MATRIX DIMENSION IS NOT " + LOCATION_MATRIX_DIMENSION + ": AT " + i);
			}

			int id = Integer.parseInt(split[0]);
			int x = Integer.parseInt(split[1]);
			int y = Integer.parseInt(split[2]);

			Location loc = new Location(x, y, id);
			mapLocation.put(id, loc);
			locList.add(loc);
		}

	}
	
	/**
	 * 
	 * @param reader
	 * @param reqList
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private void readRequest(BufferedReader reader, List<Request> reqList) throws NumberFormatException, IOException {
		int reqCount = Integer.parseInt(reader.readLine().substring(REQUESTS_VAR.length()));

		for (int i = 0; i < reqCount; ++i) {
			String reqStr = reader.readLine();
			String[] split = reqStr.split("\\s");
			if (split.length != REQUEST_MATRIX_DIMENSION) {
				throw new RuntimeException("REQUEST MATRIX DIMENSION IS NOT " + REQUEST_MATRIX_DIMENSION + ": AT " + i);
			}

			int id = Integer.parseInt(split[0]);
			int locId = Integer.parseInt(split[1]);
			int startTime = Integer.parseInt(split[2]);
			int endTime = Integer.parseInt(split[3]);
			int usageTime = Integer.parseInt(split[4]);
			int toolId = Integer.parseInt(split[5]);
			int amountOfTools = Integer.parseInt(split[6]);

			Location location = mapLocation.get(locId);
			Tool tool = mapTool.get(toolId);
			Request request = new Request(id, location, startTime, endTime, tool, usageTime, amountOfTools);
			reqList.add(request);
		}
	}

	
	/**
	 * NOT TESTED
	 * @param reader
	 * @param locCount
	 * @return
	 * @throws IOException
	 */
	private int[][] readDistance(BufferedReader reader, int locCount) throws IOException{
		int[][] ret = new int[locCount][locCount];
		for(int i = 0; i< ret.length; ++i){
			String distStr = reader.readLine();
			String[] split = distStr.split("\\s");
			if(split.length != ret.length){
				throw new RuntimeException("DISTANCE MATRIX DIMENSION IS NOT " + ret.length + ": AT " + i);
			}
			
			for(int j = 0; j < ret[0].length; ++j){
				ret[i][j] = Integer.parseInt(split[j]);
			}
			
		}		
		return ret;
	}
	
}
