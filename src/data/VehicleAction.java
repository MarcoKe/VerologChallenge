package data;

public class VehicleAction {

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
}
