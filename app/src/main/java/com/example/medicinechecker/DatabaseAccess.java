package com.example.medicinechecker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DatabaseAccess {

    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;
    private static final String TABLE_NAME="DrugData";
    //private static final String COLUMN_NAME="Brand";
    private static final String COLUMN_NAME="BrandQuery";

    String [] words;

    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    public static DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }


    public void open() {
        this.database = openHelper.getWritableDatabase();
    }


    public void close() {
        if (database != null) {
            this.database.close();
        }
    }


    public ArrayList<String> getQueryWords(String rawString) {

        Log.d("raw",rawString);

        //String pattern1 = "([\\s]+[\\d]+)";      //digit removal
        String pattern1 = "([\\s]+[mM][gG])";   //mg removal
        String pattern2 = "([\\s]+[bB][pP])";   //mg removal
        String pattern3 = "([\\s]+[\\W]+)";   //Non-alpha neumeric removal
        String pattern4 = "([\\s]+[a-zA-Z][\\s]+)";   //Single letter removal
        String pattern5 = "([\\s]+[uU][sS][pP])";   //mg removal
        String pattern6 = "([\\n]+)";   //mg removal

        String cleanString= rawString
                .replaceAll(pattern1, " ")
                .replaceAll(pattern2, " ")
                .replaceAll(pattern3, " ")
                .replaceAll(pattern4, " ")
                .replaceAll(pattern5, " ")
                .replaceAll(pattern6, " ");

        Log.d("clean",cleanString);

        String [] words = cleanString.split(" ");
        ArrayList<String> queryWords = new ArrayList<String>();

        for (String word : words) {
            queryWords.add(word.replaceAll("\r", "").replaceAll("\n", "").toLowerCase());
        }

        Set<String> Duplicates = new LinkedHashSet<String>(queryWords);
        queryWords.clear();
        queryWords.addAll(Duplicates);
        /*
        for (String str1 : queryWords) {
            Log.d("query:",str1);
        }
        */
        Log.d("query:",queryWords.toString());
        return queryWords;

    }

    public ArrayList<String> getCursorIndexes(ArrayList<String> queryWords) {

        ArrayList<String> cursorIndexes = new ArrayList<String>();

        for(String queryWord:queryWords)
        {

            Cursor cursor = database.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE LOWER("+COLUMN_NAME+") LIKE  '%@"+queryWord.toLowerCase()+"@%'",null);


            while (cursor.moveToNext())
            {
                cursorIndexes.add(cursor.getString(0));
            }


        }

        return cursorIndexes;
    }

    public HashMap<String, Integer> getIndexFrequency(ArrayList<String> cursorIndexes){

        int n = cursorIndexes.size();
        Map<String, Integer> mp = new HashMap<>();
        for (int i = 0; i < n; i++)
        {
            if (mp.containsKey(cursorIndexes.get(i)))
            {
                mp.put(cursorIndexes.get(i), mp.get(cursorIndexes.get(i)) + 1);
            }
            else
            {
                mp.put(cursorIndexes.get(i), 1);
            }
        }

        List<Map.Entry<String, Integer> > list1 =
                new LinkedList<Map.Entry<String, Integer> >(mp.entrySet());


        // Sort the list
        Collections.sort(list1, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });


        // put data from sorted list to hashmap

        HashMap<String, Integer> IndexFrequency = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list1) {
            IndexFrequency.put(aa.getKey(), aa.getValue());
        }

        return IndexFrequency;
    }

    public Integer getExactIndex(ArrayList<Integer> values){

        int i;
        for (i = 1; i < values.size(); i++) {
            //System.out.println(values.get(i));

            if(values.get(i) != values.get(i-1))
            {
                break;
            }

        }

        return (i-1);
    }

    public Cursor getCursor(String query) {

        Cursor cursor = database.rawQuery("SELECT * FROM DrugData WHERE ID='"+query+"'",null);
        //Cursor cursor = database.rawQuery("SELECT * FROM DrugData WHERE ID='14873'",null);
        return cursor;
    }


}

