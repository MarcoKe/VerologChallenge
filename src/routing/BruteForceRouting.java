package routing;

import java.util.LinkedList;
import java.util.List;

import data.DataController;
import data.VehicleAction;
import data.VehicleAction.Action;
import data.VehicleInformation;
import util.RoutingUtil;

public class BruteForceRouting implements Routing {

	private DataController data = null;

	@Override
	public List<VehicleInformation> getRouting(DataController data, List<VehicleAction> simpleLocations,
			List<MandatoryConnection> connectedLocations) {
		LinkedList<RoutingElement> dataSet = new LinkedList<>();
		if (simpleLocations != null) {
			dataSet.addAll(simpleLocations);
		}
		if (connectedLocations != null) {
			dataSet.addAll(connectedLocations);
		}
		return getRouting(data, dataSet);
	}

	@Override
	public List<VehicleInformation> getRouting(DataController data, List<RoutingElement> dataSet) {
		this.data = data;
		BestSoFar best = recursiveAlgo(new LinkedList<VehicleAction>(), dataSet);
		if(best.list == null){
			LinkedList<VehicleInformation> nok = new LinkedList<>();
			nok.add(new VehicleInformation());
			nok.add(new VehicleInformation());
			return nok;
		}
		List<VehicleInformation> ret = new LinkedList<>();
		ret.add(new VehicleInformation(best.list));
		return ret;
	}

	private BestSoFar recursiveAlgo(List<VehicleAction> set, List<RoutingElement> input) {
		VehicleInformation vI = new VehicleInformation(set);
		if (!RoutingUtil.isRoutePossible(data, vI)) {
			return new BestSoFar(new LinkedList<>(), Long.MAX_VALUE);
		}
		if (input.isEmpty()) {
			long currentCost = RoutingUtil.getVehicleInformationCost(data, vI);
			return new BestSoFar(set, currentCost);
		}

		BestSoFar myBest = new BestSoFar(new LinkedList<>(), Long.MAX_VALUE);
		for (int i = 0; i < input.size(); i++) {
			List<VehicleAction> copy = new LinkedList<>(set);
			List<RoutingElement> copyIn = new LinkedList<>(input);
			copy.addAll(input.get(i).getRouteElement());
			copyIn.remove(i);
			BestSoFar currBest = recursiveAlgo(copy, copyIn);
			if(currBest.cost<myBest.cost){
				myBest = currBest;
			}
			if (!copyIn.isEmpty()) {
				copy.add(new VehicleAction(Action.TO_DEPOT, null));
				currBest = recursiveAlgo(copy, copyIn);
				if(currBest.cost<myBest.cost){
					myBest = currBest;
				}
			}
		}
		return myBest;
	}
	
	class BestSoFar{
		private List<VehicleAction> list;
		private long cost=Long.MAX_VALUE;
		public BestSoFar(List<VehicleAction> list, long cost){
			this.list = new LinkedList<>(list);
			this.cost = cost;
		}
	}
}
