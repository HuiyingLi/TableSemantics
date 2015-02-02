package edu.cmu.lti.huiying.features;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import edu.cmu.lti.huiying.domainclasses.Field;

public class Generator {
	BagOfPartsFeatureGenerator bfg=new BagOfPartsFeatureGenerator();
	FormattingFeatureGenerator ffg=new FormattingFeatureGenerator();
	NumericFeatureGenerator nfg=new NumericFeatureGenerator();
	
	/**
	 * The feature vector specification:
	 * 
	 * 0:number of integer type in column
	 * 1:number of float type in column
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
	public Hashtable<String, String> column2Vector(List<String> column){
		ArrayList<Field> fields= new ArrayList<Field>();
		for(String s:column){
			Field f = new Field(s);
			fields.add(f);
		}
		
//		Hashtable<String,String> result=new Hashtable<String,String>();
		Hashtable<String,String> nvect=new Hashtable<String,String>();
		//if(){
			ArrayList<Double> nvec=nfg.getFeatureVector(fields);
			nvect.put("int_ratio", nvec.get(0).toString());
			nvect.put("float_ratio", nvec.get(1).toString());
			nvect.put("coor_ratio", nvec.get(2).toString());
			nvect.put("mean", nvec.get(3).toString());
			nvect.put("std", nvec.get(4).toString());
			nvect.put("range", nvec.get(5).toString());
			nvect.put("min", nvec.get(6).toString());
			nvect.put("max", nvec.get(7).toString());
			nvect.put("accuracy", nvec.get(8).toString());
			nvect.put("prec", nvec.get(9).toString());
			nvect.put("pvalue", nvec.get(10).toString());
			nvect.put("mag", nvec.get(11).toString());
//			result.putAll(nvect);
		//}
//		if(featureset.indexOf("b")!=-1){
//			Hashtable<String,String> bvec=bfg.singleColumn2Features(fields);
//			result.putAll(bvec);
//		}
//		if(featureset.indexOf("f")!=-1){
//			Hashtable<String,String> fvec=ffg.singleColumn2Features(fields);
//			result.putAll(fvec);
//		}
		return nvect;
	}
	
	public Hashtable<String, String> cell2Vector(String cell){
		Field f = new Field(cell);
		//Hashtable<String,String> result=new Hashtable<String,String>();
		Hashtable<String,String> nvect=new Hashtable<String,String>();
		//if(){
			ArrayList<Double> nvec=nfg.field2Features(f);
			nvect.put("type", nvec.get(0).toString());
			nvect.put("mainValue",nvec.get(1).toString());
			nvect.put("precision", nvec.get(2).toString());
			nvect.put("accuracy", nvec.get(3).toString());
			nvect.put("pvalue", nvec.get(4).toString());
			nvect.put("magnitude", nvec.get(5).toString());
			return nvect;
	}
			//result.putAll(nvect);
		//}
//		if(featureset.indexOf("b")!=-1){
//			Hashtable<String,String> bvec=bfg.singleColumn2Features(fields);
//			result.putAll(bvec);
//		}
//		if(featureset.indexOf("f")!=-1){
//			Hashtable<String,String> fvec=ffg.singleColumn2Features(fields);
//			result.putAll(fvec);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Generator gen=new Generator();
		List<String> col=new ArrayList<String>();
		col.add("1.1067×10-5");
		col.add("2.2×10-5 (p=0.001)");
		//col.add("4.4 (p=0.005)");
		//col.add("2.5 (p=0.01)");
		Hashtable<String,String> resut=gen.column2Vector(col);

	}

}
