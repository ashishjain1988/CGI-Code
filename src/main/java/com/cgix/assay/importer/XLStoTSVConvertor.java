package com.cgix.assay.importer;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class XLStoTSVConvertor {

	public static void main(String[] args) throws IOException {
		String summarySheetFolder;
		summarySheetFolder =  null;
		for(int i=0;i<args.length;i++)
		{
			String argument = args[i];
			if(argument.contains("-folder="))
			{
				summarySheetFolder = argument.split("=")[1];
			}
		}
		if(summarySheetFolder == null)
		{
			System.err.println("Please give valid number of arguments.");
			System.exit(0);
		}
		/*FileInputStream fis;
		HSSFWorkbook myWorkBook;
		HSSFSheet mySheet;
		Iterator<Row> rowIterator;*/
		File dir = new File(/*"/home/ashish/Downloads/Venkat_Files"*/summarySheetFolder);
		File [] files = dir.listFiles();
		File myFile;
		/*Map<String, Integer> summaryDataFieldColumnMap = new HashMap<String, Integer>();
		List<Map<String, String>> dataList = new ArrayList<Map<String,String>>();
		Map<String, String> dataMap;
		int count = 0;
		PrintWriter pw;
		List<String> headerList = new ArrayList<String>();
		BufferedReader br;*/
		for(int i=0;i<files.length;i++)
		{
			if(files[i].isFile() && files[i].getName().endsWith(".xls"))
			{
				myFile = files[i];
				String name = myFile.getName();
				FileUtils.copyFile(myFile, new File(name+".txt"));
				/*System.out.println(myFile.getName());
				fis = new FileInputStream(myFile);
				br = new BufferedReader(new FileReader(myFile));
				pw = new PrintWriter(myFile.getName()+".txt");
				String line = br.readLine();
				while(line)
				pw.println(x);*/
				/*for(String s:headerList)
				{
					pw.print(s+"\t");
				}
				pw.println();
				for(Map<String, String> data : dataList)
				{
					for(String s:headerList)
					{
						pw.print(data.get(s)+"\t");
					}
					pw.println();
				}
				pw.close();*/
				/*myWorkBook = new HSSFWorkbook(fis);
				mySheet = myWorkBook.getSheetAt(i);
				summaryDataFieldColumnMap = new HashMap<String, Integer>();
				count = 0;
				if(mySheet != null)
				{
					rowIterator = mySheet.iterator();
					while (rowIterator.hasNext()) 
					{
						Row row = rowIterator.next();
						if(count != 0)
						{
							dataMap = new HashMap<String, String>();
							Set<String> headerSet = summaryDataFieldColumnMap.keySet();
							for(String s:headerSet)
							{
								dataMap.put(s, row.getCell(summaryDataFieldColumnMap.get(s))!=null?row.getCell(summaryDataFieldColumnMap.get(s)).toString():"");
							}
							dataList.add(dataMap);
						}else
						{
							Iterator<Cell> cellIterator = row.cellIterator();
							Cell cell;
							headerList = new ArrayList<String>();
							while(cellIterator.hasNext())
							{
								cell = cellIterator.next();
								headerList.add(cell.getStringCellValue().trim());
								summaryDataFieldColumnMap.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
							}
						}
						count++;
					}
					
					pw = new PrintWriter(myFile.getName()+".txt");
					for(String s:headerList)
					{
						pw.print(s+"\t");
					}
					pw.println();
					for(Map<String, String> data : dataList)
					{
						for(String s:headerList)
						{
							pw.print(data.get(s)+"\t");
						}
						pw.println();
					}
					pw.close();
				}
				myWorkBook.close();*/
			}
		}
	}
}
