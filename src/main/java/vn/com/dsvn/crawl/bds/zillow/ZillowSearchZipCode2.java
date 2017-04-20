package vn.com.dsvn.crawl.bds.zillow;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.com.dsvn.utils.JsoupUtils;

public class ZillowSearchZipCode2 {
	private static Logger logger = LoggerFactory.getLogger(ZillowSearchZipCode2.class);
	public String f = "data/zillow/count/zipcode.statistic.tsv";

	public void run() throws IOException {
		List<String> zipCodes = FileUtils.readLines(new File("data/zillow/zipCodeOnly.tsv"));
		int maxThread = 20;
		ExecutorService executorService = Executors.newFixedThreadPool(maxThread);
		for (String zipCode : zipCodes) {
			// String link = "http://www.zillow.com/homes/" + zipCode + "_rb/";
			String link = "http://www.zillow.com/homes/for_sale/" + zipCode
					+ "/any_days/globalrelevanceex_sort/11_zm/0_mmm/";
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
		ZillowSearchZipCode2 zillowSearch = new ZillowSearchZipCode2();
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
			int count = getArticles(link);
			write(link + "\t" + count, f);
		}

		private int getArticles(String link) {
			String propertyInfoTag = "meta[name=description]";
			Document doc = JsoupUtils.getDoc(link);
			String content = doc.select(propertyInfoTag).attr("content");
			int count = 0;
			try {
				count = Integer.parseInt(content.split(" ")[2].replaceAll(",", ""));
			} catch (Exception e) {
				System.out.println(content);
			}

			return count;
		}

		private void write(String str, String file) {
			try {
				FileUtils.write(new File(file), str + "\n", true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
