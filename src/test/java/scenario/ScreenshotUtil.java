package scenario;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ScreenshotUtil {
    public static void takeScreenshot(WebDriver driver, String filePath) {
        File scr = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            Files.createDirectories(new File(filePath).getParentFile().toPath());
            Files.copy(scr.toPath(), new File(filePath).toPath(),StandardCopyOption.REPLACE_EXISTING
);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
