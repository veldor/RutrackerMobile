package net.veldor.rutrackermobile.utils;

import android.util.Log;

import net.veldor.rutrackermobile.App;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MyFileReader {

    public static final String SEARCH_AUTOCOMPLETE_FILE = "searchAutocomplete.xml";
    private static final String SEARCH_AUTOCOMPLETE_NEW = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><search> </search>";

    public static String getSearchAutocomplete() {
        File autocompleteFile = new File(App.getInstance().getFilesDir(), SEARCH_AUTOCOMPLETE_FILE);
        if (!autocompleteFile.exists()) {
            makeFile(autocompleteFile, SEARCH_AUTOCOMPLETE_NEW);
        }
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(autocompleteFile));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    private static void makeFile(File file, String content) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.append(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void saveSearchAutocomplete(String value) {
        File autocompleteFile = new File(App.getInstance().getFilesDir(), SEARCH_AUTOCOMPLETE_FILE);
        makeFile(autocompleteFile, value);
    }
}
