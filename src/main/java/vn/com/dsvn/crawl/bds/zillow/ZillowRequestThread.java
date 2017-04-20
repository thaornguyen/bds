package vn.com.dsvn.crawl.bds.zillow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import vn.com.dsvn.dto.ZillowLink;
import vn.com.dsvn.utils.JsoupUtils;

public class ZillowRequestThread {
	public String fInput;
	public String fOutput;
	public String tag;

	public ZillowRequestThread() {
//		 fInput = "data/zillow/home.tsv";
//		 fOutput = "data/zillow/state.tsv";
//		 tag = "a[href^=/browse/homes/]";

//		 fInput = "data/zillow/state.tsv";
//		 fOutput = "data/zillow/county.tsv";
//		 tag = "a[href^=/browse/homes/]";

		 fInput = "data/zillow/county.tsv";
		 fOutput = "data/zillow/zipCode.tsv";
		 tag = "a[href^=/browse/homes/]";

//		fInput = "data/zillow/zipCode.tsv";
//		fOutput = "data/zillow/street.tsv";
//		tag = "a[href^=/browse/homes/]";

//		fInput = "data/zillow/street.tsv";
//		fOutput = "data/zillow/house.tsv";
//		tag = "a[href*=homedetails]";//.property-address a[href*=homedetails]
	}

	public void run() throws IOException {
		File out = new File(fOutput);
		if (out.exists()) {
			FileUtils.forceDelete(new File(fOutput));
		}

		int maxThread = 20;
		ExecutorService executorService = Executors.newFixedThreadPool(maxThread);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(new File(fInput)), "UTF-8"));
		String line = "";
		while ((line = reader.readLine()) != null) {
//			BDSLink bds = BDSLink.toBDSLink(line);
//			executorService.execute(new ZillowThread(bds, tag));
			while (true) {
				int activeThread = ((ThreadPoolExecutor) executorService).getActiveCount();
				if (activeThread < maxThread) {
					ZillowLink bds = ZillowLink.toBDSLink(line);
					Runnable worker = new ZillowThread(bds, tag);
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
		reader.close();

		
	}

	public static void main(String[] args) {

		try {
			new ZillowRequestThread().run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class ZillowThread implements Runnable {
		private ZillowLink bds;
		private String tag;

		public ZillowThread(ZillowLink bds, String tag) {
			this.bds = bds;
			this.tag = tag;
		}

		public void run() {
			if (bds.getLink().contains("/browse/homes/")) {
				this.tag = this.tag.replace("/browse/homes/", bds.getLink().replace("http://www.zillow.com", ""));
			}
			Set<String> checkLinks = new HashSet<String>();
			Set<ZillowLink> bdsLinks = getLinks(bds.getLink(), this.tag);
			for (ZillowLink bd : bdsLinks) {
				String link = bd.getLink();
				if (checkLinks.contains(link) || link.equals("http://www.zillow.com/browse/homes/")) {
					continue;
				}
				bd.setName(bds.getName() + ":" + bd.getName());
				write(bd, fOutput);
				checkLinks.add(link);
			}
		}

		private void write(ZillowLink bdsLink, String file) {
			try {
				FileUtils.write(new File(file), bdsLink.toString() + "\n", true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private Set<ZillowLink> getLinks(String url, String tag) {
			Set<ZillowLink> links = new HashSet<ZillowLink>();
			try {
				Document doc = JsoupUtils.getDoc(url);
				Elements els = doc.select(tag);
				for (Element el : els) {
					String name = el.text();
					String link = el.absUrl("href");
					ZillowLink bdsLink = new ZillowLink(name, link);
					links.add(bdsLink);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return links;
		}

	}
}
