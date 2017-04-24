import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import vn.com.dsvn.crawl.mechanical.ludwigmeister.LudwigmeisterCrawler;

public class Test {

//	public static void main(String[] args) throws MalformedURLException, IOException, ParseException {
//		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "100");
//		List<String> lines = FileUtils.readLines(new File("/home/thaonp/Desktop/ludw.cate.txt"));
//		lines.parallelStream().forEach(line -> {
//			boolean isSub = LudwigmeisterCrawler.isSubCate(line);
//			if(!isSub){
//				System.out.println(line);
//			}
//		});
//	}

}
