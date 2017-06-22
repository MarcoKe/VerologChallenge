package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import data.DataController;
import data.Request;
import data.VehicleAction;
import data.VehicleAction.Action;
import routing.MandatoryConnection;
import routing.RoutingElement;

public class PlacementReader {
	private static final String DATASET_VAR = "DATASET = ";
	private static final String NAME_VAR = "NAME = ";
	private DataController data; 
	
	public PlacementReader(DataController data) {
		this.data = data; 
	}
	
	public List<List<RoutingElement>> readFile(String filename) { 
		List<List<RoutingElement>> solution = new ArrayList<>();
		
		File f = new File(filename);
		
		
		if (!f.exists()) {
			System.out.println("File doesnt exist");
			return solution;
		}

		List<Request> requests = data.getRequestList();
		int days = data.getGlobal().getDays();
		
		FileReader in = null;
		BufferedReader reader = null;
		try {
			in = new FileReader(f);
			reader = new BufferedReader(in);
			// Data set
			String dataset = reader.readLine().substring(DATASET_VAR.length());
			// Name
			String name = reader.readLine().substring(NAME_VAR.length());
			// ----------------------------------------
			
			for (int day = 1; day <= days; day++) {
				List<RoutingElement> dayList = new ArrayList<>(); 
				
				reader.readLine(); // blank
				reader.readLine(); // day
				
				int numSimple = Integer.parseInt(reader.readLine().split(" ")[1]); 
				for (int i = 0; i < numSimple; i++) { 
					int requestId = Integer.parseInt(reader.readLine()); 
					dayList.add(createVehicleAction(requests, requestId));
				}
				
				reader.readLine(); // blank 
				
				int numMandatory = Integer.parseInt(reader.readLine().split(" ")[1]);
				
				for (int i = 0; i < numMandatory; i++) {
					String manCon = reader.readLine();
					dayList.add(createMandatoryConnection(requests, manCon));
				}
				
				solution.add(dayList);
			}
			
			
			
				
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return solution; 
	}
	
	public VehicleAction createVehicleAction(List<Request> requests, int id) { 
		Action type = id < 0 ? Action.PICK_UP : Action.LOAD_AND_DELIVER; 
		id = Math.abs(id); 
		
		Request request = null; 
		
		for (Request r : requests) {
			if (r.getId() == id) {
				request = r; 
			}
		}
		
		return new VehicleAction(type, request); 
		
	}
	
	public MandatoryConnection createMandatoryConnection(List<Request> requests, String input) {		
		MandatoryConnection manCon = new MandatoryConnection(); 
		
		System.out.println(input);
		String[] actions = input.split(" "); 
		for (String action : actions) {
			int id = Integer.parseInt(action); 
			System.out.println(id);
			
			if (id < 0) {
				manCon.addPickupList(createVehicleAction(requests, id));
			}
			else {
				manCon.addDeliverList(createVehicleAction(requests, id));
			}			
		}
		
		return manCon;
	}
	
	public static void main(String[] args) { 
		Reader r = new Reader();		
		DataController data = r.readFile("data/ORTEC_Test/ORTEC_Test_01.txt");
		String filename = "data/ORTEC_Test/ORTEC_Test_01.plc.txt";
		PlacementReader reader = new PlacementReader(data); 
		List<List<RoutingElement>> solution = reader.readFile(filename);
		
		PlacementWriter writer = new PlacementWriter();
		writer.write(data, solution);
	}
}
