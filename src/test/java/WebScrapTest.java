
// Import necessary packages
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.bonigarcia.wdm.WebDriverManager;

public class WebScrapTest {

	WebDriver driver;
	MongoCollection<Document> webCollection;

	// Establish MongoDB connection before the test suite
	@BeforeSuite
	public void connectMongoDB() {
		// Disable MongoDB driver logs
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");

		// Create MongoDB client and connect to the "autoDB" database
		MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
		MongoDatabase database = mongoClient.getDatabase("autoDB");

		// Create connection to the "web" collection
		webCollection = database.getCollection("web");

		// Delete existing data from the "web" collection
		webCollection.drop();
	}

	// Set up the WebDriver before each test
	@BeforeTest
	public void setUp() {
		// Set up ChromeDriver in headless mode
		WebDriverManager.chromedriver().setup();
		ChromeOptions co = new ChromeOptions();
		co.addArguments("--headless");
		driver = new ChromeDriver(co);
	}

	// Provide data for web scraping from different URLs
	@DataProvider
	public Object[][] getWebData() {
		return new Object[][] { 
			{ "https://www.snapdeal.com" }, 
			{ "https://www.flipkart.com" },
			{ "https://www.gapcanada.ca" } 
		};
	}

	// Perform web scraping for each URL and store the data in MongoDB
	@Test(dataProvider = "getWebData")
	public void webScrapeTest(String appUrl) {
		// Navigate to the specified URL
		driver.get(appUrl);
		String url = driver.getCurrentUrl();
		String title = driver.getTitle();

		// Count the number of links and images on the page
		int linksCount = driver.findElements(By.tagName("a")).size();
		int imagesCount = driver.findElements(By.tagName("img")).size();

		// Extract and store attributes of all links
		List<WebElement> linksList = driver.findElements(By.tagName("a"));
		List<String> linksAttrList = new ArrayList<String>();

		for (WebElement ele : linksList) {
			String hrefValue = ele.getAttribute("href");
			linksAttrList.add(hrefValue);
		}

//		// Handle null values when extracting link attributes
//				for (WebElement ele : linksList) {
//					String hrefValue = ele.getAttribute("href");
//					if (hrefValue != null && !hrefValue.isEmpty()) {
//						linksAttrList.add(hrefValue);
//					}
//				}

		// Extract and store attributes of all images
		List<WebElement> imagesList = driver.findElements(By.tagName("img"));
		List<String> imagesAttrList = new ArrayList<String>();

		for (WebElement ele : imagesList) {
			String srcValue = ele.getAttribute("src");
			imagesAttrList.add(srcValue);
		}

//		// Handle null values when extracting image attributes
//				for (WebElement ele : imagesList) {
//					String srcValue = ele.getAttribute("src");
//					if (srcValue != null && !srcValue.isEmpty()) {
//						imagesAttrList.add(srcValue);
//					}
//				}

		// Create a MongoDB document to store the scraped data
		Document d1 = new Document();
		d1.append("url", url);
		d1.append("title", title);
		d1.append("tatalLinks", linksCount);
		d1.append("totalImages", imagesCount);
		d1.append("linksAttribute", linksAttrList);
		d1.append("imagesAttribute", imagesAttrList);

		// Create a list of documents and insert them into the MongoDB collection
		List<Document> docsList = new ArrayList<Document>();
		docsList.add(d1);

		webCollection.insertMany(docsList);
	}

	// Quit the WebDriver after each test
	@AfterTest
	public void tearDown() {
		driver.quit();
	}
}
