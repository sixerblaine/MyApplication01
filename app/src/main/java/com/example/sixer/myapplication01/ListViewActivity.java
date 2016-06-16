package com.example.sixer.myapplication01;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListViewActivity extends AppCompatActivity {

    private ListView listView;
    private String[] list = {"鉛筆", "原子筆", "鋼筆", "毛筆", "彩色筆"};
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        listView = (ListView) findViewById(R.id.listview);

        Log.d("sixer",Environment.getExternalStorageDirectory().getPath()); //  /storage/emulated/0

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File yourDir = new File(sdCardRoot, "/Music/");
        ArrayList<String> songlist = new ArrayList<String>();
        for (File f : yourDir.listFiles()) {
            if (f.isFile())
                songlist.add(f.getName());
            // Do your stuff
        }

        for (int i =0;i<songlist.size();i++)
        {
            Log.d("sixer",Integer.toString(i+1)+ ". " + songlist.get(i));
        }

        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, songlist);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "你選擇的是" + list[position], Toast.LENGTH_SHORT).show();
            }
        });
    }
}
