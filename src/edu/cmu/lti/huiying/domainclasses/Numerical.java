package edu.cmu.lti.huiying.domainclasses;
import java.util.*;

import edu.isi.karma.modeling.semantictypes.sl.Part;

public class Numerical {
	public int type=-1; //1:integer 2:float 3:2-dim 4:3-dim(like coordinates)
	public Double mainValue=null;
	public String unit=null;
	public Double precision=null;
	public Integer accuracy=null; //number of digits after the dot. e.g. 2 for 3.14
	public Double p_value=null;
	public Double[] dim2=null;
	public Double[] dim3=null;
	public Numerical(){
	}
	
	/**
	 * Conver main value into scientific notation;
	 * Returns an array of length2: first number is the part before E (belong to [1,10)),
	 * the second part is the part after E.
	 * @return
	 */
	public double[] toScientific(){
		double[] parts=new double[2];
 		if(this.mainValue==null)
			return null;
		String s=this.mainValue.toString();
		if(s.contains("E")){
			String[] spl=s.split("E");
			parts[0]=Double.parseDouble(spl[0]);
			parts[1]=Double.parseDouble(spl[1]);
		}
		else{
			int dot=s.indexOf(".");
			int n=0;
			int nonzero=-1;
			for(int i = 0; i < s.length(); i++){
				if(s.substring(i,i+1).matches("[1-9]")){
					nonzero=i;
					break;
				}
			}
			if(dot>nonzero)
				n=dot+1-(nonzero+1)-1;
			else
				n=dot-(nonzero+1)+1;
			parts[0]=this.mainValue/Math.pow(10, n);
			parts[1]=n;
		}
		return parts;
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Numerical num=new Numerical();
		num.mainValue=8300*1.0;
		num.toScientific();

	}

}
