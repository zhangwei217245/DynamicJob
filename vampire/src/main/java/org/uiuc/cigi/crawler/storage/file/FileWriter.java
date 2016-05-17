package org.uiuc.cigi.crawler.storage.file;

import java.io.FileOutputStream;
import java.io.IOException;

public class FileWriter {
	
	public static void writeIntoFile(String filePath, String content,boolean isAppend){
		FileOutputStream fos = null;
		
		try {
			fos = new FileOutputStream(filePath,isAppend);
			fos.write(content.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
