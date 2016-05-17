package org.uiuc.cigi.crawler.util;

import org.uiuc.cigi.crawler.config.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class StateReader {

	public static HashMap<String,String> readData(){
		HashMap<String,String> cbList = new HashMap<String,String>();		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(Configuration.defaultPath()+File.separator+"states.txt")));
			String line = "";
			while((line=br.readLine())!=null){
				if(line.indexOf("-")>-1){
					String[] items = line.split("-");
					cbList.put(items[0].trim(), items[1].trim());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return cbList;
	}
	public static void main(String[] args) {
		readData();

	}

}
