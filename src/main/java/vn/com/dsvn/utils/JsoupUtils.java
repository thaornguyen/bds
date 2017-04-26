package vn.com.dsvn.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.fluent.Request;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsoupUtils {
	public static final String USER_AGENT = "Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 "
			+ "(KHTML, like Gecko) Chrome/30.0.1599.114 Safari/537.36";
	public static final int TIMEOUT = 30000;
	// private static final int numMiniSecond = 2000;
	static Logger logger = LoggerFactory.getLogger(JsoupUtils.class);

	/**
	 * Get HTML of url with some marker
	 * 
	 * @param url
	 * @return html text with marker
	 */
	public static String getHtml(String url, int sleepTime) {
		long start = System.currentTimeMillis();
		try {
			Request request = Request.Get(url);
			sleep(2000);
			String html = request.userAgent(USER_AGENT).connectTimeout(TIMEOUT).execute().returnContent()
					.asString(Charset.forName("UTF-8"));
			sleep(sleepTime);
			long finish = System.currentTimeMillis();
			logger.info(String.format("REQUEST_URL (%d ms): %s", (finish - start), url));
			return html;
		} catch (Exception e) {
			long finish = System.currentTimeMillis();
			String err = e.toString();
			String status = err.replaceAll(".*Status=(\\d+).*", "$1");
			logger.error(String.format("ERROR_%s (%d ms): %s", status, (finish - start), url), e);
			return "";
		}
	} // end

	public static String getHtmlByPhantom(String url, int sleepTime) {
		long start = System.currentTimeMillis();
		try {
			WebDriver d = new PhantomJSDriver();
			d.get(url);
			d.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);

			// Predicate<WebDriver> pageLoaded = wd -> ((JavascriptExecutor) wd)
			// .executeScript("return document.readyState").equals("complete");
			// new FluentWait<WebDriver>(d).until(pageLoaded);
			String html = d.getPageSource();
			long finish = System.currentTimeMillis();
			logger.info(String.format("REQUEST_URL (%d ms): %s", (finish - start), url));
			d.close();
			return html;
		} catch (Exception e) {
			long finish = System.currentTimeMillis();
			String err = e.toString();
			String status = err.replaceAll(".*Status=(\\d+).*", "$1");
			logger.error(String.format("ERROR_%s (%d ms): %s", status, (finish - start), url), e);
			return "";
		}
	} // end

	public static String getHtmlByPhantom(String url, WebDriver d, int sleepTime) {
		long start = System.currentTimeMillis();
		try {
			d.get(url);
			// d.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);

			// Predicate<WebDriver> pageLoaded = wd -> ((JavascriptExecutor) wd)
			// .executeScript("return document.readyState").equals("complete");
			// new FluentWait<WebDriver>(d).until(pageLoaded);

			// WebDriverWait wait = new WebDriverWait(d, 30);
			//
			// wait.until(new ExpectedCondition<Boolean>() {
			// public Boolean apply(WebDriver wdriver) {
			// return ((JavascriptExecutor) d).executeScript(
			// "return document.readyState"
			// ).equals("complete");
			// }
			// });

			// long end = System.currentTimeMillis() + 5000;
			// while (System.currentTimeMillis() < end) {
			// // Browsers which render content (such as Firefox and IE) return
			// "RenderedWebElements"
			// RenderedWebElement resultsDiv = (RenderedWebElement)
			// driver.findElement(By.className("gac_m"));
			//
			// // If results have been returned, the results are displayed in a
			// drop down.
			// if (resultsDiv.isDisplayed()) {
			// break;
			// }
			// }
			d.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
			String html = d.getPageSource();
			long finish = System.currentTimeMillis();
			logger.info(String.format("REQUEST_URL (%d ms): %s", (finish - start), url));
			sleep(sleepTime);
			// d.close();
			return html;
		} catch (Exception e) {
			long finish = System.currentTimeMillis();
			String err = e.toString();
			String status = err.replaceAll(".*Status=(\\d+).*", "$1");
			logger.error(String.format("ERROR_%s (%d ms): %s", status, (finish - start), url), e);
			return "";
		}
	} // end

	public static Document getDocBySource(String source) {
		Document doc = Jsoup.parse(source);
		return doc;
	}

	public static Document getDoc(String url, int sleepTime) {
		return getDoc(url, null, sleepTime);
	}

	public static Document getDoc(String url) {
		return getDoc(url, null, 2000);
	}

	public static Document getDoc(String url, Map<String, String> headers, int sleepTime) {
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			logger.error("ERROR_URL :" + url, e);
			sleep(sleepTime);
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
					sleep(sleepTime);
					return null;
				}
			} else {
				String status = err.replaceAll(".*Status=(\\d+).*", "$1");
				finish = System.currentTimeMillis();
				logger.info(String.format("REQUEST_URL (%d ms): %s", (finish - start), url));
				logger.error(String.format("ERROR_%s (%d ms): %s", status, (finish - start), url));
			}
		}
		sleep(sleepTime);
		return doc;
	}

	public static Document getPostDoc(String url, Map<String, String> headers, Map<String, String> datas,
			int sleepTime) {
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			logger.error(String.format("ERROR_URL :%s, Data: %s", url, datas), e);
			sleep(sleepTime);
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
					sleep(sleepTime);
					return null;
				}
			} else {
				String status = err.replaceAll(".*Status=(\\d+).*", "$1");
				finish = System.currentTimeMillis();
				logger.info(String.format("REQUEST_URL (%d ms): %s", (finish - start), url));
				logger.error(String.format("ERROR_%s (%d ms): %s", status, (finish - start), url));
			}
		}
		sleep(sleepTime);
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
