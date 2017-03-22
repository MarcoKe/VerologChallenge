package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import data.DataContoller;
import data.DayInformation;
import data.Depot;
import data.Request;
import data.StrategyController;
import data.Tool;
import data.Vehicle;
import data.VehicleAction;
import data.VehicleAction.Action;
import data.VehicleInformation;
import data.Global;
import data.Location;


// hacked together, do not use as a reference 
public class StupidSolver3 implements Solver {
	
	DataContoller data; 
	Location depot; 
	Map<Integer, DayInformation> days;
	List<Request> possibleMatches;
	List<Request> requests;
	List<Request> servedRequests; 
	
	public StupidSolver3() {

	}
	
	/* 
	 * Checks whether the given request can be matched to any other request 
	 */
	public boolean isMatchable(Request request, Request potentialMatch) {
		if (inTimeWindow(request.getStartTime(), request.getEndTime(), potentialMatch.getStartTime())) {
			if (request.getStartTime() + request.getUsageTime() + 1 <= potentialMatch.getStartTime()) {
				return true; 
			}
		}
		
		return false; 
		
	}
	
	/*
	 * Checks whether a given day is within a certain time window 
	 */
	public boolean inTimeWindow(int start, int end, int day) {
		if (day >= start && day <= end) 
			return true; 
		
		return false; 
	}
	
	/*
	 * Tries to match a given request to some other request. If successful, returns true. 
	 */
	public boolean match(Request request) {
		for (int i = request.getStartTime(); i < request.getEndTime(); i++) {
			List<Request> matchSpace = new ArrayList<>(possibleMatches);
			matchSpace.remove(request);
	
			Optional<Request> possibleMatch = findRequest(matchSpace, request.getTool(), i, i+request.getUsageTime(), request);
				if (possibleMatch.isPresent()) {
					Request match = possibleMatch.get();
					
					if(!possibleTrip(request.getLocation(), match.getLocation()))
						continue;
					
					// add delivery for request
					VehicleInformation vehicInfo1 = new VehicleInformation();
					VehicleAction action1 = new VehicleAction(Action.LOAD_AND_DELIVER, request);
					vehicInfo1.addAction(action1);				
					
					addVehicle(i, vehicInfo1);					
					
					// add pickup with subsequent request serve
					VehicleInformation vehicInfo2 = new VehicleInformation();
					VehicleAction action2 = new VehicleAction(Action.PICK_UP, request);
					vehicInfo2.addAction(action2);
					VehicleAction action3 = new VehicleAction(Action.LOAD_AND_DELIVER, match);
					vehicInfo2.addAction(action3);
					
					addVehicle(i + request.getUsageTime(), vehicInfo2);
					
					// add pickup for matched request
					VehicleInformation vehicInfo3 = new VehicleInformation(); 
					VehicleAction action4 = new VehicleAction(Action.PICK_UP, match); 
					vehicInfo3.addAction(action4);
					
					addVehicle(i + request.getUsageTime() + match.getUsageTime(), vehicInfo3);
					
					possibleMatches.remove(match); 
					servedRequests.add(match);
					
					return true; 
					
				}
		}
		
		return false; 
	}
	
	public void addVehicle(int day, VehicleInformation vehicInfo) {		
		if (days.get(day) == null) {
			days.put(day, new DayInformation(day));
		}
		
		days.get(day).addVehicleInformation(vehicInfo);
	}
	

	public StrategyController solve(DataContoller d) {	
		data = d; 
		depot = data.getLocationList().get(0);
		Global g = data.getGlobal();
		requests = data.getRequestList();		
		Collections.sort(requests, (o1, o2) -> Integer.compare(o1.getStartTime(), o2.getStartTime()));
		possibleMatches = new ArrayList<>(requests);	
		servedRequests = new ArrayList<>();
		days = new TreeMap<>();		
		
		for (Request request : requests) {
			possibleMatches.remove(request);
			
			if (servedRequests.contains(request)) {
				continue;
			}				
			
			if (!match(request)) { // if no match is found just use one truck to deliver request and then pick it up again
				VehicleInformation vehicInfo1 = new VehicleInformation();
				VehicleAction action1 = new VehicleAction(Action.LOAD_AND_DELIVER, request);
				vehicInfo1.addAction(action1);				
				
				addVehicle(request.getStartTime(), vehicInfo1);
				
				VehicleInformation vehicInfo2 = new VehicleInformation();
				VehicleAction action2 = new VehicleAction(Action.PICK_UP, request);
				vehicInfo2.addAction(action2);				
				
				addVehicle(request.getStartTime()+request.getUsageTime(), vehicInfo2);
				
				possibleMatches.remove(request); 				
			}
		}
		
		return new StrategyController(new ArrayList<>(days.values()));
		
	}


	/*
	 * compute min # days needed to serve all requests and pick tools up again
	 */
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
	
	/*
	 * Find a request for a given tool on a given day if it exists
	 * (used to combine a pickup with a delivery) 
	 * we are selecting the request that minimizes the total distance 
	 */
	public Optional<Request> findRequest(List<Request> requests, Tool tool, int startTime, int endTime, Request request) {		
		  Optional<Request> chosen = requests
		            .stream()
		            .filter(r -> inTimeWindow(r.getStartTime(), r.getEndTime(), endTime)  && r.getTool().getId() == tool.getId())
		            .min((r1, r2) -> Integer.compare(tripDistance(request.getLocation(), r1.getLocation()), tripDistance(request.getLocation(), r2.getLocation())));
		  
		  return chosen;
	}
//	if (inTimeWindow(match.getStartTime(), match.getEndTime(), i + request.getUsageTime())) {
	
	/*
	 * Checks if the given trip violates the max distance constraint
	 */
	public boolean possibleTrip(Location l1, Location l2) {
		Vehicle vehicle = data.getVehicle();
		
		int distance = tripDistance(l1, l2);		
		
		return (distance <= vehicle.getMaxDistance());
	}
	
	public int tripDistance( Location l1, Location l2) {
		Global g = data.getGlobal();
		int distance = 0; 	
		distance += g.computeDistance(depot, l1);
		distance += g.computeDistance(l1, l2);
		distance += g.computeDistance(l2, depot);
		
		return distance;
	}
	
	
	
}
