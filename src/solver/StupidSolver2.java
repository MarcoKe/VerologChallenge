package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import data.DataContoller;
import data.DayInformation;
import data.Request;
import data.StrategyController;
import data.VehicleAction;
import data.VehicleAction.Action;
import data.VehicleInformation;
import data.Global;


// hacked together, do not use as a reference 
public class StupidSolver2 implements Solver {
	
	public StupidSolver2() {

	}

	public StrategyController solve(DataContoller data) {
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
			
			// deliver all requests for the day 
			List<Request> delivered = new ArrayList<>();
			int trucksUsed = 0; 
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
			List<Request> completedPickups = new ArrayList<>();
			for (Request p : pickups) {
				if (p.getStartTime() + p.getUsageTime() == i+1) {
					trucksUsed++; 
					completedPickups.add(p);
					distance += 2.0* (g.computeDistance(data.getLocationList().get(0), p.getLocation()));
					
					VehicleInformation vehicInfo = new VehicleInformation();
					VehicleAction action = new VehicleAction(Action.PICK_UP, p);
					vehicInfo.addAction(action);
					day.addVehicleInformation(vehicInfo);

				}

			}

			if (trucksUsed > maxVehicles) {
				maxVehicles = trucksUsed; 
			}

			numVehicleDays += trucksUsed; 

			vehiclesUsed[i] = trucksUsed; 
			trucksUsed = 0; 
			pickups.removeAll(completedPickups);
			pickups.addAll(delivered);
			requests.removeAll(delivered);

		}

		return new StrategyController(dayList);
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
}
