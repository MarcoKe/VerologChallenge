package routing;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


import data.DataController;
import data.VehicleAction;
import data.VehicleInformation;
import util.RoutingUtil;

public class CircularRouting implements Routing {

	
	
	public List<VehicleInformation> getRouting(DataController data, List<VehicleAction> simpleLocations,
			List<MandatoryConnection> connectedLocations) {
		
		List<RoutingElement> dataSet = new LinkedList<>(simpleLocations);
		dataSet.addAll(connectedLocations);
		
		double[] centerOfMass = RoutingUtil.getCenterOfMass(data, dataSet);
		double[] axisVec = getDirectionVector(centerOfMass, data.getDepot().getSpaceVector(data));

		Collections.sort(dataSet, new Comparator<RoutingElement>() {
			
			
			public int compare(RoutingElement o1, RoutingElement o2) {
				double[] angle = new double[2];
				double[] vec = getDirectionVector(centerOfMass, o1.getSpaceVector(data));
				//TODO compare
				
				return 0;
			}
		});
		
		
		
		
		
		
		return null;
	}
	
	private double getAngle(double[] baseVec, double[] directVec){
		double dotProct = 0;
		double baseLength = 0;
		double directLength = 0;
		for(int i=0;i<baseVec.length;i++){
			dotProct=+ baseVec[i]*directVec[i];
			baseLength=+ baseVec[i]*baseVec[i];
			directLength=+ directVec[i]*directVec[i];
		}
		baseLength = Math.sqrt(baseLength);
		directLength = Math.sqrt(directLength);
		
		return Math.acos(dotProct/(baseLength*directLength));
	}
	
	private double[] getDirectionVector(double[] from, double[] to){
		assert from.length == to.length;
		double[] ret = to.clone();
		for(int i =0; i<ret.length ; ++i){
			ret[i] = ret[i]-from[i];
		}
		return ret;
	}

}
