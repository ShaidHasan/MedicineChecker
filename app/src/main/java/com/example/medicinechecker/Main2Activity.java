package com.example.medicinechecker;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Main2Activity extends AppCompatActivity {

    ImageView imageView;
    private TextView showText,titleText;
    String rawString;
    String [] words;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        showText=(TextView) findViewById(R.id._showText);
        titleText=(TextView) findViewById(R.id._titleText);

        loadData();
    }


    public void loadData(){
        Bundle bundle=getIntent().getExtras();
        imageView= (ImageView) findViewById(R.id.titleImage);

        if(bundle!=null)
        {
            rawString=bundle.getString("ScannedText");

            if(rawString.equals("dummyText"))
            {
                titleText.setText("ওষুধের নাম স্ক্যান করুন");
                titleText.setTextColor(Color.BLUE);
                imageView.setImageResource(R.drawable.scan);
                return;

            }
            else {

                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
                databaseAccess.open();

                ArrayList<String> queryWords=databaseAccess.getQueryWords(rawString);
                ArrayList<String> cursorIndexes=databaseAccess.getCursorIndexes(queryWords);

                Log.d("msg","No of Indexes: "+cursorIndexes.size());

                if(cursorIndexes.size()==0){
                    titleText.setText("ওষুধটি রেজিস্টার্ড নয়");
                    titleText.setTextColor(Color.RED);
                    imageView.setImageResource(R.drawable.wrong);
                    return;
                }

                HashMap<String, Integer> IndexFrequency=databaseAccess.getIndexFrequency(cursorIndexes);
                //Creating an ArrayList of keys
                Set<String> keySet = IndexFrequency.keySet();
                ArrayList<String> listOfKeys = new ArrayList<String>(keySet);
                //Creating an ArrayList of values
                Collection<Integer> values = IndexFrequency.values();
                ArrayList<Integer> listOfValues = new ArrayList<Integer>(values);


                for (Map.Entry<String, Integer> en : IndexFrequency.entrySet()) {
                    Log.d("keyval","Key:" + en.getKey() + ", Value:" + en.getValue());
                }



                Integer ExactIndex=databaseAccess.getExactIndex(listOfValues);
                Log.d("msg","ExactIndex:"+ExactIndex);

                StringBuffer stringBuffer=new StringBuffer();

                for (int i = 0; i <= ExactIndex; i++) {

                    Cursor cursor = databaseAccess.getCursor(listOfKeys.get(i));

                    while (cursor.moveToNext())
                    {
                        stringBuffer.append("ওষুধের নাম: "+cursor.getString(2)+"\n");
                        stringBuffer.append("উৎপাদনকারী: "+cursor.getString(1)+"\n");
                        stringBuffer.append("রাসায়নিক নাম: "+cursor.getString(3)+"\n");
                        stringBuffer.append("মূল্য: "+cursor.getString(6)+"\n");
                        stringBuffer.append("ডোজ: "+cursor.getString(5)+"\n");
                        stringBuffer.append("রেজিস্ট্রেশন নাম্বার: "+cursor.getString(8)+"\n\n\n");
                    }

                }

                titleText.setText("ওষুধটি রেজিস্টার্ড ");
                titleText.setTextColor(Color.rgb(0,102,0));
                imageView.setImageResource(R.drawable.right);
                showText.setText(stringBuffer.toString());

            }



        }
    }

    public void scan(View v) {
        imageView.setImageResource(R.drawable.scan);
    }


}
