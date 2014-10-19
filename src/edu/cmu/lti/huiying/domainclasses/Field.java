package edu.cmu.lti.huiying.domainclasses;

import java.util.ArrayList;

import edu.isi.karma.modeling.semantictypes.sl.Lexer;
import edu.isi.karma.modeling.semantictypes.sl.Part;
import edu.isi.karma.modeling.semantictypes.sl.RegexFeatureExtractor;

public class Field {
	public String text;
	public ArrayList<Part> parts;
	public Field(String s){
		this.text=s;
		if(text.length()>0)
		this.parts=Lexer.tokenizeField(this.text);
		else
			this.parts=new ArrayList<Part>();
	}
	
	
	public String toString(){
		return text;
	}
	
	public ArrayList<String> toKarmaFeatures(){
		ArrayList<String> features=new ArrayList<String>();
		features.addAll(RegexFeatureExtractor.getFieldFeatures(this.text));
		for(Part p:parts)
			features.addAll(RegexFeatureExtractor.getTokenFeatures(p));
		return features;
	}
	
	
	public ArrayList<String> toLexicalPartFeatures(){
		ArrayList<String> features=new ArrayList<String>();
		if(this.text.length()>0){
			ArrayList<Part> parts=Lexer.tokenizeField(this.text);
			for(Part p:parts){
				if(p.type==1)
					features.add("type_num");
				else
					features.add(p.string);
			}
		}
		return features;
	}
	
}
