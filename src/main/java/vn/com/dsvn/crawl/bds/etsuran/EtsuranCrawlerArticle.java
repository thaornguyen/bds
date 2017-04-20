package vn.com.dsvn.crawl.bds.etsuran;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class EtsuranCrawlerArticle {
	private static Logger logger = Logger.getLogger(EtsuranCrawlerArticle.class);

	public static void main(String[] args) {
		args = new String[] { "etsuran_id.txt" };
		List<String> lines = new ArrayList<>();
		try {
			lines = FileUtils.readLines(new File(args[0]));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// lines.add("00012131");
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		int maxThread = 20;
		try {
			for (String line : lines) {
				while (true) {
					int activeThread = ((ThreadPoolExecutor) executorService).getActiveCount();

					if (activeThread < maxThread) {
						Runnable runable = new EtsuranThreadArticle(line);
						executorService.execute(runable);
						break;
					}
					try {
						// Sleep to wait thread free
						Thread.sleep(2 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} finally {
			executorService.shutdown();
		}
		try {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			logger.info("FINISH APP");
			logger.info("============================================================");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class EtsuranThreadArticle implements Runnable {
	private final String USER_AGENT = "Mozilla/5.0";
	private Logger logger = Logger.getLogger(EtsuranThreadArticle.class);
	private String fId = "etsuran_articles.txt";
	private String id;

	public EtsuranThreadArticle(String id) {
		this.id = id;
	}

	@Override
	public void run() {
		String url = "http://etsuran.mlit.go.jp/TAKKEN/kensetuSearchGaiyo.do";
		Map<String, String> datas = new HashMap<>();
		datas.put("hidLicenseNo", id);
		Document doc = null;
		try {
			doc = Jsoup.connect(url).data(datas).postDataCharset("UTF-8").userAgent(USER_AGENT).post();
		} catch (IOException e1) {
			try {
				Thread.sleep(300000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				doc = Jsoup.connect(url).data(datas).postDataCharset("UTF-8").userAgent(USER_AGENT).post();
			} catch (IOException e) {
				logger.error("Get Document error: " + id);
				try {
					Thread.sleep(300000);
				} catch (InterruptedException e11) {
					e11.printStackTrace();
				}
				return;
			}
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("ID", id);

		Elements els = doc.select(".re_summ>tbody>tr");
		for (Element el : els) {
			String key = el.select("th").text();
			String value = el.select("td").text();
			if (!key.isEmpty() && !value.isEmpty()) {
				jsonObject.put(key, value);
			}
		}
		els = doc.select(".re_summ_2>tbody>tr");
		for (Element el : els) {
			String key = el.select("th").text();
			String value = el.select("td").text();
			if (!key.isEmpty() && !value.isEmpty()) {
				jsonObject.put(key, value);
			}
		}
		try {
			FileUtils.write(new File(fId), jsonObject.toString() + "\n", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("OK " + id);
	}

}