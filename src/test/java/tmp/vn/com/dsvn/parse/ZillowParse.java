package tmp.vn.com.dsvn.parse;

import static org.junit.Assert.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class ZillowParse {
	@Test
	public void testGetNumBedBathSqft() {
		String textRoom = "Studio 0.5 baths 489 sqft";

		assertEquals(getNumBedBathSqft(textRoom, "bed"), "");
		assertEquals(getNumBedBathSqft(textRoom, "bath"), "0.5");
		assertEquals(getNumBedBathSqft(textRoom, "sqft"), "489");

	}

	private String getNumBedBathSqft(String textRoom, String name) {
		textRoom = textRoom.replace(",", "");
		String num = "";
		if (textRoom.contains(name)) {
			String regex = "([.\\d]+)\\s+" + name;
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(textRoom);
			if (matcher.find()) {
				String t = matcher.group().replace(name, "").trim();
				// try {
				// num = Float.parseFloat(t);
				// } catch (Exception e) {
				// }
				num = t;
				System.out.println(t);
			}
		}
		return num;
	}
}
