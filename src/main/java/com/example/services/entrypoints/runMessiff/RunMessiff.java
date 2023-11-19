package com.example.services.entrypoints.runMessiff;

import com.example.services.configuration.AppConfig;
import com.example.services.utils.SystemUtils;

public class RunMessiff {
    public static void run() {
        System.out.println("RunMessiff.run");
        SystemUtils.execInParent(new String[]{AppConfig.MESSIFF_SHORT_SKETCHES_MANAGER_SCRIPT, "-la"});
    }
}
