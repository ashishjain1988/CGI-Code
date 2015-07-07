package com.cgix.assay.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CheckForMissingSampleIds {

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("/home/ashish/Downloads/missingSampleIds.txt"));
		List<String> missingSampleIds = new ArrayList<String>();
		String line = br.readLine();
		while(line != null)
		{
			missingSampleIds.add(line.trim());
			line = br.readLine();
		}
		br.close();
		System.out.println(missingSampleIds.size());
		XSSFWorkbook myWorkBook;
		XSSFSheet mySheet;
		Iterator<Row> rowIterator;
		File myFile = new File("/home/ashish/Downloads/Gilead_0123_Sample_list_for_NGS_new_copy.xlsx");
		FileInputStream fis = new FileInputStream(myFile);
		myWorkBook = new XSSFWorkbook (fis);
		int count = 0;
		Map<String, Integer> summaryDataFieldColumnMap = new HashMap<String, Integer>();
		Set<String> s = new HashSet<String>();
		for(int i=1;i<4;i++)
		{
			mySheet = myWorkBook.getSheetAt(i);
			count = 0;
			if(mySheet != null)
			{
				rowIterator = mySheet.iterator();
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					if(count != 0)
					{
						
						if(row.getCell(summaryDataFieldColumnMap.get("Patient ID")) != null && row.getCell(summaryDataFieldColumnMap.get("Sample ID")) !=null && !row.getCell(summaryDataFieldColumnMap.get("Patient ID")).equals("") && !row.getCell(summaryDataFieldColumnMap.get("Sample ID")).equals("") && row.getCell(summaryDataFieldColumnMap.get("Cancer Study")) !=null && !row.getCell(summaryDataFieldColumnMap.get("Cancer Study")).equals(""))
						{
							String sampleId = row.getCell(summaryDataFieldColumnMap.get("Sample ID")).toString();
							if(missingSampleIds.contains(sampleId))
							{
								s.add(sampleId);
								System.out.println(sampleId);
							}
						}
						
					}else
					{
						Iterator<Cell> cellIterator = row.cellIterator();
						Cell cell;
						while(cellIterator.hasNext())
						{
							cell = cellIterator.next();
							summaryDataFieldColumnMap.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
						}
					}
					count++;
				}
			}
		}
		System.out.println(s.size());
		PrintWriter pw = new PrintWriter("finalMissingSampleIds.txt");
		for(String s1 : missingSampleIds)
		{
			if(!s.contains(s1))
			{
				pw.println(s1);
			}
		}
		pw.close();
		myWorkBook.close();
		
	}
}
