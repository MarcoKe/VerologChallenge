package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import data.DataController;
import data.DayInformation;
import data.Depot;
import data.Request;
import data.StrategyController;
import data.Tool;
import data.Vehicle;
import data.VehicleAction;
import data.VehicleAction.Action;
import data.VehicleInformation;
import data.Global;
import data.Location;

public class DebzSolver implements Solver {

	private DataController data;
	private Location depot;
	private Map<Integer, DayInformation> days;
	private List<Request> possibleMatches;
	private List<Request> requests;
	private List<Request> servedRequests;

	public DebzSolver() {

	}

	public StrategyController solve(DataController d) {
		data = d;
		depot = data.getLocationList().get(0);
		
		return null;
	}
}