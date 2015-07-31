package com.cgix.datafiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 
 * @author Ashish Jain
 * 
 * This file is being used to create the data mutation file
 * in the cbioportal format for importing the CGI gene mutation
 * data from the CGIX to cbioportal format.
 *
 */
public class CreateCbioportalFilesWithMetaData {

	/*public static final Map<String, String> Mutation_Type_Map = new HashMap<String, String>(){{
		put("missense_variant","missense_mutation");
		put("","nonsense_mutation");
		put("","nonstop_mutation");
		put("","frame_shift_del");
		put("splice_acceptor_variant","frame_shift_ins");
		put("frameshift_variant","in_frame_ins");
		put("frameshift_variant","in_frame_del");
		put("splice_region_variant","splice_site");
		put("","other");
	}};*/
	
	public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
		String metaDataFile,summarySheetFolder,databaseName,password,username,assay;
		metaDataFile = summarySheetFolder = databaseName = password = username = assay = null;
		for(int i=0;i<args.length;i++)
		{
			String argument = args[i];
			if(argument.contains("-metaDataFile="))
			{
				metaDataFile = argument.split("=")[1];
			}else if(argument.contains("-summarySheetsFolder="))
			{
				summarySheetFolder = argument.split("=")[1];
			}
			else if(argument.contains("-d="))
			{
				databaseName = argument.split("=")[1];
			}else if(argument.contains("-u="))
			{
				username = argument.split("=")[1];
			}else if(argument.contains("-p="))
			{
				password = argument.split("=")[1];
			}else if(argument.contains("-assay="))
			{
				assay = argument.split("=")[1];
			}
			
		}
		if(metaDataFile == null || summarySheetFolder == null || databaseName == null || password == null || username == null || assay == null)
		{
			System.err.println("Please give valid number of arguments.");
			System.exit(0);
		}
		//Reading the patient sample association file
		Map<String, Integer> summaryDataFieldColumnMap = new HashMap<String, Integer>();
		Map<String, Set<String>> cancerSampleMapping = new HashMap<String, Set<String>>();
		Map<String, Map<String, String>> sampleDataMapping = new HashMap<String, Map<String,String>>();
		Map<String, String> dataMapping;
		Set<String> sampleIdList;
		Set<String> sampleIdList5;
		
		Map<String, String> assayNameMapping = new HashMap<String, String>();
		Connection con = JDBCConnector.getConnection(databaseName, username, password);
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery("select * from assay");
		while(rs.next())
		{
			String assayId = rs.getString(1);
			String assayName = rs.getString(2);
			assayNameMapping.put(assayName, assayId);
		}
		rs.close();st.close();con.close();
		
		HashMap<String, Set<String>> studySampleMapping = new HashMap<String, Set<String>>();
		File myFile;
		FileInputStream fis;
		XSSFWorkbook myWorkBook;
		XSSFSheet mySheet;
		Iterator<Row> rowIterator;
		myFile = new File(/*"/media/cgix-ngs/bioinfo/ashish/Myeloid_patient_list.xlsx"*/metaDataFile);
		fis = new FileInputStream(myFile);
		myWorkBook = new XSSFWorkbook (fis);
		int count = 0;
		Map<String, String> samplePatientMapping = new HashMap<String, String>();
		int countOfSheets = myWorkBook.getNumberOfSheets();
		for(int i=0;i<countOfSheets;i++)
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
						
