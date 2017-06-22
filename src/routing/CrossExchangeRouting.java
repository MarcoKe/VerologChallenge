package routing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import analysis.RouteAnalyser;
import data.DataController;
import data.VehicleAction;
import data.VehicleAction.Action;
import data.VehicleInformation;
import util.RoutingUtil;

public class CrossExchangeRouting implements Routing {
	private static final int ITER_LIMIT = 1000; 
	private DataController data; 
	private RouteAnalyser routeAnalyser; 
	private Routing baseRouting;  // routing used to compute initial solution 
	private boolean done; 	
	private int[] maxTools; 	
	
	public CrossExchangeRouting(Routing baseRouting) {
		this.baseRouting = baseRouting; 
	}

	@Override
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
		List<VehicleInformation> r = baseRouting.getRouting(data, dataSet); 
		List<List<RoutingElement>> initial = new ArrayList<>(); 
		
		for (VehicleInformation vehicle : r) {
			List<RoutingElement> v = new ArrayList<>(); 
			
			for (VehicleAction action : vehicle.getRoute()) { 
				v.add(action); 
			}
			initial.add(v);
		}
		
		return route(data, initial);		
	}
		
	public List<VehicleInformation> route(DataController data, List<List<RoutingElement>> initial) {
		this.data = data; 
		routeAnalyser = new RouteAnalyser(data); 
		
		done = false; 
		List<List<RoutingElement>> modified = doCrossExchange(initial); 
		int iterations = 0; 
		while(!done && iterations < ITER_LIMIT) {
			modified = doCrossExchange(initial);
			iterations++; 			
		}
			
		return removeAllEmpty(convertSolution(cleanDepots(modified)));
	}
	
	public List<List<RoutingElement>> doCrossExchange(List<List<RoutingElement>> vehicleList) {
		initToolUsage(vehicleList);
		int bestFound = 0; 
		int bestVec1 = 0; 
		int bestVec2 = 0; 
		List<RoutingElement> bestRoute1 = new ArrayList<>(); 
		List<RoutingElement> bestRoute2 = new ArrayList<>(); 
		
		for (int vec1 = 0; vec1 < vehicleList.size(); vec1++) {					// for all pairs 
			for (int vec2 = vec1+1; vec2 < vehicleList.size(); vec2++) {        // of vehicles 
				
				int cost = getCost(vehicleList.get(vec1)) + getCost(vehicleList.get(vec2)); 
				
				for (int i = 0; i < vehicleList.get(vec1).size()-1; i++) {
					for (int j = 0; j < vehicleList.get(vec2).size()-1; j++) {
						List<RoutingElement> route1 = new ArrayList<>(vehicleList.get(vec1));
						List<RoutingElement> route2 = new ArrayList<>(vehicleList.get(vec2)); 
						
						RoutingElement tmp = route1.get(i+1);
						route1.set(i+1, route2.get(j+1));
						route2.set(j+1, tmp); 
						
						int newCost = getCost(route1) + getCost(route2);
						if (feasibleChange(route1, route2) && (cost-newCost > bestFound) ) { 
							bestFound = cost - newCost; 
							bestVec1 = vec1; 
							bestVec2 = vec2; 
							bestRoute1 = route1; 
							bestRoute2 = route2; 

						}
						
					}
				}
				
			}
		}	
		
		if (bestFound > 0) {
			vehicleList.set(bestVec1, bestRoute1);
			vehicleList.set(bestVec2, bestRoute2);
		}
		else {
			done = true; 
		}
		
		return vehicleList; 
	}
	
	public List<VehicleInformation> removeAllEmpty(List<VehicleInformation> in) {
		List<VehicleInformation> out = new ArrayList<>(); 
		
		for (VehicleInformation v : in) {
			boolean empty = true; 
			
			for (VehicleAction action : v.getRoute()) {
				if (action.getVehicleAction() != Action.TO_DEPOT) {
					empty = false; 
				}
			}
			
			if (!empty) out.add(v);
		}
		
		return out; 
	}
	
	public List<List<RoutingElement>> cleanDepots(List<List<RoutingElement>> solution) {
		for (List<RoutingElement> vehicle : solution) {
			vehicle = depotSensitivity(vehicle);
		}
		
		return solution;
	}
	
	/* will remove any depot visits that not beneficial 
	 * (also removes consecutive depot visits)
	 */
	public List<RoutingElement> depotSensitivity(List<RoutingElement> tour) {
		List<Integer> depotIndices = new ArrayList<>(); 
		
		for (int i = tour.size()-1; i > 0; i--) {
			if (tour.get(i).getRouteElement().size() == 1 && tour.get(i).getRouteElement().get(0).getVehicleAction() == Action.TO_DEPOT) {
				depotIndices.add(i); 
			}
		}
				
		for (int index :depotIndices) {
			List<RoutingElement> newTour = new ArrayList<>(tour); 
			newTour.remove(index); 
			if (feasibleChange(newTour)) {
				int oldCost = getCost(tour); 
				int newCost = getCost(newTour); 
				
				if (newCost <= oldCost) {
					System.out.println("yes");
					tour.remove(index);
				}
			}
		}
		
		return tour; 
	}
	
	public List<VehicleInformation> convertSolution(List<List<RoutingElement>> vehicleList) {
		List<VehicleInformation> converted = new ArrayList<>(); 
		
		for (List<RoutingElement> vehicle : vehicleList) {
			VehicleInformation vehicInfo = new VehicleInformation(RtElListToVehActList(vehicle));
			converted.add(vehicInfo); 
		}
		
		return converted; 
	}
	
	public List<VehicleAction> RtElListToVehActList(List<RoutingElement> list) {
		List<VehicleAction> actions = new ArrayList<>(); 
		
		for (RoutingElement el : list) {
			actions.addAll(el.getRouteElement()); 
		}
		
		return actions; 
	}
	
	public void initToolUsage(List<List<RoutingElement>> vehicles) {
		int[] maxToolUsage = routeAnalyser.getVehicleToolUsage(toVehicInfo(vehicles.get(0))); 
		for (List<RoutingElement> vehicle : vehicles) {
			int[] toolUsage = routeAnalyser.getVehicleToolUsage(toVehicInfo(vehicle)); 
			
			for (int i = 0; i < maxToolUsage.length; i++) {
				if (toolUsage[i] > maxToolUsage[i]) {
					maxToolUsage[i] = toolUsage[i]; 
				}
			}
			
		}
		
		
		this.maxTools = maxToolUsage; 
		
	}	
	
	public boolean feasibleChange(List<RoutingElement> v1, List<RoutingElement> v2) {		
		return RoutingUtil.isRoutePossible(data, toVehicInfo(v1)) && RoutingUtil.isRoutePossible(data, toVehicInfo(v2));	
	}
	
	public boolean feasibleChange(List<RoutingElement> v) {
		return RoutingUtil.isRoutePossible(data, toVehicInfo(v));
	}
	
	public int getCost(List<RoutingElement> vehicle) {		
		int travelDistance = RoutingUtil.getRouteStatistics(data, toVehicInfo(vehicle)).travelDistance; 
		int distanceCost =  travelDistance * data.getVehicle().getCostPerDistance(); 
		int vehicleDayCost = travelDistance > 0 ? data.getVehicle().getCostPerDay() : 0; 
		int[] toolUsage = routeAnalyser.getVehicleToolUsage(toVehicInfo(vehicle)); 
		int toolCost = 0;
//		List<Tool> tools = data.getToolList(); 
//		for (int i = 0; i < toolUsage.length; i++) {
//			if (maxTools[i] == toolUsage[i])
//				toolCost += toolUsage[i]*tools.get(i).getCost();
//		}
//		
		return distanceCost + vehicleDayCost + toolCost; 
	}
	
	public VehicleInformation toVehicInfo(List<RoutingElement> v) {
		List<VehicleAction> tour = new ArrayList<>(); 
		
		for (RoutingElement el : v) {
			tour.addAll(el.getRouteElement()); 
		}
		
		return new VehicleInformation(tour);
	}

	
	
}
