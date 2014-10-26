package edu.cmu.lti.huiying.domainclasses;

public class Header {
	public String text;
	public int byteStart;
	public int byteEnd;
	public Header(String s){
		this.text=s;
	}
	public String toString(){
		return text;
	}

}
