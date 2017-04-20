package vn.com.dsvn.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DSFileUtils {
	private static final Logger logger = LoggerFactory.getLogger(DSFileUtils.class);

	public static void write(String line, String fOut, boolean append) {
		try {
			FileUtils.write(new File(fOut), line + "\n", append);
		} catch (IOException e) {
			logger.error("File not found. File: " + fOut, e);
		}
	}

	public static void writeLine(List<String> lines, String fOut, boolean append) {
		try {
			FileUtils.writeLines(new File(fOut), lines, append);
		} catch (IOException e) {
			logger.error("File not found. File: " + fOut, e);
		}
	}
}
