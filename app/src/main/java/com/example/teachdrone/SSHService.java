package com.example.teachdrone;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SSHService extends IntentService {
    Socket socket;
    PrintWriter output;
    BufferedReader input;
    private char[]channel=new char[16];
    IBinder binder = new sBinder();
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_READ = "READ";
    private static final String ACTION_WRITE = "WRITE";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "PARAM1"; //command?
    private static final String EXTRA_PARAM2 = "PARAM2"; //what?

    public SSHService() {
        super("SSHService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    // TODO: Customize helper method
    public static void startActionRead(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SSHService.class);
        intent.setAction(ACTION_READ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionWrite(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SSHService.class);
        intent.setAction(ACTION_WRITE);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        Log.i("starting internal","begin");
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("inside","handling intent");
        if (intent != null) {
            try {
                Log.i("try","inside try");
                //socket.setSoTimeout(1000);
                socket=new Socket("192.168.86.33",8080);//change
                Log.i("socket","passed socket");
                output=new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.ISO_8859_1));
                Log.i("output","passed output");
                input=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Log.i("input","passed input");
                final String action = intent.getAction();
                if (ACTION_READ.equals(action)) {//read
                    final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                    final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                    handleActionRead(param1, param2);
                } else if (ACTION_WRITE.equals(action)) {//write
                    Log.i("write","action: write");
                    final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                    final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                    Log.i("internal 2","entering action");
                    handleActionWrite(param1, new short[]{1000,1000,1000,1000});//dummy data
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Log.i("null","null intent");
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRead(String param1, String param2) {//read
        // TODO: Handle reads for sensors
        char message[]=new char[]{'$','M','<',0,105,0};
        char[]retMessage = new char[22];
        char checksum='$';
        for(char i=1; i<5;i++){
            checksum^=message[i];
        }
        message[5]=checksum;
        try {
            output.write(message);
            input.read(retMessage);
            for(char i=5;i<21;i++){
                channel[i-5]=retMessage[i];
            }
            //do something with message output by drone, i.e. sensor data
        }catch (Exception ignore){
        }
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionWrite(String param1, short[]param2) {//write
        //param 1 = command, param 2 = quantifier.
        //potentially split the commands into separate types
        // TODO: Handle get method to retrieve channel/sensor info. can bind multiple activities
        Log.i("action","begin");
        char[]message=new char[22];
        String converted=" ";
        switch(param1){//bad - change. NaN atm
            case "up":{
                //translate to drone cmd
                converted="Up";
            } case "down":{
                converted="down";
            } case "left":{
                converted="left";
            } case "right":{
                converted="right";
            } case "forward":{
                converted="forward";
            } case "backward":{
                converted="backward";
            }
        }
        channel[0]=(char)((param2[0]&0xFF00)>>8);
        channel[2]=(char)((param2[1]&0xFF00)>>8);
        channel[4]=(char)((param2[2]&0xFF00)>>8);
        channel[6]=(char)((param2[3]&0xFF00)>>8);
        channel[1]=(char)(param2[0]&0xFF);
        channel[3]=(char)(param2[1]&0xFF);
        channel[5]=(char)(param2[2]&0xFF);
        channel[7]=(char)(param2[3]&0xFF);
        message[0]='$';
        message[1]='M';
        message[2]='<';
        message[3]=16;
        message[4]=200;
        message[5]=channel[0];
        message[6]=channel[1];
        message[7]=channel[2];
        message[8]=channel[3];
        message[9]=channel[4];
        message[10]=channel[5];
        message[11]=channel[6];
        message[12]=channel[7];
        for(char a=13;a<21;a++){
            message[a]=channel[a-5];
        }
        char checksum=message[0];
        for(char i=1;i<21;i++){
            checksum^=message[i];
        }
        message[21]=checksum;
        Log.e("channel0",String.format(Locale.ENGLISH,"%d", (int) (channel[0])));
        Log.e("channel1",String.format(Locale.ENGLISH,"%d", (int) (channel[1])));
        Log.e("channel1",String.format(Locale.ENGLISH,"%d", (int) ((int)message[4]+(int)((int)message[3]<<8))));
        Log.e("param0",String.format(Locale.ENGLISH,"%d",param2[0]));
        //set the message into correct format for protocol
        //maybe handle the message and convert, e.g. forward 10 into the correct format
        Log.i("message",new String(message));
        //Log.e("number char",((char)255&0xFF)==0xFF?"f":"t");
        output.write(message);
        output.flush();
        //throw new UnsupportedOperationException("Not yet implemented");
    }
    public class sBinder extends Binder {
        public SSHService getInstance(){
            return SSHService.this;
        }
    }
    public char[]getChannel(){
        return channel;
    }
}