package com.mattmellor.gear;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SuggestedStories extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_stories);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        recommendArticles();


    }


    private void recommendArticles() {
        try {
            String[] listOfArticleAssets = listAllArticles();
            Log.d("first article", listOfArticleAssets[0]);
            countWordsInCommon(listOfArticleAssets[0]);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private String[] listAllArticles() throws IOException {
        AssetManager assetManager = getResources().getAssets();
        String assets[] = assetManager.list("");
        return assets;
    }


    private void countWordsInCommon(String article) {
        HashMap<String, Integer> userVocab = overallUserVocab.getUserDictionary();

        InputStream input;
        String text = article;
        try {
            AssetManager assetManager = getResources().getAssets();
            input = assetManager.open(article);
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            // byte buffer into a string
            text = new String(buffer).trim();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            text = "Error Occurred";
        }

        String[] articleWords = text.split("[\\p{Punct}\\s]+");
        List<String> lowerCaseArticleWords = new ArrayList<String>();
        for (String word : articleWords) {
            lowerCaseArticleWords.add(word.toLowerCase());
        }


        for (String word : articleWords) {Log.d("word", word);};
    }



//    private int calculateDotProduct(String article1, String article2) {
//
//
//    }
//
//    private String[] constructWordVector(String article) {
//
//    }

}
