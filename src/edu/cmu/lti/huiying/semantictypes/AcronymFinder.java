package edu.cmu.lti.huiying.semantictypes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.lti.huiying.domainclasses.Article;
import edu.cmu.lti.huiying.domainclasses.Field;
import edu.cmu.lti.huiying.domainclasses.Group;
import edu.cmu.lti.huiying.domainclasses.Table;
import edu.cmu.lti.huiying.util.XmlStAXReader;

public class AcronymFinder {
	//private XmlStAXReader xmlreader = new XmlStAXReader();
	//public static Pattern uppercasePattern= Pattern.compile("[0-9a-z]*[A-Z\\-αβγδεζηθλστφωκ]{2,}[0-9a-z]*");
	public static Pattern uppercasePattern=Pattern.compile("[0-9a-z]*[A-Z]+[0-9a-z\\-αβγδεζηθλστφωκ]*([A-Z][0-9a-z\\-αβγδεζηθλστφωκ]*)+[0-9a-z]*");
	public static String[] specialmapping={"ex	x","first	1","second	2","third	3"};
	//public static String[] prep={"of","in","on","about","for","with","from","per","to","via","within"};
	public static HashSet<String> prepset=new HashSet<String>(Arrays.asList("of","in","on","about","for","with","from","per","to","via","within","and"));
	
