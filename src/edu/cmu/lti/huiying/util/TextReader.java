package edu.cmu.lti.huiying.util;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import edu.cmu.lti.huiying.domainclasses.Column;
import edu.cmu.lti.huiying.domainclasses.Field;
public class TextReader {

	public static ArrayList<Column> readColumnFromTsv(String filepath){
		ArrayList<Column> cols=new ArrayList<Column>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			String line=null;
			while((line=br.readLine())!=null){
				String[] spl=line.trim().split("\t");
				Column c=new Column();
				c.content=new ArrayList<Field>();
				for(String s:spl){
					c.content.add(new Field(s));
				}
				cols.add(c);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cols;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
