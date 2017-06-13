package util;

import data.DataController;
import data.Location;
import data.VehicleAction;
import data.VehicleAction.Action;

public class DataUtil {

	/**
	 * Get Location of given VehicleAction
	 * 
	 * @param data
	 * @param action
	 * @return Location of given action or Location of Depot if action is NULL
	 */
	public static Location getActionLocation(DataController data, VehicleAction action) {
		Location ret = data.getDepot().getLocation();
		if (action != null && action.getVehicleAction() != Action.TO_DEPOT) {
			ret = action.getRequest().getLocation();
		}
		return ret;
	}
	
	
	public static int getActionCapacity(/*DataController data,*/ VehicleAction action){
		int ret = 0;
		if(action != null && action.getRequest() != null){
			ret = action.getRequest().getAmountOfTools();
			ret*= action.getRequest().getTool().getSize();
		}
		return ret;
	}
	
}
