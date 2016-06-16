package com.example.sixer.myapplication01;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    // Storage Permissions
//    private static final int REQUEST_EXTERNAL_STORAGE = 1;
//    private static String[] PERMISSIONS_STORAGE = {
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//    };

    AlertDialog.Builder builder;
    private PopupWindow popupWindow;
    public MediaPlayer mp;
    public TextView songName, duration;
    private double timeElapsed = 0, finalTime = 0;
    private int forwardTime = 2000, backwardTime = 2000;
    private Handler durationHandler = new Handler();
    private SeekBar seekbar;
    private ImageButton btnPlay, btnPause, btnStop, btnRewind, btnForward;
    private Button btnShowSongList, btnSongNo, btnSongYes;
    private View popview;
    private ListView lvSongList;
    private RelativeLayout rlMain;

    private ListView listView;
    private String curSong;
    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> songlist = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //region 元件宣告
        rlMain = (RelativeLayout) findViewById(R.id.rlMain);
        btnRewind = (ImageButton) findViewById(R.id.media_rewind);
        btnRewind.setOnClickListener(buttonListener);
        btnPlay = (ImageButton) findViewById(R.id.media_play);
        btnPlay.setOnClickListener(buttonListener);
        btnPause = (ImageButton) findViewById(R.id.media_pause);
        btnPause.setOnClickListener(buttonListener);
        btnStop = (ImageButton) findViewById(R.id.media_stop);
        btnStop.setOnClickListener(buttonListener);
        btnForward = (ImageButton) findViewById(R.id.media_forward);
        btnForward.setOnClickListener(buttonListener);

        btnShowSongList = (Button) findViewById(R.id.btnShowSongList);
        btnShowSongList.setOnClickListener(buttonListener);

        duration = (TextView) findViewById(R.id.songDuration);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setOnSeekBarChangeListener(seekBarListener);
        songName = (TextView) findViewById(R.id.songName);

        // 設計其他layout中元件的監聽
        // 先用LayoutInflater把popupwindow填充到popview(view)
        // 在popview裡面用findViewById找到popupwindow中的元件
        // 針對元件設計onClickListener
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popview = inflater.inflate(R.layout.popupwindow, null);
        btnSongNo = (Button) popview.findViewById(R.id.btnSongNo);
        btnSongNo.setOnClickListener(buttonListener);
        lvSongList = (ListView) popview.findViewById(R.id.lvSongList);

        //endregion

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File yourDir = new File(sdCardRoot, "/Music/");

        for (File f : yourDir.listFiles()) {
            if (f.isFile())
                songlist.add(f.getName());
        }

        for (int i = 0; i < songlist.size(); i++) {
            Log.d("sixer", Integer.toString(i + 1) + ". " + songlist.get(i));
        }

        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, songlist);
        lvSongList.setAdapter(listAdapter);
        lvSongList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), "你選擇的是" + list[position], Toast.LENGTH_SHORT).show();
                Log.d("sixer", "you choose: " + songlist.get(position));
                setSongInQueue(songlist.get(position));
                popupWindow.dismiss();
                rlMain.setVisibility(View.VISIBLE);
            }
        });

        prepareToolbar();
        prepareDialog();
        prepareListView(btnShowSongList);


        curSong = "ninelie.mp3";
        songName.setText(curSong);
        String sdPath = Environment.getExternalStorageDirectory().toString() + "/Music/";
        File tmpFile = new File(sdPath, curSong);
        Uri uri = Uri.fromFile(tmpFile);

        Log.d("sixer", uri.toString()); // file:///storage/emulated/0/Music/ninelie.mp3
        Log.d("sixer", Environment.getExternalStorageDirectory().getAbsolutePath());    //  /storage/emulated/0

        mp = new MediaPlayer();

        try {
            mp.setDataSource("file:///storage/emulated/0/Music/ninelie.mp3");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 必須在mp.prepare();之後再抓mp.getDuration();才可以抓到正確的歌曲長度
        // 寫在OnPreparedListener裡面是要確保一定是在prepare之後才去抓
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                finalTime = mp.getDuration();
                seekbar.setMax((int) finalTime);
                seekbar.setClickable(true);
//                durationHandler.postDelayed(updateSeekBarTime, 100);
                Log.d("sixer", "finalTime: " + mp.getDuration());
            }
        });


        try {
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        builder.create().show();
        Log.d("sixer", "end of the onCreate()");
    }

    private void setSongInQueue(String songFileName) {
        curSong = songFileName;
        try {
            mp.release();
            mp = new MediaPlayer();
            mp.setDataSource("file:///storage/emulated/0/Music/" + songFileName);
            songName.setText(songFileName);
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    finalTime = mp.getDuration();
                }
            });

            seekbar.setMax((int) finalTime);
            seekbar.setClickable(false);
            durationHandler.postDelayed(updateSeekBarTime, 100);
            try {
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareListView(View v) {

//        popupWindow.showAsDropDown(mShowPop);

    }

    private void prepareToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnClickListener(toolbarListener);
    }

    private void prepareDialog() {
        builder = new AlertDialog.Builder(this);

        builder.setTitle("Hello Dialog")
                .setMessage("Is this material design?")
                .setPositiveButton("Yes", posbtnListener)
                .setNegativeButton("No", negbtnListener);
    }

    private View.OnClickListener toolbarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //builder.create().show();


        }
    };

    private DialogInterface.OnClickListener posbtnListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int id) {
            Log.d("sixer", "Yes pressed.");
        }
    };

    private DialogInterface.OnClickListener negbtnListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int id) {
            Log.d("sixer", "No pressed.");
        }
    };

    private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mp.isPlaying()) {
                Log.d("sixer", "you clicked " + progress);
                mp.seekTo(progress * 1000);
            } else {
                Log.d("sixer", String.format("hasMP: %d, fromUser: %b", (mp == null) ? 0 : 1, fromUser));
            }
        }
    };

    //handler to change seekBarTime
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            //get current position
            timeElapsed = mp.getCurrentPosition();
