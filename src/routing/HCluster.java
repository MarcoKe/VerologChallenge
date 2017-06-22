package routing;

import java.util.LinkedList;
import java.util.List;

import data.DataController;
import data.VehicleInformation;

public class HCluster {

	private DataController data = null;
	
	private HCluster parent;
	
	private HCluster left;
	
	private HCluster right;
	
	private List<RoutingElement> elements;
	
	public HCluster(RoutingElement route, DataController data){
		this.data = data;
		elements = new LinkedList<>();
		elements.add(route);
		parent = null;
		left = null;
		right = null;
	}
	
	public HCluster(HCluster left, HCluster right,DataController data){
		this.data = data;
		this.left = left;
		this.right = right;
		elements = new LinkedList<>();
		elements.addAll(left.getElements());
		elements.addAll(right.getElements());
		left.setParent(this);
		right.setParent(this);
	}
	
	public double getDistanceTo(HCluster other){
		double maxDist = Double.MIN_VALUE;
		for (int i = 0; i < this.elements.size(); i++) {
			for (int j = 0; j < other.elements.size(); j++) {
				double dist = getDistance(this.elements.get(i), other.elements.get(j));
				if (dist > maxDist) {
					maxDist = dist;
				}
			}
		}
		return maxDist;
	}
	
	private double getDistance(RoutingElement first, RoutingElement second) {
		double ret = 0.0;
		double[] mean = first.getSpaceVector(data);
		double[] spaceVec = second.getSpaceVector(data);
		assert mean.length == spaceVec.length;
		for (int i = 0; i < mean.length; ++i) {
			ret += Math.pow(spaceVec[i] - mean[i], 2);
		}
		return ret;
	}
	
	public DataController getData() {
		return data;
	}

	public HCluster getParent() {
		return parent;
	}

	public HCluster getLeft() {
		return left;
	}

	public HCluster getRight() {
		return right;
	}

	public List<RoutingElement> getElements() {
		return elements;
	}
	
	private void setParent(HCluster hCluster) {
		this.parent = hCluster;
	}

	public List<VehicleInformation> getRouting(Routing subRouting) {
		List<VehicleInformation> list = subRouting.getRouting(data, elements);
		if(list.size()==1)
			return list;
		
		list = left.getRouting(subRouting);
		list.addAll(right.getRouting(subRouting));
		return list;
	}
}
