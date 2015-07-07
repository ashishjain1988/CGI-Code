package com.cgix.snfsift.convertor;

public class Test {

	public static void main(String[] args) {
		String s = "(ashish)";
		s = s.replace("(","");
		s = s.replace(")","");
		System.out.println(s);
	}
}