	public static LinkedHashMap<String, HashSet<String>> extractAcronymFromKeywordString(String kw){
		LinkedHashMap<String, HashSet<String>> abbr=new LinkedHashMap<String, HashSet<String>>();
		Pattern uppercasePattern=Pattern.compile("[0-9a-z]*[A-Z\\-αβγδεζηθλστφωκ]+");
		Pattern keywordPattern=Pattern.compile("[0-9a-z]*[A-Za-z0-9\\-αβγδεζηθλστφωκ]+\\,.*");
		Matcher matcher = keywordPattern.matcher(kw);
		if(matcher.find()){
			String text=matcher.group();
			String spl[]=text.split("\\,");
			String acr=spl[0];
			if(acr.length()<2)
				return abbr;
			boolean noupper=true;
			for(char c:acr.toCharArray()){
				if(c>='A'&&c<='Z'){
					noupper=false;
					break;
				}
			}
			if(noupper)
				return abbr;
			String full=text.substring(acr.length()+1).trim();
			if(full.length()>0){
				//String[] spll=full.split("\\,-|\\s+");
				//if(spll.length>1){
//					String initials="";
//					for(String w:spll){
//						initials+=w.substring(0,1).toUpperCase();
//					}
//					int editdist=minDistance(acr,initials);
//					if(editdist*1.0/acr.length()<0.34){
				if(!abbr.containsKey(abbr)){
					abbr.put(acr, new HashSet<String>());
				}
				abbr.get(acr).add(full.toLowerCase());
				return abbr;
			//		}
//				}else{
//					if(full.startsWith(acr.toLowerCase())){
//						if(!abbr.containsKey(abbr)){
//							abbr.put(acr, new HashSet<String>());
//						}
//						abbr.get(acr).add(full.toLowerCase());
//					}
//				}
			}
		}
		//if fail first, second segment keywords according to upper/lower levels
		matcher = uppercasePattern.matcher(kw);
		if(matcher.find()){
			int end=matcher.end();
			String acr=matcher.group();
			if(acr.length()<2)
				return abbr;
			boolean noupper=true;
			for(char c:acr.toCharArray()){
				if(c>='A'&&c<='Z'){
					noupper=false;
					break;
				}
			}
			if(noupper)
				return abbr;
			String full=kw.substring(end).trim();
			if(full.length()==0){
				return abbr;
			}
			String spl[]=full.split("\\-|\\s+");
			//if(spl.length>1){//initials
//				String initials="";
//				for(String w:spl){
//					initials+=w.substring(0,1).toUpperCase();
//				}
//				int editdist=minDistance(acr, initials);
				//if(editdist*1.0/acr.length()<0.34){
					if(!abbr.containsKey(acr)){
						abbr.put(acr, new HashSet<String>());
					}
					abbr.get(acr).add(full.toLowerCase());
				//}
//			}else{//the full name is only one word, so prefix
//				if(full.startsWith(acr.toLowerCase())){
//					if(!abbr.containsKey(abbr)){
//						abbr.put(acr, new HashSet<String>());
//					}
//					abbr.get(acr).add(full.toLowerCase());
//				}
//			}
		}
		return abbr;
	}
	public static LinkedHashMap<String, HashSet<String>>extractAncronymsFromKeywords(Article article){
//		if("/home/huiying/JavaWorkspace/TableSemantics/data/NeuroScience_explode/S0306452206008104.xml".equals(article.xmlFilePath))
//			System.out.println();
		Pattern uppercasePattern=Pattern.compile("[0-9a-z]*[A-Z\\-αβγδεζηθλστφωκ]+");
		Pattern keywordPattern=Pattern.compile("[0-9a-z]*[A-Za-z0-9\\-αβγδεζηθλστφωκ]+\\,.*");
		LinkedHashMap<String, HashSet<String>> abbr= new LinkedHashMap<String, HashSet<String>>();
		if(article.keywords!=null){
			for(String kw:article.keywords){
				//first the standard keyword, explanation pattern
				Matcher matcher = keywordPattern.matcher(kw);
				if(matcher.find()){
					String text=matcher.group();
					String spl[]=text.split("\\,");
					String acr=spl[0];
					if(acr.length()<2)
						continue;
					boolean noupper=true;
					for(char c:acr.toCharArray()){
						if(c>='A'&&c<='Z'){
							noupper=false;
							break;
						}
					}
					if(noupper)
						continue;
					String full=text.substring(acr.length()+1).trim();
					if(full.length()>0){
						//String[] spll=full.split("\\,-|\\s+");
						//if(spll.length>1){
//							String initials="";
//							for(String w:spll){
//								initials+=w.substring(0,1).toUpperCase();
//							}
//							int editdist=minDistance(acr,initials);
//							if(editdist*1.0/acr.length()<0.34){
						if(!abbr.containsKey(abbr)){
							abbr.put(acr, new HashSet<String>());
						}
						abbr.get(acr).add(full.toLowerCase());
						continue;
					//		}
//						}else{
//							if(full.startsWith(acr.toLowerCase())){
//								if(!abbr.containsKey(abbr)){
//									abbr.put(acr, new HashSet<String>());
//								}
//								abbr.get(acr).add(full.toLowerCase());
//							}
//						}
					}
				}
				//if fail first, second segment keywords according to upper/lower levels
				matcher = uppercasePattern.matcher(kw);
				if(matcher.find()){
					int end=matcher.end();
					String acr=matcher.group();
					if(acr.length()<2)
						continue;
					boolean noupper=true;
					for(char c:acr.toCharArray()){
						if(c>='A'&&c<='Z'){
							noupper=false;
							break;
						}
					}
					if(noupper)
						continue;
					String full=kw.substring(end).trim();
					if(full.length()==0){
						continue;
					}
					String spl[]=full.split("\\-|\\s+");
					//if(spl.length>1){//initials
//						String initials="";
//						for(String w:spl){
//							initials+=w.substring(0,1).toUpperCase();
//						}
//						int editdist=minDistance(acr, initials);
						//if(editdist*1.0/acr.length()<0.34){
							if(!abbr.containsKey(acr)){
								abbr.put(acr, new HashSet<String>());
							}
							abbr.get(acr).add(full.toLowerCase());
						//}
//					}else{//the full name is only one word, so prefix
//						if(full.startsWith(acr.toLowerCase())){
//							if(!abbr.containsKey(abbr)){
//								abbr.put(acr, new HashSet<String>());
//							}
//							abbr.get(acr).add(full.toLowerCase());
//						}
//					}
				}
			}
		}
		return abbr;
	}
	
