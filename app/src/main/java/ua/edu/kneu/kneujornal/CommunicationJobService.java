package ua.edu.kneu.kneujornal;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Random;

public class CommunicationJobService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private final Random mGenerator = new Random();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.i("KNEU","TOPCHIK "+ mGenerator.nextInt(100));
        genRandom();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent){
        //Log.i("KNEU","TOPCHIK "+ mGenerator.nextInt(100));
       // genRandom();
        return mBinder;
    }

    public class LocalBinder extends Binder{
        CommunicationJobService getService(){
            return CommunicationJobService.this;

        }
    }

    public void genRandom(){
        while (true) {
            Log.i("KNEU","TOPCHIK"+ mGenerator.nextInt(100));
            try {
                Thread.sleep(1000);
            } catch (java.lang.InterruptedException e){

            }

        }
    }
}
