package vn.com.dsvn.crawl.bds.zillow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import vn.com.dsvn.dto.ZillowArticle;
import vn.com.dsvn.dto.Config;
import vn.com.dsvn.utils.JsoupUtils;

public class ZillowCrawlerArticle {
	private static Logger logger = LoggerFactory.getLogger(ZillowCrawlerArticle.class);
	private Config config;

	public ZillowCrawlerArticle() {
		PropertyConfigurator.configure("conf/log4j.properties");
		try {
			config = Config.parse(new File("conf/zillow_struct.conf"));
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		ExecutorService executor = Executors.newFixedThreadPool(20);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(new File("data/zillow/house.tsv")), "UTF-8"));
			String line = "";
			while ((line = reader.readLine()) != null) {
				String link = line.split("\t")[0];
				Runnable taskOne = new MyThread(link);
				executor.execute(taskOne);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		executor.shutdown();
	}

	private String getValueTag(Document doc, String tag, String url) {
		String value = "";
		Elements els = doc.select(tag);
		if (els.size() > 0) {
			value = els.first().text();
		} else {
			logger.error("FALSE TAG: " + tag + " URL: " + url);
		}
		return value;
	}

	public static void main(String[] args) {
		ZillowCrawlerArticle zillowArticle = new ZillowCrawlerArticle();
		zillowArticle.run();

		// new ZillowCrawlerArticle().new MyThread(
		// "http://www.zillow.com/homedetails/355-I-St-SW-APT-212-Washington-DC-20024/2116006804_zpid/").run();

	}

	public class MyThread implements Runnable {
		private String link;

		public MyThread(String link) {
			this.link = link;
		}

		public void run() {
			ZillowArticle bdsArticle = getArticle(link);
			try {
				String x = link + "\t" + bdsArticle.toJson() + "\n";
				FileUtils.write(new File("data/zillow/bds.data.json.tsv"), x, true);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public ZillowArticle getArticle(String link) {
			ZillowArticle article = new ZillowArticle();
			Document doc = JsoupUtils.getDoc(link);

			article.setLink(link);
			// get Address
			String address = getValueTag(doc, config.getAddressTag(), link);
			article.setAddress(address);

			// get number Beds, number Baths,square foot
			String textRoom = getValueTag(doc, config.getRoomTag(), link);

			String numBeds = getNumBedBathSqft(textRoom, "bed");
			if (!numBeds.isEmpty()) {
				article.setNumBeds(numBeds);
			}

			String numBaths = getNumBedBathSqft(textRoom, "bath");
			if (!numBaths.isEmpty()) {
				article.setNumBaths(numBaths);
			}

			String sqfts = getNumBedBathSqft(textRoom, "sqft");
			if (!sqfts.isEmpty()) {
				article.setSqft(sqfts);
			}

			// get Description
			String des = getValueTag(doc, config.getDesTag(), link);
			article.setDescription(des);

			// get BuyCost
			String strBuyCost = getValueTag(doc, config.getBuyCostTag(), link);
			strBuyCost = strBuyCost.replace("$", "").replace(",", "");
			try {
				long cost = Long.parseLong(strBuyCost);
				article.setBuyCost(cost);
			} catch (Exception e) {
				article.setBuyCost(-1);
			}

			// get BuyCost
			String strRentCost = getValueTag(doc, config.getRentCostTag(), link);
			strRentCost = strRentCost.replace("$", "").replace(",", "").split("/")[0];
			try {
				long cost = Long.parseLong(strRentCost);
				article.setRentCost(cost);
			} catch (Exception e) {
				article.setBuyCost(-1);
			}
			// parse house detail
			parseHouseDetail(article, doc, link);

			return article;
		}

		private String getNumBedBathSqft(String textRoom, String name) {
			// textRoom = textRoom.replace(",", "");
			String num = "";
			if (textRoom.contains(name)) {
				String regex = "([,.\\d]+)\\s+" + name;
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(textRoom);
				if (matcher.find()) {
					String t = matcher.group().replace(name, "").trim();
					// try {
					// num = Integer.parseInt(t);
					// } catch (Exception e) {
					// }
					num = t;
				}
			}
			return num;
		}

		private void parseHouseDetail(ZillowArticle article, Document doc, String link) {
			Elements els = doc.select(config.getGroupFeatureTag());
			if (els.size() == 0) {
				logger.error("FALSE TAG: " + config.getGroupFeatureTag() + " URL: " + link);
			}
			for (Element el : els) {
				String text = el.text();
				Elements subEls = el.select(config.getFeatureTag());
				if (subEls.size() == 0) {
					logger.error("FALSE TAG: " + config.getFeatureTag() + " URL: " + link);
				}
				if (text.contains("Facts")) {
					article.setFacts(parseJsonInfo(subEls));
				} else if (text.contains("Additional")) {
					if (subEls.size() > 0) {
						article.setAdditionalFeatures(subEls.first().text());
					}
				} else if (text.contains("Features")) {
					article.setFeatures(parseJsonInfo(subEls));
				} else if (text.contains("Open House")) {
					if (subEls.size() > 0) {
						article.setOpenHouse(subEls.first().text());
					}
				} else if (text.contains("Appliance")) {
					article.setAppliancesIncluded(parseJsonInfo(subEls));
				} else if (text.contains("Room Types")) {
					article.setRoomTypes(parseJsonInfo(subEls));
				} else if (text.contains("Construction")) {
					article.setConstruct(parseJsonInfo(subEls));
				} else if (text.contains("Other")) {
					article.setOthers(parseJsonInfo(subEls));
				} else {
					logger.error("Not parse: " + text);
				}
			}
		}

		private JSONObject parseJsonInfo(Elements els) {
			JSONObject jsonObject = new JSONObject();
			for (Element el : els) {
				String text = el.text().trim();
				if (text.contains(":")) {
					String[] tks = text.split(":");
					if (tks.length > 1) {
						jsonObject.put(tks[0].trim(), tks[1].trim());
					}
				} else {
					if (text.contains("Built in")) {
						String strBuildYear = text.replaceAll("[^0-9]", "").trim();
						jsonObject.put("Built year", strBuildYear);
					} else {
						jsonObject.put(text, "Yes");
					}
				}
			}
			return jsonObject;
		}
	}
}
