package vn.com.dsvn.zitec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.com.dsvn.crawl.mechanical.zitec.ZitecShop;
import vn.com.dsvn.utils.DSFileUtils;
import vn.com.dsvn.utils.JsoupUtils;

public class ZitecTest {
	private static final Logger logger = LoggerFactory.getLogger(ZitecTest.class);
	private String domain = "https://www.zitec-shop.de/";
	private String fOut = "data/zitec_test/";

	private void getCateLinks() {
		List<String> cateLinks = new ArrayList<>();
		try {
			Document doc = JsoupUtils.getDoc(this.domain, null);
			Elements els = doc.select(".leftmenuli3");
			for (Element el : els) {
				Elements subEls = el.select(".leftmenuli4");
				if (subEls.size() == 0) {
					Elements elLinks = el.select("a");
					for (Element elLink : elLinks) {
						String prod = elLink.text();
						int num = parseNumber(prod);
						cateLinks.add(elLink.absUrl("href") + "\t" + num);
					}
				} else {
					Elements elLinks = subEls.select("a");
					for (Element elLink : elLinks) {
						String prod = elLink.text();
						int num = parseNumber(prod);
						cateLinks.add(elLink.absUrl("href") + "\t" + num);
					}
				}
			}
		} catch (Exception e) {
			logger.error("PARSE DOMAIN FAIL.", e);
		}
		DSFileUtils.writeLine(cateLinks, fOut + "zitec.cate.txt", false);
		// List<List<String>> smallerLists = Lists.partition(cateLinks,
		// Math.abs(cateLinks.size() / 14) + 1);
		// for (int i = 0; i < smallerLists.size(); i++) {
		// DSFileUtils.writeLine(smallerLists.get(i), fOut + "zitec.cate." + (i
		// + 1) + ".txt", false);
		// }
	}

	public static void statisTest() throws IOException {
		List<String> lines = FileUtils.readLines(new File("/home/thaonp/Desktop/zitec_data/zitec.prod.link.ok.tsv"));
		Map<String, Integer> maps = new HashMap<>();
		for (String line : lines) {
			String toks[] = line.split("\t");
			int count = 0;
			if (maps.containsKey(toks[0])) {
				count = maps.get(toks[0]);
			}
			count++;
			maps.put(toks[0], count);
		}

		List<String> estimates = FileUtils.readLines(new File("/home/thaonp/Desktop/zitec.cate.estimate.tsv"));
		List<String> outs = new ArrayList<>();
		List<String> errs = new ArrayList<>();
		for (String line : estimates) {
			String toks[] = line.split("\t");
			if (maps.containsKey(toks[0])) {
				// outs.add(toks[0] + "\t" + maps.get(toks[0]));
				if (Integer.parseInt(toks[1]) != maps.get(toks[0])) {
					errs.add(line);
				}
			} else {
				// outs.add(toks[0] + "\t0");
				errs.add(line);
			}
		}
		FileUtils.writeLines(new File("/home/thaonp/Desktop/zitec.cate.crawled.tsv"), outs);
		FileUtils.writeLines(new File("/home/thaonp/Desktop/zitec.cate.not.crawled.tsv"), errs);
	}

	private int parseNumber(String str) {
		Pattern r = Pattern.compile("\\([0-9]+\\)$");
		Matcher m = r.matcher(str);
		if (m.find()) {
			try {
				String sNum = m.group(0).replace("(", "").replace(")", "");
				return Integer.parseInt(sNum);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			System.out.println("NO MATCH");
		}
		return 0;
	}

	public static void test1000product() {
		String cateLink = "https://www.zitec-shop.de/index.php?NR=&P=lst&MS=ms_500270&AID=1713581";
		ZitecShop zitec = new ZitecShop();
		zitec.parseProductLinks(cateLink);
	}

	public static void testGetCate() {
		ZitecShop zitec = new ZitecShop("conf/zitec15.properties");
		zitec.getProductLinks(new File("/data/workspace/BDSCrawler2/data/zitec_test/zitec.cate.test.txt"));

	}

	public static void splitCate() throws IOException {
		List<String> lines = FileUtils.readLines(new File("/home/thaonp/Desktop/zitec.cate.not.crawled.tsv"));
		int count = 0;
		int index = 2;
		List<String> outs = new ArrayList<>();
		for (String line : lines) {
			String toks[] = line.split("\t");
			int numProd = Integer.parseInt(toks[1]);
			count += numProd;
			outs.add(toks[0]);
			if (count > 15000) {
				if (index < 10)
					FileUtils.writeLines(new File("data/zitec_2/zitec.cate.0" + index++ + ".txt"), outs);
				else
					FileUtils.writeLines(new File("data/zitec_2/zitec.cate." + index++ + ".txt"), outs);
				count = 0;
				outs = new ArrayList<>();
			}
		}
		if (outs.size() > 0) {
			FileUtils.writeLines(new File("data/zitec_2/zitec.cate." + index++ + ".txt"), outs);
		}

	}

	public static void main(String[] args) {
		ZitecTest zitec = new ZitecTest();
		// zitec.getCateLinks();
		try {
			// statisTest();
			splitCate();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// test1000product();
		// testGetCate();
	}

}
