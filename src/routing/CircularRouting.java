package routing;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

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
		return getRouting(data, dataSet);		
	}
	
	
	@Override
	public List<VehicleInformation> getRouting(DataController data, List<RoutingElement> dataSet) {
		double[] centerOfMass = RoutingUtil.getCenterOfMass(data, dataSet);
		double[] axisVec = getDirectionVector(centerOfMass, data.getDepot().getSpaceVector(data));

		Collections.sort(dataSet, new Comparator<RoutingElement>() {
						
			public int compare(RoutingElement o1, RoutingElement o2) {
				double[] angle = new double[2];
				// o1 element
				double[] vec = getDirectionVector(centerOfMass, o1.getSpaceVector(data));
				angle[0] = getAngle(axisVec, vec);
				// o2 element
				vec = getDirectionVector(centerOfMass, o2.getSpaceVector(data));
				angle[1] = getAngle(axisVec, vec);
				
				return (int) Math.signum(angle[0] - angle[1]);
			}
		});
		
		List<VehicleInformation> ret = new LinkedList<>();
		
		List<VehicleAction> currRoute = new LinkedList<>();
		boolean isReversed = false;
		while(!dataSet.isEmpty()){
			RoutingElement rtElem = dataSet.get(0);
			List<VehicleAction> testRoute = new LinkedList<>(currRoute);
			
			if(!isReversed){
				testRoute.addAll(rtElem.getRouteElement());				
			}else{
				int insert = getLastDepot(testRoute);
				if(testRoute.get(insert) != null && testRoute.get(insert).getVehicleAction() == Action.TO_DEPOT){
					++insert;
				}
				testRoute.addAll(insert,rtElem.getRouteElement());
			}

			VehicleInformation testVehicInfo = new VehicleInformation(testRoute);
			boolean routePossible = RoutingUtil.isRoutePossible(data, testVehicInfo);
			if(routePossible){
				currRoute = testRoute;
				dataSet.remove(0);
			}
			
			if(!routePossible){
				if(!isReversed){
					reverseLastRoundtrip(testRoute);
					testVehicInfo = new VehicleInformation(testRoute);
					routePossible = RoutingUtil.isRoutePossible(data, new VehicleInformation(testRoute));
					if(routePossible){
						currRoute = testRoute;
						dataSet.remove(0);
						isReversed = true;
					}
				}
			}
			
			if(!routePossible){
				testRoute.removeAll(rtElem.getRouteElement());
				testRoute.add(new VehicleAction(Action.TO_DEPOT, null));
				testRoute.addAll(rtElem.getRouteElement());
				
				testVehicInfo = new VehicleInformation(testRoute);
				routePossible = RoutingUtil.isRoutePossible(data, testVehicInfo);
				if(routePossible){
					currRoute = testRoute;
					dataSet.remove(0);	
					isReversed = false;
				}
			}
	
			if(!routePossible){
				// currRoute can't be improved
				if(currRoute.isEmpty()){
					VehicleInformation error = new VehicleInformation(testRoute);
					throw new RuntimeException("Route \""+error.write(-1)+ "\" is not Possible");
				}
				ret.add(new VehicleInformation(currRoute));
				currRoute = new LinkedList<>();
				isReversed = false;
			}

		}
		
		if(!currRoute.isEmpty()){
			ret.add(new VehicleInformation(currRoute));
		}		
		return ret;
	}
	
	
	private void reverseLastRoundtrip(List<VehicleAction> route){
		int i = getLastDepot(route);
		int j = route.size()-1;
		if(route.get(i) != null && route.get(i).getVehicleAction() == Action.TO_DEPOT){
			++i;
		}
		if(route.get(i) != null && route.get(j).getVehicleAction() == Action.TO_DEPOT){
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
		//angleRelative to baseVector	
		double angleRelBase = Math.atan2(directVec[1], directVec[0]) - Math.atan2(baseVec[1],baseVec[0]);		
		if(angleRelBase<0){
			angleRelBase += 2*Math.PI;
		}
		return angleRelBase;
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
