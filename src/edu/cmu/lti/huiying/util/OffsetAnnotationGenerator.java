package edu.cmu.lti.huiying.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import edu.cmu.lti.huiying.domainclasses.*;
import edu.cmu.lti.huiying.features.BagOfPartsFeatureGenerator;
import edu.cmu.lti.huiying.features.FormattingFeatureGenerator;
import edu.cmu.lti.huiying.features.NumericFeatureGenerator;

public class OffsetAnnotationGenerator {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		NumericFeatureGenerator nfg = new NumericFeatureGenerator();
		FormattingFeatureGenerator ffg=new FormattingFeatureGenerator();
		BagOfPartsFeatureGenerator bfg=new BagOfPartsFeatureGenerator();
		ArrayList<File> flist= new ArrayList<File>();
		for(String p:args){
			flist.addAll(new ArrayList<File>(Arrays.asList(new File(args[0]).listFiles())));
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(args[0]+".offset"));
			for(File file:flist){
				XmlStAXReader reader = new XmlStAXReader();
	            Article a = reader.readArticleFromXml(file.getAbsolutePath());
	            for(Table t:a.tables){
	            	
	            }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
