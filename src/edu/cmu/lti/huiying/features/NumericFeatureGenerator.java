package edu.cmu.lti.huiying.features;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import edu.cmu.lti.huiying.domainclasses.Column;
import edu.cmu.lti.huiying.domainclasses.Field;
import edu.cmu.lti.huiying.domainclasses.Numerical;
import edu.cmu.lti.huiying.util.OffsetAnnotationGenerator;
import edu.isi.karma.modeling.semantictypes.sl.Part;
/**
 * The feature vector specification:
 * 
 * ratio of float type in column
 * ratio of integer type in column
 * ratio of coordinates in the column
 * mean value
 * standard deviation 
 * value range
 * min value
 * max value
 * voted accuracy (accuracy:#digits after . of flat numbers)
 * average precision (precision: the value after ±)
 * average p-value
 * voted magnitude (the part after E. if contains no E then number of 10s it can divide)
 * 
 * @author huiyingl
 *
 */
public class NumericFeatureGenerator {
	private static Pattern pattern0;
	private static Pattern pattern1;
	private static Pattern pattern2;
	private static Pattern pattern3;
	private static Pattern pattern4;
	private static Pattern pattern5;
	private static Pattern pattern6;
	private static Pattern pattern7;
	
	
	public NumericFeatureGenerator(){
		pattern0=Pattern.compile(regex0);
		pattern1=Pattern.compile(regex1);
		pattern2=Pattern.compile(regex2);
		pattern3=Pattern.compile(regex3);
		pattern4=Pattern.compile(regex4);
		pattern5=Pattern.compile(regex5);
		pattern6=Pattern.compile(regex6);
		pattern7=Pattern.compile(regex7);
	}
	
//	public static LinkedHastMap<String, Double>genSinglePartFeatures(){
//		
//	}
//	public static LinkedHashMap<String, Double> genSingleFieldFeatures(){
//		
//	}
//	public static LinkedHashMap<String,Double> genColumnFeatures(){
//		
//	}
	private static final String numreg="([\\+-]*[0-9]*\\.*[0-9]+[eE]*-*[0-9]*\\.*[0-9]*)";
	private static final String regex0="([\\+-][1-9][0-9]*)";//integer
	private static final String regex1="([\\+-]*[0-9]*\\.[0-9]*)";//float or integer
	/**
	 * Example regex5 matches: P=0.0035; p-value recognizer
	 * group1: p value
	 */
	private final static String regex5="[pP]\\s*=\\s*"+numreg;
	/**
	 * Examples regex2 matches: 24.3±2.4,±2.4,24.3±2.4%,24.3°±2.4,24.3°(±2.4)
	 * group1: main value; group2:unit; group3:precision(variance) of the value;
	 */
	//private final static String regex2="([\\+-]*[0-9]*\\.*[0-9]*)([°CF]*)\\(*±([0-9]*\\.*[0-9]*)%*\\)*";
	private final static String regex2=numreg+"([°CF]*)\\(*±([0-9]*\\.*[0-9]*[eE]*-*[0-9]*\\.*[0-9]*)%*\\)*";
	/**
	 * Example regex3 matches: 1432(0.001); possibly p-value in the brackets.
	 * group1: main value; group2: value in the bracket;
	 */
	private final static String regex3=numreg+"\\("+numreg+"\\)";
	/**
	 * Example regex4 matches: (19.1, -29); 2D coordinates
	 * group1: first coordinate, group2: second coordinate
	 */
	private final static String regex4="\\("+numreg+",\\s*"+numreg+"\\)";//coordinates
	
