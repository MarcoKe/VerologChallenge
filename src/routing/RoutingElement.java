package routing;

import java.util.List;

import data.DataController;
import data.VehicleAction;

public abstract class RoutingElement {
	
	protected final static int LOCATION_DIMENSION = 2;
	protected double[] spaceVector = null;
	
	
	public abstract double[] getSpaceVector(DataController data);
	
	public abstract List<VehicleAction> getRouteElement();
	
}
