package routing;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

import data.DataController;
import data.VehicleAction;
import data.VehicleAction.Action;
import data.VehicleInformation;
import util.RoutingUtil;

public class CircularRouting implements Routing {

	
	
	public List<VehicleInformation> getRouting(DataController data, List<VehicleAction> simpleLocations,
			List<MandatoryConnection> connectedLocations) {
		
		LinkedList<RoutingElement> dataSet = new LinkedList<>();
		if(simpleLocations != null){
			dataSet.addAll(simpleLocations);
		}
		if(connectedLocations != null){
			dataSet.addAll(connectedLocations);
		}
		
		double[] centerOfMass = RoutingUtil.getCenterOfMass(data, dataSet);
		double[] axisVec = getDirectionVector(centerOfMass, data.getDepot().getSpaceVector(data));

		Collections.sort(dataSet, new Comparator<RoutingElement>() {
						
			public int compare(RoutingElement o1, RoutingElement o2) {
				double[] angle = new double[2];
				double[] vec = getDirectionVector(centerOfMass, o1.getSpaceVector(data));
				angle[0] = getAngle(axisVec, vec);
				//TODO compare					
				vec = getDirectionVector(centerOfMass, o2.getSpaceVector(data));
				angle[1] = getAngle(axisVec, vec);
				
				return (int) Math.signum(angle[0] - angle[1]);
			}
		});
		
		List<VehicleInformation> ret = new LinkedList<>();
		
		List<VehicleAction> currRoute = new LinkedList<>();
		while(!dataSet.isEmpty()){
			RoutingElement rtElem = dataSet.pop();
			List<VehicleAction> testRoute = new LinkedList<>(currRoute);
			testRoute.addAll(rtElem.getRouteElement());
			VehicleInformation testVehicInfo = new VehicleInformation(testRoute);
			boolean routePossible = RoutingUtil.isRoutePossible(data, testVehicInfo);
			if(routePossible){
				currRoute = testRoute;
			}else{
				testRoute.add(testRoute.size()-1,new VehicleAction(Action.TO_DEPOT, null));
				testVehicInfo = new VehicleInformation(testRoute);
				routePossible = RoutingUtil.isRoutePossible(data, testVehicInfo);
				if(routePossible){
					currRoute = testRoute;
				}else{
					// currRoute can't be improved
					if(currRoute.isEmpty()){
						VehicleInformation error = new VehicleInformation(testRoute);
						throw new RuntimeException("Route \""+error.write(-1)+ "\" is not Possible");
					}
					ret.add(new VehicleInformation(currRoute));
					currRoute = new LinkedList<>();
				}
			}
//			else{
//				//try reversing last roundtrip
//				reverseLastRoundtrip(testRoute);
//				testVehicInfo = new VehicleInformation(testRoute);
//				routePossible = RoutingUtil.isRoutePossible(data, testVehicInfo);
//				if(routePossible){
//					currRoute = testRoute;
//					isReversed = true;
//				}
//			}
		}
		
		
		return ret;
	}
	
	private void reverseLastRoundtrip(List<VehicleAction> route){
		int i = getLastDepot(route);
		int j = route.size()-1;
		if(route.get(i).getVehicleAction() == Action.TO_DEPOT){
			++i;
		}
		if(route.get(j).getVehicleAction() == Action.TO_DEPOT){
			--j;
		}
				
		while(i<j){
			Collections.swap(route, i++, j--);
		}
	}

	/**
	 * return the index of the last Action.TO_DEPOT ignoring the Action.TO_DEPOT on the last position
	 * @param route
	 * @return
	 */
	private int getLastDepot(List<VehicleAction> route){
		int ret = route.size()-1;
		while(ret > 0 && route.get(--ret).getVehicleAction() != Action.TO_DEPOT );
		return ret;
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
