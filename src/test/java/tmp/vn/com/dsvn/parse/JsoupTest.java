package tmp.vn.com.dsvn.parse;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import vn.com.dsvn.utils.JsoupUtils;

public class JsoupTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String link = "http://www.realtor.com/realestateandhomes-search/Autauga-County_AL/";
		Document doc = JsoupUtils.getDoc(link);
		System.out.println(doc.toString());
		String[] tk = doc.toString().split("\n");
		System.out.println(doc.toString());
		for (String a : tk) {
			if (a.contains("1813")) {
				System.out.println(a);
			}
		}
		// Elements els = doc.select("#map-result-count-message");
		// for (Element e : els) {
		// System.out.println(e.text());
		// }
	}

}
