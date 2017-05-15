package vn.com.dsvn.zitec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import vn.com.dsvn.crawl.mechanical.ludwigmeister.LudwigmeisterCrawler;
import vn.com.dsvn.crawl.mechanical.zitec.ZitecShop;
import vn.com.dsvn.utils.DSFileUtils;
import vn.com.dsvn.utils.JsoupUtils;

public class LudwTest {
	private static final Logger logger = LoggerFactory.getLogger(LudwTest.class);
	private String domain = "https://www.zitec-shop.de/";
	private String fOut = "data/zitec_test/";

	private void getCateLinks() {
		LudwigmeisterCrawler crawled = new LudwigmeisterCrawler();
		List<String> urlSubCates = new ArrayList<>();
		List<String> urlCates = crawled.parseCates();
		urlCates.parallelStream().forEach(urlCate -> {
			List<String> urlSubCate2s = crawled.parseSubCates(urlCate);
			urlSubCate2s.parallelStream().forEach(urlSubCate2 -> {
				List<String> urlSubCate3s = crawled.parseSubCates(urlSubCate2);
				urlSubCate3s.parallelStream().forEach(urlSubCate3 -> {
					boolean isCheck = crawled.isSubCate(urlSubCate3);
					if (!isCheck) {
						List<String> urlSubCate4s = crawled.parseSubCates(urlSubCate3);
						urlSubCate4s.parallelStream().forEach(urlSubCate4 -> {
							urlSubCates.add(urlSubCate4);
						});
					} else {
						urlSubCates.add(urlSubCate3);
					}
				});

			});
		});
		List<String> estimates = new ArrayList<>();
		urlSubCates.parallelStream().forEach(cateLink -> {
			Document doc = JsoupUtils.getDoc(cateLink, 0);
			String label = doc.select("#results-label").text();
			int numProd = getNumberVariant(label);
			Elements els = doc.select(".breadcrumb");
			StringBuilder builder = new StringBuilder();
			for (Element el : els) {
				String text = el.select("li").text();
				builder.append(text + " > ");
			}
			estimates.add(cateLink + "\t" + numProd + "\t" + builder.toString());
		});
		DSFileUtils.writeLine(estimates, "/home/thaonp/Desktop/ludw_QA/estimation.tsv", false);
		// List<List<String>> smallerLists = Lists.partition(cateLinks,
		// Math.abs(cateLinks.size() / 14) + 1);
		// for (int i = 0; i < smallerLists.size(); i++) {
		// DSFileUtils.writeLine(smallerLists.get(i), fOut + "zitec.cate." + (i
		// + 1) + ".txt", false);
		// }
	}

	public static void statisTest() throws IOException {
		List<String> lines = FileUtils.readLines(new File("/home/thaonp/Desktop/ludw_QA/ludw.prod.link.tsv"));
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

		List<String> estimates = FileUtils.readLines(new File("/home/thaonp/Desktop/ludw_QA/estimation.tsv"));
		List<String> outs = new ArrayList<>();
		List<String> errs = new ArrayList<>();
		errs.add("URL\tESTIMATE\tCRAWLED");
		for (String line : estimates) {
			String toks[] = line.split("\t");
			if (maps.containsKey(toks[0])) {
				outs.add(toks[0] + "\t" + maps.get(toks[0]));
				if (Integer.parseInt(toks[1]) != maps.get(toks[0])) {
					errs.add(line + "\t" + maps.get(toks[0]));
				}
			} else {
				outs.add(toks[0] + "\t0");
				errs.add(line + "\t0");
			}
		}
		// FileUtils.writeLines(new
		// File("/home/thaonp/Desktop/zitec.cate.crawled.tsv"), outs);
		FileUtils.writeLines(new File("/home/thaonp/Desktop/ludw_QA/ludw.cate.not.crawled.tsv"), errs);
	}

