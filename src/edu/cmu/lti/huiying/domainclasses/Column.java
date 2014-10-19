package edu.cmu.lti.huiying.domainclasses;

import java.util.ArrayList;

public class Column {
	public Group g;
	public ArrayList<Field> content= null;
	public Header header;
	
	public Column(ArrayList<Field> col){
		this.content=col;
	}
	public Column(){
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
