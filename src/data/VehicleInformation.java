package data;

import java.util.LinkedList;
import java.util.List;

import data.VehicleAction.Action;

public class VehicleInformation {

	private List<VehicleAction> route;

	// TODO Additional debug information (see paper)

	public VehicleInformation() {
		route = new LinkedList<>();
		route.add(new VehicleAction(Action.TO_DEPOT, null));
		route.add(route.size(), new VehicleAction(Action.TO_DEPOT, null));
	}
	
	public VehicleInformation(List<VehicleAction> route){
		this.route = new LinkedList<>();
		this.route.addAll(route);
		if(this.route.get(0).getVehicleAction() != Action.TO_DEPOT){
			this.route.add(0,new VehicleAction(Action.TO_DEPOT, null));
		}
		if(this.route.get(this.route.size()-1).getVehicleAction() != Action.TO_DEPOT){
			this.route.add(this.route.size(), new VehicleAction(Action.TO_DEPOT, null));
		}
	}

	public void addAction(VehicleAction vehicAct) {
		route.add(route.size()-1,vehicAct);
	}	

	public VehicleAction removeAction(int index) {
		return route.remove(index);
	}

	public final List<VehicleAction> getRoute() {
		return route;
	}

	public String write(int vehicId) {
		StringBuilder sb = new StringBuilder();
		// write Route
		sb.append(vehicId + " R");
		for (int i = 0; i < route.size(); ++i) {
			sb.append(" " + route.get(i).toString());
		}
		return sb.toString();
	}

}
