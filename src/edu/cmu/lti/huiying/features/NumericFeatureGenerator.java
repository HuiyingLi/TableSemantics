package edu.cmu.lti.huiying.features;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.lti.huiying.domainclasses.Field;
import edu.cmu.lti.huiying.domainclasses.Numerical;
import edu.isi.karma.modeling.semantictypes.sl.Part;

public class NumericFeatureGenerator {
	private static Pattern pattern0;
	private static Pattern pattern1;
	private static Pattern pattern2;
	private static Pattern pattern3;
	private static Pattern pattern4;
	private static Pattern pattern5;
	private static Pattern pattern6;
	public NumericFeatureGenerator(){
		pattern0=Pattern.compile(regex0);
		pattern1=Pattern.compile(regex1);
		pattern2=Pattern.compile(regex2);
		pattern3=Pattern.compile(regex3);
		pattern4=Pattern.compile(regex4);
		pattern5=Pattern.compile(regex5);
		pattern6=Pattern.compile(regex6);
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
				Double value=Double.parseDouble(field.text);
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
					if(g3.length()>0)
						num.precision=Double.parseDouble(g3);
					break;//only deal with the first one found now...
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
							System.out.println(g1);
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
							System.out.println(g1+" "+g2);
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
							System.out.println(g1+" "+g2+" "+g3);
						}
						break;
					}
				}
				if(num.type==-1){
					matcher=pattern1.matcher(field.text);
					while(matcher.find()){
						String g1=matcher.group(1);
						try{
							num.type=2;
							num.mainValue=Double.parseDouble(g1);
							int dot=g1.indexOf(".");
							if(dot>0){
								num.accuracy=g1.length()-dot-1;
							}
						}catch(Exception ee){
							System.out.println(g1);
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
						}catch(Exception ee){
							System.out.println(g1);
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
						System.out.println(g1);
					}
					break;
				}
				
			}
		}
		return num;
	}
	
	private double columnMean(ArrayList<Numerical> numlist){
		double sum=0.0;
		for(Numerical num:numlist){
			sum+=num.mainValue;
		}
		return sum/numlist.size();
	}
	
	private double[] columnMaxMin(ArrayList<Numerical> numlist){
		double max=Double.NEGATIVE_INFINITY;
		double min=Double.POSITIVE_INFINITY;
		double[] limit=new double[2];
		for(Numerical num:numlist){
			if(num.mainValue>max){
				max=num.mainValue;
			}
			if(num.mainValue<min){
				min=num.mainValue;
			}
		}
		limit[0]=max;
		limit[1]=min;
		return limit;
	}
	
	private double columnSTD(ArrayList<Numerical> numlist){
		double mean=columnMean(numlist);
		double sigma=0.0;
		for(Numerical num:numlist){
			sigma+=(num.mainValue-mean)*(num.mainValue-mean);
		}
		sigma/=numlist.size();
		return Math.sqrt(sigma);
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
		Numerical num = nfg.recognize(new Field("-135 (this is a test)"));
		System.out.println();
		//nfg.recognize(new Field("0.7±0.3%"));
//		Pattern p = Pattern.compile(regex2);
//		Matcher matcher = p.matcher("p= 0.7e10±0.3e-5%");
//		while(matcher.find()){
//			int len=matcher.groupCount();
//			for(int i =0; i < len+1; i++){
//			System.out.println("i-value "+i);
//			System.out.println(matcher.group(i));
//			}
//		}
	}

}
