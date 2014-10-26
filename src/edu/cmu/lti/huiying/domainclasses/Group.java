package edu.cmu.lti.huiying.domainclasses;

import java.util.ArrayList;
import java.util.Hashtable;
public class Group {

	public Table table;//The table it belongs to
	public ArrayList<Header> headers;
	public ArrayList<Header> headerlist;
	public ArrayList<Column> columns; 
	public ArrayList<ArrayList<Field>> rows; 
	private int width=-1; //Number of columns in the grid. Since row may not share same size, this is the voted length
	//of all the rows.
	private int length=-1;//Number of rows
	
	public Group(){
		this.headers=null;
		this.headerlist=null;
		this.columns=new ArrayList<Column>();
		this.rows=new ArrayList<ArrayList<Field>>();
	}
	
	/**
	 * Since it is possible that rows are of various length (it should be rare), so the width of the grid will be the
	 * mostly voted length.
	 * @return
	 */
	public int getWidth(){
		
		if(this.width==-1){
			if(this.rows!=null & this.rows.size()>0){
				Hashtable<Integer,Integer> vote=new Hashtable<Integer,Integer>();
				int maxvote=-1;
				int maxnum=-1;
				for(ArrayList<Field> r:this.rows){
					int size=r.size();
					
					if(!vote.containsKey(r.size())){
						vote.put(size, 0);
					}
					vote.put(size, vote.get(size)+1);
					if(vote.get(size)>maxvote){
						maxvote=vote.get(size);
						maxnum=size;
					}
				}
				this.width=maxnum;
			}
			else{
				this.width=0;
			}
		}
		return this.width;
	}

	public int getLength(){
		if(this.length==-1){//initialize
			if(this.rows!=null)
				this.length=this.rows.size();
			else
				this.length=0;
		}
		return this.length;
	}
	
	/**
	 * This method transpose the row grid, and fill the column grid with the transposed one.
	 * The rows will be cut short if it is longer than voted width, and will prolong with 
	 * empty string if it is shorter than the voted length.
	 */
	public void transpose(){
		int nCol=getWidth();
		for(int i =0; i < nCol; i++){
			ArrayList<Field> col = new ArrayList<Field>();
			for(int j = 0; j < getLength(); j++){
				if(i<rows.get(j).size())
					col.add(this.rows.get(j).get(i));
				else
					col.add(new Field(""));
			}
			Column c = new Column(col);
			c.g=this;
			this.columns.add(c);
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