	public static Hashtable<String, String> extractAcronymFromText(String text){
		//Hashtable<String, Hashtable<String, Double>> res=new Hashtable<String, Hashtable<String, Double>>();
		//BufferedWriter bw = new BufferedWriter(new FileWriter("cs.acronym", true));
		Hashtable<String, String> results=new Hashtable<String, String>();
		
		String[] tks=text.split("\\s+");
		for(int i=0; i<tks.length; i++){
			String s=tks[i];
			Matcher matcher = AcronymFinder.uppercasePattern.matcher(s);
			int occurcnt=0;
			while(matcher.find()){
				if(occurcnt>5)
					break;
				occurcnt++;
				String acr=matcher.group();
				if(results.containsKey(acr)||acr.length()>8)
					continue;
//				if(results.containsKey(acr)||acr.length()>8||acr.equals("SIMMUNE")||acr.startsWith("MOKT"))
//					continue;
				int maxlen=acr.length()+2;
				Hashtable<String, Double> res=new Hashtable<String, Double>();
				//backward
				LinkedList<String> candi=new LinkedList<String>();
				String fullname="";
				for(int k=1; k<maxlen && i-k>=0; k++){
					candi.push(tks[i-k]);
					fullname=tks[i-k]+" "+fullname;
					HashSet<String> gens=generateAcronym(candi, acr);
					
					if(gens.contains(acr)&& !eliminatePunct(fullname.trim()).equals(acr)){
						fullname=eliminatePunct(fullname.trim());
						if(fullname.contains(" ")&&prepset.contains(fullname.split("\\s+")[0]))
							fullname=fullname.substring(fullname.indexOf(" ")+1);
						if(fullname.contains(" ")&&fullname.trim().length()>0&&prepset.contains(fullname.split("\\s+")[fullname.split("\\s+").length-1]))
							fullname=fullname.substring(0,fullname.lastIndexOf(" "));
						if(fullname.trim().length()>0&& !acr.equals(fullname))
							results.put(acr, fullname.trim().toLowerCase());
						break;
					}
					else{
						if(fullname.length()>0)
							res.put(fullname.trim().toLowerCase(), 0.0);
					}
				}
				candi.clear();
				fullname="";
				for(int k = 1; k < maxlen && i+k<tks.length; k++){
					candi.add(tks[i+k]);
					fullname+=tks[i+k]+" ";
					HashSet<String> gens=generateAcronym(candi, acr);
					if(gens.contains(acr)&& !eliminatePunct(fullname.trim()).equals(acr)){
						fullname=eliminatePunct(fullname.trim());
						if(fullname.contains(" ")&&prepset.contains(fullname.split("\\s+")[0]))
							fullname=fullname.substring(fullname.indexOf(" ")+1);
						if(fullname.contains(" ")&&fullname.trim().length()>0&&prepset.contains(fullname.split("\\s+")[fullname.split("\\s+").length-1]))
							fullname=fullname.substring(0,fullname.lastIndexOf(" "));
						if(fullname.trim().length()>0&& !acr.equals(fullname))
							results.put(acr, fullname.trim().toLowerCase());
						break;
					}
					else{
						if(fullname.length()>0)
							res.put(fullname.trim().toLowerCase(), 0.0);
					}
				}
			}
		}
		return results;
	}
	public static HashSet<String> generateAcronym(List<String> fullname, String acr){
		/***
		HashSet<String> res=new HashSet<String>();
		//take all the upper cases
		String r1="";
		for(String s:fullname){
			r1+=extractUppercase(s);
		}
		res.add(r1);
		//take all the initials case sensitive(r2) or insensitive(r3);
		String r2="";
		String r3="";
		for(String s:fullname){
			r2+=s.charAt(0);
			r3+=(""+s.charAt(0)).toUpperCase();
		}
		res.add(r2);
		res.add(r3);
		//take all the initials breaking treating hypen connection as two words
		String r4="";
		for(String s:fullname){
			String[] ss=s.split("-");
			for(String w:ss){
				r4+=(""+w.charAt(0)).toUpperCase();
			}
		}
		res.add(r4);
		//take all the initials, take away the prepositions
		String r5="";//case sensitive
		String r6="";//case insensitive
		String r7="";//break hyphen case insensitive
		for(String s:fullname){
			if(!prepset.contains(s)){
				r5+=s.charAt(0);
				r6+=(""+s.charAt(0)).toUpperCase();
				
				String[] ss=s.split("-");
				for(String w:ss){
					r7+=(""+w.charAt(0)).toUpperCase();
				}
			}
		}
		res.add(r5);
		res.add(r6);
		res.add(r7);
		
		return res;
		*/
		HashSet<String> prev=new HashSet<String>();
		prev.add("");
		
		for(String s:fullname){
			s=eliminatePunct(s);
			if(s.length()==0)
				continue;
			HashSet<String> cur=new HashSet<String>();
			String u=extractUppercase(s);
			if(u!=null){
				String ts=extractUppercase(s);
				if(ts!=null&&acr.contains(ts))
					cur.add(ts);//upper case letters
			}
			if(acr.contains(s.substring(0,1)))
				cur.add(s.substring(0,1));//case sensitive
			if(acr.contains(s.substring(0,1).toUpperCase()))
				cur.add(s.substring(0,1).toUpperCase());//case insensitive -- all upper
			for(String ts:specialMapping(s)){
				if(ts!=null&&acr.contains(ts))
					cur.add(ts);
			}
			String[] ss=s.split("-");
			if(ss.length>1){
				HashSet<String> hprev=new HashSet<String>();
				hprev.add("");
				for(String w:ss){
					if(w.length()>0){
						HashSet<String> hcur=new HashSet<String>();
						if(acr.contains(w.substring(0,1)))
							hcur.add(w.substring(0,1));
						if(acr.contains(w.substring(0,1).toUpperCase()))
							hcur.add(w.substring(0,1).toUpperCase());
						String ts=extractUppercase(w);
						if(ts!=null&&acr.contains(ts))
							hcur.add(ts);
						for(String tts:specialMapping(w)){
							if(tts!=null&&acr.contains(tts))
								hcur.add(tts);
						}
						HashSet<String> htmp=new HashSet<String>();
						for(String pp:hprev){
							for(String cc:hcur){
								if(acr.contains(pp+cc))
									htmp.add(pp+cc);
							}
						}
						hprev=htmp;
					}
				}
				cur.addAll(hprev);
			}
			HashSet<String> tmp=new HashSet<String>();
			for(String p:prev){
				for(String c:cur){
					tmp.add(p+c);
				}
			}
			prev=tmp;
		}
		return prev;
	}
	private static String eliminatePunct(String s){
		return s.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll(",", "").replaceAll("\\[", "")
				.replaceAll("\\]", "");
	}
	/**
	 * When the initial doesn't match, return a set of possible alternative special mapping
	 * @param word
	 * @return
	 */
	private static HashSet<String> specialMapping(String word){
		HashSet<String> mapping=new HashSet<String>();
		if(word.toLowerCase().startsWith("ex")){
			mapping.add("X");
			mapping.add("x");
		}
		if(word.toLowerCase().contains("first")){
			mapping.add("1");
			mapping.add("1st");
		}
		if(word.toLowerCase().contains("second")){
			mapping.add("2");
			mapping.add("2nd");
		}
		if(word.toLowerCase().equals("to")){
			mapping.add("2");
		}
//		Matcher matcher = uppercasePattern.matcher(word);
//		if(matcher.find()){
//			mapping.add(matcher.group());
//		}
		if(prepset.contains(word)){
			mapping.add("");
		}
		return mapping;
	}
	private static String extractUppercase(String s){
		String u="";
		for(int i = 0; i < s.length(); i++){
			if(s.charAt(i)>='A'&&s.charAt(i)<='Z'){
				u+=s.charAt(i);
			}
		}
		if(u.length()>0)
			return u;
		return null;
	}
//	public static String extractFullnameFromContext(String acr, Article a){
//		String splitter="\\,;\\.";
//		//with connectors
//		Pattern p1=Pattern.compile(acr+"[ ]*[=|:][ ]*[^\\,;\\.]*");
//		Pattern p2=Pattern.compile("("+acr+")");
//		
//		return null;
//	}
	/**
	 * This function I) detect acronyms in the article and II) find the possible full name
	 */
	public static Hashtable<String,Hashtable<String, Double>> resolveAcronyms(Article a){
		Hashtable<String, Hashtable<String,Double>> res=new Hashtable<String,Hashtable<String,Double>>();
		Pattern uppercasePattern=Pattern.compile("[0-9a-z]*[A-Z\\-αβγδεζηθλστφωκ]{2,}[0-9a-z]*");
		Pattern fallbackPattern=Pattern.compile("[A-Z\\-αβγδεζηθλστφωκ]{2,}");
		//From keywords, only applicable for neuroscience data
		LinkedHashMap<String, HashSet<String>> keywordAbbr=extractAncronymsFromKeywords(a);
		//From header
//		for(Table t:a.tables){
//			for(Group g:t.groups){
//				for(ArrayList<Field> row:g.rows){
//					for(Field f:row){
//						String s=f.text;
//						Matcher matcher = uppercasePattern.matcher(s);
//						while(matcher.find()){
//							String acr=matcher.group();
//							
//							//first look into the keywords section of the current paper
//							if(keywordAbbr.containsKey(acr)){
//								if(!res.containsKey(acr)){
//									res.put(acr, new Hashtable<String,Double>());
//								}
//								for(String k:keywordAbbr.get(acr)){
//									res.get(acr).put(k, 1.0);
//								}
//							}else{
//								String full=null;
//								if(t.legend!=null){
//									full=findFullnameInSentence(acr, t.legend);
//									
//									if(full!=null){
//										System.out.println(acr+"\t"+full);
//										if(!res.containsKey(acr)){
//											res.put(acr, new Hashtable<String,Double>());
//											res.get(acr).put(full, 0.0);
//										}
//										if(!res.get(acr).contains(full)){
//											res.get(acr).put(full, 0.0);
//										}
//										res.get(acr).put(full,res.get(acr).get(full)+1);
//										
//									}
//								}
//								if(t.caption!=null){
//									full=findFullnameInSentence(acr, t.caption);
//									if(full!=null){
//										if(!res.containsKey(acr)){
//											res.put(acr, new Hashtable<String,Double>());
//											res.get(acr).put(full, 0.0);
//										}
//										if(!res.get(acr).contains(full)){
//											res.get(acr).put(full, 0.0);
//										}
//										res.get(acr).put(full,res.get(acr).get(full));
//									}
//								}
//								if(t.citation!=null){
//									full=findFullnameInSentence(acr, t.citation);
//									if(full!=null){
//										if(!res.containsKey(acr)){
//											res.put(acr, new Hashtable<String,Double>());
//											res.get(acr).put(full, 0.0);
//										}
//										if(!res.get(acr).contains(full)){
//											res.get(acr).put(full, 0.0);
//										}
//										res.get(acr).put(full,res.get(acr).get(full));
//									}
//								}
//								if(t.sentences!=null){
//									for(String sent:t.sentences){
//										full=findFullnameInSentence(acr, sent);
//										if(full!=null){
//											if(!res.containsKey(acr)){
//												res.put(acr, new Hashtable<String,Double>());
//												res.get(acr).put(full, 0.0);
//											}
//											if(!res.get(acr).contains(full)){
//												res.get(acr).put(full, 0.0);
//											}
//											res.get(acr).put(full,res.get(acr).get(full));
//										}
//									}
//								}
//							}
////							else{//if no reference, fall back to simpler expression
////								Matcher smatcher=fallbackPattern.matcher(acr);
////								while(smatcher.find()){
////									String sacr=matcher.group();
////									System.out.println("**fallback**"+s+"\t"+acr);
////								}
////							}
//						}
//						
//					}
//				}
//			}
//		}
		//From content/context
		if(a.abs!=null){
			Hashtable<String, String> localacr=extractAcronymFromText(a.abs);
			for(String k:localacr.keySet()){
				if(!res.containsKey(k)){
					res.put(k, new Hashtable<String, Double>());
				}
				if(!res.get(k).containsKey(localacr.get(k))){
					res.get(k).put(localacr.get(k), 0.0);
				}
				res.get(k).put(localacr.get(k), res.get(k).get(localacr.get(k))+1.0);
			}
		}
		for(Table t:a.tables){
			String full=null;
			if(t.legend!=null){
				Hashtable<String, String> localacr=extractAcronymFromText(t.legend);
				for(String k:localacr.keySet()){
					if(!res.containsKey(k)){
						res.put(k, new Hashtable<String, Double>());
					}
					if(!res.get(k).containsKey(localacr.get(k))){
						res.get(k).put(localacr.get(k), 0.0);
					}
					res.get(k).put(localacr.get(k), res.get(k).get(localacr.get(k))+1.0);
				}
			}
			
			full=null;
			if(t.citation!=null){
				Hashtable<String, String> localacr=extractAcronymFromText(t.citation);
				for(String k:localacr.keySet()){
					if(!res.containsKey(k)){
						res.put(k, new Hashtable<String, Double>());
					}
					if(!res.get(k).containsKey(localacr.get(k))){
						res.get(k).put(localacr.get(k), 0.0);
					}
					res.get(k).put(localacr.get(k), res.get(k).get(localacr.get(k))+1.0);
				}
			}
			full=null;
			if(t.sentences!=null){
				for(String sent:t.sentences){
					Hashtable<String, String> localacr=extractAcronymFromText(sent);
					for(String k:localacr.keySet()){
						if(!res.containsKey(k)){
							res.put(k, new Hashtable<String, Double>());
						}
						if(!res.get(k).containsKey(localacr.get(k))){
							res.get(k).put(localacr.get(k), 0.0);
						}
						res.get(k).put(localacr.get(k), res.get(k).get(localacr.get(k))+1.0);
					}
				}
			}
		}
		for(String k:res.keySet()){
			double sum=0.0;
			for(String f:res.get(k).keySet()){
				sum+=res.get(k).get(f);
			}
			for(String f:res.get(k).keySet()){
				res.get(k).put(f, res.get(k).get(f)/sum);	
				System.out.println(k+"\t"+f+"\t"+res.get(k).get(f));
			}
		}
		return res;
	}
	
