package edu.cmu.lti.huiying.semantictypes;

import java.io.BufferedWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.DensityBasedSpatialClustering;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.tools.data.FileHandler;

import edu.cmu.lti.huiying.domainclasses.Article;
import edu.cmu.lti.huiying.domainclasses.Field;
import edu.cmu.lti.huiying.domainclasses.Group;
import edu.cmu.lti.huiying.domainclasses.Table;
import edu.cmu.lti.huiying.util.XmlSAXReader;

import weka.clusterers.HierarchicalClusterer;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.FastVector;
import weka.core.Attribute;

public class ColumnHierarchicalClusterer {
	private HierarchicalClusterer hc=null;
	//private Instances columnFeatVecs=null;
	public ArrayList<Article> articles=null;
	//private Hashtable<String, Integer> featuremap=null;
	private LinkedHashMap<String,Integer> featuremap=null;
	private ArrayList<Hashtable<String,Double>> columnFeatVectors;
	private ArrayList<Integer[]> featureVecIndices;//Stores the position of original column of the feature vector, in terms of
													//[i,j,k,l] i_th article, j_th table, k_th group, l_th column.
	private LinkedHashMap<String, Integer> featureFreq=null;
	private Instances dataset=null;
	
	
	public ColumnHierarchicalClusterer(){
		hc= new HierarchicalClusterer();
		featuremap=new LinkedHashMap<String,Integer>();
		columnFeatVectors=new ArrayList<Hashtable<String,Double>>();
		featureVecIndices=new ArrayList<Integer[]>();
	}
	
