package solver;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import data.DataController;
import data.DayInformation;
import data.Request;
import data.StrategyController;
import data.VehicleAction;
import data.VehicleAction.Action;
import data.VehicleInformation;
import routing.CircularRouting;
import routing.Routing;
import routing.SimpleRouting;

public class RoutingTestSolver implements Solver{

	private Routing routing = new CircularRouting();
	
	public StrategyController solve(DataController data) {
		
		List<DayInformation> dayInfoList = new LinkedList<>();
		List<Request> reqList = data.getRequestList();
		//Sort list by start time smaller first
		Collections.sort(reqList, new Comparator<Request>() {		
			public int compare(Request o1, Request o2) {				
				return o1.getStartTime()-o2.getStartTime();
			}
		});
		Queue<Request> reqQueue = new LinkedList<>();
		for(int i = 0; i< reqList.size();++i){
			reqQueue.add(reqList.get(i));
		}
		
		Map<Integer,List<Request>> dayDeliver = new TreeMap<>();
		Map<Integer,List<Request>> dayPickup = new TreeMap<>();
		//setDelivery
		while(!reqQueue.isEmpty()){
			Request req = reqQueue.poll();
			
			List<Request> workList = dayDeliver.get(req.getStartTime());
			if(workList == null){
				workList = new LinkedList<>();
				dayDeliver.put(req.getStartTime(), workList);
			}
			
			workList.add(req);
			
			workList = dayPickup.get(req.getStartTime()+req.getUsageTime());
			if(workList == null){
				workList = new LinkedList<>();
				dayPickup.put(req.getStartTime()+req.getUsageTime(), workList);
			}
			workList.add(req);			
		}
		
		Set<Integer> workDays = new TreeSet<>();
		workDays.addAll(dayDeliver.keySet());
		workDays.addAll(dayPickup.keySet());
		
		for(int day : workDays){
			System.out.println("Day: "+day);
			DayInformation dayInfo = new DayInformation(day);
			List<VehicleAction> simpleLoc= new LinkedList<>();
			List<Request> deliver = dayDeliver.get(day);
			List<Request> pickup = dayPickup.get(day);
			
			if(deliver !=null){
				for(Request req: deliver){
					simpleLoc.add(new VehicleAction(Action.LOAD_AND_DELIVER, req));
				}
			}
			if(pickup != null){
				for(Request req: pickup){
					simpleLoc.add(new VehicleAction(Action.PICK_UP, req));
				}
			}
			
			List<VehicleInformation> infoList = routing.getRouting(data, simpleLoc, null);
			dayInfo.addAllVehickeInformation(infoList);
			dayInfoList.add(dayInfo);
		}
		
		

		StrategyController strat = new StrategyController(dayInfoList);
		return strat;
	}

	
	public int findMinDays(List<Request> requests) {
		int minDays = 0;   
		for (Request request : requests) {
			int sum = request.getEndTime() + request.getUsageTime()+1; 

			if (sum > minDays) {
				minDays = sum; 
			}
		}

		return minDays-1; 
	}
}
