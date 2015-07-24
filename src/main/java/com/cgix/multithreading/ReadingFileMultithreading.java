package com.cgix.multithreading;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ReadingFileMultithreading {

	private MappedByteBuffer buffer;
	public static void main(String[] args) {
		String path = "/home/ashish/Downloads/Venkat_Files/HaloPlex_09_R_2015_07_01_00_46_41_user_SN2-13-MYELOID_HALOPLEX_062915-1_07-01-15-1_reanalysis_071615.base.cov.xls.txt";
		if (path != null){
			Path myFile = Paths.get(path);
			ReadingFileMultithreading proc = new ReadingFileMultithreading();
			try {
				proc.process(myFile);
			} catch (IOException e) {
				e.printStackTrace();
			}   
		}
	}

	public void process(Path file) throws IOException {
		readFileIntoBuffer(file);
		getBufferStream().parallel()
		.forEach(this::doIndex);
	}

	private Stream<String> getBufferStream() throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer.array())))){
			return reader.lines();
		}
	}

	private void readFileIntoBuffer(Path file) throws IOException{
		try(FileInputStream fis = new FileInputStream(file.toFile())){
			FileChannel channel = fis.getChannel();
			//channel.lock();
			buffer = channel.map(FileChannel.MapMode.PRIVATE, 0, channel.size());
		}
	}

	private void doIndex(String s){
		// Do whatever I need to do to index the line here
		System.out.println(s.split("\t")[0]);
	}
}

