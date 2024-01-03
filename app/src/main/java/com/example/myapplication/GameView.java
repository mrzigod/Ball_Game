package com.example.myapplication;

import static android.os.Looper.getMainLooper;
import static java.lang.Math.abs;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowMetrics;


import androidx.annotation.MainThread;
import androidx.annotation.UiThread;

import java.util.ArrayList;

public class GameView extends View {
    Bitmap background;
    Bitmap ball;
    Bitmap otherball;
    Rect rectBackground;
    Point size;
    float ballX;
    float ballY;

    float otherballX;
    float otherballY;
    int speedX;
    int speedY;
    ArrayList<Wall> walls;
    Goal goal;
    Handler executor;
    Runnable runnable;

    Context context;
    boolean reachedGoal;
    int level;
    int framecount;

    boolean multi;

    String name;

    public GameView(Context context,int level,String name,boolean multi,boolean host){
        super(context);
        this.context=context;
        this.level=level;
        this.name=name;
        this.multi=multi;
        reachedGoal=false;
        if(ThemeHolder.getData()) {
            background = BitmapFactory.decodeResource(getResources(), R.drawable.bg, null);
            ball = BitmapFactory.decodeResource(getResources(), R.drawable.ball, null);
            otherball=BitmapFactory.decodeResource(getResources(), R.drawable.newball, null);
        }
        else {
            background = BitmapFactory.decodeResource(getResources(), R.drawable.newbg, null);
            ball = BitmapFactory.decodeResource(getResources(), R.drawable.newball, null);
            otherball=BitmapFactory.decodeResource(getResources(), R.drawable.ball, null);
        }
        Display display=((Activity) getContext()).getWindowManager().getDefaultDisplay();
        size=new Point();
        display.getSize(size);
        rectBackground=new Rect(0,0,size.x,size.y);
        ball=Bitmap.createScaledBitmap(ball, size.x/6, size.y/12, false);
        if(multi) {
            otherball = Bitmap.createScaledBitmap(otherball, size.x / 6, size.y / 12, false);
            otherball=makeTransparentBitmap(otherball,122);
        }
        walls=new ArrayList<>();
        speedX=0;
        speedY=0;
        loadLevel(level,context,multi,host);
        framecount=0;


        executor=new Handler(getMainLooper());
        runnable= new Runnable() {
            @Override
            public void run() {
                invalidate();
                if(framecount<20000)
                    framecount++;
            }
        };
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        moveBall();
        canvas.drawBitmap(background,null,rectBackground,null);
        canvas.drawBitmap(ball,ballX,ballY,null);
        if(multi)
            canvas.drawBitmap(otherball,otherballX,otherballY,null);
        for(int i=0;i<walls.size();i++)
        {
            canvas.drawBitmap(walls.get(i).wall,walls.get(i).wallX,walls.get(i).wallY,null);
        }
        canvas.drawBitmap(goal.goal,goal.goalX,goal.goalY,null);
        if(framecount>5&&multi){
            String sstring="3\n"+ballX+"@"+ballY;
            write(sstring.getBytes());
        }
        executor.postDelayed(runnable,30);
    }

    void loadLevel(int level,Context context,boolean multi,boolean host)
    {
        LevelLoader levelLoader=new LevelLoader(this,context,this.size,this.ball);
        if(level==8) {
            if(host){
                levelLoader.generateRandomLevel(multi);
                if(multi)
                    sendLevel();
            }
            else{
                goal=new Goal(context, 5, 10, size.x / 6, size.y / 12);
            }
        }
        else
            levelLoader.loadLevel(level,multi);
    }

    void onAngleEvent(float[] ov)
    {
        if(ov[1]>0.3)
        {
            speedY-=3;
            if(speedY<-30)
                speedY=-30;
        }
        if(ov[1]<0.0)
        {
            speedY+=3;
            if(speedY>30)
                speedY=30;
        }
        if(ov[1]>0.0&&ov[1]<0.3)
        {
            if(speedY>0)
                speedY--;
            if(speedY<0)
                speedY++;
        }
        if(ov[2]>0.3)
        {
            speedX+=2;
            if(speedX<-20)
                speedX=-20;
        }
        if(ov[2]<0.0)
        {
            speedX-=2;
            if(speedX>20)
                speedX=20;
        }
        if(ov[2]>0.0&&ov[2]<0.3)
        {
            if(speedX>0)
                speedX--;
            if(speedX<0)
                speedX++;
        }
    }

    void moveBall()
    {
        ballX+=speedX;
        ballY+=speedY;
        if(ballX>size.x-ball.getWidth())
            ballX=size.x-ball.getWidth();
        if(ballX<0)
            ballX=0;
        if(ballY>size.y-2*ball.getHeight())
            ballY=size.y-2*ball.getHeight();
        if(ballY<0)
            ballY=0;
        for(int i=0;i<walls.size();i++)
        {
            if(doesBallCollideWithWall(walls.get(i)))
            {
                if(walls.get(i).vertical)
                {
                    speedX=0;
                    if(ballX+ball.getWidth()/(float)2>walls.get(i).wallX) {
                        ballX = walls.get(i).wallX;;
                    }
                    else {
                        ballX = walls.get(i).wallX - ball.getWidth();
                    }

                }
                else
                {
                    speedY=0;
                    if(ballY+ball.getHeight()/(float)2>walls.get(i).wallY)
                        ballY=walls.get(i).wallY+5;
                    else
                        ballY=walls.get(i).wallY-ball.getHeight();

                }

            }
        }
        if(!reachedGoal&&doesBallCollideWithGoal())
        {
            Intent intent=new Intent(context,EndScreen.class);
            intent.putExtra("framecount",framecount);
            intent.putExtra("level",level);
            intent.putExtra("name",name);
            context.startActivity(intent);
            ((Activity) context).finish();
        }
    }

