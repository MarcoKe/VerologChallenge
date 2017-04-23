package routing;

import java.util.List;

import data.DataController;
import data.VehicleAction;

public interface Routing {

	
	public List<List<VehicleAction>> getRouting(DataController data, List<VehicleAction> locations);
}
