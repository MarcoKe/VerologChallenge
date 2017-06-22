package solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import data.DataController;
import data.DayInformation;
import data.Global;
import data.Location;
import data.Placement;
import data.Request;
import data.StrategyController;
import data.Tool;
import data.Vehicle;
import data.VehicleAction;
import data.VehicleInformation;
import routing.MandatoryConnection;
import routing.Routing;
import routing.SimpleRouting;
import data.VehicleAction.Action;

public class DebzAttempt implements Solver {

	DataController data;
	Location depot;

	List<Request> notUsedYetList;
	List<Request> requests;
	Vehicle vehicles;
	List<Tool> tools;
	List<List<Request>> requestsLists;
	List<Request> list;
	List<Request> overlappingList;
	List<Request> maxOverlappingList;
	
	int count;
	int toolCount;

	int maxoverlaps = 0;
	int maxOverlapsId = 0;
	int countk = 0;

	int toolsAvailable;
	
	Request[] lastTimeToolUsedList;
	
	Map<Request, Integer> possition;

	Map<Request, List<Integer>> toolUsedByRequest;
	
	Map<Integer, List<Request>> deliverDay;
	
	Map<Integer,List<Request>> pickUpDay;
	
	Map<Integer, List<MandatoryConnection>> manConDay;
	
	int maxdistance;
	
	Placement placement = new Placement();
	

	public DebzAttempt() {

	}

	public StrategyController solve(DataController data) {
		this.data = data;

		requests = data.getRequestList();
		
		vehicles = data.getVehicle();
		
		maxdistance = vehicles.getMaxDistance(); 

		Collections.sort(requests, (o1, o2) -> Integer.compare(o1.getEndTime(), o2.getEndTime()));

		// Divide requests into groups depending on the tool ID

		requestsLists = new ArrayList<List<Request>>();

		for (Tool tool : data.getToolList()) {

			list = requests.stream().filter(r -> r.getTool().getId() == tool.getId()).collect(Collectors.toList());

			requestsLists.add(list);

		}

		// For each list (Group of requests with same tool) take all requests
		// and get the time windows.

		notUsedYetList = new ArrayList<Request>();
		
		deliverDay = new HashMap<>();
		
		pickUpDay = new HashMap<>();
		
		manConDay = new HashMap<>();


		for (List<Request> list : requestsLists) {

			notUsedYetList = list;
			
			toolsAvailable = list.get(0).getTool().getMaxAvailable();
			
			maxOverlapsId = 0;
			
			
			possition = new HashMap<>();
			
			toolUsedByRequest = new HashMap<>();

			while (notUsedYetList.isEmpty() == false) {
				
				
				maxoverlaps = 0;

				for (int i = 0; i < notUsedYetList.size(); i++) {

					Request reqI = notUsedYetList.get(i);
					int endTimeI = reqI.getEndTime() + reqI.getUsageTime() + 1;
					countk = 0;


					overlappingList = new ArrayList<>();

					// Getting the number of overlaps (clique)
					for (int k = 0; k < notUsedYetList.size(); k++) {
							Request reqK = notUsedYetList.get(k);

							int endTimeK = reqK.getEndTime() + reqK.getUsageTime() + 1;

							if (endTimeI  <= endTimeK) {
								if (endTimeI - reqK.getStartTime() >= 0) {

									countk++;

									overlappingList.add(reqK);

								}
							} else {
								if (endTimeK - reqI.getStartTime() >= 0) {

									countk++;

									overlappingList.add(reqK);
								}
							}

					}	
					
					// Selecting the maximum clique
					if (countk > maxoverlaps) {

						maxoverlaps = countk;

						maxOverlapsId = reqI.getId();

						maxOverlappingList = overlappingList;

					}
				}
				
				placingTools();
				notUsedYetList.removeAll(maxOverlappingList);
//				for (int i = 0; i < maxOverlappingList.size(); i++) {
//				
//					
//					notUsedYetList.remove(maxOverlappingList.get(i));
//					
//				}

			}
			
			System.out.println("-------------------------NEW LIST------------------------ ");
			
			placement.add(possition);

		}
		
		List<DayInformation> dayInfoList = new LinkedList<>();

		Set<Integer> workDays = new TreeSet<>();
		workDays.addAll(deliverDay.keySet());
		workDays.addAll(pickUpDay.keySet());
		workDays.addAll(manConDay.keySet());
		
		for(int day : workDays){
			System.out.println("day "+day);
			DayInformation dayInfo = new DayInformation(day);
			List<VehicleAction> simpleLoc= new LinkedList<>();
			List<Request> deliver = deliverDay.get(day);
			List<Request> pickup = pickUpDay.get(day);
			
			List<MandatoryConnection> manConsList = manConDay.get(day);
			if(manConsList == null){
				manConsList = new ArrayList<>();
			}
			
			
			if(deliver !=null){
				for(Request req: deliver){
					simpleLoc.add(new VehicleAction(Action.LOAD_AND_DELIVER, req));
				}
				System.out.println("deliver size "+ deliver.size());
			}
			if(pickup != null){
				for(Request req: pickup){				
					simpleLoc.add(new VehicleAction(Action.PICK_UP, req));
				}
				System.out.println("pickup size "+ pickup.size());

			}
			
			Routing routing = new SimpleRouting();
			System.out.println("simple "+simpleLoc.size());
			System.out.println("manCons "+manConsList.size());
			List<VehicleInformation> infoList = routing.getRouting(data, simpleLoc, manConsList);
			System.out.println("infoList "+infoList.size());
			dayInfo.addAllVehickeInformation(infoList);
			dayInfoList.add(dayInfo);
		}
		StrategyController strat = new StrategyController(dayInfoList);
		
		return strat;
		

	}

