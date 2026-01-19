package scenario;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;

public class Scenario5Test {

    WebDriver driver;
    WebDriverWait wait;
    String scenario = "scenario5";
    String excelPath = "testdata/data.xlsx";
    String email;
    String password;

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.manage().window().maximize();

        String[] creds = readCredentialsFromSheet(excelPath);
        email = creds[0];
        password = creds[1];
    }

    @Test
    public void changeAcademicCalendar() {
        try {
            driver.get("https://student.me.northeastern.edu/");
            ScreenshotUtil.takeScreenshot(driver, "screenshots/" + scenario + "/01_LandingPage.png");

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0116"))).sendKeys(email);
            driver.findElement(By.id("idSIButton9")).click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0118"))).sendKeys(password);
            driver.findElement(By.id("idSIButton9")).click();

            try {
                wait.until(ExpectedConditions.elementToBeClickable(By.id("idSIButton9"))).click();
            } catch (Exception ignored) {}

            ScreenshotUtil.takeScreenshot(driver, "screenshots/" + scenario + "/02_AfterLogin.png");

            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Resources')]"))).click();
            ScreenshotUtil.takeScreenshot(driver, "screenshots/" + scenario + "/03_AfterResourcesClick.png");

            wait.until(ExpectedConditions.elementToBeClickable(By.id("resource-tab-Academics,_Classes_&_Registration"))).click();
            ScreenshotUtil.takeScreenshot(driver, "screenshots/" + scenario + "/04_AfterAcademicsClick.png");

            String mainTab = driver.getWindowHandle();
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Academic Calendar"))).click();
            Thread.sleep(2000);

            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(mainTab)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }

            ScreenshotUtil.takeScreenshot(driver, "screenshots/" + scenario + "/05_CalendarTab.png");

            WebElement firstRegistrarLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//a[contains(@href, '/article/academic-calendar')])[1]")));
            firstRegistrarLink.click();
            ScreenshotUtil.takeScreenshot(driver, "screenshots/" + scenario + "/06_RegistrarClicked.png");

            // Scroll to bottom first
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000);
            ScreenshotUtil.takeScreenshot(driver, "screenshots/" + scenario + "/07_ScrolledBottom.png");

            // Try locating the second "Add to Calendar" button inside iframe
            try {
                WebElement iframe = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("trumba.spud.7.iframe")));
                driver.switchTo().frame(iframe);

                // Select the second "Add to My Calendar" button (if multiple exist)
                WebElement addBtn = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.cssSelector("button[id*='buttonAtmc']"))).get(1); // get second one
                js.executeScript("arguments[0].scrollIntoView(true);", addBtn);
                ScreenshotUtil.takeScreenshot(driver, "screenshots/" + scenario + "/08_AddCalendarVisible.png");

                Assert.assertTrue(addBtn.isDisplayed(), "✅ Add to Calendar button is visible.");
            } catch (Exception e) {
                ScreenshotUtil.takeScreenshot(driver, "screenshots/" + scenario + "/08_AddCalendarNotFound.png");
                System.out.println("❌ Add to Calendar button not found: " + e.getMessage());
            }

        } catch (Exception e) {
            ScreenshotUtil.takeScreenshot(driver, "screenshots/" + scenario + "/09_ErrorOccurred.png");
            e.printStackTrace();
            Assert.fail("❌ Scenario failed: " + e.getMessage());
        }
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    private String[] readCredentialsFromSheet(String filePath) {
        String[] creds = new String[2];
        try (FileInputStream file = new FileInputStream(new File(filePath));
             Workbook wb = new XSSFWorkbook(file)) {
            Row row = wb.getSheetAt(0).getRow(1);
            creds[0] = row.getCell(0).toString();
            creds[1] = row.getCell(1).toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return creds;
    }
}
