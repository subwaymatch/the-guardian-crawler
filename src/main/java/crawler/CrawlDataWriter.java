package crawler;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CrawlDataWriter {

	final static public String FILE_SAVE_PATH = System.getProperty("user.dir");

	final static public String FETCH_ATTEMPTS_FILE = "fetch_guardian.csv";
	final static public String SUCCESSFUL_DOWNLOADS_FILE = "visit_guardian.csv";
	final static public String DISCOVERED_URLS_FILE = "urls_guardian.csv";
	final static public String REPORT_FILE = "CrawlReport_guardian.txt";

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
				BufferedWriter fetchAttemptsWriter = Files.newBufferedWriter(Paths.get(FILE_SAVE_PATH, FETCH_ATTEMPTS_FILE));
				BufferedWriter successfulDownloadsWriter = Files.newBufferedWriter(Paths.get(FILE_SAVE_PATH, SUCCESSFUL_DOWNLOADS_FILE));
				BufferedWriter discoveredUrlsWriter = Files.newBufferedWriter(Paths.get(FILE_SAVE_PATH, DISCOVERED_URLS_FILE));

				System.out.println("Paths.get(FILE_SAVE_PATH, FETCH_ATTEMPTS_FILE) = " + Paths.get(FILE_SAVE_PATH, FETCH_ATTEMPTS_FILE));;

				fetchAttemptsCSVPrinter = new CSVPrinter(fetchAttemptsWriter, CSVFormat.DEFAULT
						.withHeader("URL", "Status Code"));
				successfulDownloadsCSVPrinter = new CSVPrinter(successfulDownloadsWriter, CSVFormat.DEFAULT
						.withHeader("URL", "Size", "Outgoing Links", "Content Type"));
				discoveredUrlsCSVPrinter = new CSVPrinter(discoveredUrlsWriter, CSVFormat.DEFAULT
						.withHeader("URL", "Internal"));
				reportPrinter = new PrintWriter(Paths.get(FILE_SAVE_PATH, REPORT_FILE).toFile());
			} catch (IOException ie) {
				// Return a null instance to indicate that CSV printers can't be loaded
				return null;
			}
		}

		return mInstance;
	}

	public synchronized void recordFetchAttempt(String url, int statusCode) {
		try {
			fetchAttemptsCSVPrinter.printRecord(url, statusCode);
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	public synchronized void recordSuccessfulDownload(String url, int size, int numOutLinks, String contentType) {
		try {
			System.out.println("url = [" + url + "], size = [" + size + "], numOutLinks = [" + numOutLinks + "], contentType = [" + contentType + "]");
			successfulDownloadsCSVPrinter.printRecord(url, size, numOutLinks, contentType);
			System.out.println("Recorded");
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

	public void close() {
		System.out.println("CrawlDataWriter.close");

		try {
			fetchAttemptsCSVPrinter.close();
			successfulDownloadsCSVPrinter.close();
			discoveredUrlsCSVPrinter.close();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
}
