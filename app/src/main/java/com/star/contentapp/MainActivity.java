package com.star.contentapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import static android.Manifest.permission.*;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CONTACTS = 1;
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int permission = ActivityCompat.checkSelfPermission(this , Manifest.permission.READ_CONTACTS);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this , new String[]{READ_CONTACTS,WRITE_CONTACTS } ,
                    REQUEST_CONTACTS );
        }
        else {
            readContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CONTACTS:
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                readContacts();
            }
            else
            {
                new AlertDialog.Builder(this).setMessage("必須允許聯絡人權限才能顯示資料")
                        .setPositiveButton("OK" , null)
                        .show();
            }
            return;
        }
    }
    private void readContacts() {
        ContentResolver resolver = getContentResolver();
        String [] projection = {Contacts._ID ,
              Contacts.DISPLAY_NAME , Phone.NUMBER};
        Cursor cursor = resolver.query(Contacts.CONTENT_URI ,
                null , null ,null ,null   );
        Log.d("RECORD", cursor + "/" + projection);
//        while (cursor.moveToNext())
//        {
//            int id = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
//            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//            Log.d("RECORD", id + "/" + name);
//        }

        list = (ListView) findViewById(R.id.list);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2,
                cursor , new String[] {Contacts.DISPLAY_NAME , Contacts.HAS_PHONE_NUMBER} ,
                new int[]{android.R.id.text1 , android.R.id.text2} , 1)  {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                super.bindView(view, context, cursor);
                TextView phone = (TextView) view.findViewById(android.R.id.text2);
                if (cursor.getInt(cursor.getColumnIndex(Contacts.HAS_PHONE_NUMBER)) == 0) {
                    phone.setText("");
                }
                else
                {
                    int id = cursor.getInt(cursor.getColumnIndex(Contacts._ID));
                    Cursor pcursor = getContentResolver().query(
                            Phone.CONTENT_URI ,null , Phone.CONTACT_ID + "=?" ,
                            new String[] {String.valueOf(id)} , null);
                    if(pcursor.moveToFirst())
                    {
                        String number = pcursor.getString(pcursor.getColumnIndex(
                                Phone.DATA));
                        phone.setText(number);
                    }
                }
            }
        };
        list.setAdapter(adapter);
        Log.d("RECORD", list + "/" + adapter);
    }
}