	private int getNumberProd(String textNum) {
		int nProd = 0;
		try {
			String textProd = textNum.substring(textNum.indexOf("von"), textNum.indexOf("Produkten")).replace("von", "")
					.replaceAll("\\s+", "").trim();
			nProd = Integer.parseInt(textProd);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return nProd;
	}

	private int getNumberVariant(String textNum) {
		int nVariant = 0;
		try {
			String textVar = textNum.substring(textNum.indexOf("Produkten"), textNum.indexOf("Varianten"))
					.replace("mit", "").replace("Produkten", "").replaceAll("\\s+", "").trim().replaceAll("\\s+", "")
					.trim();
			nVariant = Integer.parseInt(textVar);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return nVariant;
	}

	public static void test1000product() {
		String cateLink = "https://www.zitec-shop.de/index.php?NR=&P=lst&MS=ms_500270&AID=1713581";
		ZitecShop zitec = new ZitecShop();
		zitec.parseProductLinks(cateLink);
	}

	public static void parseProductLink() {
		String prodLink = "https://www.ludwigmeister.de/artikel/verschraubungen/38955/parker-fm-eo2-funktionsmutter/851fm30sssa-1658018";
		LudwigmeisterCrawler ludw = new LudwigmeisterCrawler();
		ludw.parseProd(prodLink);
	}

	public static void testGetProdLink() throws IOException {
		ZitecShop zitec = new ZitecShop("conf/zitec03.properties");
		File fProd = new File("data/zitec_2/zitec.prod.link2.tsv");
		List<String> lines = FileUtils.readLines(new File("data/zitec_2/zitec.cate.txt"));
		lines.parallelStream().forEach(cateLink -> {
			List<String> productLinks = zitec.parseProductLinks(cateLink);
			for (String prodLink : productLinks) {
				DSFileUtils.write(cateLink + "\t" + prodLink, fProd.toString(), true);
			}
			logger.info("ProdLink: " + cateLink + " , NumProd: " + productLinks.size());
		});

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

	public static void splitProd() throws IOException {
		List<String> prodLinks = FileUtils.readLines(new File("data/ludw/ludw.prod.link.tsv"));
		List<List<String>> smallerLists = Lists.partition(prodLinks, Math.abs(prodLinks.size() / 14) + 1);
		for (int i = 2; i < smallerLists.size() + 2; i++) {
			if (i < 10)
				DSFileUtils.writeLine(smallerLists.get(i - 2), "data/ludw/ludw.prod.link.0" + i + ".txt", false);
			else
				DSFileUtils.writeLine(smallerLists.get(i - 2), "data/ludw/ludw.prod.link." + i + ".txt", false);
		}
	}

	public void getProdsFromCate(File fCate) {
		try {
			List<String> lines = FileUtils.readLines(fCate);
			for (String line : lines) {
				String cateLink = line.split("\t")[0];
				Set<String> prodLinks = getProdsFromCate(cateLink);
				try {
					FileUtils.writeLines(new File("data/ludw/ludw.link.test.tsv"), prodLinks, true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Set<String> getProdsFromCate(String cateLink) {
		LudwigmeisterCrawler crawler = new LudwigmeisterCrawler();
		// cateLink =
		// "https://www.ludwigmeister.de/produkte/gehaeusezubehoer/52162";
		Set<String> prodLinks = new HashSet<>();
		Document doc = JsoupUtils.getDoc(cateLink, 0);
		String label = doc.select("#results-label").text();
		logger.info(String.format("CateLink: %s , Label: %s", cateLink, label));
		// String sProd = label.substring(label.indexOf("Produkten"),
		// label.indexOf("Varianten")).replace("mit", "")
		// .replace("Produkten", "").replaceAll("\\s+",
		// "").trim().replaceAll("\\s+", "").trim();
		int nProd = getNumberProd(label);
		Elements els = doc.select(".produktkachel");
		els.parallelStream().forEach(el -> {
			String prodLink = el.absUrl("data-href");
			String text = el.select(".zu_den_varianten").text();
			if (text == null || text.isEmpty()) {
				prodLinks.add(prodLink);
			} else {
				prodLinks.addAll(crawler.getProdsFromSubcate(prodLink));
			}
		});
		int bufSize = 25;
		if (els.size() % bufSize != 0) {
			logger.info(String.format("CateLink: %s , NumCateProdCrawled: %d", cateLink, prodLinks.size()));
			return prodLinks;
		}

		int maxPage = Math.abs(nProd / bufSize) + 1;
		for (int indexPage = 2; indexPage <= maxPage; indexPage++) {
			String url = cateLink + "?page=" + indexPage;
			doc = JsoupUtils.getDoc(url, 0);
			els = doc.select(".produktkachel");
			els.parallelStream().forEach(el -> {
				String prodLink = el.absUrl("data-href");
				String text = el.select(".zu_den_varianten").text();
				if (text == null || text.isEmpty()) {
					prodLinks.add(prodLink);
				} else {
					prodLinks.addAll(crawler.getProdsFromSubcate(prodLink));
				}
				// prodLinks.add(el.absUrl("href"));
			});
		}
		Set<String> outs = new HashSet<>();
		for (String prodLink : prodLinks) {
			outs.add(cateLink + "\t" + prodLink);
		}
		logger.info(String.format("CateLink: %s , NumCateProdCrawled: %d", cateLink, prodLinks.size()));
		return outs;
	}

	public static void test() throws IOException {
		List<String> lines = FileUtils.readLines(new File("/home/thaonp/Desktop/ludw_QA/ludw.prod.link.ok.tsv"));
		Set<String> outs = new HashSet<>();
		for (String line : lines) {
			String toks[] = line.split("\t");
			if (toks.length != 2) {
				System.out.println(line);
				System.out.println(toks.length);
			} else {
				outs.add(line);
			}

		}
		FileUtils.writeLines(new File("/home/thaonp/Desktop/ludw_QA/ludw.prod.link.ok.2.tsv"), outs);
	}

	public static void testProd() {
		String url = "https://www.ludwigmeister.de/artikel/vhm-bohrer/50420/kleinstbohrer-mikron-vhm-7-x-d/710102457-0136-2669691";
		LudwigmeisterCrawler crawler = new LudwigmeisterCrawler();
		crawler.parseProd(url);
	}

	public static void main(String[] args) throws IOException {
		LudwTest zitec = new LudwTest();
//		zitec.getCateLinks();
		testProd();
		// statisTest();
		// splitProd();
		// parseProductLink(); // splitCate();

		// try {
		// testGetProdLink();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// testGetCate();
		// String cateLink =
		// "https://www.ludwigmeister.de/produkte/verschraubungen/38955";
		// File fCate = new File("data/ludw/ludw.cate.test.txt");
		// zitec.getProdsFromCate(fCate);
		// test();
	}

}
