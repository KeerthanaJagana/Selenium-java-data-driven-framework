package scenario;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.testng.annotations.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

public class Scenario2Test {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String excelPath = "testdata/data.xlsx";
    private String email;
    private String password;
    private final String screenshotDir = "screenshots/scenario2/";

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.manage().window().maximize();

        String[] creds = readCredentialsFromSheet(excelPath);
        email = creds[0];
        password = creds[1];
    }

    @Test(priority = 1)
    public void loginToCanvas() throws InterruptedException {
        driver.get("https://canvas.northeastern.edu/");
        Thread.sleep(2000);
        takeScreenshot("01_Canvas_Home");

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Log in to Canvas')]")));
        loginButton.click();
        Thread.sleep(2000);
        takeScreenshot("02_Clicked_Login");

        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("i0116")));
        emailField.sendKeys(email);
        takeScreenshot("03_Entered_Email");
        Thread.sleep(1000);
        driver.findElement(By.id("idSIButton9")).click();
        Thread.sleep(2000);

        WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("i0118")));
        passwordField.sendKeys(password);
        takeScreenshot("04_Entered_Password");
        Thread.sleep(1000);
        driver.findElement(By.id("idSIButton9")).click();
        Thread.sleep(2000);

        try {
            WebElement staySignedInButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("idSIButton9")));
            staySignedInButton.click();
            takeScreenshot("05_StaySignedInClicked");
        } catch (Exception ignored) {}

        try {
            WebElement noTrust = wait.until(ExpectedConditions.elementToBeClickable(By.id("dont-trust-browser-button")));
            noTrust.click();
            takeScreenshot("06_NoTrustClicked");
        } catch (Exception ignored) {}

        Thread.sleep(5000);
        takeScreenshot("07_LoggedIntoCanvas");
    }

    @Test(priority = 2, dependsOnMethods = "loginToCanvas")
    public void openCalendarAndAddToDos() throws InterruptedException {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("global_nav_dashboard_link")));
        WebElement calendarLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("global_nav_calendar_link")));
        calendarLink.click();
        Thread.sleep(2000);
        takeScreenshot("08_OpenedCalendar");

        List<Map<String, String>> events = readTodoData(excelPath, 1); // Sheet 1

        for (Map<String, String> event : events) {
            System.out.println("📝 Creating To-Do: " + event.get("Title"));

            WebElement plusIcon = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("i.icon-plus")));
            plusIcon.click();
            Thread.sleep(2000);
            takeScreenshot("09_Clicked_Plus");

            WebElement addTodo = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("My To Do")));
            addTodo.click();
            Thread.sleep(2000);
            takeScreenshot("10_Selected_MyToDo");

            driver.findElement(By.id("planner_note_title")).sendKeys(event.get("Title"));
            takeScreenshot("11_Entered_Title");
            Thread.sleep(1000);

            WebElement dateInput = driver.findElement(By.id("planner_note_date"));
            dateInput.clear();
            dateInput.sendKeys(event.get("Date"));
            takeScreenshot("12_Entered_Date");
            Thread.sleep(1000);

            driver.findElement(By.id("planner_note_time")).sendKeys(event.get("Time"));
            takeScreenshot("13_Entered_Time");
            Thread.sleep(1000);

            WebElement dropdown = driver.findElement(By.id("planner_note_context"));
            new Select(dropdown).selectByVisibleText(event.get("Calendar"));
            takeScreenshot("14_Selected_Calendar");
            Thread.sleep(1000);

            driver.findElement(By.id("details_textarea")).sendKeys(event.get("Details"));
            takeScreenshot("15_Entered_Details");
            Thread.sleep(1000);

            driver.findElement(By.cssSelector(".event_button.btn.btn-primary.save_note")).click();
            Thread.sleep(2000);
            takeScreenshot("16_ToDo_Saved");
        }
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    private void takeScreenshot(String step) {
        ScreenshotUtil.takeScreenshot(driver, screenshotDir + step + ".png");
    }

    private String[] readCredentialsFromSheet(String filePath) {
        String[] creds = new String[2];
        try (FileInputStream file = new FileInputStream(new File(filePath)); Workbook wb = new XSSFWorkbook(file)) {
            Row row = wb.getSheetAt(0).getRow(1);
            creds[0] = row.getCell(0).toString();
            creds[1] = row.getCell(1).toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return creds;
    }

    private List<Map<String, String>> readTodoData(String filePath, int sheetIndex) {
        List<Map<String, String>> todos = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath)); Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(sheetIndex);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) continue;

                Map<String, String> map = new HashMap<>();
                map.put("Title", row.getCell(0).toString());

                if (row.getCell(1).getCellType() == CellType.NUMERIC) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy");
                    map.put("Date", sdf.format(row.getCell(1).getDateCellValue()));
                } else {
                    map.put("Date", row.getCell(1).toString());
                }

                if (row.getCell(2).getCellType() == CellType.NUMERIC) {
                    SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a");
                    map.put("Time", sdfTime.format(row.getCell(2).getDateCellValue()));
                } else {
                    map.put("Time", row.getCell(2).toString());
                }

                map.put("Calendar", row.getCell(3).toString());
                map.put("Details", row.getCell(4).toString());

                todos.add(map);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return todos;
    }
}
