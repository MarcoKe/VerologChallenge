package routing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import data.DataController;
import data.DayInformation;
import data.StrategyController;
import data.VehicleAction;
import data.VehicleInformation;
import util.CostUtil;
import util.RoutingUtil;

public class SimulatedAnnealingRouting implements Routing {
	private DataController data; 	
	
	public List<VehicleInformation> getRouting(DataController data, List<VehicleAction> simpleLocations, List<MandatoryConnection> connectedLocations) {
		this.data = data; 	
		
		List<List<List<VehicleAction>>> initialSolution = initialSolution(simpleLocations, connectedLocations); 
		List<List<List<VehicleAction>>> finalSolution = simulatedAnnealing(data, initialSolution, 100000,0.001);
				
		// convert solution to standard format 
		List<VehicleInformation> solution = new ArrayList<>();
		for (List<List<VehicleAction>> vehicle : finalSolution) {
			if (vehicle.size() == 0) continue; 
			VehicleInformation vehicInfo = new VehicleInformation(); 
			
			for (List<VehicleAction> metaAction : vehicle) {
				for (VehicleAction action : metaAction) {
					vehicInfo.addAction(action);
				}
			}
			
			solution.add(vehicInfo);
		}
		
		return solution; 		
	}
	
	// actions are represented as list of actions. that way we can treat mandatory connections as a single actions and don't have to 
	// worry about breaking anything 
	public List<List<List<VehicleAction>>> initialSolution(List<VehicleAction> simpleLocations, List<MandatoryConnection> connectedLocations) {
		List<List<List<VehicleAction>>> vehicles = new ArrayList<>(); 
		
		// add simpleLocations (one action = one vehicle)
		for (VehicleAction action : simpleLocations) {
			List<List<VehicleAction>> vehicle = new ArrayList<>(); 
			List<VehicleAction> vehicleActions = new ArrayList<>(); 
			vehicleActions.add(action);			
			vehicle.add(vehicleActions);
			vehicles.add(vehicle); 
		}
		
		for (MandatoryConnection manCon : connectedLocations) {			 
			 List<VehicleAction> vehicleActions = new ArrayList<>(manCon.getRoute());
			 List<List<VehicleAction>> vehicle = new ArrayList<>(); 
			 vehicle.add(vehicleActions);
			 vehicles.add(vehicle);
		}
		
		return vehicles; 		
	}
		
	public long energyFunc(List<List<List<VehicleAction>>> solution) {
		List<VehicleInformation> newVehicles = new ArrayList<>(); 
		
		for (List<List<VehicleAction>> vehicle : solution) {
			if (vehicle.size() == 0) continue; 
			VehicleInformation newVehicle = new VehicleInformation(); 
			for (List<VehicleAction> actions : vehicle) {			
				for (VehicleAction action : actions) {
					newVehicle.addAction(action);
				}
			
				newVehicles.add(newVehicle);			
			}					
		}
		
		DayInformation day = new DayInformation(1); 
		day.addAllVehickeInformation(newVehicles);
		List<DayInformation> days = new ArrayList<>(); 
		days.add(day); 
		
		return CostUtil.getOverallCost(data, new StrategyController(days)).calcTotalCost(data);		
	}
	
	public boolean feasibleChange(List<List<VehicleAction>> newVehicle) {
		VehicleInformation vehicInfo = new VehicleInformation(); 
		
		for (List<VehicleAction> metaAction : newVehicle) {
			for (VehicleAction action : metaAction) {
				vehicInfo.addAction(action);
			}
		}
		
		return RoutingUtil.isRoutePossible(data, vehicInfo);	
	}
	
	public List<List<List<VehicleAction>>> cloneList(List<List<List<VehicleAction>>> input) {
		List<List<List<VehicleAction>>> output = new ArrayList<>(); 
		
		
		for (List<List<VehicleAction>> oldList1 : input) {
			List<List<VehicleAction>> newList1 = new ArrayList<>();
			for (List<VehicleAction> oldList2 : oldList1) {
				List<VehicleAction> newList2 = new ArrayList<>(); 
				for (VehicleAction action : oldList2) {
					newList2.add(action);
				}
				newList1.add(newList2);
			}
			output.add(newList1);
		}
		
		return output;
		
	}
		
	public List<List<List<VehicleAction>>> simulatedAnnealing(DataController data, List<List<List<VehicleAction>>> initialSolution, double temperature, double coolingRate) {
		List<List<List<VehicleAction>>> currentSolution = initialSolution;
		List<List<List<VehicleAction>>> bestSolution = cloneList(initialSolution);
		double bestEnergy = energyFunc(currentSolution);
		double currentEnergy = bestEnergy; 
		
		while (temperature > 1.0) {
			List<List<List<VehicleAction>>> newSolution = cloneList(currentSolution);
			
			// select random vehicle			
			int vehicleIndex =  ThreadLocalRandom.current().nextInt(0, newSolution.size());
			
			// bandaid fix. sometimes there are empty vehicles, not sure why
			while (newSolution.get(vehicleIndex).size() == 0) {
				newSolution.remove(vehicleIndex); 
				vehicleIndex = ThreadLocalRandom.current().nextInt(0, newSolution.size());
			}
			
			
			// select random action 
			int actionIndex =  ThreadLocalRandom.current().nextInt(0, newSolution.get(vehicleIndex).size()); 			
			
			// maybe select random position in new vehicle to insert action into 
			
			List<VehicleAction> selectedAction = newSolution.get(vehicleIndex).remove(actionIndex);
			
			if (newSolution.get(vehicleIndex).size() == 0) {
				newSolution.remove(vehicleIndex);				
			}
			
			// select new vehicle 
			int newVehicleIndex =  ThreadLocalRandom.current().nextInt(0, newSolution.size()); 
			newSolution.get(newVehicleIndex).add(selectedAction);
			
			if (!feasibleChange(newSolution.get(newVehicleIndex))) 
				continue; 
		
			
			// evaluate modified solution
			long newEnergy = energyFunc(newSolution);
			
			if (acceptanceProb(currentEnergy, newEnergy, temperature) > Math.random()) {
				currentSolution = newSolution; 
				currentEnergy = newEnergy;
			}
			
			if (currentEnergy < bestEnergy) {
				bestSolution = cloneList(currentSolution); 	
				bestEnergy = currentEnergy; 				
			}
			
			temperature *= 1-coolingRate; 
		}
		
		return bestSolution; 
	}	
	
	public double acceptanceProb(double energy, double newEnergy, double temperature) { 
        if (newEnergy < energy) 
            return 1.0;        
        
        return Math.exp((energy - newEnergy) / temperature);
    }

	@Override
	public List<VehicleInformation> getRouting(DataController data, List<RoutingElement> dataSet) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
