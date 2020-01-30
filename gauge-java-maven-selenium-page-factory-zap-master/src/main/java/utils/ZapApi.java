package utils;

import com.aut.pages.BasePage;
import com.thoughtworks.gauge.AfterSuite;
import net.continuumsecurity.proxy.ProxyException;
import net.continuumsecurity.proxy.Spider;
import net.continuumsecurity.proxy.ZAProxyScanner;
import org.apache.log4j.Logger;
import org.zaproxy.clientapi.core.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ZapApi extends BasePage {

    private static Spider zapSpider;
    protected static ZAProxyScanner zapScanner = null;
    static Logger log = Logger.getLogger(ZapApi.class.getName());
    private static final String ZAP_ADDRESS = System.getenv("ZED_HOSTIP");
    private static final int ZAP_PORT = Integer.parseInt(System.getenv("ZED_HOSTPORT"));
    private static final String ZAP_API_KEY = System.getenv("ZED_APIKEY");
    private static Process p;

    private final static String[] policyNames = {"directory-browsing", "cross-site-scripting", "sql-injection", "path-traversal", "remote-file-inclusion", "server-side-include",
            "script-active-scan-rules", "server-side-code-injection", "external-redirect", "crlf-injection"};
    int currentScanID;


    public static void initiateZapScanner() {
        zapScanner = new ZAProxyScanner(ZAP_ADDRESS, ZAP_PORT, ZAP_API_KEY);
        zapScanner.clear(); //Start a new session
        zapSpider = (Spider) zapScanner;
        log.info("Created client to ZAP API");
    }

    /*
        Remove false positives, filter based on risk and reliability
     */


    public static void OpenZap() {
        try {

            if (System.getProperty("os.name").contains("Mac")) {

                String[] cmd = {"sh", System.getProperty("user.dir").concat("/resources/ZAP_2.8.02/zap.sh")};
                p = Runtime.getRuntime().exec(cmd);
                p.waitFor(30, TimeUnit.SECONDS);

            } else {
                File dir = new File(System.getProperty("user.dir").concat("\\resources\\ZAP_2.8.02"));
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C", "Start","zap.bat");
                pb.directory(dir);
                p = pb.start();

                Thread.sleep(30000);

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    protected List<Alert> filterAlerts(List<Alert> alerts) {
        List<Alert> filtered = new ArrayList<Alert>();

        for (Alert alert : alerts) {
            if (alert.getRisk().equals(Alert.Risk.High) && alert.getConfidence() != Alert.Confidence.Low)
                filtered.add(alert);
            if (alert.getRisk().equals(Alert.Risk.Medium) && alert.getConfidence() == Alert.Confidence.High)
                filtered.add(alert);
            if (alert.getRisk().equals(Alert.Risk.Low) && alert.getConfidence() == Alert.Confidence.High)
                filtered.add(alert);

        }
        return filtered;
    }

    public void setAlertAndAttackStrength(String AlertThreshold, String AttackStrength) {
        for (String policyName : policyNames) {
            String ids = enableZapPolicy(policyName);
            for (String id : ids.split(",")) {
                zapScanner.setScannerAlertThreshold(id, AlertThreshold);
                zapScanner.setScannerAttackStrength(id, AttackStrength);
            }
        }
    }

    protected List<Alert> getAlertsWithRisk(List<Alert> alertsList, Alert.Risk risk) {
        return alertsList.stream().filter(a -> a.getRisk().equals(risk)).collect(Collectors.toList());
    }

    protected void scanWithZap() throws ProxyException{
        log.info("Scanning...");
        zapScanner.scan(Url);
        currentScanID = zapScanner.getLastScannerScanId();
        int complete = 0;
        while (complete < 100) {
            complete = zapScanner.getScanProgress(currentScanID);
            log.info("Scan is " + complete + "% complete.");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("Scanning done.");
    }


    private String enableZapPolicy(String policyName) {
        String scannerIds = null;
        switch (policyName.toLowerCase()) {
            case "directory-browsing":
                scannerIds = "0";
                break;
            case "cross-site-scripting":
                scannerIds = "40012,40014,40016,40017";
                break;
            case "sql-injection":
                scannerIds = "40018";
                break;
            case "path-traversal":
                scannerIds = "6";
                break;
            case "remote-file-inclusion":
                scannerIds = "7";
                break;
            case "server-side-include":
                scannerIds = "40009";
                break;
            case "script-active-scan-rules":
                scannerIds = "50000";
                break;
            case "server-side-code-injection":
                scannerIds = "90019";
                break;
            case "remote-os-command-injection":
                scannerIds = "90020";
                break;
            case "external-redirect":
                scannerIds = "20019";
                break;
            case "crlf-injection":
                scannerIds = "40003";
                break;
            case "source-code-disclosure":
                scannerIds = "42,10045,20017";
                break;
            case "shell-shock":
                scannerIds = "10048";
                break;
            case "remote-code-execution":
                scannerIds = "20018";
                break;
            case "ldap-injection":
                scannerIds = "40015";
                break;
            case "xpath-injection":
                scannerIds = "90021";
                break;
            case "xml-external-entity":
                scannerIds = "90023";
                break;
            case "padding-oracle":
                scannerIds = "90024";
                break;
            case "el-injection":
                scannerIds = "90025";
                break;
            case "insecure-http-methods":
                scannerIds = "90028";
                break;
            case "parameter-pollution":
                scannerIds = "20014";
                break;
            default:
                throw new RuntimeException("No policy found for: " + policyName);
        }
        if (scannerIds == null) throw new RuntimeException("No matching policy found for: " + policyName);
        zapScanner.setEnableScanners(scannerIds, true);
        return scannerIds;
    }

    protected void spiderWithZap() {
        zapSpider.setThreadCount(5);
        zapSpider.setMaxDepth(5);
        zapSpider.setPostForms(false);

        // Execute the ZAP spider
        zapSpider.spider(Url);

        int spiderID = zapSpider.getLastSpiderScanId();
        int complete = 0;
        while (complete < 100) {
            complete = zapSpider.getSpiderProgress(spiderID);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (String url : zapSpider.getSpiderResults(spiderID)) {
            log.info("Found URL: " + url);
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    protected void generateHTMLReport() {
        byte[] report = null;
        report = zapScanner.getHtmlReport();
        OutputStream htmlFile = null;
        try {
            String reportPath = System.getProperty("user.dir") + "/resources/security-reports/security-report.html";
            File htmlReport = new File(reportPath);
            htmlReport.getParentFile().mkdirs();
            htmlReport.createNewFile();
            htmlFile = new FileOutputStream(htmlReport.getAbsoluteFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            htmlFile.write(report);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            htmlFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterSuite
    public void afterSuite(){

        try {

            p.destroy();
            p.destroyForcibly();
            p.exitValue();


        }catch (NullPointerException e){

            log.info("Zap is not opened");

        }


    }
}