	/**
	 * 
	 */
	public void placingTools() {
		int t = 0;
		
		lastTimeToolUsedList = new Request[toolsAvailable];

		for (int i = 0; i < maxOverlappingList.size(); i++) {
			Request req = maxOverlappingList.get(i);
			toolUsedByRequest.put(req, new ArrayList<>());
			t = 0;
			
			for (int j = 0; j < req.getAmountOfTools(); j++) {

				for (t = 0; t < lastTimeToolUsedList.length; t++) {

					if (lastTimeToolUsedList[t] == null) {

						if (!possition.containsKey(req)) {
							possition.put(req, req.getStartTime());

						}						
						addRequestToDeliveryDay(req);
						addRequestToPickupDay(req);
						
						toolUsedByRequest.get(req).add(t);
						lastTimeToolUsedList[t] = req;

						break;
					}

					else if (lastTimeToolUsedList[t] == req) {
						continue;
					}

					else if ((getPickupDay(lastTimeToolUsedList[t]) >= req.getStartTime()) &&
							(getPickupDay(lastTimeToolUsedList[t])<= req.getEndTime()) &&
							(possibleTrip(lastTimeToolUsedList[t].getLocation(),req.getLocation()))) {
//							(data.getGlobal().computeDistance(req.getLocation(), lastTimeToolUsedList[t].getLocation())
//							<= maxdistance)) {

						if (!possition.containsKey(req)) {
							possition.put(req, getPickupDay(lastTimeToolUsedList[t]));
						}

						addRequestToManConDay(lastTimeToolUsedList[t], req);
						addRequestToPickupDay(req);
						toolUsedByRequest.get(req).add(t);
						lastTimeToolUsedList[t] = req;

						break;
					}
					else if(t == lastTimeToolUsedList.length-1){
						System.out.println("no tool found");
					}

				}

			System.out.print("ID: " + req.getId()+ " Tool ID "+req.getTool().getId()+" tool used: " + t + " Starting possition: " + 
					possition.get(req) );
			System.out.println(" Ending time: " +  
							getPickupDay(req) );
			}
		}

	}
	
	
	public void addRequestToDeliveryDay(Request req){
		int delDay = possition.get(req);
		boolean reqNotInManCon = true;
		List<MandatoryConnection> manConList = manConDay.get(delDay);
		if(manConList != null){
			MandatoryConnection tmp = getConnectionOfRequest(manConList, null, req);
			if(tmp != null){
				reqNotInManCon = false;
			}
		}
			
				
		if(reqNotInManCon){
			List<Request> deliverList = deliverDay.get(delDay);
			if(deliverList == null)
			{
				deliverList = new ArrayList<>();
				deliverDay.put(delDay,deliverList);
			}
			if(!deliverList.contains(req)){
				deliverList.add(req);			
			}			
		}
		
		
	}
	
	public void addRequestToPickupDay(Request req){
		int pickDay = getPickupDay(req);
		List<Request> pickupList = pickUpDay.get(pickDay);
		if(pickupList == null)
		{
			pickupList = new ArrayList<>();
			pickUpDay.put(pickDay,pickupList);					
		}
		
		if(!pickupList.contains(req)){
			pickupList.add(req);			
		}
	}
	
	public void addRequestToManConDay(Request pick, Request del){
		int delDay = possition.get(del);
		List<MandatoryConnection> manConList = manConDay.get(delDay);
		if(manConList == null){
			manConList = new ArrayList<>();
			manConDay.put(delDay, manConList);
		}
		MandatoryConnection manCon = getConnectionOfRequest(manConList, pick, del);
		if(manCon == null){
			manCon = new MandatoryConnection();
			manConList.add(manCon);
		}
		
		manCon.addPickupList(pick);
		manCon.addDeliverList(del);
		
		//TODO remove pick from pickupDay
		List<Request> deliverList = deliverDay.get(delDay);
		if(deliverList != null){
			deliverList.remove(del);
		}
		List<Request> pickupList = pickUpDay.get(delDay);
		if(pickupList != null)
		{
			pickupList.remove(pick);
		}		
	}
	
	
	private MandatoryConnection getConnectionOfRequest (List<MandatoryConnection> manCon, Request pick, Request deliver ){
		MandatoryConnection ret = null;
		for(int i=0;i<manCon.size();++i){
			List<VehicleAction> checkList = manCon.get(i).getPickupList();
			for(VehicleAction action: checkList){
				if(action.getRequest() == pick){
					ret = manCon.get(i);
					break;
				}
			}
			checkList = manCon.get(i).getDeliverList();
			for(VehicleAction action: checkList){
				if(action.getRequest() == deliver){
					ret = manCon.get(i);
					break;
				}
			}
		}
		return ret;
	}
	
	public int getPickupDay(Request req){
		return possition.get(req) + req.getUsageTime()  ;
	}
	public boolean possibleTrip(Location l1, Location l2) {
		Vehicle vehicle = data.getVehicle();
		
		return (tripDistance(l1, l2) <= vehicle.getMaxDistance());
	}
	
	public int tripDistance( Location l1, Location l2) {
		Global g = data.getGlobal(); 
		Location depot = data.getLocationList().get(0);
		int distance = 0; 	
		distance += g.computeDistance(depot, l1);
		distance += g.computeDistance(l1, l2);
		distance += g.computeDistance(l2, depot);
		
		return distance;
	}
}
