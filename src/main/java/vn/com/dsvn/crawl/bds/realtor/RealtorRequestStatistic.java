package vn.com.dsvn.crawl.bds.realtor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.com.dsvn.dto.ZillowLink;
import vn.com.dsvn.utils.JsoupUtils;

public class RealtorRequestStatistic {
	private static Logger logger = LoggerFactory.getLogger(RealtorRequestStatistic.class);

	public void run() {
		int numReq = 0;
		String startUrl = "http://www.realtor.com/realestateforsale";
		String dir = "data/realtor";
		// Get State links
		String stateTag = ".link-secondary-color";
		Set<ZillowLink> stateLinks = getLinks(startUrl, stateTag);
		numReq++;
		write(stateLinks, dir + "/state.tsv");
		System.out.println("Number State: " + stateLinks.size());

		// Get county in State
		String countyTag = ".list-unstyled a";
		Set<ZillowLink> countyLinks = new HashSet<ZillowLink>();
		for (ZillowLink l : stateLinks) {
			Set<ZillowLink> bds = getLinks(l.getLink(), countyTag);
			numReq++;
			for (ZillowLink bd : bds) {
				bd.setName(l.getName() + "\t" + bd.getName());
				countyLinks.add(bd);
				write(bd, dir + "/county.tsv");
				break;
			}
			break;
		}
		System.out.println("Number County: " + countyLinks.size());

		for (ZillowLink l : countyLinks) {
			String link = l.getLink();
			Document doc = getDoc(link);
			sleep();
			numReq++;
			int numHouse = 0;
			try {
				numHouse = Integer.parseInt(
						doc.select("#search-result-count").text().replace("Homes", "").replace(",", "").trim());
			} catch (Exception e) {
				e.printStackTrace();
			}
			int numRequest = getNumRequest(numHouse);
			String log = l.getName() + "\t" + numHouse + "\t" + numRequest;
			write(log, dir + "/trulia_statistic_log.tsv");
			numReq += numRequest;
			break;
		}
		logger.info("Num Request: " + numReq);
	}

	private int getNumRequest(int numHouse) {
		return numHouse / 50 + 1;
	}

	private void write(String text, String file) {
		try {
			FileUtils.write(new File(file), text + "\n", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Set<ZillowLink> getLinks(String url, String tag) {
		Set<ZillowLink> links = new HashSet<ZillowLink>();
		try {
			Document doc = getDoc(url);
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
		sleep();
		return links;
	}

	private Document getDoc(String url) {
		Document doc = null;
		try {
			doc = Jsoup.parse(new URL(url), 10000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc;
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

	private void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		RealtorRequestStatistic crawler = new RealtorRequestStatistic();
		crawler.run();
	}

}
