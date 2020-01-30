package com.aut.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

public class LogoutPage extends BasePage {

    private static By logoutButton = By.xpath("//div[@title='Logout']");

//    public void visit(){
//        driver.get(Url.concat("/LoginSignUp/loginsignup_mode_login/false"));
//    }

    public  void logout(){
        click(LogoutPage.logoutButton);
    }

}
