package edu.cmu.lti.huiying.features;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import edu.cmu.lti.huiying.domainclasses.Article;
import edu.cmu.lti.huiying.domainclasses.Column;
import edu.cmu.lti.huiying.domainclasses.Field;
import edu.cmu.lti.huiying.domainclasses.Group;
import edu.cmu.lti.huiying.domainclasses.Table;
import edu.cmu.lti.huiying.util.TextReader;

public class BagOfPartsFeatureGenerator {
	public LinkedHashMap<String,Integer> featuremap=null;
	public ArrayList<Hashtable<String,Double>> columnFeatVectors=null;
	//private ArrayList<Integer[]> featureVecIndices;//Stores the position of original column of the feature vector, in terms of
													//[i,j,k,l] i_th article, j_th table, k_th group, l_th column.
	//private LinkedHashMap<String, Integer> featureFreq=null;
	public int threshold=3;
	
	public BagOfPartsFeatureGenerator(){
		this.featuremap=new LinkedHashMap<String,Integer>();
		this.columnFeatVectors=new ArrayList<Hashtable<String,Double>>();
		
	}
	public void columns2Features(ArrayList<Column> columns){
		thresholdFeatureSelection(columns,this.threshold);
		for(int i = 0; i < this.columnFeatVectors.size();i++){
			Hashtable<String,Double> colvec=this.columnFeatVectors.get(i);
			Hashtable<String,Double> filtered=new Hashtable<String,Double>();
			Enumeration<String> e = colvec.keys();
			while(e.hasMoreElements()){
				String feat=e.nextElement();
				if(this.featuremap.containsKey(feat)){
					filtered.put(feat, colvec.get(feat));
				}
			}
			this.columnFeatVectors.set(i, filtered);
		}
		
		
//		for(int l = 0; l < columns.size(); l++){
//			Column col=columns.get(l);
//			
//			Hashtable<String, Double> colvec=new Hashtable<String,Double>();//feature frequency table
//			for(Field f:col.content){
//				if(f.text.length()>0){
//					ArrayList<String> featlist=f.toKarmaFeatures();
//					for(String fname:featlist){
//						if(!colvec.containsKey(fname)){
//							colvec.put(fname, 0.0);
//						}
//						colvec.put(fname, colvec.get(fname)+1);
//					}
//				}
//			}
//			this.columnFeatVectors.add(colvec);
//	//							Integer[] ind={i,j,k,l};
//	//							this.featureVecIndices.add(ind);
//		}
					
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
	public void articles2Features(ArrayList<Article> articles){
		ArrayList<Column> columns=this.getColumnsFromArticles(articles);
//		thresholdFeatureSelection(columns, this.threshold);
		columns2Features(columns);
//			if(cnt>=2000)
//				break;
	}
	
	/**
	 * This function collects feature and filter away 
	 * the features with frequencies less than @param thres.
	 */
	public void thresholdFeatureSelection(ArrayList<Column> columns,int thres){
		int cnt=0;
		LinkedHashMap<String,Integer> ffreq=new LinkedHashMap<String,Integer>();

		for(int l = 0; l < columns.size(); l++){
			cnt++;
			Column col=columns.get(l);
			Hashtable<String, Double> colvec=new Hashtable<String,Double>();
			for(Field f:col.content){
				if(f.text.length()>0){
					//ArrayList<String> featlist=f.toKarmaFeatures();
					ArrayList<String> featlist=f.toLexicalPartFeatures();
					for(String fname:featlist){
						if(!ffreq.containsKey(fname)){
							ffreq.put(fname, 0);
						}
						ffreq.put(fname, ffreq.get(fname)+1);
						if(!colvec.containsKey(fname)){
							colvec.put(fname, 0.0);
						}
						colvec.put(fname, colvec.get(fname)+1);
					}
				}
			}
			this.columnFeatVectors.add(colvec);
//							Integer[] ind={i,j,k,l};
//							this.featureVecIndices.add(ind);
		}
					
		for(String feat:ffreq.keySet()){
			if(ffreq.get(feat)>thres){
				this.featuremap.put(feat,this.featuremap.size());
			}
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//ArrayList<Column> cols=TextReader.readColumnFromTsv(args[0]);
		ArrayList<Column> cols=TextReader.readColumnFromTsv("./10types.all");
		BagOfPartsFeatureGenerator bfg=new BagOfPartsFeatureGenerator();
		bfg.columns2Features(cols);
		for(Hashtable<String,Double> vec:bfg.columnFeatVectors){
			double[] v=new double[bfg.featuremap.size()];
			for(String f:vec.keySet()){
				Integer fid=bfg.featuremap.get(f);
				if(fid!=null){
					v[fid]=vec.get(f);
				}
			}
			for(int i = 0; i < v.length-1; i++){
				System.out.print(v[i]+",");
			}
			System.out.print(v[v.length-1]+"\n");
		}
	}

}
