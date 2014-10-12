package edu.cmu.lti.huiying.domainclasses;

import java.util.ArrayList;

import edu.isi.karma.modeling.semantictypes.sl.Lexer;
import edu.isi.karma.modeling.semantictypes.sl.Part;
import edu.isi.karma.modeling.semantictypes.sl.RegexFeatureExtractor;

public class Field {
	public String text;
	
	public Field(String s){
		this.text=s;
	}
	
	public String toString(){
		return text;
	}
	
	public ArrayList<String> toKarmaFeatures(){
		ArrayList<String> features=new ArrayList<String>();
		features.addAll(RegexFeatureExtractor.getFieldFeatures(this.text));
		ArrayList<Part> parts=Lexer.tokenizeField(this.text);
		for(Part p:parts)
			features.addAll(RegexFeatureExtractor.getTokenFeatures(p));
		return features;
	}
	
	public ArrayList<String> toLexicalPartFeatures(){
		ArrayList<String> features=new ArrayList<String>();
		ArrayList<Part> parts=Lexer.tokenizeField(this.text);
		for(Part p:parts){
			if(p.type==1)
				features.add("type_num");
			else
				features.add(p.string);
		}
		return features;
	}
	
}
