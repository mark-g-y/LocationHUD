package com.locationhud;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Mark on 27/10/2014.
 */
public class FileStorage {

    private static final String FILE_NAME = "poi";

    public static String readFromFile(Context context) {
        StringBuilder builder = new StringBuilder();
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            InputStreamReader in = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(in);
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            in.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return builder.toString();
    }

    public static void writeToFile(Context context, String data) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(data.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
