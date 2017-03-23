package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import data.DataController;
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
public class StupidSolver2 implements Solver {
	
	DataController data; 
	Location depot; 
	
	public StupidSolver2() {

	}

	public StrategyController solve(DataController d) {
		data = d;
		depot = data.getLocationList().get(0);
		
		List<DayInformation> dayList = new ArrayList<>();
		
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
			DayInformation day = new DayInformation(i+1);
			dayList.add(day);
			
			
			List<Request> delivered = new ArrayList<>();
			int trucksUsed = 0; 
			
			List<Request> completedPickups = new ArrayList<>();
			
			// perform all scheduled pickups for the day
			for (Request p : pickups) {
				if (p.getStartTime() + p.getUsageTime() == i+1) {
					trucksUsed++; 
					completedPickups.add(p);
					int tripDistance = g.computeDistance(data.getLocationList().get(0), p.getLocation());
					
					VehicleInformation vehicInfo = new VehicleInformation();
					VehicleAction action = new VehicleAction(Action.PICK_UP, p);
					vehicInfo.addAction(action);					
					
					// can we immediately deliver the tools we just picked up to another customer?
					Optional<Request> possibleRequest = findRequest(requests, p.getTool(), i+1, p);
					if (possibleRequest.isPresent()) {
						Request request = possibleRequest.get();
						List<Location> route = new ArrayList<>();
						route.add(p.getLocation()); 
						route.add(request.getLocation());
						
						if (possibleTrip(p.getLocation(), request.getLocation())) {
							requests.remove(request);  // so we don't deliver the same request twice in this loop
							delivered.add(request);    // so we can add all delivered ones to the pickup list later 
							tripDistance += g.computeDistance(p.getLocation(), request.getLocation());
							tripDistance += g.computeDistance(request.getLocation(), data.getLocationList().get(0));
							VehicleAction action2 = new VehicleAction(Action.LOAD_AND_DELIVER, request);
							vehicInfo.addAction(action2);
						}
						else {
							tripDistance *= 2; 
						}
						
					}
					else {
						tripDistance *= 2; 
					}
					
					distance += tripDistance;
					
					
					day.addVehicleInformation(vehicInfo);

				}

			}			
			pickups.removeAll(completedPickups);		
			
			
			// deliver all (remaining) requests for the day 
			for (Request request : requests) {
				if (request.getStartTime() == i+1) {
					// 
					delivered.add(request); 	
					trucksUsed++;
					distance += 2.0* (g.computeDistance(data.getLocationList().get(0), request.getLocation()));
					
					VehicleInformation vehicInfo = new VehicleInformation();
					VehicleAction action = new VehicleAction(Action.LOAD_AND_DELIVER, request);
					vehicInfo.addAction(action);					
					day.addVehicleInformation(vehicInfo);
				}

			}

			// pick up any tools from customers scheduled for the day 
			// I'm abusing the Request class to hold Pickups here, not a great idea 
			

			if (trucksUsed > maxVehicles) {
				maxVehicles = trucksUsed; 
			}

			numVehicleDays += trucksUsed; 

			vehiclesUsed[i] = trucksUsed; 
			trucksUsed = 0; 
			
			pickups.addAll(delivered);
			requests.removeAll(delivered);

		}

		return new StrategyController(dayList);
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
	 * ideally we'd be selecting the request that minimizes the total distance 
	 */
	public Optional<Request> findRequest(List<Request> requests, Tool tool, int startTime, Request request) {		
		  Optional<Request> chosen = requests
		            .stream()
		            .filter(r -> r.getStartTime() == startTime  && r.getTool().getId() == tool.getId())
		            .min((r1, r2) -> Integer.compare(tripDistance(request.getLocation(), r1.getLocation()), tripDistance(request.getLocation(), r2.getLocation())));
		  
		  return chosen;
	}
	
	
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