    boolean doesBallCollideWithWall(Wall wall)
    {
        if(wall.vertical){
            if(ballX>wall.wallX-ball.getWidth()+1&&ballX<wall.wallX-1)
            {
                if(ballY> wall.wallY-10&&ballY< wall.wallY+wall.wall.getHeight()+10)
                    return true;
                else
                {
                    float xval=abs(ballX+ball.getWidth()/(float)2- wall.wallX);
                    float yval=abs(ballY+ball.getHeight()/(float)2- wall.wallY);
                    if(xval*xval+yval*yval<(ball.getWidth()/(float)2)*(ball.getWidth()/(float)2))
                        return true;
                    yval=abs(ballY+ball.getWidth()/(float)2- (wall.wallY+wall.wall.getHeight()));
                    if(xval*xval+yval*yval<(ball.getWidth()/(float)2)*(ball.getWidth()/(float)2))
                        return true;
                }
            }
            return false;
        }
        if(ballY>wall.wallY-ball.getHeight()+1&&ballY<wall.wallY-1)
        {
            if(ballX> wall.wallX-1&&ballX<wall.wallX+wall.wall.getWidth()+1) {
                return true;
            }
            else
            {
                float xval=abs(ballX+ball.getWidth()/(float)2- wall.wallX);
                float yval=abs(ballY+ball.getHeight()/(float)2- wall.wallY);
                if(xval*xval+yval*yval<(ball.getWidth()/(float)2)*(ball.getWidth()/(float)2))
                    return true;
                xval=abs(ballX+ball.getWidth()/(float)2- (wall.wallX+wall.wall.getWidth()));
                if(xval*xval+yval*yval<(ball.getWidth()/(float)2)*(ball.getWidth()/(float)2))
                    return true;
            }
        }
        return false;
    }
    boolean doesBallCollideWithGoal()
    {
        float xval=abs(ballX+ball.getWidth()/(float)2- (goal.goalX+goal.goal.getWidth()/(float)2));
        float yval=abs(ballY+ball.getHeight()/(float)2- (goal.goalY+goal.goal.getHeight()/(float)2));
        float radiussum=ball.getWidth()/(float)2+goal.goal.getWidth()/(float)2;
        if(xval*xval+yval*yval+10<radiussum*radiussum)
        {
            reachedGoal=true;
            return true;
        }
        return false;
    }

    private static Bitmap makeTransparentBitmap(Bitmap bmp, int alpha) {
        Bitmap transBmp = Bitmap.createBitmap(bmp.getWidth(),
                bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(transBmp);
        final Paint paint = new Paint();
        paint.setAlpha(alpha);
        canvas.drawBitmap(bmp, 0, 0, paint);
        return transBmp;
    }

    public void putOtherBall(String s)
    {
        String[] a=s.split("@");
        System.out.println("moving the other ball to "+a[0]+" "+a[1]);
        otherballX=Float.parseFloat(a[0]);
        otherballY=Float.parseFloat(a[1]);
    }

    void write(byte[] bytes){
        BCSHolder.getData().write(bytes);
    }

    void sendLevel(){
        StringBuilder s= new StringBuilder("4\n" + walls.size() + "@");
        for(int i=0;i<walls.size();i++){
            s.append(walls.get(i).wallX/(size.x / 6)).append(" ").append(walls.get(i).wallY/(size.y / 12)).append(" ").append(walls.get(i).vertical).append("@");
        }
        s.append(ballX/(size.x / 6.f)).append("@").append(ballY/(size.y / 12.f)).append("@");
        s.append(goal.goalX/(size.x / 6.f)).append(" ").append(goal.goalY/(size.y / 12.f));
        String q=new String(s);
        System.out.println(q);
        BCSHolder.getData().slowWrite(q.getBytes());
    }


    void loadSentLevel(String s){
        System.out.println(s);
        String[] a=s.split("@");
        int wallcount=Integer.parseInt(a[0]);
        for(int i=0;i<wallcount;i++){
            String [] split=a[i+1].split(" ");
            walls.add(new Wall(context, Integer.parseInt(split[0]), Integer.parseInt(split[1]), Boolean.parseBoolean(split[2]), size.x / 6, size.y / 12));
        }
        ballX=Float.parseFloat(a[wallcount+1])*size.x/6;
        ballY=Float.parseFloat(a[wallcount+2])*size.y/12;
        otherballX=ballX;
        otherballY=ballY;
        String[] split=a[wallcount+3].split(" ");
        goal = new Goal(context, (int) Float.parseFloat(split[0]), (int) Float.parseFloat((split[1]))+1, size.x / 6, size.y / 12);
        framecount=0;
    }
}
