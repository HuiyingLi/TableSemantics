package edu.cmu.lti.huiying.util;

import java.util.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import edu.cmu.lti.huiying.domainclasses.*;
import edu.cmu.lti.huiying.features.BagOfPartsFeatureGenerator;
import edu.cmu.lti.huiying.features.FormattingFeatureGenerator;
import edu.cmu.lti.huiying.features.NumericFeatureGenerator;

public class OffsetAnnotationGenerator {
	/**
	 * The feature vector specification:
	 * 
	 * 0:number of float type in column
	 * 1:number of integer type in column
	 * 2:number of coordinates in the column
	 * 3:mean value
	 * 4:standard deviation 
	 * 5:value range
	 * 6:min value
	 * 7:max value
	 * 8:voted accuracy (accuracy:#digits after . of flat numbers)
	 * 9:average precision (precision: the value after ±)
	 * 10:average p-value
	 * 11:voted magnitude (number of 10s it can divide)
	 * 
	 * @author huiyingl
	 *
	 */
	public static String numericalFeatureVec2Annotation(Column c, ArrayList<Double> vec){
		String [] fnames={"float_ratio","int_ratio","coord_ratio","mean","std","range","min","max","accuracy","prec","pval","mag"};
		String l="";
		for(int i = 0; i < fnames.length; i++){
			Double d=vec.get(i);
			l+=c.g.table.article.xmlFilePath.substring(c.g.table.article.xmlFilePath.lastIndexOf('/')+1)+"\t";
			//l+="DOC0+\t";
			l+="ATTRIBUTE\t"+Integer.toString(i)+"\t";
			l+=fnames[i]+"\t(";
			for(Field f:c.content){
				l+=f.byteStart+":"+f.byteEnd+",";
			}
			l=l.substring(0,l.length()-1);//peel the last
			l+=")\t";
			l+=d+"\t"+0+"\t";
			l+="none\n";
		}
		return l;
	}
	public static String sparseFeatureVec2Annotation(Column c, LinkedHashMap<String, Double> vec, LinkedHashMap<String,Integer> fmap){
		String l="";
		for(String k:vec.keySet()){
			l+=c.g.table.article.xmlFilePath.substring(c.g.table.article.xmlFilePath.lastIndexOf('/')+1)+"\t";
			l+="ATTRIBUTE\t"+fmap.get(k)+"\t";
			l+=k+"\t(";
			for(Field f:c.content){
				l+=f.byteStart+":"+f.byteEnd+",";
			}
			l=l.substring(0,l.length()-1);
			l+=")\t";
			l+=vec.get(k)+"\t"+0+"\t";
			l+="none\n";
		}
		return l;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws javax.xml.stream.XMLStreamException {
		// TODO Auto-generated method stub
		//NumericFeatureGenerator nfg = new NumericFeatureGenerator();
		//FormattingFeatureGenerator ffg=new FormattingFeatureGenerator();
		BagOfPartsFeatureGenerator bfg=new BagOfPartsFeatureGenerator();
		ArrayList<File> flist= new ArrayList<File>();
		for(String p:args){
			File[] fflist=new File(p).listFiles();
			flist.addAll(new ArrayList<File>(Arrays.asList(fflist)));
		}
		ArrayList<Column> allcols=new ArrayList<Column>();
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("all_NeuroScience.bop.offset"));
			for(File file:flist){
				System.out.println(file.getName());
				XmlStAXReader reader = new XmlStAXReader();
	            Article a = reader.readArticleFromXml(file.getAbsolutePath());
	            for(Table t:a.tables){
	            	for(Group g:t.groups){
	            		allcols.addAll(g.columns);
	            	}
	            }
			}
			List<Column> testl = new ArrayList<Column>();
			//testl = (List<Column>) allcols.subList(0, 1000);
			//ArrayList<ArrayList<Double>> vecs=nfg.columns2Features(allcols);
			bfg.columns2Features(allcols);
			ArrayList<LinkedHashMap<String,Double>> vecs=bfg.columnFeatVectors;
			for(int i= 0; i < allcols.size(); i++){
				//String output=numericalFeatureVec2Annotation(allcols.get(i), vecs.get(i));
				String output=sparseFeatureVec2Annotation(allcols.get(i),vecs.get(i),bfg.featuremap);
				bw.write(output);
				bw.flush();
			}
			bw.close();
			System.out.println(vecs.size());
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
