package tmp.vn.com.dsvn.parse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

import vn.com.dsvn.utils.DSFileUtils;

public class Test {

	public static void main(String[] args) throws IOException {
		List<String> dus = FileUtils.readLines(new File("/home/thaonp/Desktop/ludw.cate.txt"));
		Set<String> setDus = new HashSet<>();
		setDus.addAll(dus);
		List<String> thieus = FileUtils.readLines(new File("/home/thaonp/Desktop/ludw_data/06/ludw.cate.txt"));
		Set<String> setThieus = new HashSet<>();
		setThieus.addAll(thieus);
		List<String> outs = new ArrayList<>();
		System.out.println(dus.size() + "  " + thieus.size());
		System.out.println(setDus.size() + "  " + setThieus.size());

		List<String> diffs = new ArrayList<>();

		for (String str : dus) {
			if (str.isEmpty()) {
				continue;
			}
			if (!setThieus.contains(str)) {
				outs.add(str);
			} else {
				diffs.add(str);
			}
		}

		System.out.println(outs.size());
		System.out.println(diffs.size());

		for (String str : thieus) {
			if(!diffs.contains(str)){
				System.out.println(str);
			}
		}

		String fOut = "/data/workspace/BDSCrawler2/data/ludw/";
		DSFileUtils.writeLine(outs, fOut + "ludw.cate.txt", false);
		List<List<String>> smallerLists = Lists.partition(outs, Math.abs(outs.size() / 5) + 1);
		for (int i = 0; i < smallerLists.size(); i++) {
			DSFileUtils.writeLine(smallerLists.get(i), fOut + "ludw.cate." + (i + 1) + ".txt", false);
		}
	}

}
