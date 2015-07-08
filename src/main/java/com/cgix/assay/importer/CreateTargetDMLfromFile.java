package com.cgix.assay.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CreateTargetDMLfromFile {

	public static void main(String[] args) throws IOException {
		/*BufferedReader br = new BufferedReader(new FileReader("/home/ashish/Downloads/MyeloidPanelAmplicons_coordinates_locations2.txt"));
		String line = br.readLine();
		line = br.readLine();
		String assayCode = "MYL";
		PrintWriter pw = new PrintWriter("insert-"+assayCode+"-target-dml.sql");
		int count = 0;
		while(line != null)
		{
			count++;
			String lineData[] = line.split("\t");
			String amplicon = lineData[0].trim();
			String targetRegion = lineData[1].trim();
			String geneName = targetRegion.split(" ")[0];
			String chromosome = targetRegion.split(" ")[1].split(":")[0].split("chr")[1];
			Integer startSite = Integer.valueOf(targetRegion.split(" ")[1].split(":")[1].split("-")[0]);
			Integer stopSite = Integer.valueOf(targetRegion.split(" ")[1].split(":")[1].split("-")[1].split("\\)")[0]);
			pw.println("insert into target (INTERNAL_ID,CHROMOSOME,TARGET_START,TARGET_STOP,DESCRIPTION,GENE_NAME) values ("+count+",\""+chromosome+"\","+startSite+","+stopSite+",\""+amplicon+"\",\""+geneName+"\");");
			line = br.readLine();
		}
		for(int i=1;i<=count;i++)
		{
			pw.println("insert into assay_target values (\""+assayCode+"\","+i+");");
		}
		br.close();
		pw.close();*/
		File myFile;
		FileInputStream fis;
		XSSFWorkbook myWorkBook;
		XSSFSheet mySheet;
		Iterator<Row> rowIterator;
		String assayCode = "CLL";
		PrintWriter pw = new PrintWriter("insert-"+assayCode+"-target-dml.sql");
		myFile = new File("/home/ashish/Downloads/CLL_TargetFile.xlsx");
		fis = new FileInputStream(myFile);
		myWorkBook = new XSSFWorkbook (fis);
		mySheet = myWorkBook.getSheetAt(0);
		int count = 0;
		int index = 574;
		if(mySheet != null)
		{
			rowIterator = mySheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				if(count != 0)
				{
					String amplicon = row.getCell(0).getStringCellValue();
					//String desc = row.getCell(6).getStringCellValue();
					//System.out.println(amplicon);
					String geneName = row.getCell(6).getStringCellValue();
					String chromosome = row.getCell(1).getStringCellValue().split("chr")[1];
					Integer startSite = Double.valueOf(row.getCell(2).getNumericCellValue()).intValue();
					Integer stopSite = Double.valueOf(row.getCell(3).getNumericCellValue()).intValue();
					pw.println("insert into target (INTERNAL_ID,CHROMOSOME,TARGET_START,TARGET_STOP,DESCRIPTION,GENE_NAME) values ("+index+",\""+chromosome+"\","+startSite+","+stopSite+",\""+amplicon+"\",\""+geneName+"\");");
					index++;
				}
				count++;
			}
		}
		//System.out.println(count);
		for(int i=(index-count+1);i<index;i++)
		{
			pw.println("insert into assay_target values (\""+assayCode+"\","+i+");");
		}
		myWorkBook.close();
		pw.close();

	}
}
