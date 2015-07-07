package com.cgix.datafiles;

import java.util.HashMap;
import java.util.Map.Entry;

public class Convert3to1AA {

	public static HashMap<String, String> aminoAcidMap = new HashMap<String, String>(){{
		put("Arg","R");
		put("Asn","N");
		put("Asp","D");
		put("Asx","B");
		put("Cys","C");
		put("Glu","E");
		put("Gln","Q");
		put("Glx","Z");
		put("Gly","G");
		put("His","H");
		put("Ile","I");
		put("Leu","L");
		put("Lys","K");
		put("Met","M");
		put("Phe","F");
		put("Pro","P");
		put("Ser","S");
		put("Thr","T");
		put("Trp","W");
		put("Tyr","Y");
		put("Val","V");
		put("Xxx","X");
		put("Ter","*");
		put("Ala","A");
	}};
	public static String convert3aaTo1(String HGVs)
	{
		if(HGVs != null && HGVs.split("p\\.").length > 1)
		{
			String HGV_short = HGVs.split("p\\.")[1];
			for(Entry<String, String> entry : aminoAcidMap.entrySet())
			{
				HGV_short = HGV_short.replaceAll(entry.getKey(), entry.getValue());
			}
			return HGV_short;
		}else
		{
			return HGVs;
		}
	}
	
	public static void main(String[] args) {
		System.out.println(convert3aaTo1("NP_000042.3:p.Asp2721Asn"));
	}
}
