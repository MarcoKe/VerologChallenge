package solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import analysis.PlacementAnalyser;
import analysis.RouteAnalyser;
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
import routing.MandatoryConnection;
import routing.Routing;
import routing.SimpleRouting;

public class SimulatedAnnealingSolver implements Solver {
	Routing routing = new SimpleRouting(); 
	private boolean debug = true; // TODO: remove once done 
	private Map<Integer, DayInformation> days;
	private DataController data; 
	private PlacementAnalyser placementAnalyser;
	private RouteAnalyser routeAnalyser;
	
	
	public StrategyController solve(DataController data) {
		this.data = data; 
		// find some (non-feasible) placement 		
		Placement p = stupidPlacement(data);
		
		// apply simulated annealing to hopefully make it feasible  
		System.out.println("first try (matchings not taken into acount):");
		placementAnalyser = new PlacementAnalyser(data); 
		p = simulatedAnnealing(data, p, 10000, 0.001, this::noReuseEnergyFunction); 
		
		System.out.println("second try: ");
		routeAnalyser = new RouteAnalyser(data); 
		p = simulatedAnnealing(data,p, 10000, 0.001, this::reuseEnergyFunction);
				
		return externalRouting(route(p));
//		return route(p);
		
	}
	
	/*
	 * computes constraint violations by performing a simple routing that tries to find matches
	 */
	public int reuseEnergyFunction(Placement p) {
		return routeAnalyser.getConstraintViolations(route(p));
	}
	
	/*
	 * computes constraint violations just in terms of placement (can't take tool reuse into account)
	 */
	public int noReuseEnergyFunction(Placement p) {
		return placementAnalyser.getConstraintViolations(p);
	}
		
	public Placement simulatedAnnealing(DataController data, Placement initialPlacement, double temperature, double coolingRate, Function<Placement, Integer> energyFunc) {
		Map<Request, Integer> currentPlacement = initialPlacement.getPlacement();
		Map<Request, Integer> bestPlacement = new HashMap<>(currentPlacement);
		int counter = 0; 
		int bestEnergy = energyFunc.apply(new Placement(currentPlacement));
		
		if (debug) {
			System.out.println("initial constraint violations: " + bestEnergy);
			System.out.println("initialBest (hash): " + bestPlacement.hashCode());
		}		

		while (temperature > 1.0 && bestEnergy > 0) {
			if (debug) counter++; 
			Map<Request, Integer> newPlacement = new HashMap<>(currentPlacement);
			
			// make modification to current solution (take random request and move it ta a new random day)
			Object[] keys = newPlacement.keySet().toArray();
			Request randomRequest = (Request) keys[new Random().nextInt(keys.length)];
			
			int randomDay = ThreadLocalRandom.current().nextInt(randomRequest.getStartTime(), randomRequest.getEndTime() + 1);
		
			newPlacement.put(randomRequest, randomDay);
			
			// evaluate modified solution
			int currentEnergy = energyFunc.apply(new Placement(currentPlacement)); 
			int newEnergy = energyFunc.apply(new Placement(newPlacement));
			
			if (acceptanceProb(currentEnergy, newEnergy, temperature) > Math.random()) {
				currentPlacement = newPlacement; 
				currentEnergy = newEnergy;
			}
			
			if (currentEnergy < bestEnergy) {
				bestPlacement = currentPlacement; 	
				bestEnergy = currentEnergy; 
			}
			
			temperature *= 1-coolingRate; 
		}
		
		if (debug) {
			System.out.println(counter + " iterations");
			System.out.println("final constraint violations: " + bestEnergy);
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
						addVehicle(delivery, new VehicleAction(Action.PICK_UP, selected.get()), new VehicleAction(Action.LOAD_AND_DELIVER, request));
						
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
	
	public StrategyController externalRouting(StrategyController initial) {
		
		
		List<DayInformation> newDays = new ArrayList<>(); 
		for (DayInformation day : initial.getDays()) {
			List<MandatoryConnection> mandatoryConnections = new ArrayList<>(); 
			List<VehicleAction> simpleConnections = new ArrayList<>(); 
			
			for (VehicleInformation vehicle : day.getVehicleInformationList()) {
				List<VehicleAction> route = vehicle.getRoute();
				int routeSize = route.size(); 
				
				if (routeSize > 3) {
					MandatoryConnection man = new MandatoryConnection(); 
					for (int i = 1; i < routeSize-1; i++) {
						if (route.get(i).getVehicleAction() == Action.LOAD_AND_DELIVER) 
							man.addDeliverList(route.get(i));
						else 
							man.addPickupList(route.get(i));
					}
					
					mandatoryConnections.add(man);
				}
				else {
					simpleConnections.add(route.get(1)); 
				}
				
				
			}
			
			List<VehicleInformation> dayRouting = routing.getRouting(data, simpleConnections, mandatoryConnections);
			DayInformation newDayInfo = new DayInformation(day.getDay()); 
			newDayInfo.addAllVehickeInformation(dayRouting);
			newDays.add(newDayInfo);
			
			
		}
		
		
		return new StrategyController(newDays);
		
	}
	

}
