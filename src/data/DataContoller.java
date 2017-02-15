package data;

import java.util.List;

public class DataContoller {
	private final Depot depot;
	private final Global global;
	private final List<Location> locationList;
	private final List<Request> requestList;
	private final List<Tool> toolList;
	private final Vehicle vehicle;

	public DataContoller(Depot depot, Global global, List<Location> locList, List<Request> reqList, List<Tool> toolList,
			Vehicle vehicle) {
		this.depot=depot;
		this.global= global;
		this.locationList=locList;
		this.requestList=reqList;
		this.toolList=toolList;
		this.vehicle=vehicle;
	}

	public Depot getDepot() {
		return depot;
	}

	public final Global getGlobal() {
		return global;
	}

	public final List<Location> getLocationList() {
		return locationList;
	}

	public final List<Request> getRequestList() {
		return requestList;
	}

	public final List<Tool> getToolList() {
		return toolList;
	}

	public final Vehicle getVehicle() {
		return vehicle;
	}

}
