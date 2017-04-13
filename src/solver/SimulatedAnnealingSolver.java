package solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import analysis.PlacementAnalyser;
import data.DataController;
import data.DayInformation;
import data.Placement;
import data.Request;
import data.StrategyController;
import data.VehicleAction;
import data.VehicleAction.Action;
import data.VehicleInformation;

public class SimulatedAnnealingSolver implements Solver {
	private boolean debug = true; // TODO: remove once done 
	private Map<Integer, DayInformation> days;
	
	
	public StrategyController solve(DataController data) {
		// find some (non-feasible) placement 
		List<Request> requests = data.getRequestList(); 
		Map<Request, Integer> placement = new HashMap<>(); 
		
		for (Request request : requests) {
			placement.put(request, request.getStartTime()); 
		}
		
		Placement p = new Placement(placement);
		
		// apply simulated annealing to hopefully make it feasible  
		p = simulatedAnnealing(data, p); 
		
		// convert placement to routing 
		// TODO: make this smarter by matching where possible 
		// TODO: Ideally, decouple from this class entirely 
		placement = p.getPlacement(); 
		Map<Request, Integer> pickupPlacement = new HashMap<>(); 
		
		for (Request request : placement.keySet()) {
			pickupPlacement.put(request, placement.get(request) + request.getUsageTime()); 
		}
		
		days = new TreeMap<>();
		
		for (Request request : placement.keySet()) {
			int day = placement.get(request); 
			addVehicle(day, new VehicleAction(Action.LOAD_AND_DELIVER, request)); 
			addVehicle(day + request.getUsageTime(), new VehicleAction(Action.PICK_UP, request)); 			
		}
		
		return new StrategyController(new ArrayList<DayInformation>(days.values()));
		
	}
	
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
	
	public Placement simulatedAnnealing(DataController data, Placement stupidPlacement) {
		PlacementAnalyser analyser = new PlacementAnalyser(data); 
		
		double temperature = 10000; 
		double coolingRate = 0.0001; 
		
		
		Map<Request, Integer> currentPlacement = stupidPlacement.getPlacement();
		Map<Request, Integer> bestPlacement = new HashMap<>(currentPlacement);
		
		if (debug) {
			System.out.println("initial: " + analyser.getConstraintViolations(new Placement(currentPlacement)));
			System.out.println("initialBest: " + bestPlacement.hashCode());
		}
				
		
		int counter = 0; 
		while (temperature > 1.0) {
			if (debug) counter++; 
			Map<Request, Integer> newPlacement = new HashMap<>(currentPlacement);
			
			// get random request
			Object[] keys = newPlacement.keySet().toArray();
			Request randomRequest = (Request) keys[new Random().nextInt(keys.length)];
			
			int randomDay = ThreadLocalRandom.current().nextInt(randomRequest.getStartTime(), randomRequest.getEndTime() + 1);
		
			newPlacement.put(randomRequest, randomDay);
			
			int currentEnergy = analyser.getConstraintViolations(new Placement(currentPlacement)); 
			int newEnergy = analyser.getConstraintViolations(new Placement(newPlacement));
			
			if (acceptanceProb(currentEnergy, newEnergy, temperature) > Math.random()) {
				currentPlacement = newPlacement; 
			}
			
			if (analyser.getConstraintViolations(new Placement(currentPlacement)) < analyser.getConstraintViolations(new Placement(bestPlacement))) {
				bestPlacement = currentPlacement; 				
			}
			
			temperature *= 1-coolingRate; 
		}
		
		if (debug) {
			System.out.println(counter + " iterations");
			System.out.println("final: " + analyser.getConstraintViolations(new Placement(bestPlacement)));
			System.out.println("initialBest: " + bestPlacement.hashCode());
		}
		return new Placement(bestPlacement); 
	}
	
	public double acceptanceProb(int energy, int newEnergy, double temperature) {        
        if (newEnergy < energy) 
            return 1.0;        
        
        return Math.exp((energy - newEnergy) / temperature);
    }
	
	public Placement stupidPlacement(DataController data) {
		List<Request> requests = data.getRequestList(); 
		Map<Request, Integer> placement = new HashMap<>(); 
		
		for (Request request : requests) {
			placement.put(request, request.getStartTime()); 
		}
		
		return new Placement(placement);
	}
	
	

}
