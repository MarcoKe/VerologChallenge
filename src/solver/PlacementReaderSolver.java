package solver;

import java.util.ArrayList;
import java.util.List;

import data.DataController;
import data.DayInformation;
import data.StrategyController;
import data.VehicleInformation;
import io.PlacementReader;
import routing.CircularRouting;
import routing.CrossExchangeRouting;
import routing.Routing;
import routing.RoutingElement;

public class PlacementReaderSolver implements Solver {
	
	Routing routing = new CrossExchangeRouting(new CircularRouting()); 

	@Override
	public StrategyController solve(DataController data) {
		 
		// read placement
		PlacementReader reader = new PlacementReader(data); 
		List<List<RoutingElement>> placement = reader.readFile(data.getGlobal().getFileName()); 		
		
		// get routing for each day 
		List<DayInformation> dayList = new ArrayList<>();		
		int day = 1; 
		for (List<RoutingElement> dayTasks : placement) {
			List<VehicleInformation> dayRouting = routing.getRouting(data, dayTasks);
			DayInformation newDayInfo = new DayInformation(day); 
			newDayInfo.addAllVehickeInformation(dayRouting);
			dayList.add(newDayInfo);	
			day++; 
		}
		
		return new StrategyController(dayList); 
	}

}
