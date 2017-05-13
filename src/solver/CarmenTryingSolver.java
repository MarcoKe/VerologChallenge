package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import data.DataController;
import data.Location;
import data.Request;
import data.StrategyController;
import data.Tool;
import data.Vehicle;

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

	Map<Request, Integer> toolUsedByRequest;
	
	int maxdistance;
	

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
		
		

		for (List<Request> list : requestsLists) {
			
			System.out.println("Size: " + list.size());

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
		}

		return null;

	}

	public void placingTools() {

		int t = 0;
		
		for (int i = 0; i < maxOverlappingList.size(); i++) {

			for (int j = 1; j <= maxOverlappingList.get(i).getAmountOfTools(); j++) {

				for (t = 1; t <= lastTimeToolUsedList.length; t++) {

					if (lastTimeToolUsedList[t] == null) {

						if (possition.containsKey(maxOverlappingList.get(i))) {

						}
						else {
							possition.put(maxOverlappingList.get(i), maxOverlappingList.get(i).getStartTime());
							
						}

						toolUsedByRequest.put(maxOverlappingList.get(i), t);

						lastTimeToolUsedList[t] = maxOverlappingList.get(i);

						break;
					}

					else if (lastTimeToolUsedList[t] == maxOverlappingList.get(i)) {
						continue;
					}

					else if ((possition.get(lastTimeToolUsedList[t])
							+ lastTimeToolUsedList[t].getUsageTime() <= maxOverlappingList.get(i).getEndTime())
							&& ((int) Math.sqrt(Math.pow(lastTimeToolUsedList[t].getLocation().getX() - 
									maxOverlappingList.get(i).getLocation().getX(),2) +
									Math.pow((lastTimeToolUsedList[t].getLocation().getY() -
											maxOverlappingList.get(i).getLocation().getY()),2)) <= maxdistance )) {

						if (possition.containsKey(maxOverlappingList.get(i))) {

						} else {
							possition.put(maxOverlappingList.get(i),
									possition.get(lastTimeToolUsedList[t]) + lastTimeToolUsedList[t].getUsageTime());
						}

						toolUsedByRequest.put(maxOverlappingList.get(i), t);

						lastTimeToolUsedList[t] = maxOverlappingList.get(i);

						break;
					}

					else {
						continue;
					}

				}
			
			System.out.println("ID: " + maxOverlappingList.get(i).getId() + " tool used: " + t + " Starting possition: " + 
					possition.get(maxOverlappingList.get(i)) + " Ending time: " +  
							(possition.get(maxOverlappingList.get(i)) + maxOverlappingList.get(i).getUsageTime()) );
			}
		}

	}

	public void main(String args) {
		solve(data);
	}

}
