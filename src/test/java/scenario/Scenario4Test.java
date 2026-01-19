package scenario;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.*;

public class Scenario4Test {

    WebDriver driver;
    WebDriverWait wait;
    String scenarioPath = "screenshots/scenario4/";

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @Test
    public void downloadDatasetFromOneSearch() {
        try {
            driver.get("https://onesearch.library.northeastern.edu/discovery/search?vid=01NEU_INST:NU&lang=en");
            Thread.sleep(2000);
            ScreenshotUtil.takeScreenshot(driver, scenarioPath + "01_Homepage.png");

            WebElement datasetTile = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div:nth-child(5) .item-content")));
            new Actions(driver).moveToElement(datasetTile).perform();
            Thread.sleep(1000);
            ScreenshotUtil.takeScreenshot(driver, scenarioPath + "02_HoverOnDataset.png");

            String mainWindow = driver.getWindowHandle();
            datasetTile.click();
            Thread.sleep(2000);

            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(mainWindow)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
            ScreenshotUtil.takeScreenshot(driver, scenarioPath + "03_DatasetPage.png");

            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 265)");
            Thread.sleep(1000);
            WebElement datasetsTab = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Datasets")));
            datasetsTab.click();
            Thread.sleep(1000);
            ScreenshotUtil.takeScreenshot(driver, scenarioPath + "04_DatasetsClicked.png");

            WebElement zipLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Zip File")));
            zipLink.click();
            Thread.sleep(2000);
            ScreenshotUtil.takeScreenshot(driver, scenarioPath + "05_ZipClicked.png");

            Set<String> handlesAfterDownload = driver.getWindowHandles();
            for (String handle : handlesAfterDownload) {
                if (!handle.equals(mainWindow)) {
                    driver.switchTo().window(handle);
                    driver.close(); // Close download tab
                }
            }

            driver.switchTo().window(mainWindow);
            Thread.sleep(1000);
            ScreenshotUtil.takeScreenshot(driver, scenarioPath + "06_BackToMain.png");

            Assert.assertTrue(true, "✅ Dataset download interaction completed.");
        } catch (Exception e) {
            ScreenshotUtil.takeScreenshot(driver, scenarioPath + "99_ErrorOccurred.png");
            e.printStackTrace();
            Assert.fail("❌ Scenario 4 failed: " + e.getMessage());
        }
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}
