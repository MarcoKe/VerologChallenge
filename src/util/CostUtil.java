package util;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


import data.DayInformation;
import data.Location;

import data.DataController;

import data.OverallCost;
import data.StrategyController;
import data.Tool;
import data.VehicleAction;
import data.VehicleAction.Action;
import data.VehicleInformation;

public class CostUtil {

	public static OverallCost getOverallCost(DataController data, StrategyController strat) {

		OverallCost ret = null;
		int maxVehic = 0;
		int numVehicDays = 0;
		// setup toolUsageMap
		TreeMap<Integer, Integer> toolUsageMap = new TreeMap<>();
		TreeMap<Integer,Integer > roadToolUsage = new TreeMap<>(); //number of Tool on the road
		for (Tool tool : data.getToolList()) {
			toolUsageMap.put(tool.getId(), 0);
			roadToolUsage.put(tool.getId(), 0);
		}
		
		//TreeMap<Integer,Integer > maxToolUsage = new TreeMap<>(); // max number of Tool that had been on the road on same day
		
		long distance = 0;
		List<DayInformation> dayList = strat.getDays();
		for (int dayCnt = 0; dayCnt < dayList.size(); ++dayCnt) {
			// VEHICLE
			int currDayVehic = dayList.get(dayCnt).getVehicleInformationList().size();
			numVehicDays += currDayVehic;
			if (currDayVehic > maxVehic) {
				maxVehic = currDayVehic;
			}

			
			// TOOL USAGE and DISTANCE
			List<VehicleInformation> vehicList = dayList.get(dayCnt).getVehicleInformationList();
			List<TreeMap<Integer,Integer>> routePartCost = new ArrayList<>(vehicList.size());
			for (int vehicCnt = 0; vehicCnt < vehicList.size(); ++vehicCnt) {
				// DISTANCE
				List<VehicleAction> route = vehicList.get(vehicCnt).getRoute();
				TreeMap<Integer,Integer> vehiclePartCost = new TreeMap<Integer,Integer>();
				for(Tool tool : data.getToolList()){
					vehiclePartCost.put(tool.getId(), 0);
				}
				routePartCost.add(vehiclePartCost);

				VehicleAction prevAction = null;
				VehicleAction currAction = route.size() > 0 ? route.get(0) : null;
				Location prevLocation = null;
				Location currLocation = DataUtil.getActionLocation(data, currAction);
				
				addActionPartCost(currAction, vehiclePartCost);
				//addDayToolUsage(currAction, dayToolUsage);
				for (int i = 1; i < route.size(); ++i) {
					prevAction = currAction;
					currAction = route.get(i);
					prevLocation = currLocation;
					currLocation = DataUtil.getActionLocation(data, currAction); 
					distance += data.getGlobal().computeDistance(prevLocation, currLocation);
					
					addActionPartCost(currAction, vehiclePartCost);
				}
			}
			
			// FINALIZE TOOL USAGE
			List<Tool> toolList = data.getToolList();
			for(Tool t : toolList){
				int partsFromDepot = 0; // positive
				int partsToDepot = 0; // negative
				for(int i = 0; i < routePartCost.size();++i){
					int partAmount = routePartCost.get(i).get(t.getId());
					if(partAmount >=0){
						partsFromDepot+=partAmount;
					}else{
						partsToDepot += partAmount;
					}
				}
				int currOnRoad = roadToolUsage.get(t.getId()) + partsFromDepot;
				int currMax = toolUsageMap.get(t.getId());
				if(currOnRoad > currMax){
					toolUsageMap.put(t.getId(), currOnRoad);
				}
				currOnRoad+= partsToDepot;
				roadToolUsage.put(t.getId(), currOnRoad);			
			}

	
	

			
		}

				
		ret = new OverallCost(maxVehic, numVehicDays, toolUsageMap, distance, data);
		ret.calcTotalCost(data);
		return ret;
	}

	
	private static void addActionPartCost( VehicleAction action, TreeMap<Integer,Integer> dayToolUsage){
		if(action == null || action.getVehicleAction() == Action.TO_DEPOT){
			return;
		}
	
		int toolId = action.getRequest().getTool().getId();
		int sign = action.getVehicleAction() == Action.LOAD_AND_DELIVER ? 1:-1;
		int amount = sign * action.getRequest().getAmountOfTools();
		int newUsage = dayToolUsage.get(toolId)+amount;
		dayToolUsage.put(toolId, newUsage);
	}

}
