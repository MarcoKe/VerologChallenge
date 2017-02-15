package data;

public class Request {
	
	private int id;
	private Location location;
	private int startTime;
	private int endTime;
	private Tool tool;
	private int usageTime;
	private int amountOfTools;
	
	public Request(int id, Location location, int startTime, int endTime, Tool tool, int usageTime, int amountOfTools) {
		this.id = id;
		this.location = location;
		this.startTime = startTime;
		this.endTime = endTime;
		this.tool = tool;
		this.usageTime = usageTime;
		this.amountOfTools = amountOfTools;
	}
	
	public Location getLocation() {
		return location;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}
	
	public Tool getTool() {
		return tool;
	}

	public int getUsageTime() {
		return usageTime;
	}

	public int getAmountOfTools() {
		return amountOfTools;
	}

	public int getId() {
		return id;
	}
	
}
