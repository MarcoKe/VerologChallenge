package data;

import java.util.List;
import java.util.TreeMap;

public class OverallCost {
	public final static String MAX_NUM_VEHIC = "MAX_NUMBER_OF_VEHICLES = ";
	public final static String NUM_VEHIC_DAYS = "NUMBER_OF_VEHICLE_DAYS = ";
	public final static String TOOL_USE = "TOOL_USE = ";
	public final static String DIST = "DISTANCE = ";
	public final static String TOTAL_COST = "COST = ";
	
	private final static long TOTAL_COST_DEFAULT = -1;

	private int maxVehicle;
	private int numVehicleDays;
	private TreeMap<Integer, Integer> toolUsageMap; // <ToolId, #of Tools>
	private long distance;
	private long totalCost;

	
	public OverallCost(int maxVehic, int numVehicDays, TreeMap<Integer, Integer> toolUsageMap, long distance) {
		this.maxVehicle = maxVehic;
		this.numVehicleDays = numVehicDays;
		this.toolUsageMap = toolUsageMap;
		this.distance = distance;
		totalCost = TOTAL_COST_DEFAULT;
	}
	
	public OverallCost(int maxVehic, int numVehicDays, TreeMap<Integer, Integer> toolUsageMap, long distance,
			DataController data) {
		this.maxVehicle = maxVehic;
		this.numVehicleDays = numVehicDays;
		this.toolUsageMap = toolUsageMap;
		this.distance = distance;
		this.totalCost = calcTotalCost(data);
	}

	public long calcTotalCost(DataController data) {
		long ret = totalCost;
		if(ret == TOTAL_COST_DEFAULT) {
			ret = 0;
			ret += data.getVehicle().getCostPerVehicle() * maxVehicle;
			ret += data.getVehicle().getCostPerDay() * numVehicleDays;
			ret += data.getVehicle().getCostPerDistance() * distance;
	
			List<Tool> tools = data.getToolList();
			for (Tool t : tools) {
				Integer usage = toolUsageMap.get(t.getId());
				if (usage != null) {
					ret += t.getCost() * usage;
				} else {
					throw new RuntimeException("Tool " + t.getId() + " not in toolUsageMap");
				}
			}
		}

		return ret;
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(MAX_NUM_VEHIC);
		sb.append(maxVehicle);
		sb.append('\n');
		sb.append(NUM_VEHIC_DAYS);
		sb.append(this.numVehicleDays);
		sb.append('\n');
		sb.append(TOOL_USE);
		int lastKey = toolUsageMap.lastKey();
		for(int i = toolUsageMap.firstKey(); i <= lastKey; ++i){
			Integer usage = toolUsageMap.get(i);
			if(usage != null) {
				sb.append(usage);
				sb.append(' ');
			}
		}
		sb.deleteCharAt(sb.length());	
		sb.append('\n');
		sb.append(DIST);
		sb.append(this.distance);
		sb.append('\n');
		sb.append(TOTAL_COST);
		sb.append(this.totalCost);
		sb.append('\n');
		return sb.toString();
	}

}
