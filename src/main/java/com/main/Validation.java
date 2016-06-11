package com.main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nagypeter on 2016. 05. 03..
 */
public class Validation {

    static Pattern pattern;
    static Matcher matcher;

    public enum ErrorType{
        TOO_SHORT, EMPTY, NOT_EMAIL,NOT_WEBSITE,OK,NOT_VALID
    }

    public static String REGEX_EMAIL = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public static String REGEX_WEBSITE = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    public static ErrorType ValidateWebsite(String input){

        pattern = Pattern.compile(REGEX_WEBSITE);
        matcher = pattern.matcher(input);

        if (matcher.matches())
            return ErrorType.OK;
        else
            return ErrorType.NOT_WEBSITE;

    }

    public static ErrorType ValidateEmail(String input){
        pattern = Pattern.compile(REGEX_EMAIL);
        matcher = pattern.matcher(input);
        if(matcher.matches())
            return ErrorType.OK;
        else
            return ErrorType.NOT_EMAIL;

    }

    public static ErrorType ValidateEmpty(String input){
        if (input.isEmpty())
            return ErrorType.EMPTY;
        else
            return ErrorType.OK;
    }

    public static ErrorType ValidateShort(String input,int lenght){
        if(input.length() < lenght)
            return ErrorType.TOO_SHORT;
        else
            return ErrorType.OK;
    }

    public static ErrorType ValidateAgainst(String REGEX,String input){
        pattern = Pattern.compile(REGEX);
        matcher = pattern.matcher(input);
        if(matcher.matches())
            return ErrorType.OK;
        else
            return ErrorType.NOT_VALID;
    }
}
