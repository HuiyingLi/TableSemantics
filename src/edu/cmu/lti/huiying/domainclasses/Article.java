package edu.cmu.lti.huiying.domainclasses;

import java.util.ArrayList;
/**
 * The structure that correpnds to an article.
 * It contains info about the articles such as title, author, journal name, etc., as well as 
 * the texts (if any) and the tables.
 * This directory (domainclasses/) mainly contains the structures used
 * to store paper content.
**/
public class Article {

	public String title;
	public String journalName;
	public String journalISSN;
	public String xmlFilePath;
	public ArrayList<String> authors;
	public ArrayList<Table> tables;
	public ArrayList<String> keywords;
	public Article(){
		this.authors=new ArrayList<String>();
		this.tables=new ArrayList<Table>();
		this.keywords=new ArrayList<String>();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
