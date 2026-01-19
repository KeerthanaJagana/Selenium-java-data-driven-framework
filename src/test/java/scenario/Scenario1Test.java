package scenario;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.*;
import java.time.Duration;
import java.util.*;

public class Scenario1Test {

    WebDriver driver;
    WebDriverWait wait;
    String scenario = "scenario1";
    String excelPath = "testdata/data.xlsx";

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        driver.manage().window().maximize();
    }

    @Test
    public void runTranscriptDownloadScenario() {
        try {
            // Read credentials
            FileInputStream fis = new FileInputStream(new File(excelPath));
            Workbook wb = new XSSFWorkbook(fis);
            Row row = wb.getSheetAt(0).getRow(1);
            String email = row.getCell(0).toString().trim();
            String password = row.getCell(1).toString().trim();
            String netID = row.getCell(2).toString().trim();

            // Step 1: Go to ME portal
            driver.get("https://me.northeastern.edu/");
            ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario1/01_LandedOnPortal.png");

            // Step 2: Login with email
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0116"))).sendKeys(email);
            driver.findElement(By.id("idSIButton9")).click();
            Thread.sleep(2000);

            // Step 3: Enter password
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0118"))).sendKeys(password);
            driver.findElement(By.id("idSIButton9")).click();
            Thread.sleep(2000);

            wait.until(ExpectedConditions.elementToBeClickable(By.id("idSIButton9"))).click();
            Thread.sleep(2000);
            ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario1/02_AfterLogin.png");

            // Step 4: Resources -> Academics
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-testid='link-resources']"))).click();
            ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario1/03_AfterResourcesClick.png");

            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//span[normalize-space()='Academics, Classes & Registration']"))).click();
            ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario1/04_AfterAcademicsClick.png");

            // Step 5: My Transcript (new tab)
            List<String> tabsBefore = new ArrayList<>(driver.getWindowHandles());
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("My Transcript"))).click();
            Thread.sleep(3000);

            for (String tab : driver.getWindowHandles()) {
                if (!tabsBefore.contains(tab)) {
                    driver.switchTo().window(tab);
                    break;
                }
            }
            ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario1/05_SwitchedToTranscriptTab.png");

            // Step 6: SSO Login
            WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
            WebElement pwd = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", username, netID);
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", pwd, password);
            ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario1/06_CredentialsFilled.png");

            driver.findElement(By.xpath("//button[text()='Log In']")).click();

            // Step 7: Duo
            try {
                WebElement duoIframe = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("duo_iframe")));
                driver.switchTo().frame(duoIframe);
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Send Me a Push')]"))).click();
                driver.switchTo().defaultContent();
                ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario1/07_DuoPushSent.png");
            } catch (Exception e) {
                ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario1/07_DuoPushSkipped.png");
            }

            // Step 8: Select dropdowns
            WebElement levelDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("levl_id")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", levelDropdown);
            Thread.sleep(500);
            new Select(levelDropdown).selectByValue("GR");
            ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario1/08_LevelSelected.png");

            WebElement typeDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("type_id")));
            new Select(typeDropdown).selectByValue("AUDI");
            ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario1/09_TypeSelected.png");

            // Step 9: Submit form
            WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//input[@type='submit' and @value='Submit']")));
            Thread.sleep(1000);
            submitBtn.click();
            ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario1/10_FormSubmitted.png");

            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h2[contains(text(), 'Academic Transcript')]")));
            ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario1/11_TranscriptDisplayed.png");

            // Step 10: Print to PDF via CDP
            Map<String, Object> params = new HashMap<>();
            params.put("paperWidth", 8.27);
            params.put("paperHeight", 11.69);
            params.put("printBackground", true);
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
            Thread.sleep(2000);

            String pdfBase64 = (String) ((ChromeDriver) driver).executeCdpCommand("Page.printToPDF", params).get("data");
            try (FileOutputStream fos = new FileOutputStream("transcript.pdf")) {
                fos.write(Base64.getDecoder().decode(pdfBase64));
            }
            System.out.println("✅ Transcript PDF downloaded.");

        } catch (Exception e) {
            ScreenshotUtil.takeScreenshot(driver, "screenshots/scenario1/99_ErrorOccurred.png");
            e.printStackTrace();
            Assert.fail("Scenario failed: " + e.getMessage());
        }
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}
