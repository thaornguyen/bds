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

public class EtsuranCrawlerId {
	public Logger logger = Logger.getLogger(EtsuranThreadID.class);

	public void run() {
		ExecutorService executorService = Executors.newFixedThreadPool(10);
//		String x[] = new String[] { "280", "359", "364" };
		int maxThread = 20;
		try {
			for (int i = 0; i < 580; i++) {
				// for (String xx : x) {
				while (true) {
					int activeThread = ((ThreadPoolExecutor) executorService).getActiveCount();

					if (activeThread < maxThread) {
						EtsuranThreadID runable = new EtsuranThreadID(i + "");
						executorService.execute(runable);
						break;
					}
					try {
						// Sleep to wait thread free
						Thread.sleep(1 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
//				break;
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

	public static void main(String[] args) {
		new EtsuranCrawlerId().run();
	}

	class EtsuranThreadID implements Runnable {
		private final String USER_AGENT = "Mozilla/5.0";
		private String fId = "etsuran_id.txt";
		private String fJson = "etsuran_json.txt";
		private String numPage;

		public EtsuranThreadID(String numPage) {
			this.numPage = numPage;
		}

		@Override
		public void run() {
			String url = "http://etsuran.mlit.go.jp/TAKKEN/kensetuSearchNext.do";
			Map<String, String> datas = new HashMap<>();
			datas.put("hidDispCount", "1000");
			datas.put("hidPageNo", numPage);
			Document doc = null;
			try {
				doc = Jsoup.connect(url).data(datas).postDataCharset("UTF-8").userAgent(USER_AGENT).post();
			} catch (IOException e1) {
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					doc = Jsoup.connect(url).data(datas).postDataCharset("UTF-8").userAgent(USER_AGENT).post();
				} catch (IOException e) {
					logger.error("Get Document error: " + numPage);
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e11) {
						e11.printStackTrace();
					}
					return;
				}
			}
			Elements els = doc.select(".re_disp tr");
			List<String> ids = new ArrayList<>();
			List<String>jsons = new ArrayList<>();
			for (Element el : els) {
				String attr = el.select(" td:nth-child(4) > a").attr("onclick");
				attr = attr.replace("js_ShowDetail('", "").replace("')", "");
				if (!attr.isEmpty()){
					ids.add(attr);
				}else{
					continue;
				}
				
				JSONObject jsonObject = new JSONObject();
				String x1 = el.select("td:nth-child(2)").text();
				String x2 = el.select("td:nth-child(3)").text();
				String x3 = el.select("td:nth-child(4)").text();
				String x4 = el.select("td:nth-child(5)").text();
				String x5 = el.select("td:nth-child(6)").text();
				String x6 = el.select("td:nth-child(7)").text();
				jsonObject.put("ID", attr);
				jsonObject.put("許可行政庁", x1);
				jsonObject.put("許可番号", x2);
				jsonObject.put("商号又は名称", x3);
				jsonObject.put("代表者名", x4);
				jsonObject.put("営業所名", x5);
				jsonObject.put("所在地", x6);
				jsons.add(jsonObject.toString());
			}
			if (els.size() == 0) {
				logger.info("Numpage " + numPage + " empty!");
			}
			try {
				FileUtils.writeLines(new File(fId), ids, true);
				FileUtils.writeLines(new File(fJson), jsons, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {

				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.info("OK: " + numPage);
		}

	}
}
