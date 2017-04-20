package vn.com.dsvn.crawl.bds.zillow;

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

public class ZillowRequestStatistic {

	public void run() {
		int numReq = 0;
		String dir = "data/zillow";
		String startUrl = "http://www.zillow.com/browse/homes/";

		// Get State Link
		String stateTag = "a[href^=/browse/homes]";
		Set<ZillowLink> stateLinks = getLinks(startUrl, stateTag);
		numReq++;
		for (ZillowLink l : stateLinks) {
			if (l.getName().equals("Washington, DC")) {
				l.setLink("http://www.zillow.com/browse/homes/dc/");
			}
		}
		write(stateLinks, dir + "/state.tsv");

		// Get CountyKS links
		String countyTag = "a[href^=/browse/homes]";
		Set<ZillowLink> countyLinks = new HashSet<ZillowLink>();
		for (ZillowLink l : stateLinks) {
			countyTag = countyTag.replace("/browse/homes/", l.getLink().replace("http://www.zillow.com", ""));
			Set<ZillowLink> bds = getLinks(l.getLink(), countyTag);
			numReq++;
			for (ZillowLink bd : bds) {
				bd.setName(l.getName() + "\t" + bd.getName());
				countyLinks.add(bd);
				write(bd, dir + "/county.tsv");
			}
		}

		System.out.println("Number County: " + countyLinks.size());

		// Get ZipCode in CountyKS
		String zipCodeTag = "a[href^=/browse/homes]";
		Set<ZillowLink> zipCodeLinks = new HashSet<ZillowLink>();
		for (ZillowLink l : countyLinks) {
			zipCodeTag = zipCodeTag.replace("/browse/homes/", l.getLink().replace("http://www.zillow.com", ""));
			Set<ZillowLink> bds = getLinks(l.getLink(), zipCodeTag);
			numReq++;
			for (ZillowLink bd : bds) {
				bd.setName(l.getName() + "\t" + bd.getName());
				zipCodeLinks.add(bd);
				write(bd, dir + "/zipCode.tsv");
			}
		}
		System.out.println("Number ZipCode: " + zipCodeLinks.size());
		
		// Get Streets in ZipCode
		String streetTag = "a[href^=/browse/homes]";
		Set<ZillowLink> streetLinks = new HashSet<ZillowLink>();
		for (ZillowLink l : zipCodeLinks) {
			streetTag = streetTag.replace("/browse/homes/", l.getLink().replace("http://www.zillow.com", ""));
			Set<ZillowLink> bds = getLinks(l.getLink(), streetTag);
			numReq++;
			for (ZillowLink bd : bds) {
				bd.setName(l.getName() + "\t" + bd.getName());
				streetLinks.add(bd);
				write(bd, dir + "/street.tsv");
			}
		}
		System.out.println("Number Streets: " + streetLinks.size());
		System.out.println("NumReq: "+(numReq+streetLinks.size()));
		// Get Houses and Buildings in Streets
//		String houseTag = "a[href*=homedetails]";
//		Set<BDSLink> houseLinks = new HashSet<BDSLink>();
//		for (BDSLink l : streetLinks) {
//			Set<BDSLink> bds = getLinks(l.getLink(), houseTag);
//			numReq++;
//			for (BDSLink bd : bds) {
//				bd.setName(l.getName() + "\t" + bd.getName());
//				houseLinks.add(bd);
//				write(bd, dir + "/house.tsv");
//			}
//		}
//		// write(houseLinks, dir + "/house.tsv");
//		System.out.println("Number Houses: " + houseLinks.size());
//		System.out.println(numReq);

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

	private void write(Set<ZillowLink> bdsLinks, String file) {
		Set<String> ls = new HashSet<String>();
		for (ZillowLink l : bdsLinks) {
			ls.add(l.getName() + "\t" + l.getLink());
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
		ZillowRequestStatistic crawler = new ZillowRequestStatistic();
		crawler.run();
	}

}
