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
import data.Request;
import data.StrategyController;
import data.Tool;
import data.VehicleAction;
import data.VehicleInformation;
import data.VehicleAction.Action;
import routing.MandatoryConnection;
import routing.Routing;
import routing.SimpleRouting;

public class CarmenSolverRebuild implements Solver {

	private int maxDistance;
	private Request[] lastTimeToolUsedList;
	private Map<Request, List<Integer>> toolUsedByRequest;
	private Map<Integer, List<Request>> deliverDay;
	private Map<Integer, List<Request>> pickUpDay;
	private Map<Request, Integer> positions;
	private List<Request> maxOverlappingList;

	@Override
	public StrategyController solve(DataController data) {
		// Divide requests into groups depending on the tool ID
		List<Request> requests = data.getRequestList();
		List<List<Request>> requestsPerID = new ArrayList<List<Request>>();
		Collections.sort(requests, (o1, o2) -> Integer.compare(o1.getEndTime(), o2.getEndTime()));
		for (Tool tool : data.getToolList()) {
			requestsPerID.add(
					requests.stream().filter(r -> r.getTool().getId() == tool.getId()).collect(Collectors.toList()));
		}

		maxDistance = data.getVehicle().getMaxDistance();
		deliverDay = new HashMap<>();
		pickUpDay = new HashMap<>();
		toolUsedByRequest = new HashMap<>();
		positions = new HashMap<>();
		// For each list (Group of requests with same tool) take all requests
		// and get the time windows.

		for (List<Request> list : requestsPerID) {
			lastTimeToolUsedList = new Request[list.get(1).getTool().getMaxAvailable()];
			positions.clear();
			toolUsedByRequest.clear();
			while (!list.isEmpty()) {
				int maxOverlaps = 0;

				for (int i = 0; i < list.size(); i++) {
					int countK = 0;
					Request currentI = list.get(i);
					List<Request> overlappingList = new ArrayList<>();

					// Getting the number of overlaps (clique)
					for (int k = 0; k < list.size(); k++) {
						Request currentK = list.get(k);
						if (currentI.getEndTime() <= currentK.getEndTime()) {
							if (currentI.getEndTime() - currentK.getStartTime() >= 0) {
								countK++;
								overlappingList.add(currentK);
							}
						} else {
							if (currentK.getEndTime() - currentI.getStartTime() >= 0) {
								countK++;
								overlappingList.add(currentK);
							}
						}
					}

					// Selecting the maximum clique

					if (countK > maxOverlaps) {
						maxOverlaps = countK;
						maxOverlappingList = overlappingList;
					}
				}

				placingTools();

				for (int i = 0; i < maxOverlappingList.size(); i++) {
					list.remove(maxOverlappingList.get(i));
				}

			}

			System.out.println("-------------------------NEW LIST------------------------ ");
		}

		List<DayInformation> dayInfoList = new LinkedList<>();

		Set<Integer> workDays = new TreeSet<>();
		workDays.addAll(deliverDay.keySet());
		workDays.addAll(pickUpDay.keySet());

		for (int day : workDays) {
			System.out.println(day);
			DayInformation dayInfo = new DayInformation(day);
			List<VehicleAction> simpleLoc = new LinkedList<>();
			List<Request> deliver = deliverDay.get(day);
			List<Request> pickup = pickUpDay.get(day);

			List<MandatoryConnection> manConsList = new ArrayList<>();

			if (deliver != null) {
				for (Request req : deliver) {
					simpleLoc.add(new VehicleAction(Action.LOAD_AND_DELIVER, req));
				}
			}
			if (pickup != null) {
				for (Request req : pickup) {

					simpleLoc.add(new VehicleAction(Action.PICK_UP, req));
				}
			}

			Routing routing = new SimpleRouting();
			List<VehicleInformation> infoList = routing.getRouting(data, simpleLoc, null);
			dayInfo.addAllVehickeInformation(infoList);
			dayInfoList.add(dayInfo);
		}

		return new StrategyController(dayInfoList);
	}

	private void placingTools() {

		for (int i = 0; i < maxOverlappingList.size(); i++) {
			Request request = maxOverlappingList.get(i);
			toolUsedByRequest.put(request, new ArrayList<>());

			for (int j = 1; j <= request.getAmountOfTools(); j++) {
				for (int t = 0; t < lastTimeToolUsedList.length; t++) {

					if (lastTimeToolUsedList[t] == null) {
						if (!positions.containsKey(request)) {
							positions.put(request, request.getStartTime());
						}

						toolUsedByRequest.get(request).add(t);
						lastTimeToolUsedList[t] = request;
						break;
					} else if (lastTimeToolUsedList[t] == request) {
						continue;
					} else if ((positions.get(lastTimeToolUsedList[t])
							+ lastTimeToolUsedList[t].getUsageTime() >= request.getStartTime())
							&& (positions.get(lastTimeToolUsedList[t])
									+ lastTimeToolUsedList[t].getUsageTime() <= request
											.getEndTime())
							&& ((int) Math
									.sqrt(Math
											.pow(lastTimeToolUsedList[t].getLocation().getX()
													- request.getLocation().getX(), 2)
											+ Math.pow((lastTimeToolUsedList[t].getLocation().getY()
													- request.getLocation().getY()), 2)) <= maxDistance)) {
						if (!positions.containsKey(request)) {
							positions.put(request,
									positions.get(lastTimeToolUsedList[t]) + lastTimeToolUsedList[t].getUsageTime());
						}

						toolUsedByRequest.get(request).add(t);
						lastTimeToolUsedList[t] = request;
						break;
					} else {
						continue;
					}
				}
			}

			int delDay = positions.get(request);

			System.out.println(
					"Req: " + request.getId() + " " + Arrays.toString(toolUsedByRequest.get(request).toArray()));

			// Extend list of deliveries per day
			List<Request> deliverList = deliverDay.get(delDay) == null ? new ArrayList<>() : deliverDay.get(delDay);
			deliverList.add(request);
			deliverDay.put(delDay, deliverList);

			// Extend list of pickups per day
			int pickDay = delDay + request.getUsageTime();
			List<Request> pickUpList = pickUpDay.get(pickDay) == null ? new ArrayList<>() : pickUpDay.get(pickDay);
			pickUpList.add(request);
			pickUpDay.put(pickDay, deliverList);
		}
	}

}
