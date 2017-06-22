package data;

import java.util.LinkedList;
import java.util.List;

import routing.RoutingElement;
import util.DataUtil;

public class VehicleAction extends RoutingElement {

	public enum Action {
		LOAD_AND_DELIVER, PICK_UP, TO_DEPOT
	}

	private Action vehicleAction;
	private Request request;

	public VehicleAction(Action vehicAction, Request req) {
		this.vehicleAction = vehicAction;
		this.request = req;
	}

	public final Action getVehicleAction() {
		return vehicleAction;
	}

	public final Request getRequest() {
		return request;
	}

	public String toString() {

		int ret = 0;
		if (vehicleAction != Action.TO_DEPOT) {
			// if else statement in one line
			ret = vehicleAction == Action.LOAD_AND_DELIVER ? request.getId() : -request.getId();
		}
		return "" + ret;
	}

	@Override
	public double[] getSpaceVector(DataController data) {
		if(spaceVector == null){
			spaceVector = new double[data.getToolList().size()+LOCATION_DIMENSION];
			Location loc = request.getLocation();
			spaceVector[0] = loc.getX();
			spaceVector[1] = loc.getY();
			for(int i=0;i<spaceVector.length-LOCATION_DIMENSION;++i){
				boolean toolPos = data.getToolList().get(i).getId() == request.getTool().getId();
				spaceVector[i+LOCATION_DIMENSION]= toolPos ? DataUtil.getActionCapacity(this) : 0;
			}			
		}
		return spaceVector;
	}

	
	public List<VehicleAction> getRouteElement() {
		List<VehicleAction> ret = new LinkedList<>();
		ret.add(this);
		return ret;
	}

}
