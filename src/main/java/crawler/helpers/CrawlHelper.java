package crawler.helpers;

import edu.uci.ics.crawler4j.url.WebURL;

public class CrawlHelper {
	public static boolean isUrlInternal(String url) {
		if (url == null) return false;

		return url.startsWith("https://www.theguardian.com/us") || url.startsWith("https://www.theguardian.com/us");
	}

	public static String extractContentType(String fullContentTypeString) {
		int index = fullContentTypeString.indexOf(";");

		return (index == -1) ? fullContentTypeString : fullContentTypeString.substring(0, index);
	}

	public static boolean isSuccessStatusCode(int statusCode) {
		return (statusCode >= 200) && (statusCode <= 299);
	}

	public static boolean isFailedOrAbortedStatusCode(int statusCode) {
		return (statusCode < 200) || (statusCode >= 300);
	}

	public static boolean isRssFeedUrl(String url) {
		return url.endsWith("/rss");
	}
}
