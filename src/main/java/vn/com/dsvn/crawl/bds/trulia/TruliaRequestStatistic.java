package vn.com.dsvn.crawl.bds.trulia;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.com.dsvn.dto.ZillowLink;
import vn.com.dsvn.utils.JsoupUtils;

public class TruliaRequestStatistic {
	private static Logger logger = LoggerFactory.getLogger(TruliaRequestStatistic.class);

	public void run() {
		int numReq = 0;
		String startUrl = "http://www.trulia.com/sitemap/";
		String dir = "data/trulia";

		// Get State links
		String stateTag = "body > div.pbxxl.ptxxl.pbl.ptl > div a";
		Set<ZillowLink> stateLinks = getLinks(startUrl, stateTag);
		numReq++;
		write(stateLinks, dir + "/state.tsv");
		logger.info("Number State: " + stateLinks.size());
		logger.info("NumReq: " + numReq);
		// get real estate
		String[] abc = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
				"Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
		String nameEstateTag = "tbody > tr > td:nth-child(1) > div:nth-child(3) a";
		String numEstateTag = "tbody > tr > td:nth-child(1) > div:nth-child(3) .tiny";
		for (ZillowLink l : stateLinks) {
			String link = l.getLink();
			Set<String> checkLinks = new HashSet<String>();
			for (String a : abc) {
				String link2 = link + a;
				Document doc = JsoupUtils.getDoc(link2);
				numReq++;
				Elements elNames = doc.select(nameEstateTag);
				Elements elNums = doc.select(numEstateTag);
				if (elNames.size() != elNums.size()) {
					logger.error("DIFF NAME AND NUM: " + link2);
				} else {
					for (int i = 0; i < elNames.size(); i++) {
						String nameUrl = elNames.get(i).absUrl("href");
						if (checkLinks.contains(nameUrl)) {
							continue;
						}
						int num = 0;
						try {
							num = Integer.parseInt(
									elNums.get(i).text().replace("(", "").replace(")", "").replace(",", "").trim());
						} catch (Exception e) {
							e.printStackTrace();
						}
						int numRequest = getNumRequest(num);
						String log = l.getName() + "\t" + elNames.get(i).text() + "\t" + num + "\t" + numRequest;
						write(log, dir + "/trulia_statistic_log.tsv");
						numReq += numRequest;
						checkLinks.add(nameUrl);
					}
				}

			}
			logger.info("Num Request: " + numReq);
		}
		// logger.info("Number Real Estate: " + realEstateLinks.size());
		// logger.info("Num Request: " + numReq);

	}

	private int getNumRequest(int numHouse) {
		return numHouse / 60 + 1;
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

	private Set<ZillowLink> getLinks(Document doc, String tag) {
		Set<ZillowLink> links = new HashSet<ZillowLink>();
		try {
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

	private void write(Set<ZillowLink> bdsLinks, String file) {
		Set<String> ls = new HashSet<String>();
		for (ZillowLink l : bdsLinks) {
			ls.add(l.getLink() + "\t" + l.getName());
		}
		try {
			FileUtils.writeLines(new File(file), ls);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void write(ZillowLink bdsLink, String file) {
		try {
			FileUtils.write(new File(file), bdsLink.toString() + "\n", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void write(String text, String file) {
		try {
			FileUtils.write(new File(file), text + "\n", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		TruliaRequestStatistic crawler = new TruliaRequestStatistic();
		crawler.run();
	}

}
