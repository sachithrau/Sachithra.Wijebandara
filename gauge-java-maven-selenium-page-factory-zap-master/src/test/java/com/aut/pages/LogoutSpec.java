package com.aut.pages;

import com.thoughtworks.gauge.Step;


public class LogoutSpec {

//    @Step("Logout from the application")
//    public void visit() {
//        PageFactory.logoutPage.visit();
//    }

    @Step("Logout from the application")
    public void logout() {
        PageFactory.logoutPage.logout();
    }

}
