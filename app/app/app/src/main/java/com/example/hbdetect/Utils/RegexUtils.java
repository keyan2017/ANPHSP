package com.example.hbdetect.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {
    private static Pattern NUMBER_PATTERN = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
    public static boolean isNumber(String str){
        Matcher isNum = NUMBER_PATTERN.matcher(str);
        return isNum.matches();
    }
}
