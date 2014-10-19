package edu.cmu.lti.huiying.features;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import edu.cmu.lti.huiying.domainclasses.Article;
import edu.cmu.lti.huiying.domainclasses.*;

public class ContextBOWFeatureGenerator {
	public LinkedHashMap<String,Integer> featuremap=null;
	public ArrayList<Hashtable<String,Double>> columnFeatVectors=null;
	private Hashtable<String, Integer> ffreq=new Hashtable<String, Integer>();
	public int topNFreq=300;
	public void columns2Features(ArrayList<Column> columns){
		for(Column c:columns){
			Hashtable<String,Double> colvec=new Hashtable<String,Double>();
			//words from table caption
			if(c.g.table.caption!=null){
				for(String s:c.g.table.caption.toLowerCase().split(" ")){
					addFeature(s);
					addToVec(colvec,s);
				}
			}
			//words from table context
			if(c.g.table.legend!=null){
				for(String s:c.g.table.legend.toLowerCase().split(" ")){
					addFeature(s);
					addToVec(colvec,s);
				}
			}
			//words from article title
			if(c.g.table.article.title!=null){
				for(String s:c.g.table.article.title.toLowerCase().split(" ")){
					addFeature(s);
					addToVec(colvec,s);
				}
			}
			//words from article keywords
			if(c.g.table.article.keywords!=null){
				for(String s:c.g.table.article.keywords){
					addFeature(s);
					addToVec(colvec,s);
				}
			}
		}
		
	}
	
	private Hashtable<String, Double> addToVec(Hashtable<String, Double> colvec, String s){
		if(!colvec.contains(s)){
			colvec.put(s, 1.0);
		}
		else{
			colvec.put(s, colvec.get(s)+1);
		}
		return colvec;
	}
	private void addFeature(String s){
		if(this.ffreq.contains(s)){
			this.ffreq.put(s, 1);
		}else{
			this.ffreq.put(s, ffreq.get(s)+1);
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	
	}

}
