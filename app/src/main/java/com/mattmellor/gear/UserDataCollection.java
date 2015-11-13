package com.mattmellor.gear;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/*
 * Created by Saya on 11/8/2015.
 */

public class UserDataCollection{

    private Context context;
    public UserDataCollection(Context context) {
        context = context;
    }

    public static Set<UserData> allUserData = new HashSet<>();




    public static void addUserDataToAllUserData(UserData userData) {
        if (!allUserData.contains(userData)) {
            allUserData.add(userData);
        }
        // should never get here
    }

    public static void addRating(String article, String userId, int rating){
        UserData toTest = new UserData(Integer.parseInt(userId), article);
        for(UserData data: allUserData){

            if (toTest.equals(data)){
                data.rate(rating);
            }



        }
    }

    public static Set<UserData> getAllUserData() {
        Set<UserData> copyOfData = new HashSet<UserData>();
        for (UserData data : allUserData) {
            copyOfData.add(data);
        }
        return copyOfData;
    }




    public void writeToFile(String filename) throws IOException {


        String content = "";
        for (UserData userData : getAllUserData()) {
            content += userData.toString() + ", "; // might want to and a new line
        }

        String string = "Hello world!";
        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Context context = getApplicationContext();
//
//        File file = new File(context.getFilesDir(), filename); // need to add some relative path
//
//        // if file doesnt exists, then create it
//        if (!file.exists()) {
//            file.createNewFile();
//
//        }
//
//
//        FileWriter fw = new FileWriter(file.getAbsoluteFile()); // how do I make sure that the file is not overwritten? or maybe I'm ok with it being overwritten
//        BufferedWriter bw = new BufferedWriter(fw);
//        bw.write(content);
//        bw.close();

    }


}