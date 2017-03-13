package io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import data.DataContoller;
import data.StrategyController;

public class Writer {
	private static final String DATASET_VAR = "DATASET = ";
	private static final String NAME_VAR = "NAME = ";
	
	private static boolean writeOptionalInfo = false;

	public Writer() {
	
	}
	
	
	public void write(DataContoller data,StrategyController strat, String filename){
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
	
	
	private String computeOptionalInfo(DataContoller data, StrategyController start){
		StringBuilder sb = new StringBuilder();
		sb.append("bullshit");
		return sb.toString();
	}
}
