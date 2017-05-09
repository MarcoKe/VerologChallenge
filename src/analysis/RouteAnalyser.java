package analysis;

import java.util.List;

import data.DataController;
import data.DayInformation;
import data.Global;
import data.Location;
import data.StrategyController;
import data.Tool;
import data.VehicleAction;
import data.VehicleAction.Action;
import data.VehicleInformation;

public class RouteAnalyser {
	private DataController data; 
	
	public RouteAnalyser(DataController data) {
		this.data = data; 
	}
	
	public int getConstraintViolations(StrategyController solution) {
		return getDistanceViolations(solution) + getMaxToolViolations(solution);
		
	}
	
	public int getDistanceViolations(StrategyController solution) {
		int maxDistance = data.getVehicle().getMaxDistance();
		int violations = 0; 
		
		for (DayInformation day : solution.getDays()) {
			for (VehicleInformation vehicle : day.getVehicleInformationList()) {
				int vehicleDistance = routeDistance(vehicle.getRoute()); 
				if (vehicleDistance > maxDistance) {
					violations += vehicleDistance - maxDistance; 
				}
			}
		}
		
		return violations;
	}
	
	public int routeDistance(List<VehicleAction> actions) {
		Global g = data.getGlobal(); 
		Location depot = data.getLocationList().get(0);
		int distance = 0; 
		
		for (int i = 1; i < actions.size(); i++) {
			Location loc1 = actions.get(i-1).getVehicleAction() == Action.TO_DEPOT ? depot : actions.get(i-1).getRequest().getLocation();
			Location loc2 = actions.get(i).getVehicleAction() == Action.TO_DEPOT ? depot : actions.get(i).getRequest().getLocation();
			distance += g.computeDistance(loc1, loc2);
		}
		return distance; 
	}
	
	
	
	public int getMaxToolViolations(StrategyController solution) {
		int violations = 0; 
		int numTools = data.getToolList().size();
		int[] maxTools = new int[numTools]; 
		for (Tool tool : data.getToolList()) {
			maxTools[tool.getId()-1] = tool.getMaxAvailable(); 
		}
		
		
		// calculate tool usage 
		int[][] used = new int[numTools][solution.getDays().size()];
		for (DayInformation day : solution.getDays()) {
			for (VehicleInformation vehicle : day.getVehicleInformationList()) {
				int[] vehicleToolUsage = getVehicleToolUsage(vehicle);
				for (int i = 0; i < vehicleToolUsage.length; i++) {
					used[i][day.getDay()-1] += vehicleToolUsage[i]; 
				}			
				
			}
		}
		
		//calculate in between days (in between delivery & pickup)		
		for (DayInformation day : solution.getDays()) {
			for (VehicleInformation vehicle : day.getVehicleInformationList()) {
				for (VehicleAction action : vehicle.getRoute()) {
					if (action.getVehicleAction() == Action.LOAD_AND_DELIVER) {
						int start = day.getDay();
						int finish = start + action.getRequest().getUsageTime(); 
						
						for (int i = start+1; i < finish; i++) {
							used[action.getRequest().getTool().getId()-1][i-1] += action.getRequest().getAmountOfTools(); 
						}
					}
				}
			}
		}
		
		for (int day = 0; day < solution.getDays().size(); day++) {
			for (int i = 0; i < used.length; i++) {
				if(used[i][day] > maxTools[i]) {
					violations += used[i][day] - maxTools[i]; 
				}
			}
		}
		
		return violations; 
	}
	
	public int[] getVehicleToolUsage(VehicleInformation vehicle) {
		int numTools = data.getToolList().size();
		int[] toolsInVehicle = new int[numTools]; 
		int[] needed = new int[numTools];
		for (VehicleAction action : vehicle.getRoute()) {			
			if (action.getVehicleAction() == Action.PICK_UP) {				
				Tool tool = action.getRequest().getTool();
				toolsInVehicle[tool.getId()-1] += action.getRequest().getAmountOfTools(); 
				needed[tool.getId()-1] += action.getRequest().getAmountOfTools(); 
			}
			else if (action.getVehicleAction() == Action.LOAD_AND_DELIVER) { 				
				Tool tool = action.getRequest().getTool();
				int common = Math.min(action.getRequest().getAmountOfTools(), toolsInVehicle[tool.getId()-1]);
				toolsInVehicle[tool.getId()-1] -= common;
				needed[tool.getId()-1] += action.getRequest().getAmountOfTools() - common;  
			}			
			
		}
		
		return needed; 
	}
	
	
}
