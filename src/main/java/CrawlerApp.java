import crawler.test.SimpleStatsCrawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class CrawlerApp {
	public static final String CRAWL_STORAGE = "~/Downloads/data/crawl";
	public static final int MAX_CRAWL_DEPTH = 1;
	public static final int NUMBER_OF_CRAWLERS = 4;
	public static final int POLITENESS_DELAY = 50;
	public static final int MAX_PAGES_TO_FETCH = 20;

	public static void main(String[] args) throws Exception {
		/*
			Instantiate crawl config
		 */
		CrawlConfig crawlConfig = new CrawlConfig();
		crawlConfig.setCrawlStorageFolder(CRAWL_STORAGE);
		crawlConfig.setMaxDepthOfCrawling(MAX_CRAWL_DEPTH);
		crawlConfig.setPolitenessDelay(POLITENESS_DELAY);
		crawlConfig.setMaxPagesToFetch(MAX_PAGES_TO_FETCH);

		/*
			Instantiate controller for this crawl
		 */
		PageFetcher pageFetcher = new PageFetcher(crawlConfig);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(crawlConfig, pageFetcher, robotstxtServer);

		/*
			Add seed URLs
		 */
		controller.addSeed("http://www.theguardian.com/");

		/*
			Start the crawl
		 */
		controller.startNonBlocking(SimpleStatsCrawler.class, NUMBER_OF_CRAWLERS);
	}
}
