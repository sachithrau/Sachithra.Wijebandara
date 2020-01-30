package utils;

import com.thoughtworks.gauge.AfterSuite;
import com.thoughtworks.gauge.BeforeSuite;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.Proxy;


import java.io.IOException;

public class DriverFactory {


    private static final String ZAP_ADDRESS =  System.getenv("ZED_HOSTIP");
    private static final int ZAP_PORT = Integer.parseInt(System.getenv("ZED_HOSTPORT"));

    private static WebDriver driver;

    public static WebDriver getDriver() {
        return driver;
    }

    @BeforeSuite
    public void Setup() throws IOException {
        String browser = System.getenv("BROWSER");


        // Uses chrome driver by default
        if (browser == null) {
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
        }

        switch (browser) {
            case "IE":

                WebDriverManager.iedriver().setup();
                driver = new InternetExplorerDriver();

                break;
            case "CHROME":

//                ChromeOptions chromeOptions = new ChromeOptions();
//                chromeOptions.addArguments("--headless");
//                ChromeDriverManager.chromedriver().setup();
                WebDriverManager.chromedriver().setup();
                driver = new ChromeDriver();


                break;
            case "FIREFOX":
                WebDriverManager.firefoxdriver().setup();
                driver = new FirefoxDriver();

                break;
            case "ZAP_PROXY": //set the proxy to use ZAP host and port
                DesiredCapabilities capabilities = DesiredCapabilities.chrome();
                capabilities.setCapability("proxy", createZapProxyConfiguration());

                // Set system property for chrome driver with the path
                capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.merge(capabilities);
                driver = new ChromeDriver(options);
                break;
        }

    }

    @AfterSuite
    public void TearDown() {
        driver.close();
        driver.quit();
    }

    public static Proxy createZapProxyConfiguration() {
        Proxy proxy = new Proxy();
        proxy.setHttpProxy(ZAP_ADDRESS + ":" + ZAP_PORT);
        proxy.setSslProxy(ZAP_ADDRESS + ":" + ZAP_PORT);
        return proxy;
    }

}
