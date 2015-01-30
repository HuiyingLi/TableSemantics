package edu.cmu.lti.huiying.ir.rangedsearch;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.cmu.lti.huiying.domainclasses.Article;
import edu.cmu.lti.huiying.domainclasses.Column;
import edu.cmu.lti.huiying.domainclasses.Group;
import edu.cmu.lti.huiying.domainclasses.Header;
import edu.cmu.lti.huiying.domainclasses.Numerical;
import edu.cmu.lti.huiying.domainclasses.Table;
import edu.cmu.lti.huiying.features.NumericFeatureGenerator;
import edu.cmu.lti.huiying.util.XmlStAXReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

/**
 * Index all text files under a directory.
 * <p>
 * This is a command-line application demonstrating simple Lucene indexing. Run
 * it with no command-line arguments for usage information.
 */
public class TableIndexer {
	public int totalDocAdded = 0;

	private TableIndexer() {
	}

	private XmlStAXReader xmlreader = null;

	/** Index all text files under a directory. */
	public static void main(String[] args) {
		String usage = "java org.apache.lucene.demo.IndexFiles"
				+ " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
				+ "This indexes the documents in DOCS_PATH, creating a Lucene index"
				+ "in INDEX_PATH that can be searched with SearchFiles";
		String indexPath = "index";
		String docsPath = "/home/huiying/JavaWorkspace/TableSemantics/web/numericalDemo/public/data";
		boolean create = true;
		int nfile = 0;
		// for(int i=0;i<args.length;i++) {
		// if ("-index".equals(args[i])) {
		// indexPath = args[i+1];
		// i++;
		// } else if ("-docs".equals(args[i])) {
		// docsPath = args[i+1];
		// i++;
		// } else if ("-update".equals(args[i])) {
		// create = false;
		// }
		// }

		if (docsPath == null) {
			System.err.println("Usage: " + usage);
			System.exit(1);
		}

		final File docDir = new File(docsPath);
		if (!docDir.exists() || !docDir.canRead()) {
			System.out
					.println("Document directory '"
							+ docDir.getAbsolutePath()
							+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		Date start = new Date();
		TableIndexer tind = new TableIndexer();
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");

			Directory dir = FSDirectory.open(new File(indexPath));
			// :Post-Release-Update-Version.LUCENE_XY:
			//Analyzer analyzer = new TableAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(
					Version.LUCENE_4_10_0, new StandardAnalyzer());

			if (create) {
				// Create a new index in the directory, removing any
				// previously indexed documents:
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				// Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}

			IndexWriter writer = new IndexWriter(dir, iwc);
			// tind.indexOffsetAnnotation(writer, docDir);
			tind.indexExplodedXml(writer, docDir);
			writer.close();

			Date end = new Date();
			System.out.println("total doc added:" + tind.totalDocAdded);
			System.out.println(end.getTime() - start.getTime()
					+ " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass()
					+ "\n with message: " + e.getMessage());
		}
	}

	public void indexExplodedXml(IndexWriter writer, File file)
			throws IOException {
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						indexExplodedXml(writer, new File(file, files[i]));
					}
				}
			} else {
				FileInputStream fis = new FileInputStream(file);
				try {
					NumericFeatureGenerator nfg = new NumericFeatureGenerator();
					if (this.xmlreader == null) {
						this.xmlreader = new XmlStAXReader();
					}
					Article a = xmlreader.readArticleFromXml(file
							.getAbsolutePath());
					for (Table t : a.tables) {
						for (Group g : t.groups) {
							for (Column col : g.columns) {
								// index columns
								Document coldoc=new Document();
								ArrayList<Double> cfv=nfg.getFeatureVector(col.content);
								if(cfv.get(0)!=null){
									DoubleField intratio=new DoubleField("intratio", cfv.get(0), Field.Store.NO);
									coldoc.add(intratio);
								}
								if(cfv.get(1)!=null){
									DoubleField floatratio=new DoubleField("floatratio", cfv.get(1), Field.Store.NO);
									coldoc.add(floatratio);
								}
								if(cfv.get(3)!=null){
									DoubleField mean=new DoubleField("mean", cfv.get(3),Field.Store.NO);
									coldoc.add(mean);
								}
								if(cfv.get(4)!=null){
									DoubleField std=new DoubleField("std",cfv.get(4), Field.Store.NO);
									coldoc.add(std);
								}
								if(cfv.get(6)!=null){
									DoubleField min = new DoubleField("min", cfv.get(6),Field.Store.NO);
									coldoc.add(min);
								}
								if(cfv.get(7)!=null){
									DoubleField max = new DoubleField("max", cfv.get(7), Field.Store.NO);
									coldoc.add(max);
								}
								if(cfv.get(8)!=null){
									DoubleField acc=new DoubleField("acc", cfv.get(8),Field.Store.NO);
									coldoc.add(acc);
								}
								if(cfv.get(11)!=null){
									DoubleField colmag=new DoubleField("colmag", cfv.get(11),Field.Store.NO);
									coldoc.add(colmag);
								}
								
								StringField wholegroup=new StringField("wholegroup", g.toString(), Field.Store.YES);
								if(wholegroup.stringValue().getBytes().length>32760)
								{
									wholegroup.setStringValue("Table too large...");
									System.err.println("table too large:"+wholegroup.stringValue().getBytes().length);
									
								}
								String headers="";
								if(col.headers!=null){
									for(Header hdr:col.headers){
										headers+=hdr.text.toLowerCase()+" ";
									}
								}
								TextField header=new TextField("headerkeywords", headers.trim(),Field.Store.NO);
								coldoc.add(header);
								coldoc.add(wholegroup);
								StringField fname=new StringField("filename", file.getAbsolutePath(), Field.Store.YES);
								coldoc.add(fname);
								StringField type=new StringField("type", "column", Field.Store.YES);
								coldoc.add(type);
								IntField bstart=new IntField("bytestart", col.content.get(0).byteStart, Field.Store.YES);
								IntField bend=new IntField("byteend", col.content.get(col.content.size()-1).byteEnd, Field.Store.YES);
								String content="";
								for(edu.cmu.lti.huiying.domainclasses.Field f:col.content)
									content+=f.text+"|";
								StringField colcontent=new StringField("colcontent",content.substring(0, content.length()-1), Field.Store.YES);
								coldoc.add(colcontent);
								coldoc.add(bstart);
								coldoc.add(bend);
								if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
									writer.addDocument(coldoc);
									totalDocAdded++;
								} else {
									writer.updateDocument(new Term(
											"path", file.getPath()),
											coldoc);
								}
								for (edu.cmu.lti.huiying.domainclasses.Field f : col.content) {
									// Index single cell
									Document celldoc = new Document();
									ArrayList<Double> fv = nfg
											.field2Features(f);
									if (fv.get(0) == 1 || fv.get(0) == 2) {
										try {
											DoubleField df = new DoubleField(
													"value", fv.get(1),
													Field.Store.YES);
											celldoc.add(df);
											StringField textf = new StringField(
													"text", f.text,
													Field.Store.YES);
											celldoc.add(textf);
											if(fv.get(2)!=null & fv.get(2)!=Double.NaN){
												DoubleField errf=new DoubleField("error", fv.get(2),Field.Store.NO);
												celldoc.add(errf);
											}
											if(fv.get(5)!=Double.NaN){
												DoubleField magf=new DoubleField("cellmag", fv.get(5),Field.Store.NO);
												celldoc.add(magf);
											}
											if(fv.get(4)!=null){
												DoubleField pvalue=new DoubleField("cellpvalue",fv.get(4), Field.Store.NO);
												celldoc.add(pvalue);
											}
											StringField sf = new StringField(
													"filename",
													file.getAbsolutePath(),
													Field.Store.YES);
											celldoc.add(sf);
											
											StringField ctype=new StringField("type", "cell", Field.Store.YES);
											celldoc.add(ctype);
											//StringField cwholegroup=new StringField("wholegroup", g.toString(), Field.Store.YES);
											//celldoc.add(cwholegroup);
											IntField cbstart=new IntField("bytestart", f.byteStart, Field.Store.YES);
											IntField cbend=new IntField("byteend", f.byteEnd, Field.Store.YES);
											celldoc.add(cbstart);
											celldoc.add(cbend);
										} catch (NullPointerException e) {
											e.printStackTrace();
											System.out.println(f.text);
										}
										if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
											writer.addDocument(celldoc);
											totalDocAdded++;
										} else {
											writer.updateDocument(new Term(
													"path", file.getPath()),
													celldoc);
										}
									}
								}
							}
						}
					}

				} finally {
					fis.close();
				}
			}
		}
	}

	public void indexOffsetAnnotation(IndexWriter writer, File file)
			throws IOException {
		// do not try to index files that cannot be read
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				// an IO error could occur
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						if (files[i].equals("NeuroScience.num.offset"))
							indexOffsetAnnotation(writer, new File(file,
									files[i]));
					}
				}
			} else {
				FileInputStream fis;
				try {
					fis = new FileInputStream(file);
				} catch (FileNotFoundException fnfe) {
					return;
				}

				try {

					// make a new, empty document
					Document doc = new Document();
					BufferedReader br = new BufferedReader(
							new InputStreamReader(fis, StandardCharsets.UTF_8));
					String line = null;
					String filename = null;
					while ((line = br.readLine()) != null) {
						if (line.trim().length() == 0) {
							doc.add((new StringField("filename", filename,
									Field.Store.YES)));
							if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
								writer.addDocument(doc);
							} else {
								System.out.println("updating " + file);
								writer.updateDocument(
										new Term("path", file.getPath()), doc);
							}
							doc = new Document();
							filename = null;
							continue;
						}
						String[] spl = line.split("\t");
						doc.add(new DoubleField(spl[3], Double
								.parseDouble(spl[5]), Field.Store.YES));
						if (filename == null)
							filename = spl[0];
					}
					br.close();
				} finally {
					fis.close();
				}
			}
		}
	}
}
