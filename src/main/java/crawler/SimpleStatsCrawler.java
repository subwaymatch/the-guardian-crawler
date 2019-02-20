package crawler;

import crawler.helpers.CrawlDataWriter;
import crawler.helpers.CrawlHelper;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SimpleStatsCrawler extends WebCrawler {
	public static int crawlerCount = 0;
	private int crawlerId;

	private static final Pattern DISALLOWED_EXT_FILTER = Pattern.compile(".*(\\.(css|js|mp3|mp4|zip|gz|ico|json))$");
	private static final Pattern ALLOWED_EXT_FILTER = Pattern.compile(".*(\\.(htm|html|pdf|bmp|gif|jpe?g|png?))$");
	private static final String[] ALLOWED_CONTENT_TYPES = new String[] {
			"text/html",
			"image/gif",
			"image/jpeg",
			"image/png",
			"application/pdf"
	};
	private List<String> allowedContentTypesList;
	private CrawlDataWriter crawlDataWriter;
	private HttpClient httpClient;

	public SimpleStatsCrawler() {
		super();
		  
		crawlerId = ++crawlerCount;

		System.out.println("crawlerCount = " + crawlerCount);

		allowedContentTypesList = Arrays.asList(ALLOWED_CONTENT_TYPES);
		crawlDataWriter = CrawlDataWriter.getInstance();
		httpClient = CrawlerApp.getHttpClient();
	}

	@Override
	protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
		super.handlePageStatusCode(webUrl, statusCode, statusDescription);

		System.out.println("=======================");
		System.out.println("SimpleStatsCrawler.handlePageStatusCode");
		System.out.println("webUrl = [" + webUrl + "], statusCode = [" + statusCode + "], statusDescription = [" + statusDescription + "]");

		crawlDataWriter.recordFetchAttempt(webUrl.getURL(), statusCode);
	}

	@Override
	protected WebURL handleUrlBeforeProcess(WebURL curURL) {
		System.out.println("=======================");
		System.out.println("SimpleStatsCrawler.handleUrlBeforeProcess");
		System.out.println("curURL = [" + curURL + "]");

		return super.handleUrlBeforeProcess(curURL);
	}

	@Override
	protected void onPageBiggerThanMaxSize(String urlStr, long pageSize) {
		System.out.println("=======================");
		System.out.println("SimpleStatsCrawler.onPageBiggerThanMaxSize");
		System.out.println("urlStr = [" + urlStr + "], pageSize = [" + pageSize + "]");

		super.onPageBiggerThanMaxSize(urlStr, pageSize);
	}

	@Override
	protected void onRedirectedStatusCode(Page page) {
		System.out.println("=======================");
		System.out.println("SimpleStatsCrawler.onRedirectedStatusCode");
		System.out.println("page = [" + page + "]");

		super.onRedirectedStatusCode(page);
	}

	@Override
	protected void onUnexpectedStatusCode(String urlStr, int statusCode, String contentType, String description) {
		System.out.println("=======================");
		System.out.println("SimpleStatsCrawler.onUnexpectedStatusCode");
		System.out.println("urlStr = [" + urlStr + "], statusCode = [" + statusCode + "], contentType = [" + contentType + "], description = [" + description + "]");

		super.onUnexpectedStatusCode(urlStr, statusCode, contentType, description);
	}

	@Override
	protected void onContentFetchError(Page page) {
		System.out.println("=======================");
		System.out.println("SimpleStatsCrawler.onContentFetchError");
		System.out.println("page = [" + page + "]");

		super.onContentFetchError(page);
	}

	@Override
	protected void onUnhandledException(WebURL webUrl, Throwable e) {
		System.out.println("=======================");
		System.out.println("SimpleStatsCrawler.onUnhandledException");
		System.out.println("webUrl = [" + webUrl + "], e = [" + e + "]");

		super.onUnhandledException(webUrl, e);
	}

	@Override
	protected void onParseError(WebURL webUrl) {
		System.out.println("=======================");
		System.out.println("SimpleStatsCrawler.onParseError");
		System.out.println("webUrl = [" + webUrl + "]");

		super.onParseError(webUrl);
	}

	/**
	 * Specify whether the given url should be crawled or not based on
	 * the crawling logic. Here URLs with extensions css, js, etc will not be visited
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		System.out.println("=======================");
		System.out.println("SimpleStatsCrawler.shouldVisit");
		System.out.println("referringPage = [" + referringPage + "], url = [" + url + "]");

		String href = url.getURL().toLowerCase();

		// If file extension is in one of the blocked types, do not visit
		if (DISALLOWED_EXT_FILTER.matcher(href).matches()) {
			return false;
		}

		// If domain is outside the crawling domain, do not visit
		if (!CrawlHelper.isUrlInternal(url)) {
			return false;
		}

		// If file extension is not known, make a HEAD request to find out the content type
		if (!ALLOWED_EXT_FILTER.matcher(href).matches()) {
			System.out.println("GETTING HEAD");

			HttpUriRequest httpUriRequest = new HttpHead(url.getURL());

			try {
				HttpResponse httpResponse = httpClient.execute(httpUriRequest);

				boolean isRedirect = CrawlHelper.isRedirectStatusCode(httpResponse.getStatusLine().getStatusCode());

				if (httpResponse.getStatusLine().getStatusCode() >= 400) {
					System.out.println("Something wrong, url=" + url.getURL() + ", status code=" + httpResponse.getStatusLine().getStatusCode());
				}

				if (!isRedirect) {
					Header contentTypeHeader = httpResponse.getFirstHeader("content-type");

					if (contentTypeHeader != null) {
						String contentType = contentTypeHeader.getValue();
						contentType = CrawlHelper.extractContentType(contentType);

						// If content-type is not one of the permitted ones, exit
						if (!allowedContentTypesList.contains(contentType)) {
							System.out.println("CONTENT TYPE NOT ALLOWED=" + contentType);

							return false;
						}
					} else {
						return false;
					}
				}
			} catch (Exception e) {
				// If header can't be returned, do not visit
				return false;
			}
		}

		System.out.println("Should visit URL");
		return true;
	}

	/**
	 * This function is called when a page is fetched and ready
	 * to be processed by the program
	 */
	@Override
	public void visit(Page page) {
		System.out.println("=======================");
		System.out.println("SimpleStatsCrawler.visit");
		System.out.println("page = [" + page + "], url=" + page.getWebURL().getURL());

		String url = page.getWebURL().getURL();

		crawlDataWriter.recordSuccessfulDownload(url, 0, page.getParseData().getOutgoingUrls().size(), CrawlHelper.extractContentType(page.getContentType()));

		HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
		// crawlDataWriter.recordDiscoveredUrls(htmlParseData.getOutgoingUrls());
	}
}