	/**
	 * Example regex6 matches: (19.1,-29,-1); 3D coordinates
	 * group 1,2,3 are the three coordinate values.
	 */
	private final static String regex6="\\("+numreg+",\\s*"+numreg+",\\s*"+numreg+"\\)";
	/**
	 * Recognize the scientific numbers such as:
	 * 1.67×1017, 1.67×10-5, 3×10−3
	 */
	private final static String regex7=numreg+"×10"+numreg;
	/**
	 * This function recognizes if a given field contains numerical value.
	 * If a field contains numerical value, the returning list will be the parts
	 * that constitute the numeric value. Otherwise null is returned.
	 * @return
	 */
	public Numerical recognize(Field field){
		Numerical num=new Numerical();
		try{
			Integer value = Integer.parseInt(field.toString());
			num.type=1;//integer
			num.mainValue=value.doubleValue();
		}catch(NumberFormatException e0){
			try{
				Double value=Double.parseDouble(field.text.trim());
				num.mainValue=value;
				num.type=2;
				int dot=field.text.indexOf(".");
				if(dot>0){
					num.accuracy=field.text.length()-dot-1;
				}
			}catch(Exception e1){//if cannot be parsed as an integer or a double...
				Matcher matcher = pattern2.matcher(field.text);
				while(matcher.find()){
					String g1=matcher.group(1);
					String g2=matcher.group(2);
					String g3=matcher.group(3);
					if(g1.length()>0){
						try{
							num.mainValue=Double.parseDouble(g1);
							if(g1.matches(regex0)){
								num.type=1;
							}
							else{
								num.type=2;//for now...
								int dot=g1.indexOf(".");
								if(dot>0){
									num.accuracy=g1.length()-dot-1;
								}
							}
						}catch(Exception ee){
							System.err.println(g1);
						}
					}
					if(g2.length()>0)
						num.unit=g2;
					if(g3.length()>0){
						try{
						num.precision=Double.parseDouble(g3);
						}catch(Exception e){
							System.err.println(g3);
						}
					}
					break;//only deal with the first one found now...
				}
				if(num.type==-1){
					matcher=pattern7.matcher(field.text);
					while(matcher.find()){
						String g1=matcher.group(1);
						String g2=matcher.group(2);
						Double body=null;
						Double exp=null;
						if(g1.length()>0){
							try{
								body=Double.parseDouble(g1);
								num.type=2;//has to be float whatsoever
							}catch(Exception ee){
								System.err.println(g1);
							}
						}else{
							body=1.0;
							num.type=2;
						}
						if(g2.length()>0){
							try{
								exp=Double.parseDouble(g2);
							}catch(Exception ee){
								System.err.println(g2);
							}
						}
						num.mainValue=body*Math.pow(10, exp);
						int dot=g1.indexOf(".");
						if(dot>0){
							num.accuracy=g1.length()-dot-1;
						}
					}
				}
				if(num.type==-1){//if doesn't match the first type..
					matcher = pattern3.matcher(field.text);
					while(matcher.find()){
						String g1=matcher.group(1);
						//ignore g2 for now...what is the semantic of the bracket?
						try{
							num.mainValue=Double.parseDouble(g1);
							if(g1.matches(regex0)){
								num.type=1;//integer
							}
							else{
								num.type=2;//for now...
								int dot=g1.indexOf(".");
								if(dot>0){
									num.accuracy=g1.length()-dot-1;
								}
							}
						}catch(Exception ee){
							System.err.println(g1);
						}
						break;
					}
					
				}
				if(num.type==-1){
					matcher=pattern4.matcher(field.text);
					while(matcher.find()){
						String g1=matcher.group(1);
						String g2=matcher.group(2);
						try{
							num.type=3;
							num.dim2=new Double[2];
							num.dim2[0]=Double.parseDouble(g1);
							num.dim2[1]=Double.parseDouble(g2);
							
						}catch(Exception ee){
							System.err.println(g1+" "+g2);
						}
						break;
					}
				}
				if(num.type==-1){
					matcher=pattern6.matcher(field.text);
					while(matcher.find()){
						String g1=matcher.group(1);
						String g2=matcher.group(2);
						String g3=matcher.group(3);
						try{
							num.type=4;
							num.dim3=new Double[3];
							num.dim3[0]=Double.parseDouble(g1);
							num.dim3[1]=Double.parseDouble(g2);
							num.dim3[2]=Double.parseDouble(g3);
							
						}catch(Exception ee){
							System.err.println(g1+" "+g2+" "+g3);
						}
						break;
					}
				}
				if(num.type==-1){
					matcher=pattern1.matcher(field.text);
					while(matcher.find()){
						String g1=matcher.group(1);
						try{
							int val=Integer.parseInt(g1);
							num.type=1;
							num.mainValue=val*1.0;
						}catch(NumberFormatException ei){
							num.mainValue=Double.parseDouble(g1);
							num.type=2;
							int dot=g1.indexOf(".");
							if(dot>0){
								num.accuracy=g1.length()-dot-1;
						}
						break;
					}
				}
				if(num.type==-1){
					matcher=pattern0.matcher(field.text);
					while(matcher.find()){
						String g1=matcher.group(1);
						try{
							num.type=1;
							num.mainValue=Double.parseDouble(g1);
						}catch(NumberFormatException ee){
							System.err.println(g1);
							num.mainValue=null;
						}
						break;
					}
				}
				//fetch for p-value whatsoever:
				matcher=pattern5.matcher(field.text);
				while(matcher.find()){
					String g1=matcher.group(1);
					try{
						Double pvalue=Double.parseDouble(g1);
						if(num.type!=-1){
							num.p_value=pvalue;
						}
					}catch(Exception ee){
						System.err.println(g1);
					}
					break;
				}
				
				}
			}
		}
		return num;
	}
	
