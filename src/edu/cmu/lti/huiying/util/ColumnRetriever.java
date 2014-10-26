package edu.cmu.lti.huiying.util;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import edu.cmu.lti.huiying.domainclasses.Article;
import edu.cmu.lti.huiying.domainclasses.Field;
import edu.cmu.lti.huiying.domainclasses.Table;
import edu.cmu.lti.huiying.domainclasses.Group;
import edu.cmu.lti.huiying.domainclasses.Header;
public class ColumnRetriever {

	public ArrayList<Article> corpus = null;
	
	public ColumnRetriever(ArrayList<Article> data){
		this.corpus=data;
	}
	public LinkedHashMap<String, ArrayList<ArrayList<Field>>> retrieveByHeaders(String[] headers){
		LinkedHashMap<String, ArrayList<ArrayList<Field>>> results=new LinkedHashMap<String, ArrayList<ArrayList<Field>>>();
		for(String q:headers){
			if(!results.containsKey(q)){
				results.put(q, new ArrayList<ArrayList<Field>>());
			}
		}
		for(Article a:corpus){
			for(Table t:a.tables){
				if(t.check()){
					for(Group g:t.groups){
						for(int i = 0; i < g.headers.size(); i++){
							Header hdr=g.headers.get(i);
							String text=hdr.text.toLowerCase();
							if(results.containsKey(text)){
								results.get(text).add(g.columns.get(i).content);
							}
						}
					}
				}
			}
		}
		return results;
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String[] dirs={"./data/NeuroScience_explode","./data/BrainResearch_explode"};
		XmlSAXReader reader = new XmlSAXReader();
		ArrayList<Article> articles=new ArrayList<Article>();
		for(String dir:dirs)
		{
			articles.addAll(reader.loadArticleFromDirectory(dir));
		}
		
//		Hashtable<String, Integer> headerfreq=new Hashtable<String, Integer>();
//		int totalfreq=0;
//		for(Article a:articles){
//			for(Table t:a.tables){
//				if(t.check()){
//					for(Group g:t.groups){
//						for(int i = 0; i < g.headers.size(); i++){
//							Header hdr=g.headers.get(i);
//							String text=hdr.text.toLowerCase();
//							if(!headerfreq.containsKey(text)){
//								headerfreq.put(text,0);
//							}
//							headerfreq.put(text, headerfreq.get(text)+1);
//							totalfreq+=1;
//						}
//					}
//				}
//			}
//		}
//		
//		System.out.println(totalfreq);
//		//Transfer as List and sort it
//        ArrayList<Map.Entry<String, Integer>> l = new ArrayList(headerfreq.entrySet());
//        Collections.sort(l, new Comparator<Map.Entry<String, Integer>>(){
//
//        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
//            return -1*o1.getValue().compareTo(o2.getValue());
//        }});
//        BufferedWriter bw = new BufferedWriter(new FileWriter("headerfreq.txt"));
//        for(Map.Entry<String,Integer> e:l){
//        	bw.write(e.getKey()+"\t"+e.getValue()+"\n");
//        }
//        bw.close();
//              
		String q="";
		for(String a:args)
			q+=a+" ";
		q=q.trim();
		System.err.println(q);
		q="age";
		String[] queries={q};
		ColumnRetriever cr = new ColumnRetriever(articles);
		LinkedHashMap<String, ArrayList<ArrayList<Field>>> cols=cr.retrieveByHeaders(queries);
		for(String k:cols.keySet()){
			ArrayList<ArrayList<Field>> cset=cols.get(k);
			for(ArrayList<Field>c:cset){
				String s="";
				for(Field f:c){
					if(f.text.contains("7.4"))
							System.out.println();
					s+=f.text.replace("\t", " ").trim()+"\t";
				}
				System.out.println(s);
			}
		}
		
		try{
			FileOutputStream fout= new FileOutputStream("");
			ObjectOutputStream out = new ObjectOutputStream(fout);
			out.writeObject(cols);
		}catch(IOException e){
			
		}
	}

}
