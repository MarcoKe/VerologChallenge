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
			
			toolsAvailable = list.get(1).getTool().getMaxAvailable();
			
			maxOverlapsId = 0;
			
			lastTimeToolUsedList = new Request[toolsAvailable];
			
			possition = new HashMap<>();
			
			toolUsedByRequest = new HashMap<>();

			while (notUsedYetList.isEmpty() == false) {
				
				
				maxoverlaps = 0;

				for (int i = 0; i < notUsedYetList.size(); i++) {

					countk = 0;


					overlappingList = new ArrayList<>();

					// Getting the number of overlaps (clique)
					for (int k = 0; k < notUsedYetList.size(); k++) {


							if (notUsedYetList.get(i).getEndTime() <= notUsedYetList.get(k).getEndTime()) {
								if (notUsedYetList.get(i).getEndTime() - notUsedYetList.get(k).getStartTime() >= 0) {

									countk++;

									overlappingList.add(notUsedYetList.get(k));

								}
							} else {
								if (notUsedYetList.get(k).getEndTime() - notUsedYetList.get(i).getStartTime() >= 0) {

									countk++;

									overlappingList.add(notUsedYetList.get(k));
								}
							}

					}	
					
					// Selecting the maximum clique
					if (countk > maxoverlaps) {

						maxoverlaps = countk;

						maxOverlapsId = notUsedYetList.get(i).getId();

						maxOverlappingList = overlappingList;

					}
				}
				
				placingTools();


				for (int i = 0; i < maxOverlappingList.size(); i++) {
				
					
					notUsedYetList.remove(maxOverlappingList.get(i));
					
				}

			}
			
			System.out.println("-------------------------NEW LIST------------------------ ");
			
			placement.add(possition);

		}
		
		List<DayInformation> dayInfoList = new LinkedList<>();

		Set<Integer> workDays = new TreeSet<>();
		workDays.addAll(deliverDay.keySet());
		workDays.addAll(pickUpDay.keySet());
		
		for(int day : workDays){
			System.out.println(day);
			DayInformation dayInfo = new DayInformation(day);
			List<VehicleAction> simpleLoc= new LinkedList<>();
			List<Request> deliver = deliverDay.get(day);
			List<Request> pickup = pickUpDay.get(day);
			
			List<MandatoryConnection> manConsList = new ArrayList<>();
			
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
			
			Routing routing = new SimpleRouting();
			List<VehicleInformation> infoList = routing.getRouting(data, simpleLoc, null);
			dayInfo.addAllVehickeInformation(infoList);
			dayInfoList.add(dayInfo);
		}
		
		

		StrategyController strat = new StrategyController(dayInfoList);
		
		return strat;
		

	}

	public void placingTools() {

		int t = 0;
		
		for (int i = 0; i < maxOverlappingList.size(); i++) {
			Request req = maxOverlappingList.get(i);
			toolUsedByRequest.put(req, new ArrayList<>());
			
			for (int j = 1; j <= req.getAmountOfTools(); j++) {

				for (t = 1; t <= lastTimeToolUsedList.length; t++) {

					if (lastTimeToolUsedList[t] == null) {

						if (!possition.containsKey(req)) {
							possition.put(req, req.getStartTime());

						}

						toolUsedByRequest.get(req).add(t);
						//toolUsedByRequest.put(req, temp);

						lastTimeToolUsedList[t] = req;

						break;
					}

					else if (lastTimeToolUsedList[t] == req) {
						continue;
					}

					else if ((possition.get(lastTimeToolUsedList[t])
							+ lastTimeToolUsedList[t].getUsageTime() >= req.getStartTime()) &&
							(possition.get(lastTimeToolUsedList[t]) + 
									lastTimeToolUsedList[t].getUsageTime() <= req.getEndTime())
							&& ((int) Math.sqrt(Math.pow(lastTimeToolUsedList[t].getLocation().getX() - 
									req.getLocation().getX(),2) +
									Math.pow((lastTimeToolUsedList[t].getLocation().getY() -
											req.getLocation().getY()),2)) <= maxdistance )) {

						if (!possition.containsKey(req)) {
							
							possition.put(req,
									possition.get(lastTimeToolUsedList[t]) + lastTimeToolUsedList[t].getUsageTime());
						}

						toolUsedByRequest.get(req).add(t);
						

						lastTimeToolUsedList[t] = req;

						break;
					}

					else {
						continue;
					}

				}
				
				
			
//			System.out.println("ID: " + req.getId() + " tool used: " + t + " Starting possition: " + 
//					possition.get(req) + " Ending time: " +  
//							(possition.get(req) + req.getUsageTime()) );
			}
			
			int delDay = possition.get(req);
			
			
			
			System.out.println("Req: " + req.getId() +" " +Arrays.toString(toolUsedByRequest.get(req).toArray()));
			
			List<Request> deliverList = deliverDay.get(delDay);
			if(deliverList == null)
			{
				deliverList = new ArrayList<>();
			}
			
			deliverList.add(req);
			deliverDay.put(delDay,deliverList);
			
			int pickDay = delDay + req.getUsageTime();
			List<Request> pickUpList = pickUpDay.get(pickDay);
			if(pickUpList == null)
			{
				pickUpList = new ArrayList<>();
			}
			
			pickUpList.add(req);
			pickUpDay.put(pickDay,deliverList);
			
			
			
		}

	}

	public void main(String args) {
		solve(data);
	}

}
