package solver;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import data.DataController;
import data.DayInformation;
import data.Location;
import data.Request;
import data.StrategyController;
import data.Tool;
import data.VehicleAction;
import data.VehicleInformation;
import data.VehicleAction.Action;
import io.Reader;

public class BruteforceSolver implements Solver {
	
	private int counter = 0; 
	private List<Map<Request, Integer>> feasibleSolutions;  
	private Map<Integer, DayInformation> days;
	
	public StrategyController solve(DataController data) {	
		days = new TreeMap<>();
		List<Request> requests = data.getRequestList();
		
		
		for (Tool tool : data.getToolList()) {
			System.out.println("TOOL " + tool.getId() + "\n ==================================================");
			List<Request> requestsForTool = requests.stream().filter(r -> r.getTool().getId() == tool.getId()).collect(Collectors.toList());
			
			feasibleSolutions = new ArrayList<>();
			recurse(requestsForTool, new HashMap<>(), new HashMap<>());
			System.out.println("feasible solution sizes (" + feasibleSolutions.size() + " solutions)");
			for (Map<Request, Integer> sol : feasibleSolutions) {
				System.out.println(sol.keySet().size());
			}
			
			// stupid routing, should ideally be decoupled from this class 
			Collections.sort(feasibleSolutions, Collections.reverseOrder((o1, o2) -> Integer.compare(o1.keySet().size(), o2.keySet().size())));
			Map<Request, Integer> feasiblePlacement = feasibleSolutions.get(0);   // just takes the first feasible solution
			for (Request key : feasiblePlacement.keySet()) {
				
				int day = feasiblePlacement.get(key);
				addVehicle(day, new VehicleAction(Action.LOAD_AND_DELIVER, key));
				
				int pickup = day + key.getUsageTime();
				addVehicle(pickup, new VehicleAction(Action.PICK_UP, key));				
			}			
			
			// 
		}
		
		
		for (DayInformation bla : days.values()) {
			System.out.println(bla.toString());
		}
		return new StrategyController(new ArrayList<DayInformation>(days.values())); 
	}
	

	// routing 
	public void addVehicle(int day, VehicleAction... actions) {
		if (days.get(day) == null) {
			days.put(day, new DayInformation(day));
		}
		
		VehicleInformation vehicInfo = new VehicleInformation();
		
		for (VehicleAction a : actions) {
			vehicInfo.addAction(a);
		}	
		
		days.get(day).addVehicleInformation(vehicInfo);
		
	}
	//
	
	public boolean recurse(List<Request> requests, Map<Request, Integer> placedRequests, Map<Integer, Integer> toolUsage) {
//		System.out.println("====================\nCurrent placement: ");
//		for (Request key : placedRequests.keySet()) {
//			System.out.println("Request " + key.getId() + " placed on day " + placedRequests.get(key));
//		}
//		System.out.println("Requests left: " + requests.size());
		
		
		if (requests.isEmpty()) {		
			feasibleSolutions.add(placedRequests);
//			System.out.println("solution final");
			return true; 
		}
		Request request = requests.remove(0);
		
		for (int i = request.getStartTime(); i <= request.getEndTime(); i++) {
			if (feasibleAddition(toolUsage, request, i)) {
				Map<Request, Integer> placed = new HashMap<>(placedRequests);
				placed.put(request, i); 

				Map<Integer, Integer> usage = new HashMap<>(toolUsage); 
				for (int day = i; day <= i + request.getUsageTime(); day++) { 
					usage.put(day, (usage.get(day) == null ? request.getAmountOfTools() : usage.get(day) + request.getAmountOfTools()));
				}
				
				recurse(new ArrayList<>(requests), placed, usage); 
			}
			
		}
		
		return false;
	}
	
	public boolean feasibleAddition(Map<Integer, Integer> toolUsage, Request request, int day) {
		int maxUsage = request.getTool().getMaxAvailable(); 
		
		for (int i = day; i <= day + request.getUsageTime(); i++) {
			if ((toolUsage.get(i) == null ? 0 : toolUsage.get(i)) + request.getAmountOfTools() > maxUsage) {
				return false; 
			}
		}
		return true; 
	}
	
	public static void main(String[] args) {
		String filename = "VeRoLog_r100d5_1.txt";
		if (args.length > 0) {
			filename = args[0]; 
		}
		Reader r = new Reader();
		File f = new File(filename);
		DataController data = r.readFile(f);
		
		
		BruteforceSolver b = new BruteforceSolver(); 
		b.solve(data);
		
		System.out.println("++++++++++");
		for (Map<Request,Integer> bla : b.feasibleSolutions) {
			System.out.println(bla.keySet().size());
		}
		
		
	}
	
	
}