	public void loadData(String[] dirs){
		XmlSAXReader reader = new XmlSAXReader();
		this.articles=new ArrayList<Article>();
		for(String dir:dirs)
		{
			this.articles.addAll(reader.loadArticleFromDirectory(dir));
		}
	}
	/**
	 * This function collects feature and filter away 
	 * the features with frequencies less than @param thres.
	 */
	public void thresholdFeatureSelection(int thres){
		int cnt=0;
		LinkedHashMap<String,Integer> ffreq=new LinkedHashMap<String,Integer>();
		for(int i = 0; i < this.articles.size(); i++){
			Article a = this.articles.get(i);
			for(int j = 0; j < a.tables.size();j++){
				Table t = a.tables.get(j);
				if(t.check()){
					for(int k = 0; k < t.groups.size(); k++){
						Group g = t.groups.get(k);
						for(int l = 0; l < g.columns.size(); l++){
							cnt++;
							ArrayList<Field> col=g.columns.get(l).content;
							Hashtable<String, Double> colvec=new Hashtable<String,Double>();
							for(Field f:col){
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
							Integer[] ind={i,j,k,l};
							this.featureVecIndices.add(ind);
						}
					}
				}
			}
		}
		for(String feat:ffreq.keySet()){
			if(ffreq.get(feat)>thres){
				this.featuremap.put(feat,this.featuremap.size());
			}
		}
	}
	public void columns2Features(){
		thresholdFeatureSelection(5);
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
		
		for(int i = 0; i < this.articles.size(); i++){
			Article a = this.articles.get(i);
			for(int j = 0; j < a.tables.size();j++){
				Table t = a.tables.get(j);
				if(t.check()){
//					cnt++;
//					if(cnt%1000==0)
						//System.err.println("processed "+cnt);
					
					for(int k = 0; k < t.groups.size(); k++){
						Group g = t.groups.get(k);
						for(int l = 0; l < g.columns.size(); l++){
							ArrayList<Field> col=g.columns.get(l).content;
							
							Hashtable<String, Double> colvec=new Hashtable<String,Double>();//feature frequency table
							for(Field f:col){
								if(f.text.length()>0){
									ArrayList<String> featlist=f.toKarmaFeatures();
									for(String fname:featlist){
										if(!colvec.containsKey(fname)){
											colvec.put(fname, 0.0);
										}
										colvec.put(fname, colvec.get(fname)+1);
									}
								}
							}
							this.columnFeatVectors.add(colvec);
							Integer[] ind={i,j,k,l};
							this.featureVecIndices.add(ind);
						}
					}
				}
			}
//			if(cnt>=2000)
//				break;
		}
	}
	/**
	 * This function saves the feature vectors to files with specified formats.
	 * The formats are: sparse (default), tsv, and arff.
	 * This method will create another file suffixed ".coltxt" to store the columns in original data
	 * @param filepath
	 * @param fileformat
	 */
	public void save2File(String filepath, String format){
		System.out.println("start writing to file...");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filepath+"."+format));
			int ccnt=0;
			for(Hashtable<String,Double> colvec:this.columnFeatVectors){
				ccnt++;
				if(format.equals("sparse")){
					String line="0\t";
					Enumeration<String> e = colvec.keys();
					while(e.hasMoreElements()){
						String k=e.nextElement();
						try{
						line+=Integer.toString(featuremap.get(k))+":"+Double.toString(colvec.get(k))+"\t";
						}catch(NullPointerException exc){
							System.out.println(k);
							System.out.println(featuremap.containsKey(k));
						}
						
					}
					bw.write(line.trim()+"\n");
				}
				else if(format.equals("csv")){
					if(ccnt%1000==0)
						System.out.println(ccnt);
					String line="";
					int cnt=0;
					for(String feat:this.featuremap.keySet()){
						if(colvec.containsKey(feat)){
							if (cnt==0)
								line+=Double.toString(colvec.get(feat));
							else
								line+=","+Double.toString(colvec.get(feat));
						}
						else
						{
							if(cnt==0)
								line+="0";
							else
								line+=",0";
						}
						cnt++;
					}
					bw.write(line.trim()+"\n");
					bw.flush();
				}

			}
			bw.close();
			
//			BufferedWriter bw2=new BufferedWriter(new FileWriter(filepath+".coltxt"));
//			for(Integer[] indx:this.featureVecIndices){
//				String line="";
//				ArrayList<Field> col=this.articles.get(indx[0]).tables.get(indx[1]).groups.get(indx[2]).columns.get(indx[3]);
//				for(Field f:col){
//					if(f.text.length()>0)
//						line+=f.text+"\t";
//				}
//				bw2.write(line.trim()+"\n");
//			}
//			bw2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		FastVector atts=new FastVector();
//		Set<String> keys=featuremap.keySet();
//		for(String key:keys){
//			atts.addElement(new Attribute(key));
//		}
//		this.dataset=new Instances("columns",atts,0);
//		for(Hashtable<String, Double> colvec:this.columnFeatVectors){
//			double[] vec= new double[featuremap.size()];
//			Enumeration<String> ee=colvec.keys();
//			while(ee.hasMoreElements()){
//				String fname=ee.nextElement();
//				vec[featuremap.get(fname)]=colvec.get(fname);
//			}
		//Storing then will consume too much memory!
		//dataset.add(new Instance(1.0, vec));
	}
	
	public Dataset[] DBSCANClustering(String datafile){
		Dataset data=null;
		try {
			data = FileHandler.loadSparseDataset(new File(datafile), 0,"\t", ":");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Clusterer cl= new DensityBasedSpatialClustering(0.1,1);
		Dataset[] clusters=cl.cluster(data);
		
		return clusters;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//String[] datadirs={"./data/NeuroScience_explode/","./data/BrainResearch_explode/"};
		//String[] datadirs={"../NeuroScience_explode/","../BrainResearch_explode/"};
		//ColumnHierarchicalClusterer chc=new ColumnHierarchicalClusterer();
		//chc.loadData(datadirs);
		//chc.columns2Features();
		//chc.save2File("cluster_plexall","csv");
		//chc.DBSCANClustering("./data/cluster2000.sparse");
		System.out.println("finished");
	}

}
