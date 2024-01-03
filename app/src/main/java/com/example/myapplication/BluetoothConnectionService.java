package com.example.myapplication;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

public class BluetoothConnectionService {
    String TAG="connection service";
    String name="Ball Race";
    UUID MY_UUID=UUID.fromString("9c848f56-1986-46bb-98b2-13e94e21f62c");
    private final BluetoothAdapter bluetoothAdapter;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private BluetoothDevice bluetoothDevice;
    private UUID deviceUUID;

    private ConnectedThread connectedThread;

    private Handler handler;

    private Handler gameHandler;

    public BluetoothConnectionService(Context context,Handler handler){
        BluetoothManager bluetoothManager= (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter=bluetoothManager.getAdapter();
        this.handler=handler;
        start();
    }

    private class AcceptThread extends Thread{
        private final BluetoothServerSocket serverSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(name, MY_UUID);
                Log.d(TAG,"setting up server");
            } catch (IOException | SecurityException e){
                e.printStackTrace();
            }
            serverSocket=tmp;
        }

        public void run(){
            BluetoothSocket socket=null;
            try {
                socket =serverSocket.accept();
                Log.d(TAG,"socket accepted connection");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(socket!=null){
                connected(socket,bluetoothDevice);
            }
        }

        public void cancel(){
            try{
                serverSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    private class ConnectThread extends Thread{
        private BluetoothSocket socket;
        public ConnectThread(BluetoothDevice device, UUID uuid){
            Log.d(TAG, "setting up connect thread");
            bluetoothDevice=device;
            deviceUUID=uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            try {
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (SecurityException|IOException e){
                e.printStackTrace();
            }

            socket=tmp;
            try {
                Log.d(TAG,"connected to socket");
                socket.connect();
            } catch (SecurityException|IOException e){
                e.printStackTrace();
                try{
                    socket.close();
                    e.printStackTrace();
                } catch (SecurityException|IOException e1){
                    e1.printStackTrace();
                }
                Log.d(TAG,"couldn't connect");
            }

            connected(socket,bluetoothDevice);
        }

        public void cancel(){
            try{
                socket.close();
                Log.d(TAG,"closing client socket");
            } catch (SecurityException|IOException e1){
                e1.printStackTrace();
            }
        }

    }

    public synchronized void start(){
        Log.d(TAG,"starting the service");
        if(connectThread!=null){
            connectThread.cancel();
            connectThread=null;
        }
        if(acceptThread==null){
            acceptThread=new AcceptThread();
            acceptThread.start();
        }

    }

    public void startClient(BluetoothDevice device, UUID uuid)
    {
        connectThread=new ConnectThread(device, uuid);
        connectThread.start();

    }

    private class ConnectedThread extends Thread{
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket){
            this.socket=socket;
            InputStream tmpin=null;
            OutputStream tmpout=null;
            try {
                tmpin = socket.getInputStream();
                tmpout = socket.getOutputStream();
            } catch (IOException e){
                e.printStackTrace();
            }
            inputStream=tmpin;
            outputStream=tmpout;
        }

        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;

            while(true){
                try {
                    bytes=inputStream.read(buffer);
                    String incomingMessage= new String(buffer,0,bytes);
                    String[] a=incomingMessage.split("\n");
                    if(Integer.parseInt(a[0])==1) {
                        Log.d(TAG, "inputstream: " + a[1]);
                        sendStringToActivity(a[1]);
                    }
                    if(Integer.parseInt(a[0])==2){
                        sendLevelToActivity(Integer.parseInt(a[1]));
                    }
                    if(Integer.parseInt(a[0])==3){
                        sentPositionToActivity(a[1]);
                    }
                    if(Integer.parseInt(a[0])==4){
                        sentLayoutToActivity(a[1]);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(byte[] bytes){
            String text= new String(bytes);
            Log.d(TAG,"outputstream: "+text);
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel(){
            try{
                socket.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }


    }

    private void connected(BluetoothSocket bsocket, BluetoothDevice bDevice){
        Log.d(TAG,"connected: starting");
        connectedThread=new ConnectedThread(bsocket);
        connectedThread.start();
    }

    public void write(byte[] out){
        connectedThread.write(out);
    }

    public void slowWrite(byte[] out){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connectedThread.write(out);
    }

    public boolean sendStringToActivity(String s) {
        return handler.sendMessage(Message.obtain(handler,1 , s));
    }

    public boolean sendLevelToActivity(int level){
        return handler.sendMessage(Message.obtain(handler,2,level));
    }

    public boolean sentPositionToActivity(String s) {
        if(gameHandler!=null)
            return gameHandler.sendMessage(Message.obtain(gameHandler,3 , s));
        return false;
    }

    public boolean sentLayoutToActivity(String s) {
        return gameHandler.sendMessage(Message.obtain(gameHandler,4 , s));
    }
    public boolean isConnected(){
        return connectedThread != null;
    }

    public void setGameHandler(Handler gameHandler) {
        this.gameHandler = gameHandler;
    }
}
