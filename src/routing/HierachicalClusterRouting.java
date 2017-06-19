package routing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import data.DataController;
import data.VehicleAction;
import data.VehicleInformation;

public class HierachicalClusterRouting implements Routing {

	private DataController data = null;
	private Routing subRouting = null;

	public HierachicalClusterRouting(Routing subRouting) {
		this.subRouting = subRouting;
	}

	@Override
	public List<VehicleInformation> getRouting(DataController data, List<VehicleAction> simpleLocations,
			List<MandatoryConnection> connectedLocations) {
		LinkedList<RoutingElement> dataSet = new LinkedList<>();
		if(simpleLocations != null){
			dataSet.addAll(simpleLocations);
		}
		if(connectedLocations != null){
			dataSet.addAll(connectedLocations);
		}
		return getRouting(data, dataSet);
	}
	
	@Override
	public List<VehicleInformation> getRouting(DataController data, List<RoutingElement> dataSet) {
		this.data = data;
		// Singular clusters
		List<HCluster> initCluster = new ArrayList<>();
		for (RoutingElement element : dataSet)
			initCluster.add(new HCluster(element, data));

		// Build tree
		HCluster top = buildTree(initCluster);
		return top.getRouting(subRouting);
	}

	private HCluster buildTree(List<HCluster> clusters) {
		if (clusters.size() == 1)
			return clusters.get(0);

		int minI = 0, minJ = 0;
		double minDist = Double.MAX_VALUE;
		for (int i = 0; i < clusters.size() - 1; i++) {
			for (int j = i + 1; j < clusters.size(); j++) {
				double dist = clusters.get(i).getDistanceTo(clusters.get(j));
				if (dist < minDist) {
					minDist = dist;
					minI = i;
					minJ = j;
				}
			}
		}

		List<HCluster> next = new ArrayList<>();
		HCluster combined = new HCluster(clusters.get(minI), clusters.get(minJ), data);
		next.add(combined);
		for (int i = 0; i < clusters.size(); i++) {
			if (i == minI || i == minJ)
				continue;
			next.add(clusters.get(i));
		}
		return buildTree(next);
	}
}
