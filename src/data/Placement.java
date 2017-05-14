package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import data.VehicleAction.Action;

public class Placement {
	private Map<Request, Integer> placement; 
	
	public Placement() {
		placement = new HashMap<>();
	}
	
	public Placement(Map<Request, Integer> placement) {
		this.placement = placement; 
	}
	
	public Placement(List<Request> requestList) {
		this.placement = new HashMap<>(); 
		
		for (Request request : requestList) {
			this.placement.put(request, 0);
		}
	}
	
	public void changePlacement(Request request, int day) {
		placement.put(request, day); 
	}
	
	public Map<Request, Integer> getPlacement() {
		return placement; 
	}
	
	public void add(Map<Request, Integer> additional) {
		for (Request r : additional.keySet()) {
			placement.put(r, additional.get(r)); 
		}
	}
	
	public Map<Integer, List<VehicleAction>> getVehicleActionList() {
		Map<Integer, List<VehicleAction>> actionsByDay = new TreeMap<>(); 
		
		for (Request r : placement.keySet()) {
			int deliveryDay = placement.get(r);
			int pickupDay = deliveryDay + r.getUsageTime(); 
			
			VehicleAction delivery = new VehicleAction(Action.LOAD_AND_DELIVER, r);
		    actionsByDay.computeIfAbsent(deliveryDay, v -> new ArrayList<VehicleAction>()).add(delivery);

			VehicleAction pickup = new VehicleAction(Action.PICK_UP, r); 
			actionsByDay.computeIfAbsent(pickupDay, v -> new ArrayList<VehicleAction>()).add(pickup);			
			
		}
		
		return actionsByDay; 
	}
}
