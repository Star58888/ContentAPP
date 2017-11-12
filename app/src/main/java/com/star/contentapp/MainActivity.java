package com.star.contentapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.*;
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

import java.util.ArrayList;

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
//        insertContact();
//        updateContact();
        deleteContact();
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

    private void insertContact()
    {
        ArrayList ops = new ArrayList();
        int index = ops.size();
        ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE , null)
                .withValue(RawContacts.ACCOUNT_NAME , null).build());
        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
        .withValueBackReference(Data.RAW_CONTACT_ID , index)
        .withValue(Data.MIMETYPE , StructuredName.CONTENT_ITEM_TYPE)
        .withValue(StructuredName.DISPLAY_NAME , "Jane").build());
        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
        .withValueBackReference(Data.RAW_CONTACT_ID ,index)
        .withValue(Data.MIMETYPE , Phone.CONTENT_ITEM_TYPE)
        .withValue(Phone.NUMBER , "0900112233")
        .withValue(Phone.TYPE ,Phone.TYPE_MOBILE).build());
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY , ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private void updateContact() {
        String where = Phone.DISPLAY_NAME + " = ? AND " + Data.MIMETYPE + " = ? ";
        String[] params = new String[] {"Jane" , Phone.CONTENT_ITEM_TYPE};
        ArrayList ops = new ArrayList();
        ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
        .withSelection(where , params)
        .withValue(Phone.NUMBER , "0900333333")
        .build());
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY , ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private  void deleteContact()
    {
        String where = ContactsContract.Data.DISPLAY_NAME + " = ? " ;
        String[] params = new String[] {"Jane" };
        ArrayList ops = new ArrayList();
        ops.add(ContentProviderOperation.newDelete(RawContacts.CONTENT_URI)
        .withSelection(where , params)
        .build());
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY , ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }
}
