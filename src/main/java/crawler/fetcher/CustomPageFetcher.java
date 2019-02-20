package crawler.fetcher;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import org.apache.http.client.HttpClient;

public class CustomPageFetcher extends PageFetcher {
	public CustomPageFetcher(CrawlConfig config) {
		super(config);
	}

	public HttpClient getHttpClient() {
		return this.httpClient;
	}
}
