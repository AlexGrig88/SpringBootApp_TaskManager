package com.alexgrig.education.springboottasks.business.util;

import lombok.extern.java.Log;

import java.util.logging.Level;

@Log
public class MyLogger {

    public static void debugMethodName(String name) {
        String nameWithDecor = "\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n" +
                "--------------------" + name + "-------------------------\n" +
                "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n";
        log.log(Level.INFO, nameWithDecor);
    }
}
