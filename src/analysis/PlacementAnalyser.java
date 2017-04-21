package analysis;

import java.util.List;
import java.util.Map;

import data.DataController;
import data.Placement;
import data.Request;
import data.Tool;

public class PlacementAnalyser {
	private DataController data; 
	private List<Tool> tools; 
	private int[] maxToolAmount; 
	
	public PlacementAnalyser(DataController data) {
		this.data = data; 
		this.tools = data.getToolList();
		this.maxToolAmount = new int[tools.size()]; 
		
		for (Tool tool : tools) {
			this.maxToolAmount[tool.getId()-1] = tool.getMaxAvailable();
		}
	}
	
	/*
	 * (tool, day | usage)
	 */
	public int[][] computeToolUsage(Placement p) {
		int[][] toolUsage = new int[tools.size()][data.getGlobal().getDays()];
		
		Map<Request, Integer> placement = p.getPlacement();
		for (Request request : placement.keySet()) {
			for (int day = placement.get(request)-1; day <= placement.get(request)-1 + request.getUsageTime(); day++) {
				toolUsage[request.getTool().getId()-1][day] += request.getAmountOfTools();
			}
		}
		
		return toolUsage; 
		
	}
	
	/* 
	 * compute max tool violations as a sum over all days 
	 * TODO: need to take into account that sometimes we can match a pickup & a request. This should not contribute to the violation sum
	 */
	public int getConstraintViolations(Placement p) {
		int[][] toolUsage = computeToolUsage(p); 
		int violations = 0; 
		
		for (int tool = 0; tool < toolUsage.length; tool++) {
			for (int day = 0; day < data.getGlobal().getDays(); day++) {
				int diff = toolUsage[tool][day] - maxToolAmount[tool];
				if (diff > 0) 
					violations += diff; 	 
			}
		}
		
		return violations; 
	}
	
	
	

}
