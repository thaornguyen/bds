package vn.com.dsvn.crawl.mechanical.ludwigmeister;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import vn.com.dsvn.utils.DSFileUtils;
import vn.com.dsvn.utils.JsoupUtils;

public class LudwigmeisterCrawler {
	private String domain = "https://www.ludwigmeister.de/de";
	private String fOut = "data/ludw/";
	private int numProd = 0;
	private int numVar = 0;

	public LudwigmeisterCrawler() {
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "100");
	}

	private void run() {

	}

	public void getCates() {
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
							DSFileUtils.write(urlSubCate4, fOut + "ludw.cate.txt", true);
						});
					}
				});

			});
		});
	}

	/**
	 * @description https://www.ludwigmeister.de/de ->
	 *              https://www.ludwigmeister.de/produkte/antriebstechnik/38075
	 * @author thaonp
	 * @return
	 * @since 1.0.0
	 * @throws TODO
	 */

	private List<String> parseCates() {
		List<String> cateUrls = new ArrayList<>();

		Document doc = JsoupUtils.getDoc(this.domain);
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

	private List<String> parseSubCates(String cateUrl) {
		List<String> subCateUrls = new ArrayList<>();

		Document doc = JsoupUtils.getDoc(cateUrl);
		if (doc == null) {
			return subCateUrls;
		}
		Elements els = doc.select(".subkategorien .lowprofile");
		els.forEach(el -> {
			String subCateUrl = el.absUrl("href");
			subCateUrls.add(subCateUrl);
		});
		return subCateUrls;
	}

	private boolean isSubCate(String prodUrl) {
		Document doc = JsoupUtils.getDoc(prodUrl);
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

	public static void main(String[] args) {
		LudwigmeisterCrawler ludwig = new LudwigmeisterCrawler();
		ludwig.getCates();
		System.out.println("FINISH");
	}
}
