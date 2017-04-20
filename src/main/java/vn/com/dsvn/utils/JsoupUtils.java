package vn.com.dsvn.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsoupUtils {
	public static final String USER_AGENT = "Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 "
			+ "(KHTML, like Gecko) Chrome/30.0.1599.114 Safari/537.36";
	public static final int TIMEOUT = 30000;
	private static final int numMiniSecond = 0;
	static Logger logger = LoggerFactory.getLogger(JsoupUtils.class);

	/**
	 * Get HTML of url with some marker
	 * 
	 * @param url
	 * @return html text with marker
	 */
	public static String getHtml(String url) {
		try {
			return getDoc(url, null).html();
		} catch (Exception e) {
			return "";
		}
	} // end method

	public static Document getDoc(String url) {
		return getDoc(url, null);
	}

	public static Document getDoc(String url, Map<String, String> headers) {
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			logger.error("ERROR_URL :" + url, e);
			sleep(numMiniSecond);
			return null;
		}
		Document doc = null;
		long start = 0, finish = 0;
		try {
			start = System.currentTimeMillis();
			Connection connect = Jsoup.connect(uri.toASCIIString());
			if (headers != null) {
				for (Map.Entry<String, String> header : headers.entrySet()) {
					connect.header(header.getKey(), header.getValue());
				}
			}

			doc = connect.userAgent(USER_AGENT).timeout(TIMEOUT).get();
			finish = System.currentTimeMillis();
			logger.info(String.format("REQUEST_URL (%d ms): %s", (finish - start), url));
		} catch (IOException e) {
			String err = e.toString();
			if (err.contains("timed out")) {
				finish = System.currentTimeMillis();
				// logger.error(String.format("REQUEST_TIMEOUT (%d ms): %s",
				// (finish - start), url));
				try {
					start = System.currentTimeMillis();
					doc = Jsoup.connect(uri.toASCIIString()).userAgent(USER_AGENT).timeout(TIMEOUT).get();
					finish = System.currentTimeMillis();
					logger.info(String.format("REQUEST_URL (%d ms): %s", (finish - start), url));
				} catch (IOException e1) {
					finish = System.currentTimeMillis();
					logger.info(String.format("REQUEST_URL (%d ms): %s", (finish - start), url));
					logger.error(String.format("REQUEST_TIMEOUT (%d ms): %s", (finish - start), url), e1);
					sleep(numMiniSecond);
					return null;
				}
			} else {
				String status = err.replaceAll(".*Status=(\\d+).*", "$1");
				finish = System.currentTimeMillis();
				logger.info(String.format("REQUEST_URL (%d ms): %s", (finish - start), url));
				logger.error(String.format("ERROR_%s (%d ms): %s", status, (finish - start), url));
			}
		}
		sleep(numMiniSecond);
		return doc;
	}

	public static Document getPostDoc(String url, Map<String, String> headers, Map<String, String> datas) {
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			logger.error(String.format("ERROR_URL :%s, Data: %s", url, datas), e);
			sleep(numMiniSecond);
			return null;
		}
		Document doc = null;
		long start = 0, finish = 0;
		try {
			start = System.currentTimeMillis();
			Connection connect = Jsoup.connect(uri.toASCIIString());
			if (headers != null) {
				for (Map.Entry<String, String> header : headers.entrySet()) {
					connect.header(header.getKey(), header.getValue());
				}
			}
			if (datas != null) {
				connect = connect.data(datas);
			}
			doc = connect.userAgent(USER_AGENT).timeout(TIMEOUT).post();
			finish = System.currentTimeMillis();
			logger.info(String.format("REQUEST_URL (%d ms): %s", (finish - start), url));
		} catch (IOException e) {
			String err = e.toString();
			if (err.contains("timed out")) {
				finish = System.currentTimeMillis();
				// logger.error(String.format("REQUEST_TIMEOUT (%d ms): %s",
				// (finish - start), url));
				try {
					start = System.currentTimeMillis();
					Connection connect = Jsoup.connect(uri.toASCIIString());
					if (headers != null) {
						for (Map.Entry<String, String> header : headers.entrySet()) {
							connect.header(header.getKey(), header.getValue());
						}
					}
					if (datas != null) {
						connect = connect.data(datas);
					}
					doc = connect.userAgent(USER_AGENT).timeout(TIMEOUT).post();
					finish = System.currentTimeMillis();
					logger.info(String.format("REQUEST_URL (%d ms): %s", (finish - start), url));
				} catch (IOException e1) {
					finish = System.currentTimeMillis();
					logger.info(String.format("REQUEST_URL (%d ms): %s", (finish - start), url));
					logger.error(String.format("REQUEST_TIMEOUT (%d ms): %s. Data: %s", (finish - start), url, datas),
							e1);
					sleep(numMiniSecond);
					return null;
				}
			} else {
				String status = err.replaceAll(".*Status=(\\d+).*", "$1");
				finish = System.currentTimeMillis();
				logger.info(String.format("REQUEST_URL (%d ms): %s", (finish - start), url));
				logger.error(String.format("ERROR_%s (%d ms): %s", status, (finish - start), url));
			}
		}
		sleep(numMiniSecond);
		return doc;
	}

	public static void sleep(int numMnSecond) {
		try {
			Thread.sleep(numMnSecond);
			logger.info("Sleep time: " + numMnSecond + " (ms)");
		} catch (InterruptedException e) {
			logger.error("ERROR SLEEP", e);
		}
	}
}
