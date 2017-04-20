package tmp.vn.com.dsvn.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class ZillowCount {
	public void run() throws IOException {
		Map<String, Set<String>> mapStateCountys = new HashMap<String, Set<String>>();
		Map<String, Set<String>> mapCountyZipCodes = new HashMap<String, Set<String>>();
		Map<String, Set<String>> mapZipCodeStreets = new HashMap<String, Set<String>>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("data/zillow/street.tsv")), "UTF-8"));
		String line = "";
		while ((line = reader.readLine()) != null) {
			String name = line.split("\t")[0];
			String tks[] = name.split(":");
			if (tks.length == 5) {
				push(tks[1], tks[2], mapStateCountys);
				push(tks[2], tks[3], mapCountyZipCodes);
				push(tks[3], tks[4], mapZipCodeStreets);
			} else {
				System.out.println(line);
			}
		}
		reader.close();
		write(mapStateCountys, "data/zillow/count/state_county.tsv");
		write(mapCountyZipCodes, "data/zillow/count/county_zipCode.tsv");
		write(mapZipCodeStreets, "data/zillow/count/zipCode_Street.tsv");
	}

	private void push(String key, String value, Map<String, Set<String>> maps) {
		Set<String> sets = new HashSet<String>();
		if (maps.containsKey(key)) {
			sets = maps.get(key);
		}
		sets.add(value);
		maps.put(key, sets);
	}

	private void write(Map<String, Set<String>> maps, String file) {
		List<String> outs = new ArrayList<String>();
		for (String key : maps.keySet()) {
			outs.add(key + "\t" + maps.get(key).size());
		}
		try {
			FileUtils.writeLines(new File(file), outs);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		new ZillowCount().run();
	}

}
