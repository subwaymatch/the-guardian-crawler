package crawler;

import crawler.fetcher.CustomPageFetcher;
import crawler.helpers.CrawlDataWriter;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.apache.http.client.HttpClient;

public class CrawlerApp {
	public static final String CRAWL_STORAGE = System.getProperty("user.dir");;
	public static final int MAX_CRAWL_DEPTH = 16;
	public static final int NUMBER_OF_CRAWLERS = 8;
	public static final int POLITENESS_DELAY = 50;
	public static final int MAX_PAGES_TO_FETCH = 20000;

	private static HttpClient httpClient;

	public static HttpClient getHttpClient() {
		return httpClient;
	}

	public static void main(String[] args) throws Exception {
		/*
			Instantiate crawl config
		 */
		CrawlConfig crawlConfig = new CrawlConfig();
		crawlConfig.setCrawlStorageFolder(CRAWL_STORAGE);
		crawlConfig.setMaxDepthOfCrawling(MAX_CRAWL_DEPTH);
		crawlConfig.setPolitenessDelay(POLITENESS_DELAY);
		crawlConfig.setMaxPagesToFetch(MAX_PAGES_TO_FETCH);
		crawlConfig.setMaxDownloadSize(10 * 1024 * 1024);
		crawlConfig.setIncludeHttpsPages(true);
		crawlConfig.setIncludeBinaryContentInCrawling(true);

		/*
			Instantiate controller for this crawl
		 */
		PageFetcher pageFetcher = new CustomPageFetcher(crawlConfig);
		httpClient = ((CustomPageFetcher) pageFetcher).getHttpClient();

		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(crawlConfig, pageFetcher, robotstxtServer);

		/*
			Add seed URLs
		 */
		controller.addSeed("https://www.theguardian.com/us");

		/*
			Start the crawl
		 */
		controller.start(SimpleStatsCrawler.class, NUMBER_OF_CRAWLERS);

		/*
			Close streams and flush
		 */
		CrawlDataWriter crawlDataWriter = CrawlDataWriter.getInstance();
		crawlDataWriter.generateReport();
		crawlDataWriter.close();
	}
}
