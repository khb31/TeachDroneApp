package com.example.teachdrone;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.teachdrone.ui.main.SectionsPagerAdapter;

import java.util.Timer;
import java.util.TimerTask;

public class ControlActivity extends AppCompatActivity {
        Button button;
        SSHService sshService;
        Timer timer=new Timer();
        char[]channel=new char[16];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("creation","created");
        setContentView(R.layout.activity_control);
        button=(Button)(findViewById(R.id.button));
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        Intent intent=new Intent(this,SSHService.class);
        bindService(intent,serviceConnection,0);
        timer.schedule(new TimerTask() { //runs every second, will update the channels in the service
            @Override
            public void run() {
                sshService.startActionWrite(getApplicationContext()," "," ");
            }
        },0,1000);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {//why UI thread?
                    @Override
                    public void run() {
                        channel=sshService.getChannel();//needs a block at some point - I don't think it's automatic
                        //do something with channels - maybe write them into the UI in text boxes.
                    }
                });
            }
        },500,1000);
    }
    ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SSHService.sBinder binder = (SSHService.sBinder)service;
            sshService=binder.getInstance();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    void tabSelect(int currentTab){
        //the tab view is already set, this
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        unbindService(serviceConnection);
    }

}