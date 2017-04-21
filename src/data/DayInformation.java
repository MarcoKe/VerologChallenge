package data;

import java.util.ArrayList;
import java.util.List;

public class DayInformation {

	
	private final static String DAY_KEY = "DAY = ";
	private final static String VEHICLE_NUMBER_KEY = "NUMBER_OF_VEHICLES = ";
	
	private int day;
	private List<VehicleInformation> vehicList;
	
	
	public DayInformation(int day) {
		this.day = day;
		vehicList = new ArrayList<>();
	}
	
	
	public void addVehicleInformation(VehicleInformation info){
		vehicList.add(info);
	}
	
	public List<VehicleInformation> getVehicleInformationList(){
		return vehicList;
	}
	
	public int getDay() {
		return day; 
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(DAY_KEY);
		sb.append(day);
		sb.append('\n'); 
		
		sb.append(VEHICLE_NUMBER_KEY);
		sb.append(vehicList.size());
		sb.append('\n');
		
		//write vehicle information (start at 1)
		for(int i = 0; i< vehicList.size(); ++i){
			sb.append(vehicList.get(i).write(i+1));
			sb.append('\n');
		}
		
		return sb.toString();
	}
	
}
