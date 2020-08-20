package io.xream.sqli.repository;

public class BootListener {

    public static void onStarted(){
        HealthChecker.onStarted();
    }
}
