package solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import analysis.PlacementAnalyser;
import data.DataController;
import data.DayInformation;
import data.Global;
import data.Location;
import data.Placement;
import data.Request;
import data.StrategyController;
import data.Vehicle;
import data.VehicleAction;
import data.VehicleAction.Action;
import data.VehicleInformation;

public class SimulatedAnnealingSolver implements Solver {
	private boolean debug = true; // TODO: remove once done 
	private Map<Integer, DayInformation> days;
	private DataController data; 
	
	
	public StrategyController solve(DataController data) {
		this.data = data; 
		// find some (non-feasible) placement 
		List<Request> requests = data.getRequestList(); 
		Map<Request, Integer> placement = new HashMap<>(); 
		
		for (Request request : requests) {
			placement.put(request, request.getStartTime()); 
		}
		
		Placement p = new Placement(placement);
		
		// apply simulated annealing to hopefully make it feasible  
		p = simulatedAnnealing(data, p); 
				
		return route(p);
		
	}
		
	public Placement simulatedAnnealing(DataController data, Placement stupidPlacement) {
		PlacementAnalyser analyser = new PlacementAnalyser(data); 
		
		double temperature = 10000; 
		double coolingRate = 0.001; 		
		
		Map<Request, Integer> currentPlacement = stupidPlacement.getPlacement();
		Map<Request, Integer> bestPlacement = new HashMap<>(currentPlacement);
		
		if (debug) {
			System.out.println("initial constraint violations: " + analyser.getConstraintViolations(new Placement(currentPlacement)));
			System.out.println("initialBest (hash): " + bestPlacement.hashCode());
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
			System.out.println("final constraint violations: " + analyser.getConstraintViolations(new Placement(bestPlacement)));
			System.out.println("finalBest (hash): " + bestPlacement.hashCode());
		}
		return new Placement(bestPlacement); 
	}
	
	public double acceptanceProb(int energy, int newEnergy, double temperature) {        
        if (newEnergy < energy) 
            return 1.0;        
        
        return Math.exp((energy - newEnergy) / temperature);
    }
	
	/*
	 * creates a placement where every request is executed on the 
	 * first possible day (serves as starting point for simulated annealing)
	 */
	public Placement stupidPlacement(DataController data) {
		List<Request> requests = data.getRequestList(); 
		Map<Request, Integer> placement = new HashMap<>(); 
		
		for (Request request : requests) {
			placement.put(request, request.getStartTime()); 
		}
		
		return new Placement(placement);
	}
	

	//TODO: decouple all of the following from this class 
	
	
	/*
	 * TODO: refactor this monster 
	 * This just routes the vehicles in an already existing placement 
	 * tries to take advantage of matchings 
	 */
	public StrategyController route(Placement p) { 
		days = new TreeMap<>(); 
		Map<Request, Integer> placement = p.getPlacement(); 
		Map<Integer, List<Request>> requestsByDay = new HashMap<>(); 
		Map<Integer, List<Request>> requestsByEndDay = new HashMap<>(); 
		
		// fill requestsByDay & requestsByEndDay
		for (Request request : placement.keySet()) {
			int delivery = placement.get(request); 
			int pickup = delivery + request.getUsageTime(); 
			
			if (requestsByDay.get(delivery) == null) {
				requestsByDay.put(delivery, new ArrayList<>()); 
			}
			
			requestsByDay.get(delivery).add(request); 
			
			if (requestsByEndDay.get(pickup) == null) {
				requestsByEndDay.put(pickup, new ArrayList<>()); 
			}
			
			requestsByEndDay.get(pickup).add(request); 
		}
		
		Global g = data.getGlobal(); 
		Location depot = data.getLocationList().get(0);
		
		// go through requests, try to match other requests both at beginning and end of given request 
		List<Request> accountedForDelivery = new ArrayList<>(); 
		List<Request> accountedForPickup = new ArrayList<>(); 
		for (Request request : placement.keySet()) {
			int delivery = placement.get(request); 
			int pickup = delivery + request.getUsageTime(); 
			if (!accountedForPickup.contains(request)) {
				// book keeping 
				accountedForPickup.add(request); 
				
				requestsByEndDay.get(pickup).remove(request);
				
				int distance = g.computeDistance(depot, request.getLocation());
				
				// try to find a match at end of interval 
				List<Request> matchesEnd = requestsByDay.get(pickup);
				if (matchesEnd != null) {
					matchesEnd = matchesEnd.stream().filter(m -> m.getTool().getId() == request.getTool().getId()).collect(Collectors.toList()); // filter by tool 
					Optional<Request> selected = matchesEnd.stream().min((m1, m2) -> Integer.compare(tripDistance(request.getLocation(), m1.getLocation()), tripDistance(request.getLocation(), m2.getLocation())));
					
					if (selected.isPresent() && possibleTrip(request.getLocation(), selected.get().getLocation())) {
						addVehicle(pickup, new VehicleAction(Action.PICK_UP, request), new VehicleAction(Action.LOAD_AND_DELIVER, selected.get()));
						
						// book keeping 
						accountedForDelivery.add(selected.get()); 
						requestsByDay.get(pickup).remove(selected.get());
					}
					else {
						addVehicle(pickup, new VehicleAction(Action.PICK_UP, request));
					}
				}
				else {
					addVehicle(pickup, new VehicleAction(Action.PICK_UP, request));
				}
				
			}
				
			if (!accountedForDelivery.contains(request)) {
				accountedForDelivery.add(request); 				
				requestsByDay.get(delivery).remove(request);
				
				// try to find a match at the beginning of the interval 
				List<Request> matchesBegin = requestsByEndDay.get(delivery);
				if (matchesBegin != null) {
					matchesBegin = matchesBegin.stream().filter(m -> m.getTool().getId() == request.getTool().getId()).collect(Collectors.toList()); // filter by tool 
					Optional<Request> selected = matchesBegin.stream().min((m1, m2) -> Integer.compare(tripDistance(request.getLocation(), m1.getLocation()), tripDistance(request.getLocation(), m2.getLocation())));
					
					if (selected.isPresent() && possibleTrip(selected.get().getLocation(), request.getLocation())) {
						addVehicle(delivery, new VehicleAction(Action.LOAD_AND_DELIVER, request), new VehicleAction(Action.PICK_UP, selected.get()));
						
						// book keeping 
						accountedForPickup.add(selected.get()); 
						requestsByEndDay.get(delivery).remove(selected.get());
					}
					else {
						addVehicle(delivery, new VehicleAction(Action.LOAD_AND_DELIVER, request));
					}
				}
				else {
					addVehicle(delivery, new VehicleAction(Action.LOAD_AND_DELIVER, request));
				}				
			}			
		}		
		
		return new StrategyController(new ArrayList<>(days.values()));
	}
	
	public boolean possibleTrip(Location l1, Location l2) {
		Vehicle vehicle = data.getVehicle();
		
		return (tripDistance(l1, l2) <= vehicle.getMaxDistance());
	}
	
	public int tripDistance( Location l1, Location l2) {
		Global g = data.getGlobal(); 
		Location depot = data.getLocationList().get(0);
		int distance = 0; 	
		distance += g.computeDistance(depot, l1);
		distance += g.computeDistance(l1, l2);
		distance += g.computeDistance(l2, depot);
		
		return distance;
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
	
	

}
