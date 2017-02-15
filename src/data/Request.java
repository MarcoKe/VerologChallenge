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
	
	public final Location getLocation() {
		return location;
	}

	public final int getStartTime() {
		return startTime;
	}

	public final int getEndTime() {
		return endTime;
	}
	
	public final Tool getTool() {
		return tool;
	}

	public final int getUsageTime() {
		return usageTime;
	}

	public final int getAmountOfTools() {
		return amountOfTools;
	}

	public final  int getId() {
		return id;
	}
	
}
