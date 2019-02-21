package crawler.helpers;

import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CrawlDataWriter {

	public final static String FILE_SAVE_PATH = System.getProperty("user.dir");

	public static final Path FETCH_ATTEMPTS_FILE_PATH = Paths.get(FILE_SAVE_PATH, "fetch_guardian.csv");
	public static final Path SUCCESSFUL_DOWNLOADS_FILE_PATH = Paths.get(FILE_SAVE_PATH, "visit_guardian.csv");
	public static final Path DISCOVERED_URLS_FILE_PATH = Paths.get(FILE_SAVE_PATH, "urls_guardian.csv");
	public static final Path REPORT_FILE_PATH = Paths.get(FILE_SAVE_PATH, "CrawlReport_guardian.txt");

	public static final String LESS_THAN_1KB = "< 1KB:";
	public static final String LESS_THAN_10KB = "1KB ~ <10KB:";
	public static final String LESS_THAN_100KB = "10KB ~ <100KB:";
	public static final String LESS_THAN_1MB = "100KB ~ <1MB:";
	public static final String LARGER_THAN_1MB = ">= 1MB:";

	private CSVPrinter fetchAttemptsCSVPrinter;
	private CSVPrinter successfulDownloadsCSVPrinter;
	private CSVPrinter discoveredUrlsCSVPrinter;
	private PrintWriter reportPrinter;

	private static CrawlDataWriter mInstance;

	private int numFetchesSucceeded;
	private int numFetchesFailedOrAborted;

	private int totalNumberOfUrlsExtracted;
	private Set<String> uniqueUrls;

	private Map<String, Integer> statusCodeCount;
	private Map<String, Integer> fileSizeBucketCount;
	private Map<String, Integer> contentTypeCount;

	private CrawlDataWriter() {
		try {
			Files.deleteIfExists(FETCH_ATTEMPTS_FILE_PATH);
			Files.deleteIfExists(SUCCESSFUL_DOWNLOADS_FILE_PATH);
			Files.deleteIfExists(DISCOVERED_URLS_FILE_PATH);
			Files.deleteIfExists(REPORT_FILE_PATH);

			BufferedWriter fetchAttemptsWriter = Files.newBufferedWriter(FETCH_ATTEMPTS_FILE_PATH);
			BufferedWriter successfulDownloadsWriter = Files.newBufferedWriter(SUCCESSFUL_DOWNLOADS_FILE_PATH);
			BufferedWriter discoveredUrlsWriter = Files.newBufferedWriter(DISCOVERED_URLS_FILE_PATH);

			fetchAttemptsCSVPrinter = new CSVPrinter(fetchAttemptsWriter, CSVFormat.DEFAULT
					.withHeader("URL", "Status Code"));
			successfulDownloadsCSVPrinter = new CSVPrinter(successfulDownloadsWriter, CSVFormat.DEFAULT
					.withHeader("URL", "Size", "Outgoing Links", "Content Type"));
			discoveredUrlsCSVPrinter = new CSVPrinter(discoveredUrlsWriter, CSVFormat.DEFAULT
					.withHeader("URL", "Internal"));
			reportPrinter = new PrintWriter(REPORT_FILE_PATH.toFile());

			uniqueUrls = new HashSet();

			statusCodeCount = new HashMap<>();
			fileSizeBucketCount = new HashMap<>();
			contentTypeCount = new HashMap<>();
		} catch (IOException ie) {
			System.out.println("Error deleting previous csv and report files");
		}
	}

	public static CrawlDataWriter getInstance() {
		if (mInstance == null) {
			mInstance = new CrawlDataWriter();
		}

		return mInstance;
	}

	public synchronized void recordFetchAttempt(String url, int statusCode, String statusDescription) {
		url = url.replace(",", "-");

		try {
			fetchAttemptsCSVPrinter.printRecord(url, statusCode);
		} catch (IOException ie) {
			ie.printStackTrace();
		}

		String statusCodeKey = statusCode + " " + statusDescription;
		statusCodeCount.compute(statusCodeKey, (k, v) -> (v == null) ? 1 : v + 1 );

		if (CrawlHelper.isSuccessStatusCode(statusCode)) numFetchesSucceeded++;
		else if (CrawlHelper.isAbortedStatusCode(statusCode)) numFetchesFailedOrAborted++;
	}

	public synchronized void recordSuccessfulDownload(String url, int size, int numOutLinks, String contentType) {
		url = url.replace(",", "-");

		try {
			System.out.println("url = [" + url + "], size = [" + size + "], numOutLinks = [" + numOutLinks + "], contentType = [" + contentType + "]");
			successfulDownloadsCSVPrinter.printRecord(url, size, numOutLinks, contentType);
			System.out.println("Recorded");
		} catch (IOException ie) {
			ie.printStackTrace();
		}

		// Add content type count
		contentTypeCount.compute(contentType, (k, v) -> (v == null) ? 1 : v + 1 );

		String sizeBucketKey = LARGER_THAN_1MB;

		// Add size bucket count
		if (size < 1024) {
			sizeBucketKey = LESS_THAN_1KB;
		}

		else if (size < 10 * 1024) {
			sizeBucketKey = LESS_THAN_10KB;
		}

		else if (size < 100 * 1024) {
			sizeBucketKey = LESS_THAN_100KB;
		}

		else if (size < 1024 * 1024) {
			sizeBucketKey = LESS_THAN_1MB;
		}

		// Increment count
		fileSizeBucketCount.compute(sizeBucketKey, (k, v) -> (v == null) ? 1 : v + 1);
	}

	public void recordDiscoveredUrls(Set<WebURL> webURLs) {
		try {
			if (webURLs == null) return;

			totalNumberOfUrlsExtracted += webURLs.size();

			uniqueUrls.addAll(
					webURLs
						.stream()
						.map(webURL -> webURL.getURL())
						.collect(Collectors.toSet())
			);

			for (WebURL url : webURLs) {
				// discoveredUrlsCSVPrinter.printRecord(url.getURL().replace(",", "-"), true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void generateReport() {
		reportPrinter.println("Name: ");
		reportPrinter.println("USC ID: ");
		reportPrinter.println("News site crawled: ");
		reportPrinter.println();

		reportPrinter.println("Fetch Statistics");
		reportPrinter.println("================");
		reportPrinter.println("# fetches attempted: " + (numFetchesSucceeded + numFetchesFailedOrAborted));
		reportPrinter.println("# fetches succeeded: "+ numFetchesSucceeded);
		reportPrinter.println("# fetches failed or aborted:" + numFetchesFailedOrAborted);
		reportPrinter.println();

		long numUniqueInternalUrls = uniqueUrls.stream().filter((url)-> CrawlHelper.isUrlInternal(url)).count();

		reportPrinter.println("Outgoing URLs:");
		reportPrinter.println("==============");
		reportPrinter.println("Total URLs extracted: " + totalNumberOfUrlsExtracted);
		reportPrinter.println("# unique URLs extracted: " + uniqueUrls.size());
		reportPrinter.println("# unique URLs within News Site: " + numUniqueInternalUrls);
		reportPrinter.println("# unique URLs outside News Site: " + (uniqueUrls.size() - numUniqueInternalUrls));
		reportPrinter.println();

		reportPrinter.println("Status Codes:");
		reportPrinter.println("=============");
		statusCodeCount.forEach((statusCodeKey, count) -> {
			reportPrinter.println(statusCodeKey + ": " + count);
		});
		reportPrinter.println();

		reportPrinter.println("File Sizes:");
		reportPrinter.println("===========");
		reportPrinter.println(LESS_THAN_1KB + ": " + fileSizeBucketCount.getOrDefault(LESS_THAN_1KB, 0));
		reportPrinter.println(LESS_THAN_10KB + ": " + fileSizeBucketCount.getOrDefault(LESS_THAN_10KB, 0));
		reportPrinter.println(LESS_THAN_100KB + ": " + fileSizeBucketCount.getOrDefault(LESS_THAN_100KB, 0));
		reportPrinter.println(LESS_THAN_1MB + ": " + fileSizeBucketCount.getOrDefault(LESS_THAN_1MB, 0));
		reportPrinter.println(LARGER_THAN_1MB + ": " + fileSizeBucketCount.getOrDefault(LARGER_THAN_1MB, 0));
		reportPrinter.println();

		reportPrinter.println("Content Types:");
		reportPrinter.println("==============");
		contentTypeCount.forEach((contentType, count) -> {
			reportPrinter.println(contentType + ": " + count);
		});
	}

	public void close() {
		System.out.println("CrawlDataWriter.close");

		try {
			fetchAttemptsCSVPrinter.close();
			successfulDownloadsCSVPrinter.close();
			discoveredUrlsCSVPrinter.close();
			reportPrinter.close();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
}
