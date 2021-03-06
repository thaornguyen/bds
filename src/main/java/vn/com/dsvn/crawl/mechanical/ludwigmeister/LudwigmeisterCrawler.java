package vn.com.dsvn.crawl.mechanical.ludwigmeister;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import vn.com.dsvn.utils.DSFileUtils;
import vn.com.dsvn.utils.JsoupUtils;

public class LudwigmeisterCrawler {
	private static final Logger logger = LoggerFactory.getLogger(LudwigmeisterCrawler.class);
	private String domain = "https://www.ludwigmeister.de/de";
	private String fOut = "data/ludw/";
	private String fSource = "data/ludw/source/";
	private int sleepTime = 2000;

	public LudwigmeisterCrawler() {
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "100");
	}

	private void run() {

	}

	public void getCates() {
		List<String> urlSubCates = new ArrayList<>();
		List<String> urlCates = parseCates();
		urlCates.parallelStream().forEach(urlCate -> {
			List<String> urlSubCate2s = parseSubCates(urlCate);
			urlSubCate2s.parallelStream().forEach(urlSubCate2 -> {
				List<String> urlSubCate3s = parseSubCates(urlSubCate2);
				urlSubCate3s.parallelStream().forEach(urlSubCate3 -> {
					boolean isCheck = isSubCate(urlSubCate3);
					if (!isCheck) {
						List<String> urlSubCate4s = parseSubCates(urlSubCate3);
						urlSubCate4s.parallelStream().forEach(urlSubCate4 -> {
							urlSubCates.add(urlSubCate4);
						});
					} else {
						urlSubCates.add(urlSubCate3);
					}
				});

			});
		});
		DSFileUtils.writeLine(urlSubCates, fOut + "ludw.cate.txt", true);
		List<List<String>> smallerLists = Lists.partition(urlSubCates, Math.abs(urlSubCates.size() / 5) + 1);
		for (int i = 0; i < smallerLists.size(); i++) {
			DSFileUtils.writeLine(smallerLists.get(i), fOut + "ludw.cate." + (i + 1) + ".txt", false);
		}
	}

	/**
	 * @description https://www.ludwigmeister.de/de ->
	 *              https://www.ludwigmeister.de/produkte/antriebstechnik/38075
	 * @author thaonp
	 * @return
	 * @since 1.0.0
	 * @throws TODO
	 */

	public List<String> parseCates() {
		List<String> cateUrls = new ArrayList<>();

		Document doc = JsoupUtils.getDoc(this.domain, this.sleepTime);
		if (doc == null) {
			return cateUrls;
		}
		Elements els = doc.select(".kategoriekacheln .lowprofile");
		els.forEach(el -> {
			String cateUrl = el.absUrl("href");
			cateUrls.add(cateUrl);
		});
		return cateUrls;
	}

	/**
	 * @description https://www.ludwigmeister.de/produkte/antriebstechnik/38075
	 *              -> https://www.ludwigmeister.de/produkte/kettentriebe/38077
	 * @author thaonp
	 * @param cateUrl
	 * @return
	 * @since 1.0.0
	 * @throws TODO
	 */

	public List<String> parseSubCates(String cateUrl) {
		List<String> subCateUrls = new ArrayList<>();

		Document doc = JsoupUtils.getDoc(cateUrl, this.sleepTime);
		if (doc == null) {
			return subCateUrls;
		}
		Elements els = doc.select(".subkategorien .lowprofile");
		els.forEach(el -> {
			String subCateUrl = el.absUrl("href");
			if (!subCateUrl.isEmpty())
				subCateUrls.add(subCateUrl);
		});
		return subCateUrls;
	}

	public boolean isSubCate(String prodUrl) {
		Document doc = JsoupUtils.getDoc(prodUrl, this.sleepTime);
		// String name = doc.select(".bordered .breadcrumb").text();
		String textNum = doc.select("#results-label").text();
		if (textNum.isEmpty()) {
			return false;
		}
		// try {
		// String textProd = textNum.substring(textNum.indexOf("von"),
		// textNum.indexOf("Produkten")).replace("von", "")
		// .replaceAll("\\s+", "").trim();
		// String textVar = textNum.substring(textNum.indexOf("Produkten"),
		// textNum.indexOf("Varianten"))
		// .replace("mit", "").replace("Produkten", "").replaceAll("\\s+",
		// "").trim().replaceAll("\\s+", "")
		// .trim();
		// this.numProd = this.numProd + Integer.parseInt(textProd);
		// this.numVar = this.numVar + Integer.parseInt(textVar);
		// FileUtils.write(new File(fOut), prodUrl + "\t" + name + "\t" +
		// textProd + "\t" + textVar + "\n", true);
		// } catch (Exception e) {
		// System.out.println("ER1 " + prodUrl + "\t" + textNum);
		// e.printStackTrace();
		// return false;
		// }

		return true;
	}

	public void getProductLinks(File fIn) {
		logger.info("START APP: " + fIn);
		long start = System.currentTimeMillis();
		File fProd = new File(fOut + "ludw.prod.link.tsv");
		List<String> cateLinks = new ArrayList<>();
		try {
			cateLinks = FileUtils.readLines(fIn);
		} catch (IOException e1) {
			logger.error(String.format("File %s NOT FOUND", fIn), e1);
			return;
		}
		int count = 0;
		for (String cateLink : cateLinks) {
			Set<String> productLinks = getProdsFromCate(cateLink);
			productLinks.forEach(prodLink -> {
				if (!prodLink.isEmpty()) {
					DSFileUtils.write(cateLink + "\t" + prodLink, fProd.toString(), true);
				}
			});
			logger.info("Category: " + count++ + "/" + cateLinks.size());
			// break;
		}
		long start2 = System.currentTimeMillis();
		logger.info("Total Time Get Category: " + (start2 - start) / 1000 + " ms");
	}

	public void getProdInfos(File fProd) {
		long start2 = System.currentTimeMillis();
		WebDriver d = new PhantomJSDriver();
		try {
			List<String> lines = FileUtils.readLines(fProd);
			int countProd = 0;
			for (String line : lines) {
				String toks[] = line.split("\t");
				if (toks.length == 2) {
					parseProd(toks[1], d);
				}
				if (countProd++ % 1000 == 0) {
					logger.info("Product: " + countProd + "/" + lines.size());
				}
			}
		} catch (IOException e) {
			logger.error("Read File FAIL. File: " + fOut + fProd, e);
		}
		d.close();
		long start3 = System.currentTimeMillis();
		logger.info("Total Time Get Product: " + (start3 - start2) / 1000 + " ms");
		logger.info("FINISH APP GET PRODUCT INFO");
	}

	public void parseProd(String prodLink) {
		WebDriver d = new PhantomJSDriver();
		parseProd(prodLink, d);
		d.close();
	}

	public void parseProd(String prodLink, WebDriver d) {
		boolean isClosePhantom = false;
		if (d == null) {
			d = new PhantomJSDriver();
			isClosePhantom = true;
		}
		String html = JsoupUtils.getHtmlByPhantom(prodLink, d, this.sleepTime);
		System.out.println(html);
//		String html = JsoupUtils.getHtml(prodLink, sleepTime);
		String nfProdLink = prodLink.replace("/", "-");
		try {
			FileUtils.write(new File(fSource + nfProdLink + ".html"), html);
		} catch (IOException e) {
			logger.error("File not found", e);
		}
		Document doc = JsoupUtils.getDocBySource(html);
		// Map<String, String> headers = new HashMap<>();
		// headers.put("Accept",
		// "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		// headers.put("Connection", "keep-alive");
		// headers.put("Host", "www.ludwigmeister.de");
		// headers.put("Cookie", "PHPSESSID=8iu8gmqhjcnkhq32dt5mueit13;");
		// headers.put("Cache-Control", "max-age=0");
		// Document doc = JsoupUtils.getDoc(prodLink, null, this.sleepTime);
		if (doc == null) {
			logger.error(String.format("Parse Document FAIL. Link: %s", prodLink));
			return;
		}
		String title = doc.select(".beschreibung h2").text();
		String desc = doc.select(".artikelbezeichnung,.artikelnummer,.dddwrapper").text();
		Elements elCates = doc.select(".breadcrumb li");
		List<String> nameCates = new ArrayList<>();
		for (Element el : elCates) {
			nameCates.add(el.text());
		}
		String category = String.join(">", nameCates);
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("Preis inkl. MwSt.", doc.select(".artikeldetailinfo ul li:first-child.preis .right").text());
		jsonObj.put("Standardpreis inkl. MwSt.", doc.select(".artikeldetailinfo ul .listpricegross .preis").text());
		jsonObj.put("Preis exkl. MwSt.", doc.select(".artikeldetailinfo ul .preis.alternative .right").text());
		jsonObj.put("Product Variable", doc.select(".verfuegbarkeit .verfuegbarkeit").text());

		JSONObject jsonDetailObj = new JSONObject();
		Elements detailEls = doc.select(".technischedaten .list ul li");
		for (Element el : detailEls) {
			String key = el.select("b").text();
			String value = el.select("span").text();
			jsonDetailObj.put(key, value);
		}
		DSFileUtils.write(
				String.join("\t", prodLink, title, desc, category, jsonObj.toString(), jsonDetailObj.toString()),
				fOut + "ludw.prod.tsv", true);
		if (isClosePhantom) {
			d.close();
		}
	}

	public Set<String> getProdsFromCate(String cateLink) {
		// cateLink =
		// "https://www.ludwigmeister.de/produkte/gehaeusezubehoer/52162";
		Set<String> prodLinks = new HashSet<>();
		Document doc = JsoupUtils.getDoc(cateLink, this.sleepTime);
		String label = doc.select("#results-label").text();
		logger.info(String.format("CateLink: %s , Label: %s", cateLink, label));
		int nProd = getNumberProd(label);
		Elements els = doc.select(".produktkachel");
		for (Element el : els) {
			String prodLink = el.absUrl("data-href");
			if (prodLink.isEmpty()) {
				continue;
			}
			String text = el.select(".zu_den_varianten").text();
			if (text == null || text.isEmpty()) {
				prodLinks.add(prodLink);
			} else {
				prodLinks.addAll(getProdsFromSubcate(prodLink));
			}
		}
		int bufSize = 25;
		if (els.size() % bufSize != 0) {
			logger.info(String.format("CateLink: %s , NumCateProdCrawled: %d", cateLink, prodLinks.size()));
			return prodLinks;
		}

		if (nProd > 0) {
			int maxPage = Math.abs(nProd / bufSize) + 1;
			for (int indexPage = 2; indexPage <= maxPage; indexPage++) {
				String url = cateLink + "?page=" + indexPage;
				doc = JsoupUtils.getDoc(url, this.sleepTime);
				els = doc.select(".produktkachel");
				for (Element el : els) {
					String prodLink = el.absUrl("data-href");
					String text = el.select(".zu_den_varianten").text();
					if (text == null || text.isEmpty()) {
						prodLinks.add(prodLink);
					} else {
						prodLinks.addAll(getProdsFromSubcate(prodLink));
					}
					// prodLinks.add(el.absUrl("href"));
				}
			}
		} else {
			int indexPage = 2;
			do {
				String url = cateLink + "?page=" + indexPage;
				doc = JsoupUtils.getDoc(url, this.sleepTime);
				els = doc.select(".produktkachel");
				for (Element el : els) {
					String prodLink = el.absUrl("data-href");
					String text = el.select(".zu_den_varianten").text();
					if (text == null || text.isEmpty()) {
						prodLinks.add(prodLink);
					} else {
						prodLinks.addAll(getProdsFromSubcate(prodLink));
					}
					prodLinks.add(el.absUrl("href"));
				}

				if (els.size() % bufSize != 0) {
					break;
				}

				indexPage++;
			} while (true);
		}
		logger.info(String.format("CateLink: %s , NumCateProdCrawled: %d", cateLink, prodLinks.size()));
		return prodLinks;
	}

	public Set<String> getProdsFromSubcate(String subCateLink) {
		Set<String> prodLinks = new HashSet<>();
		Document doc = JsoupUtils.getDoc(subCateLink, this.sleepTime);
		String label = doc.select("#results-label").text();
		int nProd = getNumberVariant(label);

		logger.info(String.format("SubCateLink: %s , Label: %s", subCateLink, label));

		Elements els = doc.select(".produktdetailzeile .column-title a");
		for (Element el : els) {
			String prodLink = el.absUrl("href");
			if (!prodLink.isEmpty())
				prodLinks.add(prodLink);
		}

		int bufSize = 25;

		if (els.size() % bufSize != 0) {
			return prodLinks;
		}

		if (nProd > 0) {
			int maxPage = Math.abs(nProd / bufSize) + 1;
			for (int indexPage = 2; indexPage <= maxPage; indexPage++) {
				String url = subCateLink + "?page=" + indexPage;
				doc = JsoupUtils.getDoc(url, this.sleepTime);
				els = doc.select(".produktdetailzeile .column-title a");
				for (Element el : els) {
					String prodLink = el.absUrl("href");
					if (!prodLink.isEmpty())
						prodLinks.add(prodLink);
				}

				// if (els.isEmpty() || els.size() % bufSize != 0) {
				// break;
				// }
			}
		} else {
			int indexPage = 2;
			do {
				String url = subCateLink + "?page=" + indexPage;
				doc = JsoupUtils.getDoc(url, this.sleepTime);
				els = doc.select(".produktdetailzeile .column-title a");
				for (Element el : els) {
					String prodLink = el.attr("href");
					if (!prodLink.isEmpty())
						prodLinks.add(prodLink);
				}

				if (els.isEmpty() || els.size() % bufSize != 0) {
					break;
				}

				indexPage++;
			} while (true);
		}
		logger.info(String.format("SubCateLink: %s , NumSubProd: %d", subCateLink, prodLinks.size()));

		return prodLinks;
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

	public void convertOutputToTsv() {
		String fProd =  "/home/thaonp/Desktop/ludw_QA/ludw_result_20170427/ludw.prod.json.ok.tsv";
		String fProdOk =  "/home/thaonp/Desktop/ludw_QA/ludw_result_20170427/ludw.prod.ok.tsv";
//		String fProd = fOut + "ludw.prod.ok.tsv";
//		String fProdOk = fOut + "ludw.prod.output.tsv";
		Set<String> setKeys = new HashSet<>();
		try (Stream<String> stream = Files.lines(Paths.get(fProd))) {
			stream.forEach(line -> {
				String toks[] = line.split("\t");
				if (toks.length == 6) {
					JSONObject jsonObj = new JSONObject(toks[5]);
					setKeys.addAll(jsonObj.keySet());
				}else{
					System.out.println(line);
				}
			});
		} catch (IOException e) {
			logger.error("File Not Found. " + fProd, e);
		}
		List<String> listKeys = new ArrayList<>();
		listKeys.addAll(setKeys);
		Collections.sort(listKeys);
		StringBuilder header = new StringBuilder();
		header.append(String.join("\t", "URL", "Title", "Description", "Outline", "Product_Variable",
				"Standardpreis inkl. MwSt.", "Preis exkl. MwSt.", "Preis inkl. MwSt."));
		header.append("\t" + String.join("\t", listKeys));
		DSFileUtils.write(header.toString(), fProdOk, false);
		try (Stream<String> stream = Files.lines(Paths.get(fProd))) {
			stream.forEach(line -> {
				String toks[] = line.split("\t");
				if (toks.length == 6) {
					StringBuilder builder = new StringBuilder();
					builder.append(String.join("\t", toks[0], toks[1], toks[2], toks[3]));
					JSONObject jsonObj = new JSONObject(toks[4]);
					builder.append("\t" + jsonObj.getString("Product Variable"));
					builder.append("\t" + jsonObj.getString("Standardpreis inkl. MwSt."));
					builder.append("\t" + jsonObj.getString("Preis exkl. MwSt."));
					builder.append("\t" + jsonObj.getString("Preis inkl. MwSt."));
					JSONObject jsonObjDetail = new JSONObject(toks[5]);
					for (String key : listKeys) {
						builder.append("\t");
						if (jsonObjDetail.has(key)) {
							builder.append(jsonObjDetail.get(key));
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
	// String url =
	// "https://www.ludwigmeister.de/produkt/gehaeusezubehoer/52162/skf-vierlippendichtung-fuer-stehlagergehaeuse-46737";
	// getProdsFromSubcate(url);
	// }

	public static void main(String[] args) {
		 args = new String[] { "-t", "convert" };
		 args = new String[] { "-t", "prod-info", "-i",
		 "data/ludw/ludw.prod.link.tsv" };
		LudwigmeisterCrawler ludwig = new LudwigmeisterCrawler();

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();

		Options options = new Options();
		options.addOption("t", "type", true, "{cate: get category, prod: get product}");
		options.addOption("i", "input", true, "file category links");
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			logger.error("Parse args false", e);
			formatter.printHelp("Ludwig", options);
		}

		if (!cmd.hasOption("t")) {
			logger.error("Don't have option type");
			formatter.printHelp("Ludwig", options);
			return;
		}
		String type = cmd.getOptionValue("t");
		if (type.equals("cate")) {
			ludwig.getCates();
		} else if (type.equals("prod")) {
			if (!cmd.hasOption("i")) {
				logger.error("Don't have option input");
				formatter.printHelp("Ludwig", options);
				return;
			}
			String sIn = cmd.getOptionValue("i");
			if (sIn == null) {
				logger.error("File input not null: " + sIn);
				formatter.printHelp("Ludwig", options);
				return;
			}
			File fIn = new File(sIn);
			if (!fIn.exists()) {
				logger.error("File not FOUND: " + sIn);
				formatter.printHelp("Ludwig", options);
				return;
			}
			ludwig.getProductLinks(fIn);
		} else if (type.equals("prod-info")) {
			if (!cmd.hasOption("i")) {
				logger.error("Don't have option input");
				formatter.printHelp("Ludwig", options);
				return;
			}
			String sIn = cmd.getOptionValue("i");
			if (sIn == null) {
				logger.error("File input not null: " + sIn);
				formatter.printHelp("Ludwig", options);
				return;
			}
			File fIn = new File(sIn);
			if (!fIn.exists()) {
				logger.error("File not FOUND: " + sIn);
				formatter.printHelp("Ludwig", options);
				return;
			}
			ludwig.getProdInfos(fIn);
		}else if (type.equals("convert")) {
			ludwig.convertOutputToTsv();
		}

		System.out.println("FINISH");
	}
}
