package vn.com.dsvn.crawl.bds.realtor;

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

public class RealtorCrawlerLink {

	public void run() {
		String startUrl = "http://www.realtor.com/realestateforsale";
		String dir = "data/realtor";
		// Get State links
		String stateTag = ".link-secondary-color";
		Set<ZillowLink> stateLinks = getLinks(startUrl, stateTag);
		write(stateLinks, dir + "/state.tsv");
		System.out.println("Number State: " + stateLinks.size());

		// Get county in State
		String countyTag = ".list-unstyled a";
		Set<ZillowLink> countyLinks = new HashSet<ZillowLink>();
		for (ZillowLink l : stateLinks) {
			Set<ZillowLink> bds = getLinks(l.getLink(), countyTag);
			for (ZillowLink bd : bds) {
				bd.setName(l.getName() + ":" + bd.getName());
				countyLinks.add(bd);
				write(bd, dir + "/county.tsv");
			}
//			break;
		}
		System.out.println("Number County: " + countyLinks.size());

		for (ZillowLink l : countyLinks) {
			String link = l.getLink();
			Document doc = null;
			int index = 1;
			while ((doc = JsoupUtils.getDoc(link+"/pg-"+index,0)).hasClass(".page")) {
				System.out.println(index);
				index++;
			}
			System.out.println(link+"---"+index);
		}

		// Get Streets in ZipCode
		String streetTag = "a[href^=/browse/homes/ks]";
		Set<ZillowLink> streetLinks = new HashSet<ZillowLink>();
		for (ZillowLink l : countyLinks) {
			Set<ZillowLink> bds = getLinks(l.getLink(), streetTag);
			for (ZillowLink bd : bds) {
				bd.setName(l.getName() + ":" + bd.getName());
				streetLinks.add(bd);
				write(bd, dir + "/street.tsv");
			}
		}
		// write(streetLinks, dir + "/street.tsv");
		System.out.println("Number Streets: " + streetLinks.size());

		// Get Houses and Buildings in Streets
		String houseTag = "a[href*=homedetails]";
		Set<ZillowLink> houseLinks = new HashSet<ZillowLink>();
		for (ZillowLink l : streetLinks) {
			Set<ZillowLink> bds = getLinks(l.getLink(), houseTag);
			for (ZillowLink bd : bds) {
				bd.setName(l.getName() + ":" + bd.getName());
				houseLinks.add(bd);
				write(bd, dir + "/house.tsv");
			}
		}
		// write(houseLinks, dir + "/house.tsv");
		System.out.println("Number Houses: " + houseLinks.size());

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
		RealtorCrawlerLink crawler = new RealtorCrawlerLink();
		crawler.run();
	}

}
