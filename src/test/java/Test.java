import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Test {

	public static void main(String[] args) throws MalformedURLException, IOException, ParseException {
		System.out.println(new SimpleDateFormat("d MMMMM y").parse("19 january 2016"));
	}

}
