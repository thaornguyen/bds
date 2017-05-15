package vn.com.dsvn.crawl.mechanical.ludwigmeister;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.com.dsvn.utils.DSFileUtils;
import vn.com.dsvn.utils.JsoupUtils;

public class LudwigDesCrawler {
	private static final Logger logger = LoggerFactory.getLogger(LudwigDesCrawler.class);
	private String fOut = "data/ludw/";

	private void run(String sLink) {
		File fProd = new File(sLink);
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
			logger.error("Read File FAIL. File: " + sLink + fProd, e);
		}

	}

	public void parseProd(String prodLink) {
//		String html = JsoupUtils.getHtml(prodLink, 2000);
//		Document doc = JsoupUtils.getDocBySource(html);
		Document doc = JsoupUtils.getDoc(prodLink, 2000);
		if (doc == null) {
			logger.error(String.format("Parse Document FAIL. Link: %s", prodLink));
			return;
		}
		String desc = doc.select(".artikelbezeichnung,.artikelnummer,.dddwrapper").text();
		desc = desc.replaceAll("\t", " ").replaceAll("\n", " ");
		DSFileUtils.write(String.join("\t", prodLink, desc), fOut + "ludw.prod.desc.tsv", true);
	}

	public static void main(String[] args) {
		LudwigDesCrawler ludw = new LudwigDesCrawler();
		ludw.run(args[0]);
	}

}
