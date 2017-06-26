package routing;

import java.util.LinkedList;
import java.util.List;

import data.DataController;
import data.VehicleAction;
import data.VehicleInformation;
import util.RoutingUtil;

public class PermutationRouting implements Routing{
	
	private Routing subRouting;
	private int searchDepth;
	
	public PermutationRouting(Routing subRouting, int searchDepth) {
		this.subRouting = subRouting;
		this.searchDepth = searchDepth;
	}

	
	public List<VehicleInformation> getRouting(DataController data, List<VehicleAction> simpleLocations,
			List<MandatoryConnection> connectedLocations) {
		LinkedList<RoutingElement> dataSet = new LinkedList<>();
		if(simpleLocations != null){
			dataSet.addAll(simpleLocations);
		}
		if(connectedLocations != null){
			dataSet.addAll(connectedLocations);
		}		
		return this.getRouting(data, dataSet);
	}

	@Override
	public List<VehicleInformation> getRouting(DataController data, List<RoutingElement> dataSet) {
		List<VehicleInformation> route = subRouting.getRouting(data, dataSet);
		for(int i=0;i<route.size();++i){
			VehicleInformation tmp = route.get(i);
			tmp = permutateInit(data, tmp, searchDepth);			
			route.remove(i);
			route.add(i,tmp);
		}
		
		return route;
	}
	
	
	
	
	private VehicleInformation permutateInit(DataController data, VehicleInformation vehicInfo, int searchDepth){
		List<VehicleAction> input = new LinkedList<>();
		List<VehicleAction> prefix = new LinkedList<>();
		List<VehicleAction>	 sufix = new LinkedList<>();
		for(int i=0; i< vehicInfo.getRoute().size();++i){
			if(i < searchDepth){
				input.add(vehicInfo.getRoute().get(i));
			}else {
				sufix.add(vehicInfo.getRoute().get(i));
			}
		}
		
		BestSoFar currBest = new BestSoFar(vehicInfo, Long.MAX_VALUE);
		while(!sufix.isEmpty()){			
			BestSoFar tmp = permutateRec(data,input, new LinkedList<>(), prefix, sufix);
			if(tmp.cost < currBest.cost){
				currBest = tmp;
			}
			prefix.add(input.get(0));
			input.remove(0);

			input.add(sufix.get(0));
			sufix.remove(0);
		}
		
		return currBest.ret;
	}
	
	
	private BestSoFar permutateRec(DataController data, List<VehicleAction> input, List<VehicleAction> permRoute, List<VehicleAction> prefixRoute, List<VehicleAction> sufixRoute){
		if(input.isEmpty()){
			BestSoFar ret = new BestSoFar(null, Long.MAX_VALUE);
			List<VehicleAction> route = new LinkedList<>(prefixRoute);
			route.addAll(permRoute);
			route.addAll(sufixRoute);
			VehicleInformation retVehicInfo = new VehicleInformation(route);
			if(RoutingUtil.isRoutePossible(data, retVehicInfo)){
				ret = new BestSoFar(retVehicInfo, RoutingUtil.getVehicleInformationCost(data, retVehicInfo));
			}
			return ret;
		}
		
		BestSoFar currBest = new BestSoFar(null, Long.MAX_VALUE);
		for(int i = 0; i < input.size(); ++i){
			List<VehicleAction> copyIn = new LinkedList<>(input);
			permRoute.add(copyIn.get(i));
			copyIn.remove(i);
			BestSoFar tmp = permutateRec(data,copyIn, permRoute, prefixRoute, sufixRoute);
			if(tmp.cost < currBest.cost){
				currBest = tmp;
			}			
			permRoute.remove(permRoute.size()-1);
		}
		return currBest;
	}
	
	class BestSoFar{
		private VehicleInformation ret;
		private long cost;
		public BestSoFar(VehicleInformation ret, long cost) {
			this.ret = ret;
			this.cost = cost;
		}
	}

}