	private double columnMean(ArrayList<Numerical> numlist){
		double sum=0.0;
		for(Numerical num:numlist){
			if(num.type!=-1 && num.mainValue!=null)
				sum+=num.mainValue;
		}
		return sum/numlist.size();
	}
	
	private double[] columnMaxMin(ArrayList<Numerical> numlist){
		double max=Double.NEGATIVE_INFINITY;
		double min=Double.POSITIVE_INFINITY;
		double[] limit=new double[2];
		for(Numerical num:numlist){
			if(num.type!=-1 && num.mainValue!=null){
				if(num.mainValue>max){
					max=num.mainValue;
				}
				if(num.mainValue<min){
					min=num.mainValue;
				}
			}
		}
		if(max!=Double.NEGATIVE_INFINITY && min!=Double.POSITIVE_INFINITY){
			limit[0]=max;
			limit[1]=min;
		}
		return limit;
	}
	
	private double columnSTD(ArrayList<Numerical> numlist){
		double mean=columnMean(numlist);
		double sigma=0.0;
		for(Numerical num:numlist){
			if(num.type!=-1&&num.mainValue!=null)
				sigma+=(num.mainValue-mean)*(num.mainValue-mean);
		}
		sigma/=numlist.size();
		return Math.sqrt(sigma);
	}
	
	public ArrayList<ArrayList<Double>> columns2Features(ArrayList<Column> cols){
		ArrayList<ArrayList<Double>> vecs = new ArrayList<ArrayList<Double>>();
		for(Column c:cols){
			vecs.add(getFeatureVector(c.content));
		}
		return vecs;
	}
	
