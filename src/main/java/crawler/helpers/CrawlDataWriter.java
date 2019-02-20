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
import java.util.Set;

public class CrawlDataWriter {

	final static public String FILE_SAVE_PATH = System.getProperty("user.dir");

	final static public Path FETCH_ATTEMPTS_FILE_PATH = Paths.get(FILE_SAVE_PATH, "fetch_guardian.csv");
	final static public Path SUCCESSFUL_DOWNLOADS_FILE_PATH = Paths.get(FILE_SAVE_PATH, "visit_guardian.csv");
	final static public Path DISCOVERED_URLS_FILE_PATH = Paths.get(FILE_SAVE_PATH, "urls_guardian.csv");
	final static public Path REPORT_FILE_PATH = Paths.get(FILE_SAVE_PATH, "CrawlReport_guardian.txt");

	private static CSVPrinter fetchAttemptsCSVPrinter;
	private static CSVPrinter successfulDownloadsCSVPrinter;
	private static CSVPrinter discoveredUrlsCSVPrinter;
	private static PrintWriter reportPrinter;

	private static CrawlDataWriter mInstance;

	private CrawlDataWriter() {
		super();
	}

	public static CrawlDataWriter getInstance() {
		if (mInstance == null) {
			mInstance = new CrawlDataWriter();

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
			} catch (IOException ie) {
				System.out.println("Error deleting previous csv and report files");

				// Return a null instance to indicate that CSV printers can't be loaded
				return null;
			}
		}

		return mInstance;
	}

	public synchronized void recordFetchAttempt(String url, int statusCode) {
		url = url.replace(",", "-");

		try {
			fetchAttemptsCSVPrinter.printRecord(url, statusCode);
		} catch (IOException ie) {
			ie.printStackTrace();
		}
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
	}

	public void recordDiscoveredUrls(Set<WebURL> urls) {
		try {
			for (WebURL url : urls) {
				discoveredUrlsCSVPrinter.printRecord(url.getURL().replace(",", "-"), true);
			}
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	public synchronized void recordDiscoveredUrls(String url, boolean isInternal) {
		try {
			discoveredUrlsCSVPrinter.printRecord(url, isInternal);
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	public void generateReport() {
		reportPrinter.println("Name: ");
		reportPrinter.println("USC ID: ");
		reportPrinter.println("News site crawled: ");
		reportPrinter.println();

		reportPrinter.println("Fetch Statistics");
		reportPrinter.println("================");
		reportPrinter.println("# fetches attempted:");
		reportPrinter.println("# fetches succeeded:");
		reportPrinter.println("# fetches failed or aborted:");
		reportPrinter.println();

		reportPrinter.println("Outgoing URLs:");
		reportPrinter.println("==============");
		reportPrinter.println("Total URLs extracted:");
		reportPrinter.println("# unique URLs extracted:");
		reportPrinter.println("# unique URLs within News Site:");
		reportPrinter.println("# unique URLs outside News Site:");
		reportPrinter.println();

		reportPrinter.println("Status Codes:");
		reportPrinter.println("=============");
		reportPrinter.println("200 OK:");
		reportPrinter.println("301 Moved Permanently:");
		reportPrinter.println("401 Unauthorized:");
		reportPrinter.println("403 Forbidden:");
		reportPrinter.println("404 Not Found:");
		reportPrinter.println();

		reportPrinter.println("File Sizes:");
		reportPrinter.println("===========");
		reportPrinter.println("< 1KB:");
		reportPrinter.println("1KB ~ <10KB:");
		reportPrinter.println("10KB ~ <100KB:");
		reportPrinter.println("100KB ~ <1MB:");
		reportPrinter.println(">= 1MB:");
		reportPrinter.println();

		reportPrinter.println("Content Types:");
		reportPrinter.println("==============");
		reportPrinter.println("text/html:");
		reportPrinter.println("image/gif:");
		reportPrinter.println("image/jpeg:");
		reportPrinter.println("image/png:");
		reportPrinter.println("application/pdf:");

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
