package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import data.DataController;
import data.Location;
import data.Request;
import data.StrategyController;
import data.Tool;

public class CarmenTryingSolver implements Solver {

	DataController data;
	Location depot;
	List<Request> requests;
	List<Tool> tools;
	List<List<Request>> requestsLists;
	List<Request> list;

	int count;
	int toolCount;

	int maxoverlaps = 0;
	int maxOverlapsId = 0;
	int countk = 0;

	public CarmenTryingSolver() {

	}

	public StrategyController solve(DataController data) {

		requests = data.getRequestList();
		
		Collections.sort(requests, (o1, o2) -> Integer.compare(o1.getEndTime(), o2.getEndTime())); 

		// Divide requests into groups depending on the tool ID

		requestsLists = new ArrayList<List<Request>>();

		for (Tool tool : data.getToolList()) {
			list = requests.stream().filter(r -> r.getTool().getId() == tool.getId()).collect(Collectors.toList());

			requestsLists.add(list);

		}

		// For each list (Group of requests with same tool) take all requests
		// and get the time windows.

		for (List<Request> list : requestsLists) {

			maxoverlaps = 0;
			maxOverlapsId = 0;

			for (int i = 0; i < list.size(); i++) {
				
				countk = 0;
				
						
				// Getting the number of overlaps (clique)
					for (int k = 0; k < list.size() ; k++) {

						if (i != k) {
							
							if(list.get(i).getEndTime() <= list.get(k).getEndTime())
							{
								if (list.get(i).getEndTime() - list.get(k).getStartTime() >= 0) {

									countk++;
								}	
							}
							else
							{
								if (list.get(k).getEndTime() - list.get(i).getStartTime() >= 0) {

									countk++;	
								}	
							}
						}
					
					}
					
					System.out.println("Id " + list.get(i).getId() + ": " + "  overlaps: " + countk);
			
					

					// Selecting the maximum clique
					if (countk > maxoverlaps) {
							
						maxoverlaps = countk;
							
						maxOverlapsId = list.get(i).getId();
					}
				}
			

			 System.out.println("-------------------------NEW LIST------------------------ ");
			 System.out.println("maxoverlaps: " + maxoverlaps + " maxOverlapsId: " +  maxOverlapsId);

			}
		
		for (List<Request> list : requestsLists) 
		{
			
		}
		
			


	return null;
	
	}

	public void main(String args) {
		solve(data);
	}

}
