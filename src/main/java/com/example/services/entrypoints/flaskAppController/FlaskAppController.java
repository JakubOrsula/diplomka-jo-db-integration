package com.example.services.entrypoints.flaskAppController;

import com.example.services.utils.SystemUtils;
import com.example.utils.UnrecoverableError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class FlaskAppController {
    public static void restartFlask() {
        System.out.println("""
                ############################################################
                # RESTART FLASK START
                ############################################################
                """);
        SystemUtils.exec(new String[]{"sudo", "systemctl", "restart", "protein-search-web.service"});
        System.out.println("""
                ############################################################
                # RESTART FLASK END
                ############################################################
                """);
    }
}