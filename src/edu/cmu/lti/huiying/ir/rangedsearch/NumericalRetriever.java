package edu.cmu.lti.huiying.ir.rangedsearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class NumericalRetriever {

	IndexSearcher searcher;
	IndexReader reader;
	int hitsPerPage = 10;
	int singlemax=500;
	int repeat = 0;
	boolean raw = false;

	public NumericalRetriever(){
		try {
			reader = DirectoryReader.open(FSDirectory.open(new File("index")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.searcher = new IndexSearcher(reader);
		
	}
	public void search(String q) throws ParseException, IOException {
		//System.out.println(q);
		String[] spl=q.split("###");
//		if(spl.length%3!=0){
//			System.out.println("WRONG");
//			System.exit(1);
//		}
		ArrayList<Query> qlist=new ArrayList<Query>();
		for(int i = 0; i < spl.length; i++){
			String[] ss=spl[i].split("\t");
			Double low=Double.NEGATIVE_INFINITY;
			Double up=Double.MAX_VALUE;
			if(ss.length>=2&&ss[1].length()>0)
				low=new Double(ss[1]);
			if(ss.length>=3&&ss[2].length()>0)
				up=new Double(ss[2]);
			if(ss.length>1){
				Query query = NumericRangeQuery.newDoubleRange(ss[0],low, up, true, true);
				qlist.add(query);
			}
		}
		
		//Query query2=NumericRangeQuery.newDoubleRange(spl[3], new Double(spl[4]), new Double(spl[5]), true, true);
		BooleanQuery combined=new BooleanQuery();
		for(Query qry:qlist){
			combined.add(qry, Occur.MUST);
		}
		System.out.println("Searching for:" + combined.toString()+"\t");
		

//			if (repeat > 0) { // repeat & time as benchmark
//				Date start = new Date();
//				for (int i = 0; i < repeat; i++) {
//					searcher.search(query, null, 100);
//				}
//				Date end = new Date();
//				System.out.println("Time: " + (end.getTime() - start.getTime())
//						+ "ms");
//			}
//
//			doPagingSearch(in, searcher, query, hitsPerPage, raw,
//					queries == null && queryString == null);
			
	//	}
		TopDocs results=searcher.search(combined, singlemax);
		ScoreDoc[] hits=results.scoreDocs;
		System.out.println(results.totalHits + " total matching documents\t");
		int total = Math.min(singlemax, results.totalHits);
		for(int i = 0; i<total; i++){
			Document doc=searcher.doc(hits[i].doc);
			if(doc.getField("type").stringValue().equals("cell")){
				System.out.println(doc.getField("text").stringValue()+" "+ doc.get("filename")+"\t");
			}
			if(doc.getField("type").stringValue().equals("column")){
				System.out.println(doc.getField("filename").stringValue()+"\t");
			}
		}
	}

	public  void doPagingSearch(BufferedReader in,
			IndexSearcher searcher, Query query, int hitsPerPage, boolean raw,
			boolean interactive) throws IOException {
		// Collect enough docs to show 5 pages
		TopDocs results = searcher.search(query, singlemax);
		ScoreDoc[] hits = results.scoreDocs;

		
		int numTotalHits = results.totalHits;
		System.err.println(numTotalHits + " total matching documents");

		int start = 0;
		int end = Math.min(numTotalHits, hitsPerPage);

		while (true) {
			if (end > hits.length) {
				System.out
						.println("Only results 1 - " + hits.length + " of "
								+ numTotalHits
								+ " total matching documents collected.");
				System.out.println("Collect more (y/n) ?");
				String line = in.readLine();
				if (line.length() == 0 || line.charAt(0) == 'n') {
					break;
				}

				hits = searcher.search(query, numTotalHits).scoreDocs;
			}

			end = Math.min(hits.length, start + hitsPerPage);

			for (int i = start; i < end; i++) {
				if (raw) { // output raw format
					System.out.println("doc=" + hits[i].doc + " score="
							+ hits[i].score);
					continue;
				}

				Document doc = searcher.doc(hits[i].doc);
				String mean = doc.getField("value").stringValue();
				if (mean != null) {
					System.out.println((i + 1) + ". " + mean);
					String filename = doc.get("filename");
					if (filename != null) {
						System.out.println("   filename: " + doc.get("filename"));
					}
				} else {
					System.out.println((i + 1) + ". "
							+ "No mean for this document");
				}

			}

			if (!interactive || end == 0) {
				break;
			}

			if (numTotalHits >= end) {
				boolean quit = false;
				while (true) {
					System.out.print("Press ");
					if (start - hitsPerPage >= 0) {
						System.out.print("(p)revious page, ");
					}
					if (start + hitsPerPage < numTotalHits) {
						System.out.print("(n)ext page, ");
					}
					System.out
							.println("(q)uit or enter number to jump to a page.");

					String line = in.readLine();
					if (line.length() == 0 || line.charAt(0) == 'q') {
						quit = true;
						break;
					}
					if (line.charAt(0) == 'p') {
						start = Math.max(0, start - hitsPerPage);
						break;
					} else if (line.charAt(0) == 'n') {
						if (start + hitsPerPage < numTotalHits) {
							start += hitsPerPage;
						}
						break;
					} else {
						int page = Integer.parseInt(line);
						if ((page - 1) * hitsPerPage < numTotalHits) {
							start = (page - 1) * hitsPerPage;
							break;
						} else {
							System.out.println("No such page");
						}
					}
				}
				if (quit)
					break;
				end = Math.min(numTotalHits, start + hitsPerPage);
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException, IOException {
		// TODO Auto-generated method stub
		NumericalRetriever nr=new NumericalRetriever();
		String in="";
		String input=null;
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			in+=br.readLine();
//			while((input=br.readLine())!=null){
//				in+=input;
//			}
		}catch(IOException e){
			e.printStackTrace();
		}
		//System.out.println(in);
		nr.search(in);
	}

}
