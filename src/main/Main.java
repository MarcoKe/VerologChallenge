package main;

import java.util.ArrayList;
import java.util.HashMap;

import data.DataContoller;
import data.Depot;
import data.Global;
import data.Location;
import data.Request;
import data.Tool;
import data.Vehicle;

public class Main {
	public static void main(String[] args) {
		/*
		//Test Data
		Global glo = new Global(5, null);
		
		Vehicle vec = new Vehicle(50, 1000, 1000, 100, 1, 1);
		Location loc = new Location(0, 0, 0);
		Tool t1 = new Tool(5, 20, 1);
		
		HashMap<Tool,Integer> hash = new HashMap<>();
		hash.put(t1, 20);
		
		Depot dep = new Depot(loc, hash);
				
		Request req = new Request(1, loc, 0, 3, t1, 5, 3);

		
		ArrayList<Location> locList = new ArrayList<>();
		ArrayList<Request> reqList = new ArrayList<>();
		ArrayList<Tool> toolList = new ArrayList<>();
		
		locList.add(loc);
		
		reqList.add(req);
		
		toolList.add(t1);
		
		DataContoller dc = new DataContoller(dep, glo, locList, reqList, toolList, vec);
		
		System.out.println("distance " + dc.getGlobal().computeDistance(loc, loc));
		*/
		
		System.out.println("Hello World");
	}
}
