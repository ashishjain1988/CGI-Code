package com.cgix.snfsift.convertor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AnnotationParser {

	public static void main(String[] args) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader("/home/ashish/snpEff/vcftotxt_CLC.txt"));
		ArrayList<String> dataList = new ArrayList<String>(Arrays.asList("Allele","Annotation","Annotation_Impact","Gene_Name","Gene_ID","Feature_Type","Feature_ID","Transcript_BioType","Rank","HGVS.c","HGVS.p","cDNA.pos / cDNA.length","CDS.pos / CDS.length","AA.pos / AA.length","Distance"));
		Map<Integer,String> summaryDataFieldColumnMapping = new HashMap<Integer,String>();
		//List<Map<Integer,String>> dataFieldList = new ArrayList<Map<Integer,String>>();
		//Map<Integer,String> dataFields = new HashMap<Integer, String>();
		String line = br.readLine();
		int count = 0;
		PrintWriter pw = new PrintWriter("/home/ashish/snpEff/vcftest.txt");
		while(line != null)
		{
			String lineData [] = line.split("\t");
			if(count != 0)
			{
				//dataFields = new HashMap<Integer, String>();
				for(int i=0;i<lineData.length;i++)
				{
					//dataFields.put(i, lineData[i]);
					if(summaryDataFieldColumnMapping.get(i).equals("ANN"))
					{
						String l[] = lineData[i].split("\\|");
						for(int j=0;j<15;j++)
						{
							if(j>=l.length)
							{
								pw.print("\t");
							}else
							{
								pw.print(l[j]+"\t");
							}
						}
					}else
					{
						pw.print(lineData[i]+"\t");
					}
				}
			}else
			{
				for(int i=0;i<lineData.length;i++)
				{
					summaryDataFieldColumnMapping.put(i,lineData[i]);
					if(lineData[i].equals("ANN"))
					{
						for(String s : dataList)
						{
							pw.print(s+"\t");
						}
						
					}else
					{
						pw.print(lineData[i]+"\t");
					}
				}
			}
			pw.println();
			count++;
			line = br.readLine();
		}
		br.close();
		pw.close();
	}
}
