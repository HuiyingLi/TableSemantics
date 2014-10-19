package edu.cmu.lti.huiying.util;

import java.util.ArrayList;

import weka.core.Debug.Random;

public class Exepriment {

	public static ArrayList [] splitData(ArrayList data, double ratio){
		ArrayList[] result=new ArrayList[2];
		ArrayList train=new ArrayList();
		ArrayList test=new ArrayList();
		Random r = new Random();
		for(int i = 0; i < data.size();i++){
			float f=r.nextFloat();
			if(f>ratio){
				test.add(data.get(i));
			}
			else
				train.add(data.get(i));
		}
		result[0]=train;
		result[1]=test;
		return result;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
