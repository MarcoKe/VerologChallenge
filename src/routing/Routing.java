package routing;

import java.util.LinkedList;
import java.util.List;

import data.DataController;
import data.VehicleAction;
import data.VehicleInformation;

public interface Routing {

	
	public List<VehicleInformation> getRouting(DataController data, List<VehicleAction> simpleLocations, List<MandatoryConnection> connectedLocations);
}

class MandatoryConnection{
	private List<VehicleAction> pickupList;
	private List<VehicleAction> deliverList;
	
	public MandatoryConnection() {
		pickupList = new LinkedList<>();
		deliverList = new LinkedList<>();
	}
	
	public MandatoryConnection(List<VehicleAction> pickUpList, List<VehicleAction> deliverList ){
		this.pickupList = pickUpList;
		this.deliverList = deliverList;
	}
	
	public void addPickupList(VehicleAction pickup){
		pickupList.add(pickup);
	}
	
	public void addDeliverList(VehicleAction deliver){
		deliverList.add(deliver);
	}

	public final List<VehicleAction> getPickupList() {
		return pickupList;
	}

	public final List<VehicleAction> getDeliverList() {
		return deliverList;
	}

	
	
	
	
}