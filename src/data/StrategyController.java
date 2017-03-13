package data;

import java.util.List;

public class StrategyController {

	
	private List<DayInformation> days;
	
	public StrategyController(List<DayInformation> days) {
		this.days = days;
	}

	public final List<DayInformation> getDays() {
		return days;
	}
	
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i< days.size();++i){
			sb.append(days.get(i).toString());
			sb.append('\n');
		}
		return sb.toString();
	}
	
}
