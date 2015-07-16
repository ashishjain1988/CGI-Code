package com.cgix.snfsift.convertor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VCFInfoParser {

	public static void main(String[] args) throws IOException {
		/*String vcfFileName,txtFileName,resultFileName;
		vcfFileName = txtFileName = resultFileName = null;
		for(int i=0;i<args.length;i++)
		{
			String argument = args[i];
			if(argument.contains("vcfFile="))
			{
				vcfFileName = argument.split("=")[1];
			}else if(argument.contains("txtFile="))
			{
				txtFileName = argument.split("=")[1];
			}else if(argument.contains("resultFileName="))
			{
				resultFileName = argument.split("=")[1];
			}
		}
		if(vcfFileName == null || txtFileName == null || resultFileName == null)
		{
			System.err.println("Please give valid number of arguments.");
			System.exit(0);;
		}*/
		BufferedReader br = new BufferedReader(new FileReader("/home/ashish/Downloads/ensembl-tools-release-80/scripts/variant_effect_predictor/result.txt"/*txtFileName*/));
		//ArrayList<String> dataList = new ArrayList<String>(Arrays.asList("Allele","Annotation","Annotation_Impact","Gene_Name","Gene_ID","Feature_Type","Feature_ID","Transcript_BioType","Rank","HGVS.c","HGVS.p","cDNA.pos / cDNA.length","CDS.pos / CDS.length","AA.pos / AA.length","Distance"));
		Map<String,Integer> summaryDataFieldColumnMapping = new HashMap<String,Integer>();
		List<Map<Integer,String>> dataFieldList = new ArrayList<Map<Integer,String>>();
		Map<Integer,String> dataFields = new HashMap<Integer, String>();
		List<String> headerList = new ArrayList<String>();
		String line = br.readLine();
		int count = 0;
		while(line != null)
		{
			String lineData [] = line.split("\t");
			if(count != 0)
			{
				dataFields = new HashMap<Integer, String>();
				for(int i=0;i<lineData.length;i++)
				{
					dataFields.put(i, lineData[i].trim());
				}
				dataFieldList.add(dataFields);
			}else
			{
				for(int i=0;i<lineData.length;i++)
				{
					String header = lineData[i].trim();
					if(headerList.contains(header))
					{
						header = header+"_"+i;
					}
					summaryDataFieldColumnMapping.put(header.trim(),i);
					headerList.add(lineData[i].trim());
				}
			}
			count++;
			line = br.readLine();
		}
		br.close();
		Map<String, String> infoDescription = new HashMap<String,String>();
		br = new BufferedReader(new FileReader("/home/ashish/Downloads/ensembl-tools-release-80/scripts/variant_effect_predictor/CLCBio_SU314.ann.vcf"/*vcfFileName*/));
		line = br.readLine();
		while(line != null && !line.equals("#CHROM"))
		{
			if(line.contains("##INFO="))
			{
				String info[] = line.split("INFO=<")[1].split(",");
				String ID = null;
				String description = null;
				for(String s : info)
				{
					if(s.contains("ID="))
					{
						ID = s.split("=")[1].trim();
						
					}else if(s.contains("Description"))
					{
						description = s.split("=")[1];
						if(description.contains("|") && description.contains("'"))
						{
							description = description.split("\\'")[1];
						}else
						{
							if(description.charAt(description.length()-1) == '>')
							description = description.substring(0,description.length()-1);//Removing '>'
						}
					}
				}
				//System.out.println(description);
				infoDescription.put(ID, description);
			}
			line = br.readLine();
		}
		br.close();
		/*File folder = new File(resultFileName);
		if(!folder.exists())
		{
			folder.mkdir();
		}*/
		PrintWriter pw = new PrintWriter("/home/ashish/Downloads/ensembl-tools-release-80/scripts/variant_effect_predictor/vcftest_vcf.txt"/*resultFileName+".txt"*/);
		//For Header
		for(String s : headerList)
		{
			//System.out.println(s);
			String desc = infoDescription.get(s);
			if(desc == null)
			{
				pw.print(s+"\t");
			}else if(desc.contains("|") && !s.equals("GENEINFO"))
			{
				System.out.println(s);
				String l[] = desc.split("\\|");
				for(int j=0;j<l.length;j++)
				{
					String header = l[j].trim();
					header = header.contains("\"")?header.replace("\"", ""):header;
					//System.out.println(header);
					pw.print(header+"\t");
				}
			}else
			{
				pw.print(s+"("+desc+")"+"\t");
			}
		}
		pw.println();
		//Printing Data
		for(int i=0;i<dataFieldList.size();i++)
		{
			dataFields = dataFieldList.get(i);
			for(String s : headerList)
			{
				String desc = infoDescription.get(s);
				if(desc != null && desc.contains("|") && !s.equals("GENEINFO"))
				{
					int length = desc.split("\\|").length;
					//System.out.println(s);
					String data1 = dataFields.get(summaryDataFieldColumnMapping.get(s));
					//System.out.println(data1);
					if(data1 != null)
					{
						data1 = data1.replace("(","");
						data1 = data1.replace(")","");
						String data[] = data1.split("\\|");
						for(int j=0;j<length;j++)
						{
							if(j>=data.length)
							{
								pw.print("\t");
							}else
							{
								pw.print(data[j]+"\t");
							}
						}
					}
				}else
				{
					pw.print(dataFields.get(summaryDataFieldColumnMapping.get(s))+"\t");
				}
			}
			pw.println();
		}
		pw.close();
	}
}
