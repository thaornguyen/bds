package vn.com.dsvn.crawl.mechanical.zitec;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import vn.com.dsvn.utils.DSFileUtils;
import vn.com.dsvn.utils.JsoupUtils;

public class ZitecShop {
	private static final Logger logger = LoggerFactory.getLogger(ZitecShop.class);
	private String domain = "https://www.zitec-shop.de/";
	private String fOut = "data/zitec/";
	private Properties prof;
	private int sleepTime = 2000;

	public ZitecShop() {
		prof = new Properties();
		try {
			prof.load(new FileInputStream(new File("conf/zitec02.properties")));
		} catch (IOException e) {
			logger.error("File conf/zitec.properties Not Found", e);
			return;
		}
	}

	public ZitecShop(String fConfig) {
		prof = new Properties();
		try {
			prof.load(new FileInputStream(new File(fConfig)));
			logger.info("Load config file: " + fConfig);
		} catch (IOException e) {
			logger.error("File conf/zitec.properties Not Found", e);
			return;
		}
		// System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
		// "20");
	}

	public void getProductLinks(File fIn) {
		logger.info("START APP: " + fIn);
		long start = System.currentTimeMillis();
		File fProd = new File(fOut + "zitec.prod.link.tsv");
		List<String> cateLinks = new ArrayList<>();
		try {
			cateLinks = FileUtils.readLines(fIn);
		} catch (IOException e1) {
			logger.error(String.format("File %s NOT FOUND", fIn), e1);
			return;
		}
		int count = 0;
		for (String cateLink : cateLinks) {
			List<String> productLinks = parseProductLinks(cateLink);
			for (String prodLink : productLinks) {
				DSFileUtils.write(cateLink + "\t" + prodLink, fProd.toString(), true);
			}
			logger.info("Category: " + count++ + "/" + cateLinks.size());
			logger.info("ProdLink: " + cateLink + " , NumProd: " + productLinks.size());
			// break;
		}
		long start2 = System.currentTimeMillis();
		logger.info("Total Time Get Category: " + (start2 - start) / 1000 + " s");
		logger.info("FINISH APP PRODUCT LINK");
	}

	public void getProductInfos(File fProd) {
		long start2 = System.currentTimeMillis();
		try {
			List<String> lines = FileUtils.readLines(fProd);
			int countProd = 0;
			for (String line : lines) {
				String toks[] = line.split("\t");
				if (toks.length == 2) {
					parseProd(toks[1]);
				}
				if (countProd++ % 1000 == 0) {
					logger.info("Product: " + countProd + "/" + lines.size());
				}
			}
			
		} catch (IOException e) {
			logger.error("Read File FAIL. File: " + fOut + fProd, e);
		}
		long start3 = System.currentTimeMillis();
		logger.info("Total Time Get Product: " + (start3 - start2) / 1000 + " ms");
		logger.info("FINISH APP PRODUCT INFO");
	}

	private void getCateLinks() {
		List<String> cateLinks = new ArrayList<>();
		try {
			Document doc = JsoupUtils.getDoc(this.domain, null,this.sleepTime);
			Elements els = doc.select(".leftmenuli3");
			for (Element el : els) {
				Elements subEls = el.select(".leftmenuli4");
				if (subEls.size() == 0) {
					Elements elLinks = el.select("a");
					for (Element elLink : elLinks) {
						cateLinks.add(elLink.absUrl("href"));
					}
				} else {
					Elements elLinks = subEls.select("a");
					for (Element elLink : elLinks) {
						cateLinks.add(elLink.absUrl("href"));
					}
				}
			}
		} catch (Exception e) {
			logger.error("PARSE DOMAIN FAIL.", e);
		}
		DSFileUtils.writeLine(cateLinks, fOut + "zitec.cate.txt", false);
		List<List<String>> smallerLists = Lists.partition(cateLinks, Math.abs(cateLinks.size() / 14) + 1);
		for (int i = 0; i < smallerLists.size(); i++) {
			DSFileUtils.writeLine(smallerLists.get(i), fOut + "zitec.cate." + (i + 1) + ".txt", false);
		}
	}

