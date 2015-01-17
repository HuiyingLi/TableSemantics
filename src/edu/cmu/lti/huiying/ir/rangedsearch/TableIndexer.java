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
import edu.cmu.lti.huiying.domainclasses.Table;
import edu.cmu.lti.huiying.util.XmlStAXReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Index all text files under a directory.
 * <p>
 * This is a command-line application demonstrating simple Lucene indexing. Run
 * it with no command-line arguments for usage information.
 */
public class TableIndexer {

	private TableIndexer() {
	}
	private XmlStAXReader xmlreader=null;
	
	/** Index all text files under a directory. */
	public static void main(String[] args) {
		String usage = "java org.apache.lucene.demo.IndexFiles"
				+ " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
				+ "This indexes the documents in DOCS_PATH, creating a Lucene index"
				+ "in INDEX_PATH that can be searched with SearchFiles";
		String indexPath = "index";
		String docsPath = "/home/huiying/JavaWorkspace/TableSemantics/";
		boolean create = true;
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
		TableIndexer tind=new TableIndexer();
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");

			Directory dir = FSDirectory.open(new File(indexPath));
			// :Post-Release-Update-Version.LUCENE_XY:
			Analyzer analyzer = new TableAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(
					Version.LUCENE_4_10_0, analyzer);

			if (create) {
				// Create a new index in the directory, removing any
				// previously indexed documents:
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				// Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}

			IndexWriter writer = new IndexWriter(dir, iwc);
			tind.indexOffsetAnnotation(writer, docDir);

			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime()
					+ " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass()
					+ "\n with message: " + e.getMessage());
		}
	}
	public void indexExplodedXml(IndexWriter writer, File file) throws IOException{
		if(file.canRead()){
			if(file.isDirectory()){
				String[] files=file.list();
				if(files!=null){
					for(int i = 0; i < files.length; i++){
						indexExplodedXml(writer, new File(file, files[i]));
					}
				}
			} else {
				FileInputStream fis=new FileInputStream(file);
				try{
					Document doc = new Document();
					if(this.xmlreader==null){
						this.xmlreader=new XmlStAXReader();
						Article a= xmlreader.readArticleFromXml(file.getAbsolutePath());
						//Index single cells
						for(Table t:a.tables){
							for(Group g:t.groups){
								for(Column col:g.columns){
									for(edu.cmu.lti.huiying.domainclasses.Field f:col.content){
										
									}
								}
							}
						}
					}
				}finally{
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
								// New index, so we just add the document (no
								// old document can be there):
								// System.out.println("adding " + file);
								writer.addDocument(doc);
							} else {
								// Existing index (an old copy of this document
								// may have been indexed) so
								// we use updateDocument instead to replace the
								// old one matching the exact
								// path, if present:
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