	public static String findFullnameInSentence(String acr, String sentence){
		if(!sentence.contains(acr))
			return null;
		int n=acr.length();
		int i=sentence.indexOf(acr);
		while(i>=0){
			String s=sentence.substring(0,i+n);
			String[] spl=s.split("\\s+");
			String initials="";
			String full="";
			for(int j=0; j<n && spl.length-2-j>=0; j++){
				initials=spl[spl.length-2-j].substring(0,1)+initials;
				full=spl[spl.length-2-j]+" "+full;
			}
			if(minDistance(initials.toLowerCase(), acr.toLowerCase())*1.0/acr.length()<=0.5){
				return full;
			}
			full="";
			initials="";
			i=sentence.indexOf(acr,i+1);
		}
		return null;
		
	}
	public static int minDistance(String word1, String word2) {
		int len1 = word1.length();
		int len2 = word2.length();
	 
		// len1+1, len2+1, because finally return dp[len1][len2]
		int[][] dp = new int[len1 + 1][len2 + 1];
	 
		for (int i = 0; i <= len1; i++) {
			dp[i][0] = i;
		}
	 
		for (int j = 0; j <= len2; j++) {
			dp[0][j] = j;
		}
	 
		//iterate though, and check last char
		for (int i = 0; i < len1; i++) {
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++) {
				char c2 = word2.charAt(j);
	 
				//if last two chars equal
				if (c1 == c2) {
					//update dp value for +1 length
					dp[i + 1][j + 1] = dp[i][j];
				} else {
					int replace = dp[i][j] + 1;
					int insert = dp[i][j + 1] + 1;
					int delete = dp[i + 1][j] + 1;
	 
					int min = replace > insert ? insert : replace;
					min = delete > min ? min : delete;
					dp[i+1][j+1]=min;
				}
			}
		}
		return dp[len1][len2];
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
//		String p="[0-9a-z]*[A-Z]+\\,.*";
//		Pattern pattern=Pattern.compile(p);
//		Matcher matcher=pattern.matcher("DDfadf afad");
//		while(matcher.find()){
//			System.out.println(matcher.group());
//		}
//		String acr="CVR";
//		Pattern p1=Pattern.compile(acr+"[ ]*[=|:][ ]*[^\\,;\\.]*");
//		String s="CVR : fadf adfa fad; getfadfa afa.";
//		Matcher matcher=p1.matcher(s);
//		while(matcher.find()){
//			System.out.println(matcher.group());
//		}
		
