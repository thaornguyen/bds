package vn.com.dsvn.crawl.bds.trulia;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import vn.com.dsvn.dto.ZillowLink;
import vn.com.dsvn.utils.JsoupUtils;

public class TruliaCrawlerLink {

	public void run() {
		String startUrl = "http://www.trulia.com/sitemap/";
		String dir = "data/trulia";
		// Get State links
		String stateTag = "body > div.pbxxl.ptxxl.pbl.ptl > div a";
		Set<ZillowLink> stateLinks = getLinks(startUrl, stateTag);
		write(stateLinks, dir + "/state.tsv");
		System.out.println("Number State: " + stateLinks.size());

		// get real estate
		String[] abc = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
				"Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
		String realEstateTag = "tbody > tr > td:nth-child(1) > div:nth-child(3) a";
		Set<ZillowLink> realEstateLinks = new HashSet<ZillowLink>();
		for (ZillowLink l : stateLinks) {
			String link = l.getLink();
			for (String a : abc) {
				String link2 = link + "/" + a;
				Set<ZillowLink> bds = getLinks(link2, realEstateTag);
				for (ZillowLink bd : bds) {
					if (!realEstateLinks.contains(bd)) {
						bd.setName(l.getName() + ":" + a + ":" + bd.getName());
						realEstateLinks.add(bd);
						write(bd, dir + "/realEstate.tsv");
					}
				}
			}
		}
		System.out.println("Number Real Estate: " + realEstateLinks.size());

		String lastPageTag = "#photoPagination > div:nth-child(1) > div.mts > a:nth-child(4)";
		String houseTag = ".primaryLink.pdpLink";
		Set<ZillowLink> houseLinks = new HashSet<ZillowLink>();

		for (ZillowLink l : realEstateLinks) {
			String link = l.getLink();
			for (int i = 1; i < 10000; i++) {
				String link2 = link + i + "_p";
				Document doc = JsoupUtils.getDoc(link2,0);
				if (doc == null) {
					break;
				}

				Set<ZillowLink> bds = getLinks(doc, houseTag);
				for (ZillowLink bd : bds) {
					if (!houseLinks.contains(bd)) {
						bd.setName(l.getName() + ":" + bd.getName());
						houseLinks.add(bd);
						write(bd, dir + "/house.tsv");
					}
				}
				if (doc.select(lastPageTag).size() == 0) {
					break;
				}
			}

		}

	}

	private Set<ZillowLink> getLinks(String url, String tag) {
		Set<ZillowLink> links = new HashSet<ZillowLink>();
		try {
			Document doc = JsoupUtils.getDoc(url,0);
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

	public static void main(String[] args) {
		TruliaCrawlerLink crawler = new TruliaCrawlerLink();
		crawler.run();
	}

}
