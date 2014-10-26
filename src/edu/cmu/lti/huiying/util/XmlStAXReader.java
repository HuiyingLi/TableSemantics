package edu.cmu.lti.huiying.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.FileUtils;

import edu.cmu.lti.huiying.domainclasses.*;

public class XmlStAXReader {
	private XMLInputFactory factory=XMLInputFactory.newInstance();
	private Article article=new Article();
	
	private Table t = new Table();
	private Group g = new Group();
	private Header hdr=new Header("");
	private Field field=new Field("");
	private ArrayList<Field> r = new ArrayList<Field>();
	private boolean hdrlist=false;
	private boolean hdrs=false;
    private boolean title = false;
    private boolean author = false;
    private boolean journal = false;
    private boolean issn = false;
    private boolean keyword = false;
    private boolean table = false;
    private boolean caption = false;
    private boolean legend = false;
    private boolean context = false;
    private boolean headings = false;
    private boolean citation = false;
    private boolean sentence = false;
    private boolean group = false;
    private boolean header = false;
    private boolean row = false;
    private boolean value = false;
    private int fstart=-1;
    private int fend=-1;
    private int hstart=-1;
    private int hend=-1;

	public Article readArticleFromXml(String path){
		try {
//			FileInputStream fis = new FileInputStream(path);
//			InputStreamReader isr=new InputStreamReader(fis,"utf-8");
			ByteArrayInputStream bais=new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(path)));
			XMLStreamReader sreader=factory.createXMLStreamReader(bais);
			article.xmlFilePath=path;
			while(sreader.hasNext()){
				sreader.next();
				//int ss=sreader.getLocation().getCharacterOffset();
				if(sreader.getEventType()==XMLStreamReader.START_ELEMENT){
					//int ss1=sreader.getLocation().getCharacterOffset();
					String eName=sreader.getLocalName();
					//int ss2=sreader.getLocation().getCharacterOffset();
					if(eName.equals("author")){
						author=true;
					}else if(eName.equals("article-title")){
						title=true;
					}else if(eName.equals("journal")){
						journal=true;
					}else if(eName.equals("keyword")){
						keyword=true;
					}else if(eName.equals("issn")){
						issn=true;
					}else if(eName.equals("table")){
						table=true;
						t.article=article;
					}else if(eName.equals("caption")){
						caption=true;
					}else if(eName.equals("legend")){
						legend=true;
					}else if(eName.equals("group")){
						group=true;
					}else if(eName.equals("headerlist")){
						g.headerlist=new ArrayList<Header>();
						hdrlist=true;
					}else if(eName.equals("headers")){
						g.headers=new ArrayList<Header>();
						hdrs=true;
					}else if(eName.equals("header")){
						hstart=sreader.getLocation().getCharacterOffset()+1;
						header=true;
					}else if(eName.equals("row")){
						row=true;
					}else if(eName.equals("value")){
						fstart=sreader.getLocation().getCharacterOffset()+1;
						value=true;
					}
				}else if(sreader.getEventType()==XMLStreamReader.CHARACTERS){
					if(author){
						article.authors.add(cleanText(sreader.getText()));
						author=false;
					}else if(title){
						article.title=cleanText(sreader.getText());
						title=false;
					}else if(journal){
						article.journalName=cleanText(sreader.getText());
						journal=false;
					}else if(keyword){
						article.keywords.add(cleanText(sreader.getText()));
						keyword=false;
					}else if(issn){
						article.journalISSN=cleanText(sreader.getText());
						issn=false;
					}else if(caption){
						t.caption=cleanText(sreader.getText());
						caption=false;
					}else if(legend){
						t.legend=cleanText(sreader.getText());
						legend=false;
					}else if(header){
						hdr.text=cleanText(sreader.getText());
						header=false;
					}else if(value){
						field.text=cleanText(sreader.getText());
						field.byteEnd=sreader.getLocation().getCharacterOffset();
						value=false;
					}
				}else if(sreader.getEventType()==XMLStreamReader.END_ELEMENT){
					String eName=sreader.getLocalName();
					if(eName.equals("table")){
						article.tables.add(t);
						t.article=article;
						t=new Table();
						table=false;
					}else if(eName.equals("group")){
						g.table=t;
						g.transpose();
						t.groups.add(g);
						g=new Group();
						group=false;
					}else if(eName.equals("headers")){
						hdrs=false;
					}else if(eName.equals("headerlist")){
						hdrlist=false;
					}else if(eName.equals("header")){
						hend=sreader.getLocation().getCharacterOffset()-8;
						hdr.byteEnd=hend;
						hdr.byteStart=hstart;
						hend=-1;
						hstart=-1;
						if(hdrs){
							g.headers.add(hdr);
						}else if(hdrlist){
							g.headerlist.add(hdr);
						}
						hdr=new Header("");
						header=false;
					}else if(eName.equals("row")){
						g.rows.add(r);
						r=new ArrayList<Field>();
						row=false;
					}else if(eName.equals("value")){
						fend=sreader.getLocation().getCharacterOffset()-7;
						field.byteStart=fstart;
						field.byteEnd=fend;
						r.add(field);
						fstart=-1;
						fend=-1;
						field=new Field("");
						value=false;
					}
				}
//				if(sreader.getEventType()==XMLStreamReader.START_DOCUMENT){
//					this.article=new Article();
//				}else if(sreader.getEventType()==XMLStreamReader.START_ELEMENT){
//					String eName=sreader.getLocalName();
//					if(eName.equals("author")){
//						article.authors.add(sreader.getText().replace("XREF", "").trim());
//					}else if(eName.equals("article-title")){
//						article.title=sreader.getText().replace("XREF", "").trim();
//					}else if(eName.equals("journal")){
//						article.journalName=sreader.getText().replace("XREF", "").trim();
//					}else if(eName.equals("keyword")){
//						article.keywords.add(sreader.getText().replace("XREF", "").trim());
//					}else if(eName.equals("issn")){
//						article.journalISSN=sreader.getText().replace("XREF", "").trim();
//					}else if(eName.equals("table")){
//						t=new Table();
//						t.article=article;
//					}else if(eName.equals("caption")){
//						t.caption=sreader.getText().replace("XREF", "").trim();
//					}else if(eName.equals("legend")){
//						t.legend=sreader.getText().replace("XREF", "").trim();
//					}else if(eName.equals("group")){
//						g=new Group();
//						g.table=t;
//					}else if(eName.equals("headerlist")){
//						g.headerlist=new ArrayList<Header>();
//						hdrlist=true;
//					}else if(eName.equals("headers")){
//						g.headers=new ArrayList<Header>();
//						hdrs=true;
//					}else if(eName.equals("header")){
//						if(hdrlist){
//							g.headerlist.add(new Header(sreader.getText()));
//						}else if(hdrs){
//							g.headers.add(new Header(sreader.getText()));
//						}
//					}else if(eName.equals("row")){
//						r=new ArrayList<Field>();
//					}else if(eName.equals("value")){
//						Field field=new Field(sreader.getText().replace("XREF", "").trim());
//						int s=sreader.getLocation().getCharacterOffset();
//						int e=sreader.getLocation().getCharacterOffset();
//						field.byteStart=s;
//						field.byteEnd=e;
//						r.add(field);
//					}
//				}else if(sreader.getEventType()==XMLStreamReader.END_ELEMENT){
//					String eName=sreader.getLocalName();
//					if(eName.equals("hdrlist")){
//						hdrlist=false;
//					}else if(eName.equals("hdrs")){
//						hdrs=false;
//					}else if(eName.equals("table")){
//						article.tables.add(t);
//					}else if(eName.equals("group")){
//						t.groups.add(g);
//					}
//				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.article;
	}
	
	private String cleanText(String s){
		return s.replaceAll("XREF", "").trim();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//String path= "./data/NeuroScience_explode/030645229600190X.xml";
        String dir = "./data/BrainResearch_explode/";
        File[] files = new File(dir).listFiles();
        int cnt = 0;
        int totalTable = 0;
        int totalGroup = 0;
        int totalUnequal = 0;
        int legit = 0;
        for (File f : files) {
                XmlStAXReader reader = new XmlStAXReader();
                Article a = reader.readArticleFromXml(f.getAbsolutePath());
                for (Table t : a.tables) {
                        totalTable++;
                        if (t.check()) {
                                legit++;
                        }
                }
                // Article a=reader.article;
        }

	}

}
