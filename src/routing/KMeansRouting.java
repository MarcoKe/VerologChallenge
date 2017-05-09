package routing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


import data.DataController;
import data.VehicleAction;
import data.VehicleInformation;

public class KMeansRouting implements Routing{
	
	private static final boolean capacityVariance = false;
	
	public KMeansRouting() {
	}

	
	public List<VehicleInformation> getRouting(DataController data, List<VehicleAction> dataSet,List<MandatoryConnection> conLoc) {
		List<VehicleInformation> ret = null;
		boolean foundSolution = false;
		int k = 1;
		while(!foundSolution){
			Set<MeanCoord> meanSet = initRandomMeans(k, dataSet);
			Map<MeanCoord,List<VehicleAction>> clusters = null;
			boolean noChange = true;
			while(noChange){
				clusters = assignToMean( meanSet, dataSet);
				Set<MeanCoord> newMeanSet = calcNewMean(clusters);
				if(meanSet.containsAll(newMeanSet)){
					noChange = false;
				}
				meanSet = newMeanSet;			
			};
			
			//TODO Find a routing solution for List<VehicleAction> in $clusters
			// use interface to make it easy to swap between routing algorithms where, List<VehicleAction> is solver by one vehicle ?
			//
			
			//TODO delete
			if(k == 5){
				foundSolution = true;
			}
			
			
			//increase k until found solution
			++k;
		}
		
		return ret;
	}

	
	
	private Map<MeanCoord,List<VehicleAction>> assignToMean(Set<MeanCoord> meanSet, List<VehicleAction> dataSet ){
		Map<MeanCoord,List<VehicleAction>> ret = new TreeMap<>();
		for(MeanCoord mean: meanSet){
			ret.put(mean, new ArrayList<>());
		}

		for(int actionPos = 0 ; actionPos<dataSet.size(); ++actionPos){
			VehicleAction currAction = dataSet.get(actionPos);
			MeanCoord minMean = null;
			double minVariance = Double.MAX_VALUE;
			for(MeanCoord mean :meanSet){
				double variance = getVariance(mean, currAction);
				if(variance < minVariance){
					minMean = mean;
					minVariance = variance;
				}
			}	
			ret.get(minMean).add(currAction);
		}
		return ret;
	}
	
	
	private Set<MeanCoord> calcNewMean(Map<MeanCoord,List<VehicleAction>> clusters){
		Set<MeanCoord> ret = new TreeSet<>();
		for(MeanCoord mean : clusters.keySet()){
			List<VehicleAction> dataList = clusters.get(mean);
			int clusterSize = dataList.size();
			
			//TODO Handle overflow
			double meanX = 0,meanY = 0, meanCapacity = 0;	
			double safeX = 0, safeY= 0, safeCapacity = 0;
			for(int i = 0; i < dataList.size(); ++i){
				VehicleAction action = dataList.get(i);
				double actionX = action.getRequest().getLocation().getX();
				double actionY = action.getRequest().getLocation().getY();
				int partAmount = action.getRequest().getAmountOfTools();
				int partSize = action.getRequest().getTool().getSize();
				double actionCapacity = partAmount*partSize;

				//check for overflow (might want to do underflow but we are only working with positive numbers (variance was squared))
				boolean isOverflowSafe = (safeX >0 && actionX > Double.MAX_VALUE- safeX );
				isOverflowSafe = isOverflowSafe && (safeY >0 && actionY > Double.MAX_VALUE- safeY);
				isOverflowSafe = isOverflowSafe && (safeCapacity >0 && actionCapacity > Double.MAX_VALUE- safeCapacity);			
				if (isOverflowSafe){
					safeX+=actionX;
					safeY+=actionY;
					safeCapacity=actionCapacity;
				}else {
					meanX += safeX/clusterSize;
					meanY += safeY/clusterSize;
					meanCapacity += safeCapacity/clusterSize;
					
					safeX=0;
					safeX=0;
					safeCapacity=0;
				}
				
				/*meanX += action.getRequest().getLocation().getX();
				meanY += action.getRequest().getLocation().getY();
				int partAmount = action.getRequest().getAmountOfTools();
				int partSize = action.getRequest().getTool().getSize();
				meanCapacity = partAmount*partSize;		*/	
			}
			meanX += safeX/clusterSize;
			meanY += safeY/clusterSize;
			meanCapacity += safeCapacity /clusterSize;
			
			
			ret.add(new MeanCoord(meanX, meanY, meanCapacity));
		}
		
		return ret;		
	}
	
	private double getVariance( MeanCoord mean, VehicleAction action){
		double ret = 0.0;
		ret+= Math.pow(action.getRequest().getLocation().getX()-mean.x , 2);
		ret+= Math.pow(action.getRequest().getLocation().getY()-mean.y , 2);
		
		if(capacityVariance){
			int partAmount = action.getRequest().getAmountOfTools();
			int partSize = action.getRequest().getTool().getSize();		
			ret+= Math.pow((partAmount*partSize)-mean.capacity , 2);			
		}	
		return ret;
	}
	
	/**
	 * Get k randomly selected means without duplicates, where capacity is zero (to make sure that a location is not twice in the set)
	 * 
	 * @param k
	 * @param dataSet
	 * @return
	 */
	private Set<MeanCoord> initRandomMeans(int k ,List<VehicleAction> dataSet){
		Set<MeanCoord> ret = new TreeSet<>();
		Random rng = new Random();
		while(ret.size() < k){
			int listPos = rng.nextInt(dataSet.size());
			MeanCoord tmp = convertActionToCoord(dataSet.get(listPos));
			tmp.capacity = 0;
			ret.add(tmp);			
		}
		return ret;
	}

	private MeanCoord convertActionToCoord(VehicleAction action	){
		double x = action.getRequest().getLocation().getX();
		double y = action.getRequest().getLocation().getY();
		int partAmount = action.getRequest().getAmountOfTools();
		int partSize = action.getRequest().getTool().getSize();
		double capacity = partAmount*partSize;		
		return new MeanCoord(x, y, capacity);
	}
	
}

class MeanCoord{
	
	public double x,y,capacity;
	public MeanCoord(double x, double y, double capacity) {
		this.x = x;
		this.y = y;
		this.capacity = capacity;
	}
}
