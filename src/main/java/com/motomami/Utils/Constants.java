package com.motomami.Utils;

import java.util.ArrayList;

public class Constants {
    public final static String C_SOURCE_PARTS = "PARTS";
    public final static String C_SOURCE_CUSTOMER = "CUSTOMERS";
    public final static String C_SOURCE_VEHICLE = "VEHICLES";
    public final static String[] estadoFichero = {"N", "E" ,"P"};
    public final static String[] operation = {"New", "Update", "Delete"};

    public final static String user = "mm_app";

    public static ArrayList<String> customersDNI = new ArrayList<>();

    public static ArrayList<Integer> vehiclesId = new ArrayList<>();

    public static ArrayList<String> vehiclesIdExternal = new ArrayList<>();

    public enum TABLE_NAME {mm_intcustomers}
}
