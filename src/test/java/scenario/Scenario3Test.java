package scenario;

import java.time.Duration;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;

import io.github.bonigarcia.wdm.WebDriverManager;

public class Scenario3Test {

    WebDriver driver;
    WebDriverWait wait;

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        driver.manage().window().maximize();
    }

    @Test
    public void reserveSnellRoom() throws InterruptedException {
        driver.get("https://library.northeastern.edu/");
        ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario3/1_homepage.png");

        step1_clickReserveLink();
        step2_selectBoston();
        step3_clickBookARoom();
        step4_selectIndividualStudy();
        step5_selectCapacity();
        step6_scrollToBottom();
    }

    private void step1_clickReserveLink() throws InterruptedException {
        WebElement reserveLink = driver.findElement(By.linkText("Reserve A Study Room"));
        scrollTo(reserveLink);
        Thread.sleep(1000);
        ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario3/2_clickReserve.png");
        reserveLink.click();
    }

    private void step2_selectBoston() throws InterruptedException {
        Thread.sleep(3000);
        ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario3/3_selectBoston.png");
        driver.findElement(By.cssSelector(".col-md-6:nth-child(1) .pt-cv-thumbnail")).click();
    }

    private void step3_clickBookARoom() throws InterruptedException {
        Thread.sleep(3000);
        ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario3/4_bookRoom.png");
        driver.findElement(By.linkText("Book a Room")).click();
    }

    private void step4_selectIndividualStudy() throws InterruptedException {
        try {
            ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario3/5_selectSeatStyle.png");
            WebElement seatStyleDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//select[@name='gid' and @id='gid']")));
            new Select(seatStyleDropdown).selectByValue("13369");
            System.out.println("Selected 'Individual Study' via XPath");
            ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario3/5_selectedSeatStyle.png");
            Thread.sleep(3000);
        } catch (Exception e) {
            System.out.println("Failed to select 'Individual Study': " + e.getMessage());
            ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario3/5_selectSeatStyleFail.png");
            throw e;
        }
    }

    private void step5_selectCapacity() throws InterruptedException {
        try {
            WebElement capacityDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//select[@name='capacity' and @id='capacity']")));
            new Select(capacityDropdown).selectByValue("1");
            System.out.println("Selected 'Space For 1-4 people' via XPath");
            ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario3/6_selectCapacity.png");
            Thread.sleep(3000);
        } catch (Exception e) {
            System.out.println("Failed to select 'Space For 1-4 people': " + e.getMessage());
            ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario3/6_selectCapacityFail.png");
            throw e;
        }
    }

    private void step6_scrollToBottom() throws InterruptedException {
        WebElement footer = driver.findElement(By.id("s-lc-public-cust-footer"));
        scrollTo(footer);
        Thread.sleep(1000);
        ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario3/7_scrolledDown.png");
    }

    private void scrollTo(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    @AfterClass
    public void teardown() {
        driver.quit();
    }
}

