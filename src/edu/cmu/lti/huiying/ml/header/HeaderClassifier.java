package edu.cmu.lti.huiying.ml.header;

import edu.cmu.lti.huiying.domainclasses.Group;
import edu.cmu.lti.huiying.domainclasses.Table;

/**
 * The header classifier assign each row a category: header or field. 
 * The current implementation calls the python module which classifies using a 
 * trained SVM model.
 * @author huiyingl
 *
 */
public class HeaderClassifier {

	public int[] classifyRows(Group group){
		int[] pred=new int[group.getLength()];
		return pred;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
