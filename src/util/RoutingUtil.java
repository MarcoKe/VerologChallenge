package util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import data.DataController;
import data.Location;
import data.VehicleAction;
import data.VehicleAction.Action;
import routing.RouteStatistics;
import data.VehicleInformation;

public class RoutingUtil {

	// public static void getSmallestPartCostBruteforce(DataController data,
	// List<VehicleAction> dataSet ){
	//
	// List<VehicleAction> pickUpList = new ArrayList<>(),
	// deliverList = new ArrayList<>();
	//
	// int pickUpSum = 0;
	// int deliverSum = 0;
	// for(VehicleAction action: dataSet){
	// if(action.getVehicleAction() == Action.PICK_UP){
	// pickUpSum+=DataUtil.getActionCapacity(action);
	// pickUpList.add(action);
	// }else if(action.getVehicleAction() == Action.LOAD_AND_DELIVER){
	// deliverSum+= DataUtil.getActionCapacity(action);
	// deliverList.add(action);
	// }
	// }
	//
	// }

	public static int getVehicleInformationCost(DataController data, VehicleInformation vehicInfo) {
		int ret = 0;
		int currCapacity = 0;
		ret += data.getVehicle().getCostPerDay();
		for (int i = 1; i < vehicInfo.getRoute().size(); ++i) {
			Location loc1 = DataUtil.getActionLocation(data, vehicInfo.getRoute().get(i - 1));
			Location loc2 = DataUtil.getActionLocation(data, vehicInfo.getRoute().get(i));
			ret += data.getGlobal().computeDistance(loc1, loc2);
		}
		return ret;
	}

	public static RouteStatistics getRouteStatistics(DataController data, VehicleInformation vehicInfo){
		// toolcapacity the vehicle has because of pickups
		Map<Integer, Integer> pickupCapacity = new HashMap<>();
		// Capacity the vehicle has because of delivering requests at the beginning of a round trip
		int fromDepotLoad = 0;
		// the maximum a roundtrip had on pickupload
		int maxVehicLoad = 0;
		int travelDistance = 0;
		//roundtripIndex {from, to}
		int rtIdx[] = {0,0};
		
				
		for(int i = 0; i< vehicInfo.getRoute().size();++i){
			VehicleAction action = vehicInfo.getRoute().get(i);
			int toolId = -1;
			int reqCapacity = 0;
			if(action.getRequest() != null){
				toolId = action.getRequest().getTool().getId();
				reqCapacity = DataUtil.getActionCapacity(action);
				if(!pickupCapacity.containsKey(toolId)){
					pickupCapacity.put(toolId, 0);
				}
			}
			//Calculate fromDepotLoad with PICK_UP and LOAD_AND_DELIVER, then simulate the roundtrip with that fromDepotLoad at the start
			if(action.getVehicleAction() == Action.PICK_UP){
				int pickupCapacityUpdate = pickupCapacity.get(toolId) + reqCapacity;
				pickupCapacity.put(toolId, pickupCapacityUpdate);	
				
			}else if(action.getVehicleAction() == Action.LOAD_AND_DELIVER){
				//remaining capacity of pickupCapacity (negativ -> tool from Depot, positiv -> from pickup)
				int diff = pickupCapacity.get(toolId) - reqCapacity;
				pickupCapacity.put(toolId,Math.max(0, diff));
				//increase loadsize from depot onwards,if diff < 0
				fromDepotLoad += Math.max(0, -1*diff); 
				
			}else{
				int currLoad = fromDepotLoad;
				rtIdx[1] = i;
				while(rtIdx[0] < rtIdx[1]){
					++rtIdx[0];
					VehicleAction tmpAction = vehicInfo.getRoute().get(rtIdx[0]);
					int loadMod = tmpAction.getVehicleAction() == Action.PICK_UP ? 1 : -1;
					int tmpReqCapacity = loadMod * DataUtil.getActionCapacity(tmpAction);
					currLoad += tmpReqCapacity;
					maxVehicLoad = Math.max(maxVehicLoad, currLoad);	
				}
				
				fromDepotLoad = 0;
				pickupCapacity.clear();
			}
						
			if(i != 0){
				Location prevLoc = DataUtil.getActionLocation(data, vehicInfo.getRoute().get(i-1));
				Location currLoc = DataUtil.getActionLocation(data, action);
				travelDistance+= data.getGlobal().computeDistance(prevLoc, currLoc);
			}
			
		}

		
		return new RouteStatistics(travelDistance,maxVehicLoad);
		
	}
	
	
	public static boolean isRoutePossible(DataController data, VehicleInformation vehicInfo) {
		boolean ret = true;
		RouteStatistics rtStat = getRouteStatistics(data, vehicInfo);
		
		if(rtStat.travelDistance > data.getVehicle().getMaxDistance()){
			ret = false;
		}
		
		if(rtStat.maxVehicleLoad > data.getVehicle().getCapacity()){
			ret = false;
		}
		return ret;
	}
}
