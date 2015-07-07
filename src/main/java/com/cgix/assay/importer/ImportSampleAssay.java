package com.cgix.assay.importer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import com.cgix.datafiles.JDBCConnector;
/**
 * 
 * @author Ashish Jain
 *
 */
public class ImportSampleAssay {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException, SQLException {
		String fileName,databaseName,password,username;
		fileName = databaseName = password = username = null;
		for(int i=0;i<args.length;i++)
		{
			String argument = args[i];
			if(argument.contains("-f="))
			{
				fileName = argument.split("=")[1];
			}else if(argument.contains("-d="))
			{
				databaseName = argument.split("=")[1];
			}else if(argument.contains("-u="))
			{
				username = argument.split("=")[1];
			}else if(argument.contains("-p="))
			{
				password = argument.split("=")[1];
			}
		}
		if(fileName == null || databaseName == null || password == null || username == null)
		{
			System.err.println("Please give valid number of arguments.");
			System.exit(0);;
		}
		Connection con = JDBCConnector.getConnection(databaseName,username,password);
		Statement st = con.createStatement();
		HashMap<String, String> assaymapping = new HashMap<String, String>();
		ResultSet rs = st.executeQuery("select * from assay");
		while(rs.next())
		{
			assaymapping.put(rs.getString(2), rs.getString(1));
		}
		
		HashMap<String, Integer> sampleIdmapping = new HashMap<String, Integer>();
		rs = st.executeQuery("select * from sample");
		while(rs.next())
		{
			sampleIdmapping.put(rs.getString(2), rs.getInt(1));
		}
		
		PreparedStatement ps = con.prepareStatement("insert into sample_assay values (?,?,?,?)");
		BufferedReader br = new BufferedReader(new FileReader(/*"/home/ashish/Downloads/sample_assay_mapping.txt"*/fileName));
		String line = br.readLine();
		line = br.readLine();
		while(line!=null)
		{
			String lineData[] = line.split("\t");
			Integer sampleId = sampleIdmapping.get(lineData[0]);
			String assayType = assaymapping.get(lineData[1]);
			if(sampleId != null && assayType != null)
			{
				String runId = lineData.length > 3?lineData[3].trim():null;
				String rundate = lineData.length > 2?lineData[2].trim():"";
				Date date = null;
				if(!rundate.equals(""))
				{
					date = new Date(100+Integer.parseInt(rundate.substring(0,2)),Integer.parseInt(rundate.substring(2,4))-1,Integer.parseInt(rundate.substring(4,6)));
				}
				ps.setString(1, assayType);
				ps.setInt(2, sampleId);
				ps.setDate(3, date);
				ps.setString(4, runId);
				ps.addBatch();
			}
			line = br.readLine();
		}
		br.close();
		ps.executeBatch();
	}
}