		XmlStAXReader xmlreader = new XmlStAXReader();
		List<Article> alist=xmlreader.loadArticleFromDirectory("data/NeuroScience_explode/");
		for(Article a:alist){
			resolveAcronyms(a);
		}
//		findFullnameInSentence("VE","The cMRF cell was classified as Vestibular-Eye movement (VE) because modulation during");
		
//		Hashtable<String, Integer> pairs=new Hashtable<String, Integer>();
//		for(Article a:alist){
//			LinkedHashMap<String, HashSet<String>> abbr=extractAncronymsFromKeywords(a);
//			for(String acr:abbr.keySet()){
//				for(String full: abbr.get(acr)){
//					String p=acr+"\t"+full;
//					if(!pairs.containsKey(p)){
//						pairs.put(p, 1);
//					}else{
//						pairs.put(p, pairs.get(p)+1);
//					}
//				}
//			}
//		}
//		List<Map.Entry<String, Integer>> res=new LinkedList<>(pairs.entrySet());
//		Collections.sort(res, new Comparator<Map.Entry<String, Integer>>()
//			{
//				public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2){
//					return (o2.getValue()).compareTo(o1.getValue());
//				}
//			});
//		BufferedWriter bw = new BufferedWriter(new FileWriter("acronyms.txt"));
//		for(Map.Entry<String, Integer> s:res){
//			bw.write(s.getKey()+"\t"+s.getValue()+"\n");
//		}
//		bw.close();
		