	public List<String> parseProductLinks(String cateLink) {
		List<String> prodLinks = new ArrayList<>();
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.put("Referer", cateLink);
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "www.zitec-shop.de");
		headers.put("Origin", "https://www.zitec-shop.de");
		headers.put("Cache-Control", "max-age=0");
		headers.put("Connection", "keep-alive");
		// headers.put("Origin", "https://www.zitec-shop.de");
		String cookie = this.prof.getProperty("Cookie");
		headers.put("Cookie", cookie);
		// List<String> listCookie = new ArrayList<>();
		// listCookie.add(
		// "PHPSESSID=dmk258fggs29dd6vjkbao4p5l0; BT_ctst=;
		// BT_sdc=eyJldF9jb2lkIjoiTkEiLCJyZnIiOiIiLCJ0aW1lIjoxNDkyNjEzMDU4Mzk2LCJwaSI6MSwicmV0dXJuaW5nIjowLCJldGNjX2NtcCI6Ik5BIn0%3D;
		// BT_pdc=eyJldGNjX2N1c3QiOjAsImVjX29yZGVyIjowLCJldGNjX25ld3NsZXR0ZXIiOjB9;
		// noWS_hQxUXE=true; cb-enabled=enabled");
		// Random ran = new Random();
		// int x = ran.nextInt(listCookie.size());
		// headers.put("Cookie", listCookie.get(x));
		Document doc = JsoupUtils.getDoc(cateLink, headers,this.sleepTime);
		if (doc == null) {
			DSFileUtils.write(cateLink, fOut + "zitec.cate.error.txt", true);
			return prodLinks;
		}
		Elements els = doc.select(".productbox >div > a");
		String restoreSearch = "";
		for (Element el : els) {
			String prodLink = el.absUrl("href");
			if (prodLink.contains("RestoreSearch")) {
				String toks[] = prodLink.split("&");
				for (String tok : toks) {
					if (tok.contains("RestoreSearch")) {
						restoreSearch = tok.split("=")[1];
					}
				}
				break;
			}
		}

		if (restoreSearch.isEmpty()) {
			return prodLinks;
		}

