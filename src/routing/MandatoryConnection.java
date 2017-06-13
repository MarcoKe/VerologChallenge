package routing;

import java.util.LinkedList;
import java.util.List;

import data.Request;
import data.VehicleAction;
import data.VehicleAction.Action;

public class MandatoryConnection {
	private List<VehicleAction> pickupList;
	private List<VehicleAction> deliverList;
	
	public MandatoryConnection() {
		pickupList = new LinkedList<>();
		deliverList = new LinkedList<>();
	}
	
	public MandatoryConnection(List<VehicleAction> pickUpList, List<VehicleAction> deliverList ){
		this.pickupList = new LinkedList<>(pickupList);
		this.deliverList = new LinkedList<>(deliverList);
	}
	
	public MandatoryConnection(MandatoryConnection manCon){
		this.pickupList = new LinkedList<>();
		this.deliverList = new LinkedList<>();
		if(manCon != null){
			this.pickupList.addAll(manCon.getPickupList());
			this.deliverList.addAll(manCon.getDeliverList());			
		}
	}
	
	
	public void addPickupList(VehicleAction pickup){
		if(!pickupList.contains(pickup)){
			pickupList.add(pickup);			
		}
	}
	
	public void addDeliverList(VehicleAction deliver){
		if(!deliverList.contains(deliver)){
			deliverList.add(deliver);			
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

}
