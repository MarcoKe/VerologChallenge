package routing;

import java.util.List;

import data.DataController;
import data.VehicleAction;
import data.VehicleInformation;

public interface Routing {

	
	public List<VehicleInformation> getRouting(DataController data, List<VehicleAction> simpleLocations, List<MandatoryConnection> connectedLocations);

	public List<VehicleInformation> getRouting(DataController data, List<RoutingElement> dataSet);

}