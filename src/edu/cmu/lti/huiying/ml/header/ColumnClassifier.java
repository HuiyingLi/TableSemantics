package edu.cmu.lti.huiying.ml.header;
import java.util.*;

import edu.cmu.lti.huiying.util.Exepriment;
/**
 * This class takes in an arraylist of feature vectors.
 * 
 * @author huiyingl
 *
 */
public class ColumnClassifier {

	public ArrayList<ArrayList<LinkedHashMap<String, Double>>> knownSamples;
	public ArrayList<ArrayList<LinkedHashMap<String, Double>>> train;
	public ArrayList<ArrayList<LinkedHashMap<String, Double>>> test;
	public void prepareDataset(){
		
	}
	public ColumnClassifier(ArrayList<ArrayList<LinkedHashMap<String, Double>>> data){
		this.knownSamples=data;
		ArrayList[] split= Exepriment.splitData(data, 0.8);
		train=(ArrayList<ArrayList<LinkedHashMap<String, Double>>>)split[0];
		test=(ArrayList<ArrayList<LinkedHashMap<String, Double>>>)split[0];
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
