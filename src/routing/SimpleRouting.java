package routing;

import java.util.LinkedList;
import java.util.List;

import data.DataController;
import data.VehicleAction;
import data.VehicleInformation;

public class SimpleRouting implements Routing{

	public List<VehicleInformation> getRouting(DataController data, List<VehicleAction> simpleLocs,
			List<MandatoryConnection> conLocs) {
		
		LinkedList<RoutingElement> dataSet = new LinkedList<>();
		if(simpleLocs != null){
			dataSet.addAll(simpleLocs);
		}
		if(conLocs!= null){
			dataSet.addAll(conLocs);
		}
		
		return getRouting(data, dataSet);
		
	/*	//MandatoryConnections
		if(conLocs != null){
			for(MandatoryConnection manCon: conLocs){
				VehicleInformation vehicInfo = new VehicleInformation();
				
				for(VehicleAction pick : manCon.getPickupList()){
					vehicInfo.addAction(pick);
				}
				for(VehicleAction deliver : manCon.getDeliverList()){
					vehicInfo.addAction(deliver);
				}
				ret.add(vehicInfo);
			}			
		}
		
		// simpleLocations
		if(simpleLocs != null){
			for(VehicleAction simple : simpleLocs ){
				VehicleInformation vehicInfo = new VehicleInformation();
				vehicInfo.addAction(simple);
				ret.add(vehicInfo);
			}
		}
		
		return ret;*/
	}

	public List<VehicleInformation> getRouting(DataController data, List<RoutingElement> dataSet) {
		
		List<VehicleInformation> ret = new LinkedList<>();
		for(RoutingElement rtElem : dataSet){
			ret.add(new VehicleInformation(rtElem.getRouteElement()));
		}
		
		return ret;
	}
	
	


	
}
