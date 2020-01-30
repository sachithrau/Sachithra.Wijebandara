package com.aut.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;


public class LoginPage extends BasePage {

    // Login Page locators
    private static By usernameText = By.xpath("(//input[@name='username'])[2]");
    private static By passwordText = By.xpath("(//input[@name='password'])[2]");
    private static By loginButton  = By.xpath("(//input[@name='signInSubmitButton'])[2]");
    private static By homePage     = By.xpath("//p[text()='Provider']");


    public void visit(){
        driver.get(Url);
        driver.manage().window().maximize();
    }

    public void login(String username, String password){

        sendKeys(LoginPage.usernameText, username);
        sendKeys(LoginPage.passwordText, password);
        click(LoginPage.loginButton);
        waitForElementVisibleCondition(LoginPage.homePage);


    }



}
