package crawler.test;

import crawler.CrawlDataWriter;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.util.Set;
import java.util.regex.Pattern;

public class SimpleStatsCrawler extends WebCrawler {
	public static int crawlerCount = 0;
	private int crawlerId;

	private CrawlDataWriter mCrawlDataWriter;

	public SimpleStatsCrawler() {
		super();

		crawlerId = ++crawlerCount;

		System.out.println("crawlerCount = " + crawlerCount);

		mCrawlDataWriter = CrawlDataWriter.getInstance();
	}

	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg|png|mp3|mp4|zip|gz|ico))$");

	/**
	 * Specify whether the given url should be crawled or not based on
	 * the crawling logic. Here URLs with extensions css, js, etc will not be visited
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		System.out.println(crawlerId + "=>shouldVisit: " + url.getURL().toLowerCase());

		String href = url.getURL().toLowerCase();
		boolean willVisit = !FILTERS.matcher(href).matches();

		if (willVisit) {
			System.out.println("Should visit URL");

			mCrawlDataWriter.recordFetchAttempt(url.toString(), referringPage.getStatusCode());
		} else {
			System.out.println("Should NOT visit URL");
		}

		return willVisit;
	}

	/**
	 * This function is called when a page is fetched and ready
	 * to be processed by the program
	 */
	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();
		System.out.println(crawlerId + "=>visit: " + url);

		System.out.println("Content type: " + page.getContentType());
		System.out.println("Status code: " + page.getStatusCode());

		System.out.println("-------------------------------------------");

		mCrawlDataWriter.recordSuccessfulDownload(url, 0, page.getParseData().getOutgoingUrls().size(), page.getContentType());

		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();

			String text = htmlParseData.getText(); // extract text from page
			Set<WebURL> links = htmlParseData.getOutgoingUrls();

			System.out.println("Page URL: " + url);
			System.out.println("Text length: " + text.length());
		}
	}
}
