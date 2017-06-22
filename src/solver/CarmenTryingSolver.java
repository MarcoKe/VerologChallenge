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
import util.RoutingUtil;
import data.VehicleAction.Action;

public class CarmenTryingSolver implements Solver {

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
	

	public CarmenTryingSolver() {

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
		
		for( int i = 0; i < list.size(); i++)
		{
			System.out.println("ID" + list.get(i).getId());
		}
		
		List<Request> helper = requestsLists.get(0);
		
		for (int i = helper.size()-1; i>0; i--) {
		System.out.println(helper.get(i).getId());}
		//System.out.println(requestsLists.get(0).getId());
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
			boolean manConDelDaySet = false;
			for (int j = req.getAmountOfTools(); j > 0; --j) {
				boolean toolPlaced = false;
				while( t < lastTimeToolUsedList.length && !toolPlaced) {

					Request prevReq = lastTimeToolUsedList[t];
	
					if (prevReq == null) {
						if (!possition.containsKey(req)) {
							possition.put(req, req.getStartTime());

						}						
						addRequestToDeliveryDay(req);
						addRequestToPickupDay(req);
						toolPlaced = true;
					}

					else if (prevReq == req) {
						continue;
					}

					else if (getPickupDay(prevReq) >= req.getStartTime() &&
							(getPickupDay(prevReq)<= req.getEndTime()) &&
							(data.getGlobal().computeDistance(req.getLocation(), prevReq.getLocation())
							<= maxdistance)) {
						int delDay = getPickupDay(prevReq);
						if(manConDelDaySet){
							delDay = possition.get(req);
						}
						List<MandatoryConnection> manConList = manConDay.get(delDay);
						if(manConList == null){
							manConList = new ArrayList<>();
							manConDay.put(delDay, manConList);
						}
						
						MandatoryConnection manCon = getConnectionOfRequest(manConList, prevReq, req);
						MandatoryConnection cpyManCon = new MandatoryConnection(manCon);
						cpyManCon.addPickupList(prevReq);
						cpyManCon.addDeliverList(req);
						
						boolean isManConPossible = RoutingUtil.isRoutePossible(data, new VehicleInformation(cpyManCon.getRoute()));

											
						if(isManConPossible){	
							if (!possition.containsKey(req)) {
								possition.put(req, getPickupDay(prevReq));
							}

							addRequestToManConDay(manCon, prevReq, req);
							addRequestToPickupDay(req);
							manConDelDaySet = true;
							toolPlaced = true;
						}
					}
					else if(t == lastTimeToolUsedList.length-1){
						System.out.println("no tool found");
					}
					
					//Iterate through lastTimeToolUsedList where behavior is the same as t
					while(j>0 && t<lastTimeToolUsedList.length && lastTimeToolUsedList[t] == prevReq){
						if(toolPlaced){
							toolUsedByRequest.get(req).add(t);
							lastTimeToolUsedList[t]= req;
							--j;
						}
						++t;
					}
					
				}

//			System.out.print("ID: " + req.getId()+ " Tool ID "+req.getTool().getId()+" tool used: " + t + " Starting possition: " + 
//					possition.get(req) );
//			System.out.print(" Ending time: " +  
//							getPickupDay(req) );
//			System.out.println(" Tool amound "+req.getAmountOfTools());		
				
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
	
	public void addRequestToManConDay(MandatoryConnection manCon,Request pick, Request del){
		int delDay = possition.get(del);
		
		if(manCon == null){
			manCon = new MandatoryConnection();
		}
		
		manCon.addPickupList(pick);
		manCon.addDeliverList(del);
		
		
		//move pick so that delDay  == pickupday from pickup request
		if(getPickupDay(pick)!= delDay){
			List<Request> pickDeliverList = deliverDay.get(possition.get(pick));
			if(pickDeliverList != null){
				pickDeliverList.remove(pick);
			}
		while(getPickupDay(pick) < delDay){
				possition.put(pick, possition.get(pick)+1);
			}			
			addRequestToDeliveryDay(pick);
		}
		
		List<Request> pickupList = pickUpDay.get(getPickupDay(pick));
		if(pickupList != null)
		{
			pickupList.remove(pick);
		}	
		
		List<Request> deliverList = deliverDay.get(delDay);
		if(deliverList != null){
			deliverList.remove(del);
		}
		
		if(!manConDay.get(delDay).contains(manCon)){
			manConDay.get(delDay).add(manCon);
		}
	}
	
	
	private MandatoryConnection getConnectionOfRequest (List<MandatoryConnection> manCon, Request pick, Request deliver ){
		MandatoryConnection ret = null;

		for(int i=0;i<manCon.size()&& ret == null;++i){
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
		return possition.get(req) + req.getUsageTime();
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
