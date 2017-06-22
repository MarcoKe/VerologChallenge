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
				System.out.println(k);
				Set<DoubleArray> meanSet = null;
				meanSet = initRandomMeans(k, dataSet);

				Map<DoubleArray, List<RoutingElement>> clusters = null;

				boolean noChange = true;
				while (noChange) {

					clusters = assignToMean(meanSet, dataSet);

					Set<DoubleArray> newMeanSet = calcNewMean(clusters);

					if (meanSet.containsAll(newMeanSet)) {
						noChange = false;
					}

					meanSet = newMeanSet;
				}
				

				ret = new LinkedList<>();
				for(List<RoutingElement>  clusterList:	clusters.values()){
					List<VehicleInformation> list = subRouting.getRouting(data, clusterList);
					ret.addAll(list);
				}
				
				if(ret.size() == k){
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
	

	private Map<DoubleArray, List<RoutingElement>> assignToMean(Set<DoubleArray> meanSet, List<RoutingElement> dataSet) {
		Map<DoubleArray, List<RoutingElement>> ret = new TreeMap<>();
		for (DoubleArray mean : meanSet) {
			ret.put(mean, new ArrayList<>());
		}

		for (int actionPos = 0; actionPos < dataSet.size(); ++actionPos) {
			RoutingElement currElem = dataSet.get(actionPos);
			DoubleArray minMean = null;
			double minVariance = Double.MAX_VALUE;
			for (DoubleArray mean : meanSet) {
				double variance = getVariance(mean, currElem);
				if (variance < minVariance) {
					minMean = new DoubleArray(mean.ar);
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
	private Set<DoubleArray> calcNewMean(Map<DoubleArray, List<RoutingElement>> clusters) {
		Set<DoubleArray> ret = new TreeSet<>();
		for (DoubleArray mean : clusters.keySet()) {
			if(clusters.get(mean).isEmpty()){
				continue;
			}
			DoubleArray newMean =new DoubleArray(RoutingUtil.getCenterOfMass(data, clusters.get(mean)));
			ret.add(newMean);
		}
		return ret;
	}

	private double getVariance(DoubleArray mean, RoutingElement routeElem) {
		double ret = 0.0;
		double[] spaceVec = routeElem.getSpaceVector(data);
		assert mean.ar.length == spaceVec.length;
		for (int i = 0; i < mean.ar.length; ++i) {
			ret += Math.pow(spaceVec[i] - mean.ar[i], 2);
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
	private Set<DoubleArray> initRandomMeans(int k, List<RoutingElement> dataSet) throws Exception {
		Set<DoubleArray> ret = new TreeSet<>();
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
			ret.add(new DoubleArray(tmp));
		}
		return ret;
	}
	
}


class DoubleArray implements Comparable<DoubleArray>{
	final public double[] ar;
	
	public DoubleArray(double[] ar) {
		this.ar = ar;
	}

	@Override
	public int compareTo(DoubleArray o) {
		double ret = 0;
		int i=0;
		while(i<this.ar.length && ret == 0){
			ret = this.ar[i]-o.ar[i];
			++i;
		}			
		return (int) Math.signum(ret);
	}


	
}
