package routing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import data.DataController;
import data.VehicleAction;
import data.VehicleInformation;

public class SimpleRouting implements Routing{

	public List<VehicleInformation> getRouting(DataController data, List<VehicleAction> simpleLocs,
			List<MandatoryConnection> conLocs) {
		List<VehicleInformation> ret = new LinkedList<>();
		
		//MandatoryConnections
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
		
		return ret;
	}

	
}
