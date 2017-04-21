package io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


import data.OverallCost;
import data.DataController;

import data.StrategyController;
import util.CostUtil;

public class Writer {
	private static final String DATASET_VAR = "DATASET = ";
	private static final String NAME_VAR = "NAME = ";
	
	private static boolean writeOptionalInfo = true;

	public Writer() {
	
	}
	
	
	public void write(DataController data,StrategyController strat, String filename){
		String fileName = filename.split("\\.")[0] + ".sol.txt";
		BufferedWriter bw = null;	
		try {
			bw = new BufferedWriter(new FileWriter(fileName));
			
			bw.write(DATASET_VAR+data.getGlobal().getDataSet()+'\n');
			bw.write(NAME_VAR+data.getGlobal().getName()+'\n');
			bw.write('\n');
			if(writeOptionalInfo){
				bw.write(computeOptionalInfo(data, strat));
				bw.write('\n');
			}
						
			bw.write(strat.toString());			
			
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
	
	

	private String computeOptionalInfo(DataController data, StrategyController strat){

		StringBuilder sb = new StringBuilder();
		OverallCost cost = CostUtil.getOverallCost(data, strat);
		
		sb.append(cost);		
		return sb.toString();
	}
}