//            Log.d("sixer", "timeElapsed(updateSeekBarTime): " + String.valueOf(timeElapsed));
            //set seekbar progress
            seekbar.setProgress((int) timeElapsed);
            //set time remaing
            double timeRemaining = finalTime - timeElapsed;
            duration.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining), TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));
//            Log.d("sixer", "timeRemaining(updateSeekBarTime): " + String.valueOf(timeRemaining));
//            Log.d("sixer", "timeRemaining毫秒(updateSeekBarTime): " + TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining));
//            Log.d("sixer", "timeRemaining分(updateSeekBarTime): " + TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining)));


            //repeat yourself that again in 100 miliseconds
            durationHandler.postDelayed(this, 100);
        }
    };

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.media_rewind:
                    if ((timeElapsed - backwardTime) > 0) {
                        timeElapsed = timeElapsed - backwardTime;

                        //seek to the exact second of the track
                        mp.seekTo((int) timeElapsed);
                    }
                    break;
                case R.id.media_pause:
                    if (mp.isPlaying()) {
                        mp.pause();
                        durationHandler.removeCallbacks(updateSeekBarTime);
                    }
                    break;
                case R.id.media_play:
                    mp.start();
                    timeElapsed = mp.getCurrentPosition();
                    Log.d("sixer", "timeElapsed: " + String.valueOf(timeElapsed));

                    songName.setText(curSong);
                    seekbar.setProgress((int) timeElapsed);
                    durationHandler.postDelayed(updateSeekBarTime, 100);
                    break;
                case R.id.media_stop:
                    if (mp.isPlaying()) {
                        mp.stop();
                        //durationHandler.postDelayed(updateSeekBarTime, 100);
                        durationHandler.removeCallbacks(updateSeekBarTime);
                        duration.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) finalTime), TimeUnit.MILLISECONDS.toSeconds((long) finalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime))));
//                        seekbar.setProgress((int) finalTime);
                        seekbar.setProgress((int) 0);

                        try {
                            mp.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d("sixer", "沒放歌你還按stop");
                    }
                    break;
                case R.id.media_forward:
                    if ((timeElapsed + forwardTime) <= finalTime) {
                        timeElapsed = timeElapsed + forwardTime;

                        //seek to the exact second of the track
                        mp.seekTo((int) timeElapsed);
                    }
                    break;
                case R.id.btnShowSongList:
                    popupWindow = new PopupWindow(popview);
                    popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);  //讓彈出視窗佔滿畫面
                    popupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT); //讓彈出視窗佔滿畫面
                    popupWindow.showAtLocation(btnShowSongList, Gravity.CENTER, 0, 0); //讓彈出視窗佔滿畫面
                    rlMain.setVisibility(View.INVISIBLE);
                    break;
                case R.id.btnSongNo:
                    popupWindow.dismiss();
                    rlMain.setVisibility(View.VISIBLE);
                    Log.d("sixer", "you clicked cancel.");
                    break;
            }
        }
    };


}
