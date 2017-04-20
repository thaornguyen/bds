package vn.com.dsvn.crawl.bds.zillow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.com.dsvn.dto.ZillowArticle;
import vn.com.dsvn.utils.JsoupUtils;

public class ZillowTest {
	private static Logger logger = LoggerFactory.getLogger(ZillowTest.class);
	public String f = "data/zillow/count/zipcode.article.tsv";

	public void run() throws IOException {
		List<String> zipCodes = FileUtils.readLines(new File("data/zillow/zipCodeOnly.tsv"));
		int maxThread = 20;
		ExecutorService executorService = Executors.newFixedThreadPool(maxThread);
		for (String zipCode : zipCodes) {
			String link = "http://www.zillow.com/homes/" + zipCode + "_rb/";
			while (true) {
				int activeThread = ((ThreadPoolExecutor) executorService).getActiveCount();

				if (activeThread < maxThread) {
					Runnable worker = new ZillowThreadCount(link);
					executorService.execute(worker);
					break;
				}
				try {
					// Sleep to wait thread free
					Thread.sleep(1 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		executorService.shutdown();
	}

	public static void main(String[] args) {
		ZillowTest zillowSearch = new ZillowTest();
		try {
			zillowSearch.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class ZillowThreadCount implements Runnable {
		private String link;

		public ZillowThreadCount(String link) {
			this.link = link;
		}

		@Override
		public void run() {
			for (int i = 1; i <= 40; i++) {
				String url = link + i + "_p/";
				List<ZillowArticle> articles = getArticles(url);
				for (ZillowArticle art : articles) {
					try {
						FileUtils.write(new File(f), url + "\t" + art.getStatus() + "\t" + art.toSimpleJson() + "\n",
								true);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private String getTextNum(String text) {
			String text2 = text.replaceAll("[^0-9\\.]", "").trim();
			return text2;
		}

		private void write(ZillowArticle article, String file) {
			try {
				FileUtils.write(new File(file),
						article.getLink() + "\t" + article.getStatus() + "\t" + article.toSimpleJson() + "\n", true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private List<ZillowArticle> getArticles(String link) {
			String propertyInfoTag = ".property-info";
			String addressTag = ".property-address a[href*=homedetails]";
			String statusTag = ".listing-type.zsg-content_collapsed";
			String priceTag = ".price-large";
			String featureTag = ".property-data";

			List<ZillowArticle> articles = new ArrayList<ZillowArticle>();

			Document doc = JsoupUtils.getDoc(link);
			Elements infoEls = doc.select(propertyInfoTag);
			if (infoEls.size() == 0) {
				doc = JsoupUtils.getDoc(link);
				infoEls = doc.select(propertyInfoTag);
			}
			for (Element infoEl : infoEls) {
				String address = infoEl.select(addressTag).text();
				String url = infoEl.select(addressTag).attr("abs:href");
				if (url.isEmpty()) {
					continue;
				}
				String status = infoEl.select(statusTag).text();
				String price = infoEl.select(priceTag).text();
				long buyCost = 0;
				try {
					buyCost = Long.parseLong(price.replace("$", "").replace(",", "").trim());
				} catch (Exception e) {
					logger.error("PARSE COST FALSE: " + price + "\t" + link);
				}
				String info = infoEl.select(featureTag).text();
				ZillowArticle article = new ZillowArticle();
				article.setAddress(address);
				article.setLink(url);
				article.setBuyCost(buyCost);
				articles.add(article);
				article.setStatus(status);
				parseSimpleInfo(article, info);
			}
			return articles;
		}

		private void parseSimpleInfo(ZillowArticle article, String info) {
			String tks[] = info.split("•");
			for (String tk : tks) {
				String textNum = getTextNum(tk);

				if (tk.contains("bds")) {
					article.setNumBeds(textNum);
				} else if (tk.contains("ba")) {
					article.setNumBaths(textNum);
				} else if (tk.contains("sqft") && !tk.contains("lot")) {
					article.setSqft(textNum);
				} else if (tk.contains("sqft lot")) {
					article.setSqftLot(textNum);
				} else if (tk.contains("ac lot")) {
					article.setLotAc(textNum);
				} else if (tk.contains("Built")) {
					article.setBuiltYear(textNum);
				}
			}
		}
	}

}
