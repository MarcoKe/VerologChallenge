package routing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import data.DataController;
import data.Location;
import data.Request;
import data.VehicleAction;
import data.VehicleAction.Action;
import util.DataUtil;
import util.RoutingUtil;

public class MandatoryConnection extends RoutingElement {
	private List<VehicleAction> pickupList;
	private List<VehicleAction> deliverList;
	
	//if any changes happened since last call{spaceVector}
	boolean change[] = new boolean[1];
	
	public MandatoryConnection() {
		pickupList = new LinkedList<>();
		deliverList = new LinkedList<>();
		setChangeTrue();
	}
	
	public MandatoryConnection(List<VehicleAction> pickUpList, List<VehicleAction> deliverList ){
		this.pickupList = new LinkedList<>(pickupList);
		this.deliverList = new LinkedList<>(deliverList);
		setChangeTrue();
	}
	
	public MandatoryConnection(MandatoryConnection manCon){
		this.pickupList = new LinkedList<>();
		this.deliverList = new LinkedList<>();
		if(manCon != null){
			this.pickupList.addAll(manCon.getPickupList());
			this.deliverList.addAll(manCon.getDeliverList());			
		}
		setChangeTrue();
	}
	
	
	public void addPickupList(VehicleAction pickup){
		if(!pickupList.contains(pickup)){
			pickupList.add(pickup);	
			setChangeTrue();
		}
	}
	
	public void addDeliverList(VehicleAction deliver){
		if(!deliverList.contains(deliver)){
			deliverList.add(deliver);
			setChangeTrue();
		}
	}
	
	public void addPickupList(Request req){
		boolean add = true;
		for(VehicleAction action: pickupList){
			if(action.getRequest() == req){
				add=false;
				break;
			}
		}		
		if(add){
			addPickupList(new VehicleAction(Action.PICK_UP,req));			
		}
	}
	public void addDeliverList(Request req){
		boolean add = true;
		for(VehicleAction action: deliverList){
			if(action.getRequest() == req){
				add=false;
				break;
			}
		}		
		if(add){
			addDeliverList(new VehicleAction(Action.LOAD_AND_DELIVER,req));			
		}
	}
	
	public List<VehicleAction> getRoute(){
		List<VehicleAction> ret = new LinkedList<>(pickupList);
		ret.addAll(deliverList);
		return ret;
	}
	
	public final List<VehicleAction> getPickupList() {
		return pickupList;
	}
	
	public final List<VehicleAction> getDeliverList() {
		return deliverList;
	}
	private void setChangeTrue(){
		for(int i=0;i<change.length;++i){
			change[i]= true;
		}
	}
	


	@Override
	public double[] getSpaceVector(DataController data) {
		if(change[0]){
			spaceVector = null;
		}
		if(spaceVector == null){
			List<RoutingElement> rtElemList = new ArrayList<>(getRoute());
			spaceVector = RoutingUtil.getCenterOfMass(data, rtElemList);
			if(spaceVector == null){
				spaceVector = new double[data.getToolList().size()+LOCATION_DIMENSION];
			}
			change[0] = false;
		}	
		return spaceVector;
	}

	public List<VehicleAction> getRouteElement() {
		return getRoute();
	}

}
