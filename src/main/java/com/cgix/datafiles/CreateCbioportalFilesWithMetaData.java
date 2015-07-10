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

	public static String CENTER = "CGI_Rutherford";
	public static String NCBI_BUILD = "GRCh37";
	//public static String cancercode = "CLL";
	public static String year = "2015";
	public static String CBIOPORTAL = "cbioportal";
	//public static String SEQUENCER = "MiSeq_M00177";
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
		myFile = new File(/*"/home/ashish/Downloads/Gilead_0123_Sample_list_for_NGS_new_copy.xlsx"*/metaDataFile);
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
						
						if(row.getCell(summaryDataFieldColumnMap.get("Patient ID")) != null && row.getCell(summaryDataFieldColumnMap.get("Sample ID")) !=null && !row.getCell(summaryDataFieldColumnMap.get("Patient ID")).equals("") && !row.getCell(summaryDataFieldColumnMap.get("Sample ID")).equals("") && row.getCell(summaryDataFieldColumnMap.get("Cancer Study")) !=null && !row.getCell(summaryDataFieldColumnMap.get("Cancer Study")).equals(""))
						{
							String patientId;
							if(row.getCell(summaryDataFieldColumnMap.get("Patient ID")).getCellType() == Cell.CELL_TYPE_NUMERIC)
							{
								Double patientId1 = row.getCell(summaryDataFieldColumnMap.get("Patient ID")).getNumericCellValue();
								patientId = String.valueOf(patientId1.intValue());
							}else
							{
								patientId = String.valueOf(row.getCell(summaryDataFieldColumnMap.get("Patient ID")).toString());
							}
							String sampleId = row.getCell(summaryDataFieldColumnMap.get("Sample ID")).toString();
							String cancerStudy = row.getCell(summaryDataFieldColumnMap.get("Cancer Study")).toString();
							String assayType = row.getCell(summaryDataFieldColumnMap.get("Assay Type")).toString();
							String cancerType = row.getCell(summaryDataFieldColumnMap.get("Type of cancer")).toString();
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
							dataMapping.put("Assay Type", assayType);
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
		ArrayList<String> dataList = new ArrayList<String>(Arrays.asList("Tumor_Sample_Barcode", "Hugo_Symbol", "Center","NCBI_Build","Start_Position","Variant_Classification","Variant_Type","Reference_Allele","Tumor_Seq_Allele1",
				"Tumor_Seq_Allele2","dbSNP_RS","t_alt_count","t_ref_count","Chromosome","Sequencer","Amino_Acid_Change","Mutation_Status","End_Position","Comments","Sign_Out_Status","Polphen_value","Polphen_call",
				"Sift_value","Sift_call","Final_Assesment","Validation_Status","Validation_Method","Assay_Id"));
		//String excelFileName = args[0];
		//Reading from Final Mutation Summary Analysis File
		File dir = new File(/*"/home/ashish/Downloads/CGIX_CLL_Summary_Data"*/summarySheetFolder);
		File [] files = dir.listFiles();
		//Sets to find faulty files and patients Ids not in meta file.
		Set<String> faultFiles = new HashSet<String>();
		Set<String> missingPatientsIds = new HashSet<String>();
		for(int i=0;i<files.length;i++)
		{
			if(files[i].isFile())
			{
				myFile = /*new File("/home/ashish/Downloads/1-4-15_Analysis Summary Sheet_011415.xlsx"excelFileName)*/files[i];
				//System.out.println(myFile.getName());
				fis = new FileInputStream(myFile);
				myWorkBook = new XSSFWorkbook (fis);
				//Reading data from the SummarySheet of the Excel file.
				mySheet = myWorkBook.getSheet("SummarySheet");
				rowIterator = mySheet.iterator();
				count = 0;
				Map<String, Integer> summaryDataFieldColumnMapping = new HashMap<String, Integer>();

				// Traversing over each row of XLSX file
				Set<String> sampleList = new HashSet<String>();
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					// For each row, iterate through each columns
					if(count !=0)
					{
						//int length = row.getLastCellNum();
						if(row.getCell(summaryDataFieldColumnMapping.get("Sample")) != null)
						{
							String sampleId = row.getCell(summaryDataFieldColumnMapping.get("Sample")).toString();
							if(!sampleId.equalsIgnoreCase("Positive") && !sampleId.startsWith("R"))//Check for Controls and Repeats. 
							{
								String geneName = row.getCell(summaryDataFieldColumnMapping.get("Gene_0")).toString();
								Double startCoordinate = row.getCell(summaryDataFieldColumnMapping.get("Coordinate_3")).getNumericCellValue();
								int startCoordinate1 = startCoordinate.intValue();
								int stopCoordinate = 0;
								if(summaryDataFieldColumnMapping.get("Variant Length_4") != null)
								{
									Double variantLength = row.getCell(summaryDataFieldColumnMapping.get("Variant Length_4")).getNumericCellValue();
									stopCoordinate = startCoordinate1 + variantLength.intValue();
								}
								String geneMutationData = row.getCell(summaryDataFieldColumnMapping.get("Variant_1")).toString();
								Double readDepth1 = row.getCell(summaryDataFieldColumnMapping.get("Read Depth_14")).getNumericCellValue();
								int readDepth = readDepth1.intValue();
								Double altReadDepth1 = row.getCell(summaryDataFieldColumnMapping.get("Alt Read Depth_15")).getNumericCellValue();
								int altReadDepth = altReadDepth1.intValue();
								String variantClassification = row.getCell(summaryDataFieldColumnMapping.get("Consequence_29")).toString();
								Double chrom = row.getCell(summaryDataFieldColumnMapping.get("Chr_2")).getNumericCellValue();
								int chromosome = chrom.intValue();
								String variantType = row.getCell(summaryDataFieldColumnMapping.get("Type_5")).toString();
								String dbSNP = "";
								if(row.getCell(summaryDataFieldColumnMapping.get("dbSNP ID_45")) != null)
								{
									dbSNP = row.getCell(summaryDataFieldColumnMapping.get("dbSNP ID_45")).toString();
								}
								String siftValue,polyphenValue;
								siftValue = polyphenValue = "";
								if(row.getCell(summaryDataFieldColumnMapping.get("Sift_40")) != null)
								{
									siftValue = row.getCell(summaryDataFieldColumnMapping.get("Sift_40")).toString();
								}
								if(row.getCell(summaryDataFieldColumnMapping.get("PolyPhen_41")) != null)
								{
									polyphenValue = row.getCell(summaryDataFieldColumnMapping.get("PolyPhen_41")).toString();
								}

								mappingMap = new HashMap<String, String>();
								//Check for the errors.
								sampleId = checkSampleId(sampleId);
								sampleList.add(sampleId);
								mappingMap.put("Assay_Id",assayNameMapping.get(assay/*"FocusCLL"*/));
								mappingMap.put("Tumor_Sample_Barcode",sampleId);
								mappingMap.put("Hugo_Symbol",geneName);
								mappingMap.put("Center",CENTER);
								mappingMap.put("NCBI_Build",NCBI_BUILD);
								mappingMap.put("Start_Position", String.valueOf(startCoordinate1));
								mappingMap.put("Variant_Classification",variantClassification);
								mappingMap.put("Variant_Type",variantType);
								mappingMap.put("Reference_Allele", geneMutationData.split(">")[0]);
								mappingMap.put("Tumor_Seq_Allele1", geneMutationData.split(">")[1].split("/")[0]);
								mappingMap.put("Tumor_Seq_Allele2", geneMutationData.split(">")[1].split("/")[1]);
								mappingMap.put("dbSNP_RS", dbSNP);
								mappingMap.put("t_alt_count", String.valueOf(altReadDepth));
								mappingMap.put("t_ref_count", String.valueOf(readDepth-altReadDepth));
								mappingMap.put("Chromosome", String.valueOf(chromosome));
								if(summaryDataFieldColumnMapping.get("Variant Length_4") != null)
								{
									mappingMap.put("End_Position", String.valueOf(stopCoordinate));
								}
								mappingMap.put("Mutation_Status", "Somatic");//TODO
								if(polyphenValue != null && !polyphenValue.equals(""))
								{
									mappingMap.put("Polphen_value", polyphenValue.split("\\(")[1].split("\\)")[0]);
									mappingMap.put("Polphen_call", polyphenValue.split("\\(")[0]);
								}
								if(siftValue != null && !siftValue.equals(""))
								{
									mappingMap.put("Sift_value", siftValue.split("\\(")[1].split("\\)")[0]);
									mappingMap.put("Sift_call", siftValue.split("\\(")[0]);
								}
								/*if(cosmicId != null && !cosmicId.equals(""))
								{
									cosmicId = processCosmicId(cosmicId);
									mappingMap.put("keyword", cosmicId);
								}*/

								if(row.getCell(summaryDataFieldColumnMapping.get("HGVSp_44")) != null)
								{
									String HGVs = row.getCell(summaryDataFieldColumnMapping.get("HGVSp_44")).toString();
									mappingMap.put("Amino_Acid_Change", Convert3to1AA.convert3aaTo1(HGVs));
								}else
								{
									String HGVs = "";
									mappingMap.put("Amino_Acid_Change", Convert3to1AA.convert3aaTo1(HGVs));
								}
								dataMap.put(sampleId.trim()+":"+startCoordinate1+":"+row.getCell(summaryDataFieldColumnMapping.get("HGVSc_43")).toString().trim(), mappingMap);
							}
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

				//Reading data from the RunSummary of the Excel file.
				mySheet = myWorkBook.getSheet("RunSummary");
				rowIterator = mySheet.iterator();
				boolean getSequencer = true;
				String SEQUENCER = "";
				String RunId = "";
				boolean getHeader = true;
				while (rowIterator.hasNext()) {

					Row row = rowIterator.next();
					if(getSequencer && row.getCell(3) != null && row.getCell(3).toString() != "" && row.getCell(3).toString().startsWith("Run ID:"))
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
					if(getHeader && cell != null && cell1 != null && cell2!= null && cell.getStringCellValue().equals("Sample") && cell1.getStringCellValue().equals("Gene") && cell2.getStringCellValue().equals("Coordinate_3"))
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
							//Condition to get the negative samples from the summary sheet.
							if(row.getCell(0) != null && !row.getCell(0).toString().equals("") && row.getCell(1) != null && row.getCell(1).toString().equalsIgnoreCase("Negative"))
							{
								String sampleId = row.getCell(0).toString().trim();
								//Check for the errors.
								sampleId = checkSampleId(sampleId);
								dataMapping = sampleDataMapping.get(sampleId);
								if(dataMapping != null)
								{
									if(!RunId.equals(""))
									{
										dataMapping.put("Run Id", RunId.split(":")[1]);
										dataMapping.put("Run Date",RunId.split(":")[1].split("_")[0]);
										sampleDataMapping.put(sampleId, dataMapping);
									}
								}else
								{
									//System.out.println("Not in patient file"+sampleId);
									missingPatientsIds.add(sampleId);
								}
							}

							//Condition to uniquely map the rows in the Run Summary sheet with the rows of Summary sheet
							if(row.getCell(summaryDataFieldColumnMap.get("Sample")) != null && row.getCell(summaryDataFieldColumnMap.get("Coordinate_3")) != null && row.getCell(summaryDataFieldColumnMap.get("Coordinate_3")).getCellType() == Cell.CELL_TYPE_NUMERIC && row.getCell(summaryDataFieldColumnMap.get("HGVSc_43")) != null)
							{
								Double coordinate = row.getCell(summaryDataFieldColumnMap.get("Coordinate_3")).getNumericCellValue();
								int coordinate1 = coordinate.intValue();
								String sampleId = row.getCell(summaryDataFieldColumnMap.get("Sample")).toString().trim();
								String identifier = sampleId+":"+coordinate1+":"+row.getCell(summaryDataFieldColumnMap.get("HGVSc_43")).toString().trim();
								mappingMap = dataMap.get(identifier);
								if(mappingMap != null)
								{
									if(row.getCell(summaryDataFieldColumnMap.get("Confirmation")) != null && !row.getCell(summaryDataFieldColumnMap.get("Confirmation")).toString().equals(""))
									{
										String validation = row.getCell(summaryDataFieldColumnMap.get("Confirmation")).toString();
										mappingMap.put("Validation_Method", validation.split("_")[0]);
										mappingMap.put("Validation_Status", validation.split("_")[1]);
									}
									if(row.getCell(summaryDataFieldColumnMap.get("Comments")) != null)
									{
										mappingMap.put("Comments", row.getCell(summaryDataFieldColumnMap.get("Comments")).toString());
									}
									if(row.getCell(summaryDataFieldColumnMap.get("FINAL CALL")) != null)
									{
										mappingMap.put("Sign_Out_Status", row.getCell(summaryDataFieldColumnMap.get("FINAL CALL")).toString());
									}
									if(row.getCell(summaryDataFieldColumnMap.get("Functional Impact")) != null)
									{
										Cell localCell = row.getCell(summaryDataFieldColumnMap.get("Functional Impact"));
										/*if(!(localCell.toString().equals("Benign") || localCell.toString().equals("UNS") || localCell.toString().equals("Pathogenic") || localCell.toString().equals("")))
											faultFiles.add(myFile.getName());*/
										mappingMap.put("Final_Assesment", localCell.toString());
									}
								}
								//Check for the errors.
								sampleId = checkSampleId(sampleId);
								dataMapping = sampleDataMapping.get(sampleId);
								if(dataMapping != null)
								{
									if(!RunId.equals(""))
									{
										dataMapping.put("Run Id", RunId.split(":")[1]);
										dataMapping.put("Run Date",RunId.split(":")[1].split("_")[0]);
										sampleDataMapping.put(sampleId, dataMapping);
									}
								}else
								{
									//System.out.println("Not in patient file"+sampleId);
									missingPatientsIds.add(sampleId);
								}

							}
						}

					}
				}
				for(Entry<String,Map<String, String>> entry : dataMap.entrySet())
				{
					mappingMap = entry.getValue();
					mappingMap.put("Sequencer", SEQUENCER);
				}
				myWorkBook.close();
			}
		}
		//Printing the data to the cbioportal data files
		List<String> sampleAssayMappingList = new ArrayList<String>();
		PrintWriter pw;
		for(Entry<String, Set<String>> e1 : studySampleMapping.entrySet())
		{
			Set<String> studySampleIdList = e1.getValue();
			String cancerStudy = e1.getKey();
			for(Entry<String, Set<String>> e : cancerSampleMapping.entrySet())
			{
				String cancerCode = e.getKey();
				String folderName = CBIOPORTAL+"/"+cancerStudy+"/"+cancerCode;
				File folder = new File(CBIOPORTAL);
				if(!folder.exists())
				{
					folder.mkdir();
				}
				folder = new File(CBIOPORTAL+"/"+cancerStudy);
				if(!folder.exists())
				{
					folder.mkdir();
				}
				folder = new File(CBIOPORTAL+"/"+cancerStudy+"/"+cancerCode);
				if(!folder.exists())
				{
					folder.mkdir();
				}
				Set<String> sampleIdListIncancer = e.getValue();
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
					if(studySampleIdList.contains(sampleId))//Check for Sample in Patient Sample mapping File
					{
						if(sampleIdListIncancer.contains(sampleId))
						{
							mappingMap = entry.getValue();
							/*//Check for the positive variant cases
							if((mappingMap.get("Sign_Out_Status") != null && mappingMap.get("Sign_Out_Status").equals("Positive")) || (mappingMap.get("Validation_Status") != null && mappingMap.get("Validation_Status").equals("Valid")))
							{*/
								for (String key : dataList) { 
									pw.print((mappingMap.get(key) == null?"":mappingMap.get(key))+"\t");
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
				for(String s:sampleIdListIncancer)
				{
					if(studySampleIdList.contains(s))
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
				pw = new PrintWriter(folderName+"/cases_all.txt");
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
			pw.println(sampleId+"\t"+dataMapping.get("Assay Type")+"\t"+(dataMapping.get("Run Date") == null?"":dataMapping.get("Run Date"))+"\t"+(dataMapping.get("Run Id")==null?"":dataMapping.get("Run Id")));
		}
		pw.close();
	}
	
	public static String checkSampleId(String sampleId)
	{
		if(sampleId.contains(".txt"))
		{
			sampleId = sampleId.split("\\.txt")[0];
		}
		if(!sampleId.startsWith("M") && sampleId.contains("-"))
		{
			sampleId = sampleId.replace("-", "_");
		}
		return sampleId;
	}
	
	private static String processCosmicId(String cosmicId)
	{
		cosmicId = cosmicId.replace("COSM", "");
		cosmicId = cosmicId.replace(";", ",");
		return cosmicId;
	}
	
}
