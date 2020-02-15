package com.example.medicinechecker;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

//public class Main2Activity extends AppCompatActivity{
public class Main2Activity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    Button speakBtn;
    ImageView imageView;
    private TextView showText,titleText;
    String rawString;
    String audioString;
    String [] words;
    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        speakBtn = (Button)findViewById(R.id.btnSpeech);
        textToSpeech = new TextToSpeech(this, this);
        showText=(TextView) findViewById(R.id._showText);
        titleText=(TextView) findViewById(R.id._titleText);

        loadData();

        speakBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texttoSpeak();
            }
        });
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

                audioString="Oshudh ere chobi tuloon";

                return;

            }
            else {

                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
                databaseAccess.open();

                ArrayList<String> queryWords=databaseAccess.getQueryWords(rawString);
                ArrayList<String> cursorIndexes=databaseAccess.getCursorIndexes(queryWords);

                Log.d("number","No of Indexes: "+cursorIndexes.size());

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

                //Integer ExactIndex=databaseAccess.getExactIndex(listOfValues);
                Integer ExactIndex=0;
                Log.d("exact","ExactIndex:"+ExactIndex);

                StringBuffer stringBuffer=new StringBuffer();
                StringBuffer medicineNameBuffer=new StringBuffer();
                StringBuffer medicineStrengthBuffer=new StringBuffer();

                for (int i = 0; i <= ExactIndex; i++) {

                    Cursor cursor = databaseAccess.getCursor(listOfKeys.get(i));

                    while (cursor.moveToNext())
                    {

                        medicineNameBuffer.append(cursor.getString(2));
                        medicineStrengthBuffer.append(cursor.getString(4));


                        stringBuffer.append("ওষুধের নাম: "+cursor.getString(2)+"\n");
                        stringBuffer.append("উৎপাদনকারী: "+cursor.getString(1)+"\n");
                        stringBuffer.append("রাসায়নিক নাম: "+cursor.getString(3)+"\n");
                        stringBuffer.append("মূল্য: "+cursor.getString(6)+"\n");
                        stringBuffer.append("ডোজ: "+cursor.getString(5)+"\n");
                        stringBuffer.append("ব্যবহার: "+cursor.getString(7)+"\n");
                        stringBuffer.append("রেজিস্ট্রেশন নাম্বার: "+cursor.getString(8)+"\n\n\n");
                    }

                }

                titleText.setText("ওষুধটি রেজিস্টার্ড ");
                titleText.setTextColor(Color.rgb(0,102,0));
                imageView.setImageResource(R.drawable.right);
                showText.setText(stringBuffer.toString());

                HashMap<String, String> mgAudio = new HashMap<String, String>();
                mgAudio.put("10", "Dosh mili gram");
                mgAudio.put("15", "pon ero mili gram");
                mgAudio.put("20", "bish mili gram");
                mgAudio.put("25", "po chish mili gram");
                mgAudio.put("50", "pon chash mili gram");
                mgAudio.put("75", "pochat toor mili gram");
                mgAudio.put("100", "ek show mili gram");
                mgAudio.put("120", "ek show bish mili gram");
                mgAudio.put("125", "ek show po chish mili gram");
                mgAudio.put("150", "ek show pon chash mili gram");
                mgAudio.put("200", "dui show mili gram");
                mgAudio.put("250", "dui show pon chash mili gram");
                mgAudio.put("400", "char show mili gram");
                mgAudio.put("450", "char show pon chash mili gram");
                mgAudio.put("500", "pash show mili gram");

                String strengthAudio=null;
                String rawString=medicineStrengthBuffer.toString();
                String [] strengthWords = rawString.replaceAll("([\\D]+)", " ").split(" ");

                for (String strengthWord : strengthWords) {

                    if(mgAudio.containsKey(strengthWord)){
                        strengthAudio=" Oshudh tir power  ..."+mgAudio.get(strengthWord);
                        break;
                    }
                    else{
                        strengthAudio="null";
                    }

                }

                audioString="Upnar Oshudh ti Registered ..."+ " a bong oshudh ti manush ere babohar ere jaunno ..."+" Oshudh tir nam ... "+medicineNameBuffer.toString()+" ..."+strengthAudio;

            }

        }
    }

    public void scan(View v) {
        imageView.setImageResource(R.drawable.scan);
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("error", "This Language is not supported");
            } else {
                texttoSpeak();
            }
        } else {
            Log.e("error", "Failed to Initialize");
        }
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void texttoSpeak() {
        String text = audioString;
        if ("".equals(text)) {
            text = "Please enter some text to speak.";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
        else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
