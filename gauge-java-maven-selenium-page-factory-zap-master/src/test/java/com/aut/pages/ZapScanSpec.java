package com.aut.pages;

import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;
import org.junit.Assert;
import org.zaproxy.clientapi.core.ClientApiException;
import utils.ZapApi;
import org.zaproxy.clientapi.core.Alert;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


import java.util.List;
import java.util.logging.Logger;

import static org.hamcrest.core.IsEqual.equalTo;

public class ZapScanSpec extends ZapApi {

    static Logger log = Logger.getLogger(ZapScanSpec.class.getName());


    @Step("Configure ZAP Vulnerability Scan Settings and Start the Scanning <table>")
    public void ConfigZapSettings(Table table) {
        for (TableRow row : table.getTableRows()) {

            //--------------------  Using ZAP Spider    --------------------

            log.info("Started spidering..........");
            spiderWithZap();
            log.info("Ended spidering  !");

            // ---------------   Setting alert and attack   ----------------

            setAlertAndAttackStrength(row.getCell("attack_alert_threshold"), row.getCell("attack_strength"));

            if (row.getCell("passive_scan").contentEquals("true")) {

                zapScanner.setEnablePassiveScan(true);

            }else {

                zapScanner.setEnablePassiveScan(false);

            }

            //--------------   Using ZAP Scanner   -------------------

            log.info("Started scanning........");

            scanWithZap();

            log.info("Ended scanning   !");

            generateHTMLReport();

        }

    }

    @Step("Validate Vulnerability Alerts are less than <table>")
    public void validateAlerts(Table table) {

        for (TableRow row : table.getTableRows()) {

            List<Alert> generatedAlerts = filterAlerts(zapScanner.getAlerts());

            List<Alert> lowAlerts = getAlertsWithRisk(generatedAlerts, Alert.Risk.Low);
            List<Alert> mediumAlerts = getAlertsWithRisk(generatedAlerts, Alert.Risk.Medium);
            List<Alert> highAlerts = getAlertsWithRisk(generatedAlerts, Alert.Risk.High);

            System.out.println(generatedAlerts);


            String setPlainText = "\033[0;0m";
            String setBoldText = "\033[0;1m";
            log.info(setBoldText + " Found " + lowAlerts.size() + " low risk alerts: " + setPlainText);
            lowAlerts.forEach(a -> log.info("Alert: " + a.getAlert() + " at URL: " + a.getUrl() + " Parameter: " + a.getParam() + " CWE ID: " + a.getCweId() + " Risk: " + a.getRisk()));
            log.info(setBoldText + " Found " + mediumAlerts.size() + " medium risk alerts: " + setPlainText);
            mediumAlerts.forEach(a -> log.info("Alert: " + a.getAlert() + " at URL: " + a.getUrl() + " Parameter: " + a.getParam() + " CWE ID: " + a.getCweId() + " Risk: " + a.getRisk()));
            log.info(setBoldText + " Found " + highAlerts.size() + " high risk alerts: " + setPlainText);
            highAlerts.forEach(a -> log.info("Alert: " + a.getAlert() + " at URL: " + a.getUrl() + " Parameter: " + a.getParam() + " CWE ID: " + a.getCweId() + " Risk: " + a.getRisk()));


            assertThat(lowAlerts.size()).as("Low risk Alerts").isLessThanOrEqualTo(Integer.parseInt(row.getCell("low")));
            assertThat(mediumAlerts.size()).as("Medium risk Alerts").isLessThanOrEqualTo(Integer.parseInt(row.getCell("medium")));
            assertThat(highAlerts.size()).as("High risk Alerts").isLessThanOrEqualTo(Integer.parseInt(row.getCell("high")));

        }
    }


    @Step("Exclude following URLs from the Scan <table>")
    public void excludeUrls(Table table) {
        for (TableRow row : table.getTableRows()){

            if (row.getCell("url").isEmpty()) {

                log.info("No Urls to be exclude");

            }else {
                zapScanner.excludeFromSpider(row.getCell("url"));
                zapScanner.excludeFromScanner(row.getCell("url"));
            }
        }

    }
}
