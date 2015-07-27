package edu.cmu.lti.huiying.ir.rangedsearch;
import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.scene.shape.Path;

import edu.cmu.lti.huiying.domainclasses.Column;
import edu.cmu.lti.huiying.domainclasses.Field;
import edu.cmu.lti.huiying.domainclasses.Group;
import edu.cmu.lti.huiying.domainclasses.Header;
import edu.cmu.lti.huiying.domainclasses.Table;
import edu.cmu.lti.huiying.features.NumericFeatureGenerator;
import edu.cmu.lti.huiying.semantictypes.AcronymFinder;
import edu.cmu.lti.huiying.util.XmlStAXReader;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class Morelikethis {
	public Hashtable<String, Hashtable<String, Double>> acronyms= new Hashtable<String, Hashtable<String, Double>>();
	public Hashtable<String, Hashtable<String, Double>> similar=new Hashtable<String, Hashtable<String, Double>>();
	public HashSet<String> stopwords=new HashSet<String>();
	public StanfordCoreNLP pipeline=null;
	public NumericFeatureGenerator nfg=new NumericFeatureGenerator();
	public int fcounter=0;
	//public Hashtable<String, >
	public Morelikethis(String acronympath, String simpath, String stoppath) throws NumberFormatException, IOException{
		//Load the acronym maps.
		BufferedReader br = new BufferedReader(new FileReader(acronympath));
		String line=null;
		while((line=br.readLine())!=null){
			String[] words= line.split("\t");
			if(!acronyms.containsKey(words[0])){
				acronyms.put(words[0], new Hashtable<String, Double>());
			}
			acronyms.get(words[0]).put(words[1], Double.parseDouble(words[2]));
		}
		br.close();
		
		//Load Stanford Corenlp
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
	    pipeline = new StanfordCoreNLP(props);
	    
	    //Load semantic similar words
	    BufferedReader br2 = new BufferedReader(new FileReader(simpath));
	    line=null;
	    while((line=br2.readLine())!=null){
	    	String[] words=line.split("\\s+");
	    	similar.put(words[0], new Hashtable<String, Double>());
	    	for(int i=1; i<words.length; i++){
	    		if(i%2==0){
	    		similar.get(words[0]).put(words[i-1], Double.parseDouble(words[i]));
	    		}
	    	}
	    }
	    br2.close();
	    
	    BufferedReader br3=new BufferedReader(new FileReader(stoppath));
	    line=null;
	    while((line=br3.readLine())!=null){
	    	String sw=line.trim();
	    	stopwords.add(sw);
	    }
	    br3.close();
	}
	/**
	 * 
	 * @param xml
	 * @param query
	 * @return
	 * 
	 * 1. add acronyms
	 * 2. 
	 */
	private Double[] initarr(int flen){
		Double[] dv=new Double[flen];
		for(int i=0; i<flen; i++)
			dv[i]=new Double(0.0);
		return dv;
	}
	public LinkedHashMap<String, Double[]> processTable(String xml, String query){
		
		int flen=7;
		double qcoeff=2.0;
		int contextWindow=3;
		double nnweight=0.5;
		double simweight=0.5;
		LinkedHashMap<String, Double[]> res=new LinkedHashMap<String,Double[]>();
		
		String[] qterms=query.split("\\s+");
		XmlStAXReader xmlreader=new XmlStAXReader();
		Table t=xmlreader.xmlstr2table(xml);
		if(t==null){
			return res;
		}
		String[] atitle={};
		String[] caption={};
		if(t.article.title!=null)
			atitle=t.article.title.split("\\s+");
		if(t.caption!=null)
			caption=t.caption.split("\\s+");
		//footnote???
		
		/**This is the frequency counting part**/
		//article title
		for(String s:atitle){
			if(!res.containsKey(s))
				res.put(s, initarr(flen));
			res.get(s)[0]++;
		}
		for(String s:caption){
			if(!res.containsKey(s))
				res.put(s, initarr(flen));
			res.get(s)[1]++;
		}
		if(t.article.keywords!=null){
			for(String s: t.article.keywords){
				LinkedHashMap<String, HashSet<String>> abbr=AcronymFinder.extractAcronymFromKeywordString(s);
				if(abbr.size()==0){
					for(String ss:s.split("\\s+")){
						if(!res.containsKey(ss)){
							res.put(ss, initarr(flen));
						}
						res.get(ss)[2]++;
					}
				}
			}
		}
		
		for(Group g:t.groups){
			if(g.headerlist!=null){
				for(Header hdr:g.headerlist){
					if(!res.containsKey(hdr.text))
						res.put(hdr.text, initarr(flen));
					res.get(hdr.text)[4]++;
				}
				for(Field f:g.columns.get(0).content){
					if(!res.containsKey(f.text))
						res.put(f.text, initarr(flen));
					res.get(f.text)[5]++;
				}
			}
		}
		
		/**Higher the weight of NNs near the table headers in the context**/
		for(Group g:t.groups){
			if(g.headerlist!=null){
				for(Header hdr:g.headerlist){
					for(String hs:hdr.text.split("\\s+")){
						if(t.article.title.contains(hs)){
							int start= t.article.title.indexOf(hs);
							
							Annotation doc=new Annotation(t.article.title);
							List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
							if(sentences!=null){
								for(CoreMap sentence: sentences) {
									List<CoreLabel> tokens= sentence.get(TokensAnnotation.class);
									int i=0;
									while(i<tokens.size()){
										if(tokens.get(i).beginPosition()==start)
											break;
										i++;
									}
									//left
									for(int b=1; b<contextWindow && i-b>=0; b++){
										if(tokens.get(i-b).get(PartOfSpeechAnnotation.class).equals("NN")){
											String lw=tokens.get(i-b).originalText();
											if(!res.containsKey(lw)){
												res.put(lw, initarr(flen));
											}
											res.get(lw)[0]+=nnweight;
										}
									}
									//right
									for(int b=1; b<contextWindow && i+b<tokens.size(); b++){
										if(tokens.get(i+b).get(PartOfSpeechAnnotation.class).contains("NN")){
											String rw=tokens.get(i+b).originalText();
											if(!res.containsKey(rw)){
												res.put(rw, initarr(flen));
											}
											res.get(rw)[0]+=nnweight;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		/**Add the expansions of the acronyms from background**/
		for(String s:atitle){
			if(acronyms.containsKey(s)){
				Hashtable<String, Double> fullnames=acronyms.get(s);
				for(String fn:fullnames.keySet()){
					if(!res.containsKey(fn)){
						res.put(fn, initarr(flen));
					}
					res.get(fn)[0]+=fullnames.get(fn);
				}
			}
		}
		
		for(String s:caption){
			if(acronyms.containsKey(s)){
				Hashtable<String, Double> fullnames=acronyms.get(s);
				for(String fn:fullnames.keySet()){
					if(!res.containsKey(fn)){
						res.put(fn, initarr(flen));
					}
					res.get(fn)[1]+=fullnames.get(fn);
				}
			}
		}
		
		for(String s: t.article.keywords){
			LinkedHashMap<String, HashSet<String>> abbr=AcronymFinder.extractAcronymFromKeywordString(s);
			if(abbr.size()==0){
				for(String ss:s.split("\\s+")){
					if(!res.containsKey(ss)){
						res.put(ss, initarr(flen));
					}
					res.get(ss)[2]++;
				}
			}
		}
		
		for(Group g:t.groups){
			if(g.headerlist!=null){
				for(Header hdr:g.headerlist){
					if(acronyms.containsKey(hdr.text)){
						Hashtable<String, Double> fullnames=acronyms.get(hdr.text);
						for(String fn:fullnames.keySet()){
							if(!res.containsKey(fn)){
								res.put(fn, initarr(flen));
							}
							res.get(fn)[4]+=fullnames.get(fn);
						}
					}
				}
				for(Field f:g.columns.get(0).content){
					if(acronyms.containsKey(f.text)){
						Hashtable<String, Double> fullnames=acronyms.get(f.text);
						for(String fn:fullnames.keySet()){
							if(!res.containsKey(fn)){
								res.put(fn, initarr(flen));
							}
							res.get(fn)[5]+=fullnames.get(fn);
						}
					}
				}
			}
		}
		
		/**Higher the weight of words contained in the queries by multiplying an coeff**/
		
		/**Add the numerical values for any columns that matches a word in the query **/
		for(Group g:t.groups){
			for(Column c:g.columns){
				for(Header h:c.headers){
					for(String q:query.split("\\s+")){
						if(h.text.contains(q)){
							Double[] v=initarr(12);
							v=nfg.getFeatureVector(c.content).toArray(v);
							res.put("NUM"+fcounter, v);
						}
					}
				}
			}
		}
		/**Add the most similar words from word embeddings**/
		for(String q:query.split("\\s+")){
			q=q.toLowerCase();
			if(similar.containsKey(q)){
				for(String simw:similar.get(q).keySet()){
					if(!res.containsKey(simw)){
						res.put(simw, initarr(flen));
					}
					res.get(simw)[6]=simweight*similar.get(q).get(simw);
				}
			}
		}
		
		//Filter out stopwords and numbers
		LinkedHashMap<String, Double[]> fres=new LinkedHashMap<String, Double[]>();
		for(String k:res.keySet()){
			if(!stopwords.contains(k.toLowerCase())&& !k.matches("([\\+-]*[0-9]*\\.[0-9]*)")){
				fres.put(k, res.get(k));
			}
			
		}
		return fres;
	}
	
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static void main(String[] args) throws NumberFormatException, IOException {
		// TODO Auto-generated method stub
		int i=0;
		Morelikethis m = new Morelikethis("acronyms.neuro.prob.txt", "neuro.similar.w2v.0","data/smartstopwords.txt");
		String in =Files.lines(Paths.get("S0149763408001504.xml")).collect(Collectors.joining(""));
		
		//String in= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><tables><metadata><journal>Neuroscience</journal><issn>0306-4522</issn><volume>74</volume><issue>1</issue><page>13–20</page><date>September 1996</date></metadata><article-title>METABOTROPIC GLUTAMATE RECEPTOR ACTIVATION CONTRIBUTES TO NOCICEPTIVE REFLEX ACTIVITY IN THE RAT SPINAL CORD IN VITRO</article-title><authors><author>BOXALL, S.J.</author><author>THOMPSON , S.W.N.</author><author>DRAY, A.</author><author>DICKENSON, A.H.</author><author>URBAN, L.</author></authors><keywords><keyword>Metabotropic glutamate receptor</keyword><keyword>Spinal cord</keyword><keyword>Ventral root potential</keyword><keyword>in vitro</keyword><keyword>(1 S,3 R)-ACPD, (1 S,3 R)-1-aminocyclopentane-1,3-dicarboxylic acid</keyword><keyword>ACSF, artificial cerebrospinal</keyword><keyword>Fluid</keyword><keyword>AMPA, α-amino-3-hydroxy-5-methyl-4-isoxazolepropionate</keyword><keyword>d-AP5, d(−)-2-amino-5-phosphonopentanoic</keyword><keyword>Acid</keyword><keyword>l-AP3, l(+)-2-amino-3-phosphonopropionic acid</keyword><keyword>MCPG, (+)-α-methyl-4-carboxyphenylglycine</keyword><keyword>mGluR,</keyword><keyword>Metabotropic glutamate receptor</keyword><keyword>NMDA, N-methyl- d-aspartate</keyword><keyword>VRP, ventral root potential</keyword></keywords><table><caption>Comparison of the effects of d-AP5, l-AP3 and MCPG against ventral root responses evoked by high-intensity single-shock and repetitive electrical stimulation of the L5 dorsal root</caption><contexts><context><headings/><citation>Representative traces compare the inhibitory effects of d-AP5 and MCPG on high-intensity, single-shock electrical stimulation (50 V, 200 μs) of the dorsal root (A), and cumulative depolarization (“wind-up”) of the ventral root evoked by 20 s of 5 Hz repetitive electrical stimulation of the L5 dorsal root (B).   Traces represent different experiments. <sentence> (A) Fifteen minute perfusion of 40 μM d-AP5 (left panel) or 5 mM MCPG (right panel) reduces the amplitude (and consequently the area under the curve between 0 and 8 s; see Table 1) of the late (polysynaptic) phase of the single-shock-evoked VRP in the isolated hemisected spinal cord of the rat. </sentence> (B) Fifteen minute perfusion of 40 μM d-AP5 (left panel) or 5 mM MCPG (right panel) reduces the amplitude of the cumulative VRP.   Arrowheads indicate the time when amplitude was measured.   For further details and cumulative data see text and Table 1.  </citation></context><context><headings/><citation>Representative traces compare the inhibitory effects of d-AP5 and MCPG on high-intensity, single-shock electrical stimulation (50 V, 200 μs) of the dorsal root (A), and cumulative depolarization (“wind-up”) of the ventral root evoked by 20 s of 5 Hz repetitive electrical stimulation of the L5 dorsal root (B).   Traces represent different experiments.   (A) Fifteen minute perfusion of 40 μM d-AP5 (left panel) or 5 mM MCPG (right panel) reduces the amplitude (and consequently the area under the curve between 0 and 8 s; see Table 1) of the late (polysynaptic) phase of the single-shock-evoked VRP in the isolated hemisected spinal cord of the rat.   (B) Fifteen minute perfusion of 40 μM d-AP5 (left panel) or 5 mM MCPG (right panel) reduces the amplitude of the cumulative VRP.   Arrowheads indicate the time when amplitude was measured. <sentence> For further details and cumulative data see text and Table 1. </sentence></citation></context><context><headings><ce:section-title xmlns=\"http://www.elsevier.com/xml/cja/dtd\" xmlns:ce=\"http://www.elsevier.com/xml/common/dtd\" xmlns:sb=\"http://www.elsevier.com/xml/common/struct-bib/dtd\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">RESULTS</ce:section-title></headings><citation>The ventral root potential evoked following single-shock electrical stimulation of the L5 dorsal root was reduced following perfusion with the NMDA receptor antagonist d-AP5 (40 μM) (Fig. <sentence> 3Aa, Table 1). </sentence> This reduction was consistent with previous findings.   [30] This concentration of d-AP5 reduced the amplitude of the VRP within 8 s after electrical stimulation, leaving the late, prolonged phase of the response intact.  </citation></context><context><headings><ce:section-title xmlns=\"http://www.elsevier.com/xml/cja/dtd\" xmlns:ce=\"http://www.elsevier.com/xml/common/dtd\" xmlns:sb=\"http://www.elsevier.com/xml/common/struct-bib/dtd\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">RESULTS</ce:section-title></headings><citation><sentence>Perfusion of the mGluR antagonist, MCPG, produced a concentration-dependent inhibition of the C-fibre-evoked response (ic50 = 2.9 ± 0.2 mM; n = 3–5 at 100 μM, 1.0 mM and 5 mM concentrations) with a significant reduction of the control response at all tested concentrations (Table 1). </sentence> The maximum reduction was 43 ± 0.4% of control at 5.0 mM (n = 3; P &lt; 0.001).   However, unlike d-AP5, MCPG appeared to selectively reduce the prolonged component of the VRP (for comparison, see Fig.   3A).  </citation></context><context><headings><ce:section-title xmlns=\"http://www.elsevier.com/xml/cja/dtd\" xmlns:ce=\"http://www.elsevier.com/xml/common/dtd\" xmlns:sb=\"http://www.elsevier.com/xml/common/struct-bib/dtd\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">RESULTS</ce:section-title></headings><citation><sentence>In separate experiments (n = 9), 100 μM l-AP3 did not affect (1 S,3 R)-ACPD-induced ventral root depolarizations and did not alter VRPs evoked by electrical stimulation (Table 1). </sentence></citation></context><context><headings><ce:section-title xmlns=\"http://www.elsevier.com/xml/cja/dtd\" xmlns:ce=\"http://www.elsevier.com/xml/common/dtd\" xmlns:sb=\"http://www.elsevier.com/xml/common/struct-bib/dtd\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">RESULTS</ce:section-title></headings><citation>The maximum amplitude of the cumulative VRP (wind-up), following 1, 5 and 10 Hz stimulation, was reduced to 58.9 ± 2.7%, 66.2 ± 4.4% and 68.0 ± 2.2% (n = 10 and P &lt; 0.001 for each value) of control value following superfusion with d-AP5 (40 μM), respectively. <sentence> MCPG also produced a significant, concentration-dependent reduction of the amplitude of the cumulative VRP throughout the same frequency range (see Table 1) with a maximum reduction to 79.7 ± 4.5% (at 1 Hz; n = 4; P &lt; 0.05) of control value at 1.0 mM, and to 69.5 ± 0.4% of control value at 5 mM concentration (at 10 Hz; n = 3; P &lt; 0.001). </sentence> At 100 μM concentration, MCPG produced consistent non-significant reduction at 1 and 5 Hz stimulus frequencies.   At 10 Hz stimulus frequency, the value was 82.6 ± 2.5% of the control value (n = 5; P &lt; 0.05).   There was no significant correlation between the degree of inhibition by both mGluR and NMDA receptor antagonists of the cumulative VRP and the frequency of the electrical stimulation in the range between 1.0 and 10.0 Hz.  </citation></context></contexts><group><row spans=\"\" newspans=\"0 0 0 0 0\"><value/><value/><value>Wind-up</value><value>Wind-up</value><value>Wind-up</value></row><row spans=\"0 0 0 0 0\" newspans=\"0 0 0 0 0\"><value>Antagonist</value><value>Single shock</value><value>1 Hz</value><value>5 Hz</value><value>10 Hz</value></row><row spans=\"0 0 0 0 0\" newspans=\"0 0 0 0 0\"><value>d-AP5, 40 μM (n = 10)</value><value>68.7 ± 5.1***</value><value>58.9 ± 2.7***</value><value>66.2 ± 4.4***</value><value>68.0 ± 2.0***</value></row><row spans=\"0 0 0 0 0\" newspans=\"0 0 0 0 0\"><value>MCPG, 100 μm (n = 5)</value><value>83.2 ± 4.2**</value><value>90.8 ± 10.7</value><value>88.5 ± 7.6</value><value>82.6 ± 3.5*</value></row><row spans=\"0 0 0 0 0\" newspans=\"0 0 0 0 0\"><value>MCPG, 1 mM (n = 4)</value><value>59.0 ± 8.5**</value><value>79.7 ± 4.5*</value><value>89.8 ± 2.6*</value><value>87.9 ± 2.5*</value></row><row spans=\"0 0 0 0 0\" newspans=\"0 0 0 0 0\"><value>MCPG, 5 mM (n = 3)</value><value>42.7 ± 0.5***</value><value>78.1 ± 8.1*</value><value>74.2 ± 5.7**</value><value>69.5 ± 0.4***</value></row><row spans=\"0 0 0 0 0\" newspans=\"0 0 0 0 0\"><value>l-AP3, 100 μm (n = 9)</value><value>107.7 ± 5.1</value><value>100.4 ± 4.6</value><value>99.6 ± 4.1</value><value>102.2 ± 4.9</value></row></group></table></tables>";
//		for(File f:new File("data/NeuroScience_explode/").listFiles()){
//			String in =Files.lines(Paths.get(f.getAbsolutePath())).collect(Collectors.joining(""));
//			//0149-7634/exploded/0149763485900041.xml
//			m.processTable(in, "amplitude");
//			i++;
//			System.out.println(i);
//			
//		}
		LinkedHashMap<String, Double[]> res=m.processTable(in, "peak amplitude");
		for(String k:res.keySet()){
			System.out.println(k);
		}
		
	}

}