	/**
	 * The feature vector specification:
	 * 0: type: 1:integer 2:float 3:2-dim 4:3-dim(like coordinates)
	 * 1: mainValue
	 * 2: precision
	 * 3. accuracy
	 * 4: p value
	 * @param field
	 * @return
	 */
	public ArrayList<Double> field2Features(Field field){
		Numerical num=this.recognize(field);
		Double[] vec={0.0,0.0,0.0,0.0,0.0};
		if(num.type!=-1){
			vec[0]=num.type*1.0;
			vec[1]=num.mainValue;
			if(num.type==2){
				vec[1]=num.precision;
				vec[1]=num.accuracy*1.0;
			}
			if(num.p_value!=null)
				vec[1]=num.p_value;
			
		}
		
		return new ArrayList<Double>(Arrays.asList(vec));
	}
	public ArrayList<Double> getFeatureVector(ArrayList<Field> column){
		/**
		 * The feature vector specification:
		 * 
		 * 0:number of integer type in column
		 * 1:number of float type in column
		 * 2:number of coordinates in the column
		 * 3:mean value
		 * 4:standard deviation 
		 * 5:value range
		 * 6:min value
		 * 7:max value
		 * 8:voted accuracy (accuracy:#digits after . of flat numbers)
		 * 9:average precision (precision: the value after ±)
		 * 10:average p-value
		 * 11:voted magnitude (number of 10s it can divide)
		 * 
		 * @author huiyingl
		 *
		 */
		double[] vec = new double[12];
		ArrayList<Numerical> numlist = new ArrayList<Numerical>();
		Hashtable<Integer, Integer> votedAccuracy=new Hashtable<Integer, Integer>();
		Hashtable<Double, Integer> votedMagnitude=new Hashtable<Double,Integer>();
		Double avgprec=0.0;
		Double avgp=0.0;
		int preccount=0;
		int pcount=0;
		for(Field field:column){
			Numerical num = this.recognize(field);
			if(num.type!=-1){//is recognized as a number
				if(num.type==1)
				{
					vec[0]++;
				}
				else if(num.type==2){
					vec[1]++;
				}
				else if(num.type==3||num.type==4){
					vec[2]++;
				}
				numlist.add(num);
				if(num.accuracy!=null){
					if(!votedAccuracy.containsKey(num.accuracy)){
						votedAccuracy.put(num.accuracy, 1);
					}
					else{
						votedAccuracy.put(num.accuracy, votedAccuracy.get(num.accuracy)+1);
					}
				}
				if(num.mainValue!=null){
					double[] mag=num.toScientific();
					if(!votedMagnitude.containsKey(mag[1])){
						votedMagnitude.put(mag[1], 1);
					}
					else{
						votedMagnitude.put(mag[1], votedMagnitude.get(mag[1])+1);
					}
				}
				
				if(num.precision!=null){
					avgprec+=num.precision;
					preccount++;
				}
				if(num.p_value!=null){
					avgp+=num.p_value;
					pcount++;
				}
				
			}
		}
		if(numlist.size()>0){
			vec[0]/=numlist.size();
			vec[1]/=numlist.size();
			vec[2]/=numlist.size();
			vec[3]=this.columnMean(numlist);
			vec[4]=this.columnSTD(numlist);
			double[] maxmin=this.columnMaxMin(numlist);
			vec[7]=maxmin[0];
			vec[6]=maxmin[1];
			vec[5]=maxmin[0]-maxmin[1];
			int maxv=-1;
			Enumeration<Integer> e=votedAccuracy.keys();
			while(e.hasMoreElements()){
				int v=e.nextElement();
				if(votedAccuracy.get(v)>maxv)
				{
					maxv=votedAccuracy.get(v);
					vec[8]=v*1.0;
				}
			}
			maxv=-1;
			for(Double v:votedMagnitude.keySet()){
				if(votedMagnitude.get(v)>maxv){
					maxv=votedMagnitude.get(v);
					vec[11]=v*1.0;
				}
			}
			if(preccount!=0)
				vec[9]=avgprec/preccount;
			if(pcount!=0)
				vec[10]=avgp/pcount;
		}
		Double[] dvec=new Double[vec.length];
		for(int i=0;i<vec.length; i++)
			dvec[i]=vec[i];
		ArrayList<Double> res=new ArrayList<Double>(Arrays.asList(dvec));
		return res;
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//System.out.println(regex2.matches(regex2.toString(),"-123.5"));
		//Matcher matcher = regex2.matcher("18.97F±2.8%");
		//Matcher matcher = regex5.matcher("18.97F±2.8% p=0.00001*");
		//Matcher matcher = regex4.matcher("(0.23, 0.52)");
		//Matcher matcher = regex2.matcher("78.3±33.6(6)");
//		while(matcher.find()){
//			int len=matcher.groupCount();
//			for(int i =0; i < len+1; i++){
//			System.out.println("i-value "+i);
//			System.out.println(matcher.group(i));
//			}
//		}
		NumericFeatureGenerator nfg = new NumericFeatureGenerator();
//		try {
//			BufferedReader br = new BufferedReader(new FileReader("materialtab1.csv"));
//			int cnt=0;
//			String line = null;
//			while((line=br.readLine())!=null){
//				cnt++;
//				ArrayList<Field> col=new ArrayList<Field>();
//				String[] spl=line.trim().split("\t");
//				for(String s:spl){
//					Field f = new Field(s);
//					col.add(f);
//				}
//				try{
//					ArrayList<Double>vec =nfg.getFeatureVector(col);
//					String s="";
//					for(Double d:vec){
//						s+=d.toString()+" ";
//					}
//					System.out.println(s.trim());
//				}catch(NullPointerException e){
//					System.err.println(line);
//				}
//			}
//			System.err.println(cnt);
//		
//		
		
		Numerical num = nfg.recognize(new Field("1.1067×10-17"));
//		System.out.println();
//		//nfg.recognize(new Field("0.7±0.3%"));
//		Pattern p = Pattern.compile(regex7);
//		Matcher matcher = p.matcher("1.1067×10-17");
//		while(matcher.find()){
//			int len=matcher.groupCount();
//			for(int i =0; i < len+1; i++){
//			System.out.println("i-value "+i);
//			System.out.println(matcher.group(i));
//			}
//		}
		
//		} catch (FileNotFoundException e) {
//	//		// TODO Auto-generated catch block
//	//		e.printStackTrace();
//	//	
//	//	}
//		catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try {
//			byte[] tab="\t".getBytes();
//			byte[] ret="\n".getBytes();
//			ByteArrayInputStream bais=new ByteArrayInputStream(FileUtils.readFileToByteArray(new File("./metallicmaterial.csv")));
//			//ByteArrayInputStream arrayInputStream = new ByteArrayInputStream();
//			int n=bais.available();
//			int start=0;
//			int end=0;
//			String text="";
//			ArrayList<ArrayList<Field>> rows=new ArrayList<ArrayList<Field>>();
//			ArrayList<Field> r=new ArrayList<Field>();
//			Field field=new Field("");
//			int bc=0;
//			byte[] buff=new byte[n];
//			for(int i = 0; i < n; i++){
//				int b=bais.read();
//				if(b==tab[0]){
//					end=i;
//					text = new String(buff, 0, end-start, StandardCharsets.UTF_8);
//					field.text=text;
//					field.byteStart=start;
//					field.byteEnd=end;
//					r.add(field);
//					field=new Field("");
//					buff=new byte[n];
//					bc=0;
//					start=i+1;
//				}
//				else if(b==ret[0]){
//					end=i;
//					text = new String(buff,0, end-start, StandardCharsets.UTF_8);
//					field.text=text;
//					field.byteStart=start;
//					field.byteEnd=end;
//					r.add(field);
//					rows.add(r);
//					field=new Field("");
//					r=new ArrayList<Field>();
//					buff=new byte[n];
//					bc=0;
//					start=i+1;
//				}else{
//					buff[bc]=(byte)b;
//					bc++;
//				}
//			}
//			NumericFeatureGenerator nfg=new NumericFeatureGenerator();
//			ArrayList<Column> columns=new ArrayList<Column>();
//			for(int i = 0; i < rows.get(0).size(); i++){
//				columns.add(new Column());
//				
//			}
//			for(int i = 1; i < rows.size(); i++){
//				ArrayList<Field> row = rows.get(i);
//				for(int j=0; j<row.size(); j++){
//					if(columns.get(j).content==null){
//						columns.get(j).content=new ArrayList<Field>();
//					}
//					columns.get(j).content.add(row.get(j));
//				}
//			}
//			ArrayList<ArrayList<Double>> vecs=nfg.columns2Features(columns);
//			for(int i = 0; i <rows.get(0).size();i++){
//				Field f=rows.get(0).get(i);
//				if(f.text.contains("Fty")){
//					for(int j = 0; j < columns.get(i).content.size(); j++){
//						Field ff=columns.get(i).content.get(j);
//						ArrayList<Double>fvec=nfg.field2Features(ff);
//						if(fvec.get(1)>90 && fvec.get(1)<125)
//						{
//							System.out.print((j+2)+" ");
//							for(int k=0;k<rows.get(j+1).size();k++)
//								System.out.print(rows.get(j+1).get(k).text+"\t");
//							System.out.println();
//						}
//					}
//				}
//			}
//			
			
//			BufferedWriter bw = new BufferedWriter(new FileWriter("./mm.column.num.offset"));
//			for(int i = 0; i < vecs.size(); i++){
//				String output=OffsetAnnotationGenerator.numericalFeatureVec2Annotation(columns.get(i), vecs.get(i));
//				bw.write(output);
//			}
//			for(ArrayList<Field> row:rows){
//				for(Field f:row){
//					String l="";
//					ArrayList<Double> vec= nfg.field2Features(f);
//					l+="DOC0\t"+"ATTRIBUTE\t0\ttype\t"+vec.get(0)+"\t"+f.byteStart+"\t"+f.byteEnd+"0\tnone\n";
//					l+="DOC0\tATTRIBUTE\t1\tmainvalue\t"+vec.get(1)+"\t"+f.byteStart+"\t"+f.byteEnd+"0\tnone\n";
//					l+="DOC0\tATTRIBUTE\t2\tprec\t"+vec.get(2)+"\t"+f.byteStart+"\t"+f.byteEnd+"0\tnone\n";
//					l+="DOC0\tATTRIBUTE\t3\taccuracy\t"+vec.get(3)+"\t"+f.byteStart+"\t"+f.byteEnd+"0\tnone\n";
//					l+="DOC0\tATTRIBUTE\t4\tpvalue\t"+vec.get(4)+"\t"+f.byteStart+"\t"+f.byteEnd+"0\tnone\n";
//					bw.write(l);
//				}
//				
//			}
			//bw.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