		// next page
		int indexPage = 0;
		int bufSize = 48;
		do {
			Set<String> subProdLinks = parseProductLinks(cateLink, restoreSearch, indexPage);

			if (subProdLinks.isEmpty()) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				subProdLinks = parseProductLinks(cateLink, restoreSearch, indexPage);
				if (subProdLinks.isEmpty()) {
					logger.error(String.format("PARSE PRODUCT EMPTY. Link: %s, IndexPage: %d", cateLink, indexPage));
					DSFileUtils.write(cateLink, fOut + "zitec.cate.error.txt", true);
					break;
				}
			}
			prodLinks.addAll(subProdLinks);
			if (subProdLinks.size() % bufSize != 0) {
				break;
			}
			indexPage++;
		} while (true);

		return prodLinks;
	}

	private Set<String> parseProductLinks(String cateLink, String restoreSearch, int indexPage) {
		int bufSize = 48;
		//
		Set<String> prodLinks = new HashSet<>();
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.put("Referer", cateLink);
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "www.zitec-shop.de");
		headers.put("Origin", "https://www.zitec-shop.de");
		headers.put("Cache-Control", "max-age=0");
		headers.put("Connection", "keep-alive");
		String cookie = this.prof.getProperty("Cookie");
		headers.put("Cookie", cookie);

		Map<String, String> datas = new HashMap<>();
		datas.put("ANZ_REC", String.valueOf(bufSize));
		datas.put("pagecount", String.valueOf(bufSize));
		datas.put("RestoreSearch", restoreSearch);
		datas.put("blaetter", String.valueOf(indexPage));
		datas.put("slct_cmspage", String.valueOf(indexPage));
		Document prodDoc = JsoupUtils.getPostDoc("https://www.zitec-shop.de/index.php", headers, datas,this.sleepTime);
		if (prodDoc == null) {
			DSFileUtils.write(cateLink, fOut + "zitec.cate.error.txt", true);
			return prodLinks;
		}
		Elements prodEls = prodDoc.select(".productbox >div > a");
		Set<String> nrChecks = new HashSet<>();
		for (Element el : prodEls) {
			if (el.attr("style").contains("underline"))
				continue;
			String[] toks = el.absUrl("href").split("&");
			boolean flag = false;
			for (String a : toks) {
				if (a.contains("NR=")) {
					if (nrChecks.contains(a)) {
						flag = true;
					}
				}
			}
			if (flag) {
				continue;
			}
			prodLinks.add(el.absUrl("href"));
		}
		logger.info(String.format("Get Product Link. Link: %s , RestoreSearch:%s,  IndexPage: %d, Product: %d",
				cateLink, restoreSearch, indexPage, prodLinks.size()));
		return prodLinks;
	}

	private void parseProd(String prodLink) {
		Document doc = JsoupUtils.getDoc(prodLink, null, this.sleepTime);
		if (doc == null) {
			logger.error(String.format("Parse Document FAIL. Link: %s", prodLink));
			return;
		}
		String title = doc.select("#artbez").text();
		String desc = doc.select("#artbesch").text();
		Elements els = doc.select(".parlistcnt");
		JSONObject jsonObj = new JSONObject();
		for (Element el : els) {
			String key = el.select(".parlistheadn,.parlistn").text();
			String value = el.select(".parlistheadv,.parlistv").text();
			if (!key.isEmpty() && !value.isEmpty()) {
				jsonObj.put(key, value);
			}
		}
		DSFileUtils.write(String.join("\t", prodLink, title, desc, jsonObj.toString()), fOut + "zitec.prod.tsv", true);
		logger.info("PROD-OK: "+prodLink);
	}

	public void getProdFromCateErr() {
		Set<String> setProds = getProdLinkExists();
		// Set<String> setProdCrawleds = getProdLinkCrawled();
		File fProd = new File(fOut + "zitec.prod.link.tsv");
		File fCateErr = new File(fOut + "zitec.cate.error.txt");
		try {
			List<String> cateLinkErrs = FileUtils.readLines(fCateErr);
			FileUtils.write(fCateErr, "");
			for (String cateLink : cateLinkErrs) {
				List<String> productLinks = parseProductLinks(cateLink);
				for (String prodLink : productLinks) {
					if (!setProds.contains(prodLink)) {
						setProds.add(prodLink);
						DSFileUtils.write(cateLink + "\t" + prodLink, fProd.toString(), true);
						// if (!setProdCrawleds.contains(prodLink)) {
						// parseProd(prodLink);
						// setProdCrawleds.add(prodLink);
						// }
					}
				}
			}
		} catch (IOException e) {
			logger.error("File not found! File: " + fCateErr.toString(), e);
		}
	}

	public void getProdErr() {
		Set<String> setProds = getProdLinkCrawled();

		Set<String> setProdLinks = getProdLinkExists();
		for (String prodLink : setProdLinks) {
			if (!setProds.contains(prodLink)) {
				parseProd(prodLink);
			}
		}
	}

	public Set<String> getProdLinkExists() {
		Set<String> setProds = new HashSet<>();
		try {
			List<String> prodLinks = FileUtils.readLines(new File(fOut + "zitec.prod.link.tsv"));
			for (String prodLink : prodLinks) {
				String toks[] = prodLink.split("\t");
				if (toks.length == 2) {
					setProds.add(toks[1]);
				}
			}

		} catch (IOException e) {
			logger.error("File not Found! File: " + fOut + "zitec.prod.link.tsv", e);
		}
		return setProds;
	}

	public Set<String> getProdLinkCrawled() {
		Set<String> setProds = new HashSet<>();
		try (Stream<String> stream = Files.lines(Paths.get(fOut + "zitec.prod.tsv"))) {
			stream.forEach(line -> {
				String toks[] = line.split("\t");
				if (toks.length > 0) {
					setProds.add(toks[0]);
				}
			});
		} catch (IOException e) {
			logger.error("File Not Found. " + fOut + "zitec.prod.tsv", e);
		}

		return setProds;
	}

	public void convertOutputToTsv() {
		String fProd = fOut + "zitec.prod.tsv";
		String fProdOk = fOut + "zitec.prod.ok.tsv";
		Set<String> setKeys = new HashSet<>();
		try (Stream<String> stream = Files.lines(Paths.get(fProd))) {
			stream.forEach(line -> {
				String toks[] = line.split("\t");
				if (toks.length == 4) {
					JSONObject jsonObj = new JSONObject(toks[3]);
					setKeys.addAll(jsonObj.keySet());
				}
			});
		} catch (IOException e) {
			logger.error("File Not Found. " + fProd, e);
		}
		List<String> listKeys = new ArrayList<>();
		listKeys.addAll(setKeys);
		Collections.sort(listKeys);
		StringBuilder header = new StringBuilder();
		header.append(String.join("\t", "URL", "Name", "Outline"));
		header.append("\t" + String.join("\t", listKeys));
		DSFileUtils.write(header.toString(), fProdOk, false);
		try (Stream<String> stream = Files.lines(Paths.get(fProd))) {
			stream.forEach(line -> {
				String toks[] = line.split("\t");
				if (toks.length == 4) {
					StringBuilder builder = new StringBuilder();
					builder.append(String.join("\t", toks[0], toks[1], toks[2]));
					JSONObject jsonObj = new JSONObject(toks[3]);
					for (String key : listKeys) {
						builder.append("\t");
						if (jsonObj.has(key)) {
							builder.append(jsonObj.get(key));
						}
					}
					DSFileUtils.write(builder.toString(), fProdOk, true);
				}
			});
		} catch (IOException e) {
			logger.error("File Not Found. " + fProd, e);
		}
	}

	// @Test
	// public void test() {
	// String cateUrl =
	// "https://www.zitec-shop.de/index.php?NR=&P=lst&MS=ms_500280&AID=1714865";
	// List<String> productLinks = parseProductLinks(cateUrl);
	// // List<String> productLinks = parseProductLinks(cateUrl);
	// System.out.println(productLinks.size());
	// }

	public static void main(String[] args) {
		// args = new String[] { "-t", "convert", "-c", "zitec01.properties" };
//		args = new String[] { "-t", "prod-info", "-i", "/data/workspace/BDSCrawler3/data/zitec/zitec.prod.link.local.txt", "-c",
//				"conf/zitec02.properties" };

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();

		Options options = new Options();
		options.addOption("t", "type", true, "{cate: get category, prod: get product}");
		options.addOption("i", "input", true, "file category links");
		options.addOption("c", "config", true, "file config");
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			logger.error("Parse args false", e);
			formatter.printHelp("ZitecCrawler", options);
		}

		if (!cmd.hasOption("c")) {
			logger.error("Don't have option type");
			formatter.printHelp("ZitecCrawler", options);
			return;
		}
		String fConfig = cmd.getOptionValue("c");
		ZitecShop zitec = new ZitecShop(fConfig);

		if (!cmd.hasOption("t")) {
			logger.error("Don't have option type");
			formatter.printHelp("ZitecCrawler", options);
			return;
		}
		String type = cmd.getOptionValue("t");
		if (type.equals("cate")) {
			zitec.getCateLinks();
		} else if (type.equals("prod")) {
			if (!cmd.hasOption("i")) {
				logger.error("Don't have option input");
				formatter.printHelp("ZitecCrawler", options);
				return;
			}
			String sIn = cmd.getOptionValue("i");
			if (sIn == null) {
				logger.error("File input not null: " + sIn);
				formatter.printHelp("ZitecCrawler", options);
				return;
			}
			File fIn = new File(sIn);
			if (!fIn.exists()) {
				logger.error("File not FOUND: " + sIn);
				formatter.printHelp("ZitecCrawler", options);
				return;
			}
			zitec.getProductLinks(fIn);
		} else if (type.equals("prod-info")) {
			if (!cmd.hasOption("i")) {
				logger.error("Don't have option input");
				formatter.printHelp("ZitecCrawler", options);
				return;
			}
			String sIn = cmd.getOptionValue("i");
			if (sIn == null) {
				logger.error("File input not null: " + sIn);
				formatter.printHelp("ZitecCrawler", options);
				return;
			}
			File fIn = new File(sIn);
			if (!fIn.exists()) {
				logger.error("File not FOUND: " + sIn);
				formatter.printHelp("ZitecCrawler", options);
				return;
			}
			zitec.getProductInfos(fIn);
		} else if (type.equals("cate-err")) {
			zitec.getProdFromCateErr();
		} else if (type.equals("prod-err")) {
			zitec.getProdErr();
		} else if (type.equals("convert")) {
			zitec.convertOutputToTsv();
		} else {
			formatter.printHelp("ZitecCrawler", options);
			return;
		}

	}

}
