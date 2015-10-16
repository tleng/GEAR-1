package com.mattmellor.gear;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class  MainActivity extends AppCompatActivity {
    private static String LOG_APP_TAG = "tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //TextView txtContent = (TextView) findViewById(R.id.articleView);
        final EditText txtContent = (EditText) findViewById(R.id.articleView);
        final TextView definition = (TextView) findViewById(R.id.definition_box);

        View.OnLongClickListener lc = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int selection_start = txtContent.getSelectionStart();
                int selection_end = txtContent.getSelectionEnd();

                Spannable str = txtContent.getText();
                str.setSpan(new BackgroundColorSpan(0xFFFF),selection_start,selection_end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                String text = txtContent.getText().toString();
                String copy = text.substring(selection_start, selection_end);


                String definitionWords = dictionaryOutput(copy);
                definition.setText(definitionWords);

                Context context = getApplicationContext();
                CharSequence message = copy + " ausgewählt.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, message, duration);
                toast.setGravity(Gravity.TOP,0,0);
                toast.show();
                definition.setText(copy);

                return false;
            }
        };

        txtContent.setOnLongClickListener(lc);

        AssetManager assetManager = getAssets();

        String story = getIntent().getExtras().getString("story");
        // To load text file
        InputStream input;
        String text = story;
        try {
            input = assetManager.open(story);
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            // byte buffer into a string
            text = new String(buffer);
            System.out.println(text);

            txtContent.setText(text);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            text = "Error Occurred";
        }

        System.out.println(story);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    

    public void toggleDictionary(View view){
        TextView dictionaryText = (TextView) findViewById(R.id.definition_box);
        dictionaryText.setVisibility(dictionaryText.isShown() ? View.GONE : View.VISIBLE);
        RadioButton radioButton = (RadioButton) findViewById(R.id.show_dictionary);
        radioButton.setVisibility(dictionaryText.isShown() ? View.GONE : View.VISIBLE);
    }

    public void showDictionary(View view) {
        TextView dictionaryText = (TextView) findViewById(R.id.definition_box);
        dictionaryText.setVisibility(dictionaryText.isShown() ? View.GONE : View.VISIBLE);
        RadioButton radioButton = (RadioButton) findViewById(R.id.show_dictionary);
        radioButton.setVisibility(dictionaryText.isShown() ? View.GONE : View.VISIBLE);
    }

    public String dictionaryOutput(String word){

        String start = "Definition: " + word + " \n";

        return start;

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
