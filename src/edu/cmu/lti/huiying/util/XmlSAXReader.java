package edu.cmu.lti.huiying.util;

import java.io.File;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.cmu.lti.huiying.domainclasses.Article;
import edu.cmu.lti.huiying.domainclasses.Field;
import edu.cmu.lti.huiying.domainclasses.Group;
import edu.cmu.lti.huiying.domainclasses.Header;
import edu.cmu.lti.huiying.domainclasses.Table;

public class XmlSAXReader {

	private SAXParserFactory factory = null;// = SAXParserFactory.newInstance();
	private SAXParser saxParser = null;// factory.newSAXParser();

	private Article article = null;

	// class ArticleHandler extends DefaultHandler {
	// }

	class ArticleHandler extends DefaultHandler {
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
		private boolean headerlist = false;
		private boolean headers = false;
		private boolean header = false;
		private boolean row = false;
		private boolean value = false;
		private Table t = new Table();
		private Group grp = new Group();
		private Field field = new Field("");
		private Header hder = new Header("");
		private ArrayList<Field> r = new ArrayList<Field>();
		public String filePath = null;

		public void startDocument() throws SAXException {
			article = new Article();
			article.xmlFilePath = this.filePath;
		}

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (qName.equals("author")) {
				author = true;
			} else if (qName.equals("article-title")) {
				title = true;
			} else if (qName.equals("journal")) {
				journal = true;
			} else if (qName.equals("keyword")) {
				keyword = true;
			} else if (qName.equals("issn")) {
				issn = true;
			} else if (qName.equals("table")) {
				table = true;
				t.article = article;
				// article.tables.add(new Table());
			} else if (qName.equals("caption")) {
				caption = true;
			} else if (qName.equals("legend")) {
				legend = true;
			}
			// else if(qName.equals("context")){
			// context=true;
			// }
			//
			// else if(qName.equals("headings")){
			// headings=true;
			// }
			// else if(qName.equals("citation")){
			// citation=true;
			// }
			// else if(qName.equals("sentence")){
			// sentence=true;
			// }
			else if (qName.equals("group")) {
				group = true;
			} else if (qName.equals("headerlist")) {
				headerlist = true;
				grp.headerlist = new ArrayList<Header>();
			} else if (qName.equals("headers")) {
				headers = true;
				grp.headers = new ArrayList<Header>();
			} else if (qName.equals("header")) {
				header = true;
			} else if (qName.equals("row")) {
				row = true;
			} else if (qName.equals("value")) {
				value = true;
			}
		}

		public void characters(char ch[], int start, int length)
				throws SAXException {
			if (author) {
				article.authors.add(new String(ch, start, length).replace(
						"XREF", "").trim());
				author = false;
			} else if (title) {
				article.title = new String(ch, start, length).replace("XREF",
						"").trim();
				title = false;
			} else if (journal) {
				article.journalName = new String(ch, start, length).replace(
						"XREF", "").trim();
				journal = false;
			} else if (keyword) {
				article.keywords.add(new String(ch, start, length).replace(
						"XREF", "").trim());
				keyword = false;
			} else if (issn) {
				article.journalISSN = new String(ch, start, length).replace(
						"XREF", "").trim();
				issn = false;
			} else if (caption) {
				t.caption = new String(ch, start, length).replace("XREF", "").trim();
				caption = false;
			} else if (legend) {
				t.legend = new String(ch, start, length).replace("XREF", "").trim();
				legend = false;
			} else if (header) {
				this.hder.text = new String(ch, start, length).replace("XREF",
						"").trim();

			} else if (value) {
				// r.add(new Field(new String(ch,start,length).replace("XREF",
				// "")));
				this.field.text = new String(ch, start, length).replace("XREF",
						"").trim();
			}
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (qName.equals("table")) {
				article.tables.add(t);
				t.article = article;
				t = new Table();
				table = false;
			} else if (qName.equals("group")) {
				grp.table = t;
				grp.transpose();// generate column grid
				t.groups.add(grp);
				grp = new Group();
				group = false;
			} else if (qName.equals("headerlist")) {
				headerlist = false;
			} else if (qName.equals("headers")) {
				headers = false;
			} else if (qName.equals("header")) {
				if (headerlist) {
					grp.headerlist.add(hder);
				}
				if (headers) {
					grp.headers.add(hder);
				}
				hder = new Header("");
				header = false;
			} else if (qName.equals("row")) {
				grp.rows.add(r);
				r = new ArrayList<Field>();
				row = false;
			} else if (qName.equals("value")) {
				r.add(field);
				field = new Field("");
				value = false;
			}
		}
	}

	public Article readArticleFromXml(String path) {
		try {
			if (this.factory == null)
				this.factory = SAXParserFactory.newInstance();
			if (this.saxParser == null)
				this.saxParser = this.factory.newSAXParser();
			ArticleHandler ahd = new ArticleHandler();
			ahd.filePath = path;
			saxParser.parse(new File(path), ahd);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.article;
	}

	public ArrayList<Article> loadArticleFromDirectory(String dir) {
		ArrayList<Article> articles = new ArrayList<Article>();
		File[] files = new File(dir).listFiles();
		try {
			if (this.factory == null)
				this.factory = SAXParserFactory.newInstance();
			if (this.saxParser == null)
				this.saxParser = this.factory.newSAXParser();
			for(File f:files){
				ArticleHandler ahd = new ArticleHandler();
				ahd.filePath=f.getAbsolutePath();
				saxParser.parse(f, ahd);
				articles.add(this.article);
				this.article=null;
			}
		} catch (ParserConfigurationException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return articles;

	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// String path= "./data/NeuroScience_explode/030645229600190X.xml";
		String dir = "./data/NeuroScienceResearch_explode/";
		File[] files = new File(dir).listFiles();
		int cnt = 0;
		int totalTable = 0;
		int totalGroup = 0;
		int totalUnequal = 0;
		int legit = 0;
		for (File f : files) {
			XmlSAXReader reader = new XmlSAXReader();
			Article a = reader.readArticleFromXml(f.getAbsolutePath());
			for (Table t : a.tables) {
				totalTable++;
				if (t.check()) {
					legit++;
				}
			}
			// Article a=reader.article;
		}
		System.out.println(totalTable);
		System.out.println(legit);

	}

}