						if(row.getCell(summaryDataFieldColumnMap.get(Constants.PATIENT_ID)) != null && row.getCell(summaryDataFieldColumnMap.get(Constants.SAMPLE_ID)) !=null && !row.getCell(summaryDataFieldColumnMap.get(Constants.PATIENT_ID)).equals("") && !row.getCell(summaryDataFieldColumnMap.get(Constants.SAMPLE_ID)).equals("") && row.getCell(summaryDataFieldColumnMap.get(Constants.CANCER_STUDY)) !=null && !row.getCell(summaryDataFieldColumnMap.get(Constants.CANCER_STUDY)).equals(""))
						{
							String patientId;
							if(row.getCell(summaryDataFieldColumnMap.get(Constants.PATIENT_ID)).getCellType() == Cell.CELL_TYPE_NUMERIC)
							{
								Double patientId1 = row.getCell(summaryDataFieldColumnMap.get(Constants.PATIENT_ID)).getNumericCellValue();
								patientId = String.valueOf(patientId1.intValue());
							}else
							{
								patientId = String.valueOf(row.getCell(summaryDataFieldColumnMap.get(Constants.PATIENT_ID)).toString());
							}
							String sampleId = row.getCell(summaryDataFieldColumnMap.get(Constants.SAMPLE_ID)).toString().trim();
							String cancerStudy = row.getCell(summaryDataFieldColumnMap.get(Constants.CANCER_STUDY)).toString();
							String assayType = row.getCell(summaryDataFieldColumnMap.get(Constants.ASSAY_TYPE)).toString();
							String cancerType = row.getCell(summaryDataFieldColumnMap.get(Constants.TYPE_OF_CANCER)).toString();
							if(cancerSampleMapping.get(cancerType) != null)
							{
								sampleIdList = cancerSampleMapping.get(cancerType);
							}else
							{
								sampleIdList = new HashSet<String>();
							}
							sampleIdList.add(sampleId);
							cancerSampleMapping.put(cancerType, sampleIdList);//Cancer Sample Mapping
							samplePatientMapping.put(sampleId, patientId);//Patient Sample Mapping
							dataMapping = new HashMap<String, String>();
							dataMapping.put(Constants.ASSAY_TYPE, assayType);
							sampleDataMapping.put(sampleId, dataMapping);//Sample Meta Data mapping
							if(studySampleMapping.get(cancerStudy) != null)
							{
								sampleIdList5 = studySampleMapping.get(cancerStudy);
							}else
							{
								sampleIdList5 = new HashSet<String>();
							}
							sampleIdList5.add(sampleId);
							studySampleMapping.put(cancerStudy, sampleIdList5);//Study Sample Mapping
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
		myWorkBook.close();
		
		Map<String, Map<String, String>> dataMap = new HashMap<String, Map<String,String>>();
		Map<String, String> mappingMap;
		ArrayList<String> dataList = new ArrayList<String>(Arrays.asList(Constants.TUMOR_SAMPLE_BARCODE, Constants.HUGO_SYMBOL, Constants.CENTER,Constants.NCBI_BUILD,Constants.START_POSITION,Constants.VARIANT_CLASSIFICATION,Constants.VARIANT_TYPE,Constants.REFERENCE_ALLELE,Constants.TUMOR_SEQ_ALLELE_1,
				Constants.TUMOR_SEQ_ALLELE_2,Constants.DBSNP_RS,Constants.T_ALT_COUNT,Constants.T_REF_COUNT,Constants.CHROMOSOME,Constants.SEQUENCER,Constants.AMINO_ACID_CHANGE,Constants.MUTATION_STATUS,Constants.END_POSITION,Constants.COMMENTS,Constants.CGIX_SIGN_OUT_STATUS,Constants.CGIX_POLYPHEN_VALUE,Constants.CGIX_POLYPHEN_CALL,
				Constants.CGIX_SIFT_VALUE,Constants.CGIX_SIFT_CALL,Constants.CGIX_FINAL_ASSESMENT,Constants.VALIDATION_STATUS,Constants.VALIDATION_METHOD,Constants.CGIX_ASSAY_ID));
		//Reading from Final Mutation Summary Analysis File
		File dir = new File(/*"/media/cgix-ngs/bioinfo/ashish/myeloid-test/"*/summarySheetFolder);
		File [] files = dir.listFiles();
		//Sets to find faulty files and patients Ids not in meta file.
		Set<String> faultFiles = new HashSet<String>();
		Set<String> missingPatientsIds = new HashSet<String>();
		for(int i=0;i<files.length;i++)
		{
			if(files[i].isFile() && (files[i].getName().endsWith(".xlsx") || files[i].getName().endsWith(".xls")))
			{
				myFile = files[i];
				fis = new FileInputStream(myFile);
				myWorkBook = new XSSFWorkbook (fis);
				//System.out.println(myFile.getName());
				//Reading data from the RunSummary of the Excel file.
				mySheet = myWorkBook.getSheet(Constants.RUN_SUMMARY);
				rowIterator = mySheet.iterator();
				boolean getSequencer = true;
				String SEQUENCER = "";
				String RunId = "";
				boolean getHeader = true;
				while (rowIterator.hasNext()) {

					Row row = rowIterator.next();
					if(getSequencer && row.getCell(3) != null && row.getCell(3).toString() != "" && row.getCell(3).toString().startsWith(Constants.RUN_ID))
					{
						RunId = row.getCell(3).toString();
						SEQUENCER = row.getCell(3).toString().split("_")[1];
						if(SEQUENCER.startsWith("M"))
						{
							SEQUENCER = "MiSeq_"+SEQUENCER;
						}
						getSequencer = false;
					}
					Cell cell = row.getCell(0);
					Cell cell1 = row.getCell(1);
					Cell cell2 = row.getCell(2);
					//Condition to check the header in the file.
					if(getHeader && cell != null && cell1 != null && cell2!= null && cell.getStringCellValue().equals(Constants.SAMPLE) && cell1.getStringCellValue().equals(Constants.GENE) && cell2.getStringCellValue().equals(Constants.START_COORDINATE))
					{
						Iterator<Cell> cellIterator = row.cellIterator();
						while(cellIterator.hasNext())
						{
							cell = cellIterator.next();
							//System.out.println(cell.getStringCellValue());
							summaryDataFieldColumnMap.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
						}
						getHeader = false;
					}else
					{
						if(!getHeader)
						{
							if(row.getCell(summaryDataFieldColumnMap.get(Constants.SAMPLE)) != null && !row.getCell(summaryDataFieldColumnMap.get(Constants.SAMPLE)).toString().equals("") && !row.getCell(summaryDataFieldColumnMap.get(Constants.SAMPLE)).toString().trim().contains("Positive") && !row.getCell(summaryDataFieldColumnMap.get(Constants.SAMPLE)).toString().trim().startsWith("R"))
							{
								//Condition to get the negative samples from the summary sheet.
								if(row.getCell(1) != null && row.getCell(1).toString().equalsIgnoreCase("Negative"))
								{
									String sampleId = row.getCell(summaryDataFieldColumnMap.get(Constants.SAMPLE)).toString().trim();
									//Check for the errors.
									sampleId = checkSampleId(sampleId);
									dataMapping = sampleDataMapping.get(sampleId);
									if(dataMapping != null)
									{
										if(!RunId.equals(""))
										{
											dataMapping.put(Constants.RUN_ID, RunId.split(":")[1]);
											dataMapping.put(Constants.RUN_DATE,RunId.split(":")[1].split("_")[0]);
											sampleDataMapping.put(sampleId, dataMapping);
										}
									}else
									{
										//System.out.println("Not in patient file"+sampleId);
										missingPatientsIds.add(sampleId);
									}
								}

								//Condition to uniquely map the rows in the Run Summary sheet with the rows of Summary sheet
								if(row.getCell(summaryDataFieldColumnMap.get(Constants.START_COORDINATE)) != null && row.getCell(summaryDataFieldColumnMap.get(Constants.START_COORDINATE)).getCellType() == Cell.CELL_TYPE_NUMERIC && row.getCell(summaryDataFieldColumnMap.get(Constants.BASE_CHANGE)) != null)
								{
									Double coordinate = row.getCell(summaryDataFieldColumnMap.get(Constants.START_COORDINATE)).getNumericCellValue();
									int coordinate1 = coordinate.intValue();
									String sampleId = row.getCell(summaryDataFieldColumnMap.get(Constants.SAMPLE)).toString().trim();
									//Check for the errors.
									sampleId = checkSampleId(sampleId);
									String hgvSC = row.getCell(summaryDataFieldColumnMap.get(Constants.BASE_CHANGE)) == null?"":row.getCell(summaryDataFieldColumnMap.get(Constants.BASE_CHANGE)).toString().trim();
									String identifier = sampleId+":"+coordinate1+":"+hgvSC;
									mappingMap = new HashMap<String, String>();
									/*if(sampleId.equals("33412_002728"))
									System.out.println(sampleId+ " "+myFile.getName());*/
									mappingMap.put(Constants.TUMOR_SAMPLE_BARCODE,sampleId);
									if(row.getCell(summaryDataFieldColumnMap.get(Constants.CONFIRMATION)) != null && !row.getCell(summaryDataFieldColumnMap.get(Constants.CONFIRMATION)).toString().equals(""))
									{
										String validation = row.getCell(summaryDataFieldColumnMap.get(Constants.CONFIRMATION)).toString();
										mappingMap.put(Constants.VALIDATION_METHOD, validation.split("_")[0]);
										mappingMap.put(Constants.VALIDATION_STATUS, validation.split("_")[1]);
									}
									if(row.getCell(summaryDataFieldColumnMap.get(Constants.COMMENTS)) != null)
									{
										mappingMap.put(Constants.COMMENTS, row.getCell(summaryDataFieldColumnMap.get(Constants.COMMENTS)).toString());
									}
									if(row.getCell(summaryDataFieldColumnMap.get(Constants.FINAL_CALL)) != null)
									{
										mappingMap.put(Constants.CGIX_SIGN_OUT_STATUS, row.getCell(summaryDataFieldColumnMap.get(Constants.FINAL_CALL)).toString().trim());

									}
									if(row.getCell(summaryDataFieldColumnMap.get(Constants.FUNCTIONAL_IMPACT)) != null)
									{
										Cell localCell = row.getCell(summaryDataFieldColumnMap.get(Constants.FUNCTIONAL_IMPACT));
										/*if(!(localCell.toString().equals("Benign") || localCell.toString().equals("UNS") || localCell.toString().equals("Pathogenic") || localCell.toString().equals("")))
											faultFiles.add(myFile.getName());*/
										mappingMap.put(Constants.CGIX_FINAL_ASSESMENT, localCell.toString());
									}

									dataMapping = sampleDataMapping.get(sampleId);
									if(dataMapping != null)
									{
										if(!RunId.equals(""))
										{
											dataMapping.put(Constants.RUN_ID, RunId.split(":")[1]);
											dataMapping.put(Constants.RUN_DATE,RunId.split(":")[1].split("_")[0]);
											sampleDataMapping.put(sampleId, dataMapping);
										}
									}
									dataMap.put(identifier, mappingMap);
								}
							}
						}

					}
				}
				for(Entry<String,Map<String, String>> entry : dataMap.entrySet())
				{
					mappingMap = entry.getValue();
					mappingMap.put(Constants.SEQUENCER, SEQUENCER);
				}
				
				//Reading data from the SummarySheet of the Excel file.
				mySheet = myWorkBook.getSheet("SummarySheet");
				rowIterator = mySheet.iterator();
				count = 0;
				Map<String, Integer> summaryDataFieldColumnMapping = new HashMap<String, Integer>();

				// Traversing over each row of XLSX file
				//Set<String> sampleList = new HashSet<String>();
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					// For each row, iterate through each columns
					if(count !=0)
					{
						if(row.getCell(summaryDataFieldColumnMapping.get(Constants.SAMPLE)) != null)
						{
							String sampleId = row.getCell(summaryDataFieldColumnMapping.get(Constants.SAMPLE)).toString().trim();
							String variantClassification = row.getCell(summaryDataFieldColumnMapping.get(Constants.VARIANT_CONSEQUENCE)) == null?"":row.getCell(summaryDataFieldColumnMapping.get(Constants.VARIANT_CONSEQUENCE)).toString();
							/*if(!sampleId.equalsIgnoreCase("Positive") && !sampleId.startsWith("R") && !variantClassification.contains("intron"))//Check for Controls and Repeats. 
							{*/
							String geneName = row.getCell(summaryDataFieldColumnMapping.get(Constants.GENE_NAME)).toString();
							Double startCoordinate = row.getCell(summaryDataFieldColumnMapping.get(Constants.START_COORDINATE)).getNumericCellValue();
							int startCoordinate1 = startCoordinate.intValue();
							int stopCoordinate = 0;
							if(summaryDataFieldColumnMapping.get(Constants.VARIANT_LENGTH) != null)
							{
								Double variantLength = row.getCell(summaryDataFieldColumnMapping.get(Constants.VARIANT_LENGTH)).getNumericCellValue();
								stopCoordinate = startCoordinate1 + variantLength.intValue();
							}
							String geneMutationData = row.getCell(summaryDataFieldColumnMapping.get(Constants.VARIANT)).toString();
							Double readDepth1 = row.getCell(summaryDataFieldColumnMapping.get(Constants.READ_DEPTH)).getNumericCellValue();
							int readDepth = readDepth1.intValue();
							Double altReadDepth1 = row.getCell(summaryDataFieldColumnMapping.get(Constants.ALT_READ_DEPTH)).getNumericCellValue();
							int altReadDepth = altReadDepth1.intValue();
							String chromosome = "";
							try {
								Double chrom = row.getCell(summaryDataFieldColumnMapping.get(Constants.CHR)).getNumericCellValue();
								chromosome = String.valueOf(chrom.intValue());
							} catch (IllegalStateException e) {
								chromosome = row.getCell(summaryDataFieldColumnMapping.get(Constants.CHR)).toString();
							}
							String variantType = row.getCell(summaryDataFieldColumnMapping.get(Constants.VARIANT_TYPE_CGI)).toString();
							String dbSNP = "";
							if(row.getCell(summaryDataFieldColumnMapping.get(Constants.DBSNP)) != null)
							{
								dbSNP = row.getCell(summaryDataFieldColumnMapping.get(Constants.DBSNP)).toString();
							}
							String siftValue,polyphenValue;
							siftValue = polyphenValue = "";
							if(row.getCell(summaryDataFieldColumnMapping.get(Constants.SIFT)) != null)
							{
								siftValue = row.getCell(summaryDataFieldColumnMapping.get(Constants.SIFT)).toString();
							}
							if(row.getCell(summaryDataFieldColumnMapping.get(Constants.POLYPHEN)) != null)
							{
								polyphenValue = row.getCell(summaryDataFieldColumnMapping.get(Constants.POLYPHEN)).toString();
							}
							//Check for the errors.
							sampleId = checkSampleId(sampleId);
							String hgvSC = row.getCell(summaryDataFieldColumnMapping.get(Constants.BASE_CHANGE)) == null?"":row.getCell(summaryDataFieldColumnMapping.get(Constants.BASE_CHANGE)).toString().trim();
							String identifier = sampleId+":"+startCoordinate1+":"+hgvSC;
							mappingMap = dataMap.get(identifier);
							if(mappingMap != null)
							{
								//Check for the errors.
								sampleId = checkSampleId(sampleId);
								//sampleList.add(sampleId);
								mappingMap.put(Constants.CGIX_ASSAY_ID,assayNameMapping.get(assay/*"FocusCLL"*/));
								//mappingMap.put(Constants.TUMOR_SAMPLE_BARCODE,sampleId);
								mappingMap.put(Constants.HUGO_SYMBOL,geneName);
								mappingMap.put(Constants.CENTER,Constants.CENTER_CGI);
								mappingMap.put(Constants.NCBI_BUILD,Constants.NCBI_BUILD_CGI);
								mappingMap.put(Constants.START_POSITION, String.valueOf(startCoordinate1));
								mappingMap.put(Constants.VARIANT_CLASSIFICATION,variantClassification);
								mappingMap.put(Constants.VARIANT_TYPE,variantType);
								mappingMap.put(Constants.REFERENCE_ALLELE, geneMutationData.split(">")[0]);
								mappingMap.put(Constants.TUMOR_SEQ_ALLELE_1, geneMutationData.split(">")[1].split("/")[0]);
								mappingMap.put(Constants.TUMOR_SEQ_ALLELE_2, geneMutationData.split(">")[1].split("/")[1]);
								mappingMap.put(Constants.DBSNP_RS, dbSNP);
								mappingMap.put(Constants.T_ALT_COUNT, String.valueOf(altReadDepth));
								mappingMap.put(Constants.T_REF_COUNT, String.valueOf(readDepth-altReadDepth));
								mappingMap.put(Constants.CHROMOSOME, String.valueOf(chromosome));
								if(summaryDataFieldColumnMapping.get(Constants.VARIANT_LENGTH) != null)
								{
									mappingMap.put(Constants.END_POSITION, String.valueOf(stopCoordinate));
								}
								mappingMap.put(Constants.MUTATION_STATUS, "Somatic");//TODO
								if(polyphenValue != null && !polyphenValue.equals(""))
								{
									mappingMap.put(Constants.CGIX_POLYPHEN_VALUE, polyphenValue.split("\\(")[1].split("\\)")[0]);
									mappingMap.put(Constants.CGIX_POLYPHEN_CALL, polyphenValue.split("\\(")[0]);
								}
								if(siftValue != null && !siftValue.equals(""))
								{
									mappingMap.put(Constants.CGIX_SIFT_VALUE, siftValue.split("\\(")[1].split("\\)")[0]);
									mappingMap.put(Constants.CGIX_SIFT_CALL, siftValue.split("\\(")[0]);
								}
								if(row.getCell(summaryDataFieldColumnMapping.get(Constants.AMINO_ACID_CHANGE_CGIX)) != null)
								{
									String HGVs = row.getCell(summaryDataFieldColumnMapping.get(Constants.AMINO_ACID_CHANGE_CGIX)).toString();
									mappingMap.put(Constants.AMINO_ACID_CHANGE, Convert3to1AA.convert3aaTo1(HGVs));
								}else
								{
									String HGVs = "";
									mappingMap.put(Constants.AMINO_ACID_CHANGE, Convert3to1AA.convert3aaTo1(HGVs));
								}
							}
							//dataMap.put(sampleId.trim()+":"+startCoordinate1+":"+row.getCell(summaryDataFieldColumnMapping.get(Constants.BASE_CHANGE)).toString().trim(), mappingMap);
							//}
						}
					}else
					{
						Iterator<Cell> cellIterator = row.cellIterator();
						Cell cell;
						while(cellIterator.hasNext())
						{
							cell = cellIterator.next();
							summaryDataFieldColumnMapping.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
						}

					}
					count++;
				}

				
				myWorkBook.close();
			}
		}
		//Printing the data to the cbioportal data files
		List<String> sampleAssayMappingList = new ArrayList<String>();
		PrintWriter pw;
		for(Entry<String, Set<String>> e : cancerSampleMapping.entrySet())
		{
			Set<String> sampleIdListIncancer = e.getValue();
			String cancerCode = e.getKey();
			for(Entry<String, Set<String>> e1 : studySampleMapping.entrySet())
			{
				Set<String> studySampleIdList = e1.getValue();
				String cancerStudy = e1.getKey();
				String folderName = Constants.CBIOPORTAL+"/"+cancerCode+"/"+cancerStudy;
				File folder = new File(Constants.CBIOPORTAL);
				if(!folder.exists())
				{
					folder.mkdir();
				}
				folder = new File(Constants.CBIOPORTAL+"/"+cancerCode);
				if(!folder.exists())
				{
					folder.mkdir();
				}
				folder = new File(Constants.CBIOPORTAL+"/"+cancerCode+"/"+cancerStudy);
				if(!folder.exists())
				{
					folder.mkdir();
				}
				folder = new File(Constants.CBIOPORTAL+"/"+cancerCode+"/"+cancerStudy+"/case_lists");
				if(!folder.exists())
				{
					folder.mkdir();
				}
				con = JDBCConnector.getConnection(databaseName, username, password);
				st = con.createStatement();
				rs = st.executeQuery("select * from type_of_cancer where type_of_cancer_id=\""+cancerCode+"\"");
				rs.next();
				//Printing to Meta Study file
				pw = new PrintWriter(folderName+"/meta_study.txt");
				String cancerIdentifier = cancerCode+"_"+cancerStudy;
				pw.println("type_of_cancer:"+ cancerCode);
				pw.println("cancer_study_identifier:"+cancerIdentifier);
				pw.println("name: "+rs.getString(2)+"("+cancerStudy+")");
				pw.println("description: "+rs.getString(2)+"("+cancerStudy+")");//Need to give this
				pw.println("dedicated_color: "+rs.getString(4));
				pw.println("short_name: "+rs.getString(5));
				pw.close();
				rs.close();st.close();con.close();

				//Printing to Meta Mutation File
				pw = new PrintWriter(folderName+"/meta_mutations_extended.txt");
				pw.println("cancer_study_identifier: "+ cancerIdentifier);
				pw.println("genetic_alteration_type: MUTATION_EXTENDED");
				pw.println("datatype: MAF");
				pw.println("stable_id: "+cancerIdentifier+"_mutations");
				pw.println("show_profile_in_analysis_tab: true");
				pw.println("profile_description: Mutation data from whole exome sequencing.");
				pw.println("profile_name: Mutations");
				pw.close();

				//Printing Data to the Mutation Data File
				pw = new PrintWriter(folderName+"/data_mutations_extended.txt");
				for (String key : dataList) { 
					pw.print(key+"\t");
				}
				pw.println();
				for(Entry<String,Map<String, String>> entry : dataMap.entrySet())
				{
					String sampleId = entry.getKey().split(":")[0];
					//Check for errors
					sampleId = checkSampleId(sampleId);
					if(sampleIdListIncancer.contains(sampleId))//Check for Sample in Patient Sample mapping File
					{
						if(studySampleIdList.contains(sampleId))//Check for sample in a cancer
						{
							mappingMap = entry.getValue();
							//Check for the positive variant cases
							/*if((mappingMap.get(Constants.CGIX_SIGN_OUT_STATUS) != null && mappingMap.get(Constants.CGIX_SIGN_OUT_STATUS).equals("Positive")) || (mappingMap.get(Constants.VALIDATION_STATUS) != null && mappingMap.get(Constants.VALIDATION_STATUS).equals("Valid")))
							{*/
								for (String key : dataList) { 
									pw.print((mappingMap.get(key) == null?"":mappingMap.get(key))+"\t");
									/*if(key.equals(Constants.CGIX_SIGN_OUT_STATUS) && (mappingMap.get(key) == null || mappingMap.get(key).equals("")))
									{
										System.out.println(mappingMap.get(Constants.TUMOR_SAMPLE_BARCODE));
									}*/
								}
								pw.println();
							/*}*/

						}
					}else{
						//System.out.println("Sample Id not in Patient Sample mapping File "+sampleId);
						missingPatientsIds.add(sampleId);
					}
				}
				pw.close();

				//Printing to Meta Clinical File
				pw = new PrintWriter(folderName+"/meta_clinical.txt");
				pw.println("cancer_study_identifier: "+ cancerIdentifier);
				pw.println("genetic_alteration_type: CLINICAL");
				pw.println("datatype: ;:FREE-FORM");
				pw.println("stable_id: "+cancerIdentifier+"_clinical");
				pw.println("show_profile_in_analysis_tab: false");
				pw.println("profile_description: Sample clinical data for this study.");
				pw.println("profile_name: Clinical data for CGIX");
				pw.close();

				//Printing to Data Clinical File
				Set<String> patientList = new HashSet<String>();
				pw = new PrintWriter(folderName+"/data_clinical.txt");
				pw.println("#Patient Identifier\t#Sample Identifier");
				pw.println("#Patient Identifier\t#Sample Identifier");
				pw.println("#STRING\t#STRING");
				pw.println("#PATIENT\tSAMPLE");
				pw.println("#1\t1");
				pw.println("PATIENT_ID\tSAMPLE_ID");
				count = 0;
				for(String s:studySampleIdList)
				{
					if(sampleIdListIncancer.contains(s))
					{
						s = checkSampleId(s);
						if(samplePatientMapping.get(s)!=null)
						{
							pw.println(samplePatientMapping.get(s)+"\t"+s);
							patientList.add(samplePatientMapping.get(s));
						}else
						{
							pw.println(s+"\t"+s);
							patientList.add(s);
						}
						sampleAssayMappingList.add(s);
					}
				}
				pw.close();
				
				//Printing to Patient Id File List
				pw = new PrintWriter(folderName+"/case_lists"+"/cases_all.txt");
				pw.println("cancer_study_identifier: "+ cancerIdentifier);
				pw.println("stable_id: "+cancerIdentifier+"_all");
				pw.println("case_list_name: All Tumors");
				pw.println("case_list_description: Patient List ("+patientList.size()+" patients)");
				pw.print("case_list_ids:");
				for (String s : patientList) { 
					pw.print(s+"\t");
				}
				pw.println();
				pw.close();
			}
		}
		/*System.out.println(sampleDataMapping.size());
		pw = new PrintWriter("faultyfiles.txt");
		for(String s : faultFiles)
		{
			pw.println(s);
		}
		pw.close();*/
		
