package edu.cmu.lti.huiying.features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.cmu.lti.huiying.domainclasses.*;

public class ContextBOWFeatureGenerator {
	public LinkedHashMap<String,Integer> featuremap=null;
	public ArrayList<Hashtable<String,Double>> columnFeatVectors=null;
	private Hashtable<String, Integer> ffreq=new Hashtable<String, Integer>();
	private HashSet<String> stopwords=new HashSet<String>();
	public int topNFreq=300;
	
	public void columns2Features(ArrayList<Column> columns){
		topNFeatureSelection(columns, this.topNFreq);
		for(int i = 0; i < this.columnFeatVectors.size(); i++){
			Hashtable<String,Double> colvec=this.columnFeatVectors.get(i);
			Hashtable<String,Double> filtered=new Hashtable<String,Double>();
			for(String s:colvec.keySet()){
				if(this.featuremap.containsKey(s)){
					filtered.put(s, colvec.get(s));
				}
			}
			this.columnFeatVectors.set(i, filtered);
		}
	}
	
	private void topNFeatureSelection(ArrayList<Column> columns, int topN){
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
			this.columnFeatVectors.add(colvec);
		}
		//selection:sort feature frequency table
		ArrayList<Map.Entry<String, Integer>> l = new ArrayList(this.ffreq.entrySet());
		Collections.sort(l,new Comparator<Map.Entry<String,Integer>>(){
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String,Integer> o2){
				return -1*o1.getValue().compareTo(o2.getValue());
			}
		});
		for(int i = 0; i < topN; i++){
			this.featuremap.put(l.get(i).getKey(), featuremap.size());
		}
	}
	private ArrayList<Column> getColumnsFromArticles(ArrayList<Article> articles){
		ArrayList<Column> cols=new ArrayList<Column>();
		for(Article a:articles){
			for(Table t:a.tables){
				if(t.check()){
					for(Group g:t.groups){
						for(int i = 0; i < g.columns.size(); i++){
							Column column=new Column(g.columns.get(i));
							column.g=g;
							if(g.headers!=null && i<g.headers.size())
								column.header=g.headers.get(i);
							cols.add(column);
						}
					}
				}
			}
		}
		return cols;
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
