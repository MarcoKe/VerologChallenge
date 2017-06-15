package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import data.VehicleAction.Action;
import routing.RoutingElement;

public class Depot extends RoutingElement{

	private Location location;
	private HashMap<Tool, Integer> toolAvailable;

	public Depot(Location location, HashMap<Tool, Integer> toolAvailable) {
			this.location=location;
			this.toolAvailable=toolAvailable;
			
					
	}

	public final Location getLocation() {
		return location;
	}

	public final HashMap<Tool, Integer> getToolAvailable() {
		return toolAvailable;
	}

	@Override
	public double[] getSpaceVector(DataController data) {
		if(spaceVector == null){
			spaceVector = new double[data.getToolList().size()+LOCATION_DIMENSION];
			spaceVector[0] = location.getX();
			spaceVector[1] = location.getY();
			for(int i = LOCATION_DIMENSION ; i<spaceVector.length;++i){
				spaceVector[i] = 0;
			}
		}
		return spaceVector;
	}

	@Override
	public List<VehicleAction> getRouteElement() {
		List<VehicleAction> ret = new ArrayList<>();
		ret.add(new VehicleAction(Action.TO_DEPOT, null));
		return ret;
	}
}
