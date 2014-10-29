package edu.cmu.lti.huiying.features;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import edu.cmu.lti.huiying.domainclasses.Column;
import edu.cmu.lti.huiying.domainclasses.Field;
import edu.cmu.lti.huiying.util.TextReader;

public class FormattingFeatureGenerator {

	public LinkedHashMap<String,Integer> featuremap=null;
	public ArrayList<LinkedHashMap<String,Double>> columnFeatVectors=null;
	public int thres=3;
	public FormattingFeatureGenerator(){
		this.featuremap=new LinkedHashMap<String,Integer>();
		this.columnFeatVectors=new ArrayList<LinkedHashMap<String,Double>>();
	}
	public void columns2Features(ArrayList<Column> columns){
		thresholdFeatureSelection(columns, this.thres);
		for(int i = 0; i < this.columnFeatVectors.size(); i++){
			LinkedHashMap<String,Double> vec=this.columnFeatVectors.get(i);
			LinkedHashMap<String,Double> filtered=new LinkedHashMap<String,Double>();
			for(String k:vec.keySet()){
				if(this.featuremap.containsKey(k)){
					filtered.put(k, vec.get(k));
				}
			}
			this.columnFeatVectors.set(i, filtered);
		}
	}
	private void thresholdFeatureSelection(ArrayList<Column> columns,int thres){
		Hashtable<String, Integer> ffreq=new Hashtable<String,Integer>();
		for(Column c:columns){
			LinkedHashMap<String,Double> colvec=new LinkedHashMap<String,Double>();
			for(Field f:c.content){
				ArrayList<String> karmafeat=f.toKarmaFeatures();
				for(String fname:karmafeat){
					if(!ffreq.containsKey(fname)){
						ffreq.put(fname, 1);
					}
					else{
						ffreq.put(fname, ffreq.get(fname)+1);
					}
					
					if(!colvec.containsKey(fname)){
						colvec.put(fname, 1.0);
					}else{
						colvec.put(fname, colvec.get(fname)+1);
					}
					
				}
			}
			this.columnFeatVectors.add(colvec);
		}
		for(String f:ffreq.keySet()){
			if(ffreq.get(f)>thres){
				this.featuremap.put(f, this.featuremap.size());
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<Column> cols=TextReader.readColumnFromTsv("10types.all");
		//ArrayList<Column> cols=TextReader.readColumnFromTsv(args[0]);
		FormattingFeatureGenerator ffg=new FormattingFeatureGenerator();
		ffg.columns2Features(cols);
		for(LinkedHashMap<String,Double> vec:ffg.columnFeatVectors){
			double[] v=new double[ffg.featuremap.size()];
			for(String f:vec.keySet()){
				v[ffg.featuremap.get(f)]=vec.get(f);
			}
			for(int i =0; i < v.length-1;i++){
				System.out.print(v[i]+",");
			}
			System.out.print(v[v.length-1]+"\n");
		}
	}

}
