package tmp.vn.com.dsvn.parse;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class Test {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		List<String> lines = FileUtils.readLines(new File("data/zillow/zipCode.tsv"));
		Set<String> outs = new HashSet<>();
		for (String line : lines) {
			String tx[] = line.split("\t");
			String link = tx[tx.length-1];
			String tks[] = link.split("/");
			String zipcode = tks[tks.length - 1];
			if(zipcode.equals("Hickory County MO")){
				System.out.println(line);
			}
			outs.add(zipcode);
		}
		FileUtils.writeLines(new File("data/zillow/zipCodeOnly.tsv"), outs);
	}

}
