package routing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import data.DataController;
import data.VehicleAction;
import data.VehicleInformation;
import util.RoutingUtil;

public class KMeansRouting implements Routing {


	private DataController data = null;
	private Routing subRouting = null;
	
	
	public KMeansRouting(Routing subRouting) {
		this.subRouting = subRouting;
	}

	public List<VehicleInformation> getRouting(DataController data, List<VehicleAction> simpleLoc,
			List<MandatoryConnection> conLoc) {
		LinkedList<RoutingElement> dataSet = new LinkedList<>();
		if(simpleLoc != null){
			dataSet.addAll(simpleLoc);
		}
		if(conLoc != null){
			dataSet.addAll(conLoc);
		}
		
		return getRouting(data, dataSet);		
	}
	
	
	public List<VehicleInformation> getRouting(DataController data, List<RoutingElement> dataSet) {
		List<VehicleInformation> ret = null;
		boolean foundSolution = false;
		this.data = data;
		int k = 1;
		try {
			while (!foundSolution) {
				Set<double[]> meanSet = null;
				meanSet = initRandomMeans(k, dataSet);
				Map<double[], List<RoutingElement>> clusters = null;
				boolean noChange = true;
				while (noChange) {
					clusters = assignToMean(meanSet, dataSet);
					Set<double[]> newMeanSet = calcNewMean(clusters);
					if (meanSet.containsAll(newMeanSet)) {
						noChange = false;
					}
					meanSet = newMeanSet;
				}
				

				// TODO Find a routing solution for List<VehicleAction> in
				// $clusters
				// use interface to make it easy to swap between routing
				// algorithms where, List<VehicleAction> is solver by one
				// vehicle ?
				//

				// TODO delete
				if (k == 5) {
					foundSolution = true;
				}

				// increase k until found solution
				++k;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}
	

	private Map<double[], List<RoutingElement>> assignToMean(Set<double[]> meanSet, List<RoutingElement> dataSet) {
		Map<double[], List<RoutingElement>> ret = new TreeMap<>();
		for (double[] mean : meanSet) {
			ret.put(mean, new ArrayList<>());
		}

		for (int actionPos = 0; actionPos < dataSet.size(); ++actionPos) {
			RoutingElement currElem = dataSet.get(actionPos);
			double[] minMean = null;
			double minVariance = Double.MAX_VALUE;
			for (double[] mean : meanSet) {
				double variance = getVariance(mean, currElem);
				if (variance < minVariance) {
					minMean = mean;
					minVariance = variance;
				}
			}
			ret.get(minMean).add(currElem);
		}
		return ret;
	}

	
	/**
	 * 
	 * @param clusters
	 * @return Center of Mass of cluster values (RoutingElemen)
	 */
	private Set<double[]> calcNewMean(Map<double[], List<RoutingElement>> clusters) {
		Set<double[]> ret = new TreeSet<>();
		for (double[] mean : clusters.keySet()) {
			double[] newMean = RoutingUtil.getCenterOfMass(data, clusters.get(mean));
			ret.add(newMean);
		}
		return ret;
	}

	private double getVariance(double[] mean, RoutingElement routeElem) {
		double ret = 0.0;
		double[] spaceVec = routeElem.getSpaceVector(data);
		assert mean.length == spaceVec.length;
		for (int i = 0; i < mean.length; ++i) {
			ret += Math.pow(spaceVec[i] - mean[i], 2);
		}
		return ret;
	}

	/**
	 * Get k randomly selected means without duplicates, where capacity is zero
	 * (to make sure that a location is not twice in the set)
	 * 
	 * @param k
	 * @param dataSet
	 * @return
	 * @throws Exception
	 */
	private Set<double[]> initRandomMeans(int k, List<RoutingElement> dataSet) throws Exception {
		Set<double[]> ret = new TreeSet<>();
		Set<Integer> rngList = new HashSet<>();
		rngList.add(-1);
		Random rng = new Random();
		if (k > dataSet.size()) {
			throw new Exception("KMeanRouting: k is too large");
		}
		while (ret.size() < k) {
			// make sure no double randoms
			int listPos = -1;
			while (rngList.contains(listPos)) {
				listPos = rng.nextInt(dataSet.size());
			}
			rngList.add(listPos);

			double[] tmp = dataSet.get(listPos).getSpaceVector(data);
			ret.add(tmp);
		}
		return ret;
	}


}
