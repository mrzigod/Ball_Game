package com.example.myapplication;

import android.content.Context;
import android.os.Handler;

import java.util.Collection;

public class BCSHolder {
    private static BluetoothConnectionService data;

    private static boolean started=false;

    public static BluetoothConnectionService getData(){return data;}

    public static boolean isStarted(){
        return started;
    }

    public static void startBCS(Context context, Handler handler){
        data=new BluetoothConnectionService(context,handler);
        started=true;
    }

    public static void setGameHandler(Handler gameHandler){
        data.setGameHandler(gameHandler);
    }


}
