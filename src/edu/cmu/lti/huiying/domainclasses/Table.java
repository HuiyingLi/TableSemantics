package edu.cmu.lti.huiying.domainclasses;

import java.util.ArrayList;

public class Table {
	public Article article;
	public String caption;
	public String legend;
	public ArrayList<Group> groups;
	
	public Table(){
		this.groups=new ArrayList<Group>();
	}

	
	public void findHeaders(){
		
	}
	
	
	/**
	 * This function checks if a table is mal-formed.
	 * The criterions include:
	 * if table contains groups
	 * if header exists
	 * if header length equals to row width
	 * Any fails by the above will lead to false return value.
	 * @return
	 */
	public boolean check(){
		if(this.groups.size()==0)
			return false;
		for(Group g:this.groups){
			if(g.headers==null)
				return false;
			if(g.headers.size()!=g.getWidth())
				return false;
		}
		return true;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
