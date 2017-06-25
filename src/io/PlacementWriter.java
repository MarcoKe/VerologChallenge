
package io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import data.DataController;
import data.Global;
import data.VehicleAction;
import routing.MandatoryConnection;
import routing.RoutingElement;
public class PlacementWriter {
	private static final String DATASET_VAR = "DATASET = ";
	private static final String NAME_VAR = "NAME = ";	
	
	public void write(DataController data, List<List<RoutingElement>> solution) {
		List<List<VehicleAction>> simpleLocations = new ArrayList<>(); 
		List<List<MandatoryConnection>> connectedLocations = new ArrayList<>(); 
		
		for (List<RoutingElement> elList : solution) {
			List<VehicleAction> simpleDay = new ArrayList<>(); 
			List<MandatoryConnection> connectedDay = new ArrayList<>(); 
			
			
			for (RoutingElement el : elList) {
				if (el instanceof VehicleAction) {
					simpleDay.add((VehicleAction) el); 
				}
				else if(el instanceof MandatoryConnection) {
					connectedDay.add((MandatoryConnection) el); 
				}
			}
			
			simpleLocations.add(simpleDay); 
			connectedLocations.add(connectedDay);
		}
		
		write(data, simpleLocations, connectedLocations); 
	}
	
	public void write(DataController data, List<List<VehicleAction>> simpleLocations, List<List<MandatoryConnection>> connectedLocations){
		String fileName = data.getGlobal().getFileName().split("\\.")[0] + ".txt.plc";
		BufferedWriter bw = null;	
		try {
			bw = new BufferedWriter(new FileWriter(fileName));
			
			bw.write(DATASET_VAR+data.getGlobal().getDataSet()+'\n');
			bw.write(NAME_VAR+data.getGlobal().getName()+'\n');
			bw.write('\n');
						
			for (int i = 0; i < simpleLocations.size(); i++) {
				bw.write("DAY " + (i+1));
				bw.write("\nsimple: " + simpleLocations.get(i).size() +"\n");
				
				for (VehicleAction action: simpleLocations.get(i)) {
					bw.write(action.toString() +"\n");
				}
				
				bw.write("\nmandatory: " + connectedLocations.get(i).size() +"\n");
				
				for (RoutingElement el : connectedLocations.get(i)) {
					for (VehicleAction action : el.getRouteElement()) {
						bw.write(action.toString() + " ");
					}
					bw.write("\n");
				}
				
				bw.write("\n");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if ( bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}	
}