		/*pw = new PrintWriter("missingSampleIds.txt");
		for(String s : missingPatientsIds)
		{
			if(!s.startsWith("R") && !s.contains("Positive"))
			{
				pw.println(s);
			}
		}
		pw.close();*/
		pw = new PrintWriter("sample_assay_mapping.txt");
		pw.println("SampleId\tAssayType\tRunDate\tRunId");
		for(String s:sampleAssayMappingList) {
			String sampleId = s.trim();
			sampleId = checkSampleId(sampleId);
			dataMapping = sampleDataMapping.get(sampleId);
			pw.println(sampleId+"\t"+dataMapping.get(Constants.ASSAY_TYPE)+"\t"+(dataMapping.get(Constants.RUN_DATE) == null?"":dataMapping.get(Constants.RUN_DATE))+"\t"+(dataMapping.get(Constants.RUN_ID)==null?"":dataMapping.get(Constants.RUN_ID)));
		}
		pw.close();
	}
	
	public static String checkSampleId(String sampleId)
	{
		if(sampleId.contains(".txt"))
		{
			sampleId = sampleId.split("\\.txt")[0];
		}
		/*if(!sampleId.startsWith("M") && sampleId.contains("-"))
		{
			sampleId = sampleId.replace("-", "_");
		}*/
		return sampleId;
	}
}
