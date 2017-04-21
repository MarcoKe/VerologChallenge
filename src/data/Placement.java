package data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Placement {
	private Map<Request, Integer> placement; 
	
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
}
