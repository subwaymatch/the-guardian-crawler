package crawler.helpers;

import edu.uci.ics.crawler4j.url.WebURL;

public class CrawlHelper {
	public static boolean isUrlInternal(WebURL url) {
		if (url == null) return false;

		return url.getDomain().equals("theguardian.com")
				&& url.getSubDomain().equals("www")
				&& url.getPath().startsWith("/us");
	}

	public static String extractContentType(String fullContentTypeString) {
		int index = fullContentTypeString.indexOf(";");

		return (index == -1) ? fullContentTypeString : fullContentTypeString.substring(0, index);
	}

	public static boolean isRedirectStatusCode(int statusCode) {
		return (statusCode >= 300) && (statusCode <= 399);
	}
}
