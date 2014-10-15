package edu.cmu.lti.huiying.domainclasses;
import java.util.*;

import edu.isi.karma.modeling.semantictypes.sl.Part;

public class Numerical {
	public int type=-1; //1:integer 2:float 3:2-dim 4:3-dim(like coordinates)
	public Double mainValue=null;
	public String unit=null;
	public Double precision;
	public int accuracy; //number of digits after the dot. e.g. 2 for 3.14
	public Double p_value;
	public Double[] dim2=null;
	public Double[] dim3=null;
	public Numerical(){
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