		/**test generator**/
//		ArrayList<String> test=new ArrayList<String>(Arrays.asList("(PATent and Article Tracking, Retrieval and AnalysiS)".split("\\s+")));
//		ArrayList<String> test=new ArrayList<String>(Arrays.asList("Exploratory Inspection Machine".split("\\s+")));
//		HashSet<String> acrs=generateAcronym(test);
//		int i=0;
//		BufferedReader br = new BufferedReader(new FileReader("computerscience.raw.w2v"));
//		String line=null;
//		BufferedWriter bw = new BufferedWriter(new FileWriter("cs.acronym.new"));
//		Hashtable<String,HashSet<String>> results=new Hashtable<String, HashSet<String>>();
//		while((line=br.readLine())!=null){
//			Hashtable<String, String> res=extractAcronymFromText(line);
////			for(String k:res.keySet()){
////				if(!results.containsKey(k)){
////					results.put(k, new HashSet<String>());
////				}
////				results.get(k).add(res.get(k));
////			}
//			for(String k:res.keySet()){
//				bw.write(k+"\t"+res.get(k)+"\n");
//			}
//			System.err.println("######################"+ ++i);
//		}
////		BufferedWriter bw = new BufferedWriter(new FileWriter("cs.acronym"));
////		for(String k:results.keySet()){
////			HashSet<String> fs=results.get(k);
////			for(String f:fs){
////				bw.write(k+"\t"+fs+"\n");
////			}
////		}
//		bw.close();
	}

}
