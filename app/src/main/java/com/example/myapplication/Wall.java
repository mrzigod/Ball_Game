package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Wall {
    Bitmap wall;
    int wallX,wallY;
    boolean vertical;

    public Wall(Context context, int wallX, int wallY, boolean vertical,int xScale, int yScale){
        if(ThemeHolder.getData())
            wall= BitmapFactory.decodeResource(context.getResources(),R.drawable.newwall);
        else
            wall= BitmapFactory.decodeResource(context.getResources(),R.drawable.wall);
        this.vertical=vertical;
        if(vertical)
            wall=Bitmap.createScaledBitmap(wall, 10, yScale, false);
        else
            wall=Bitmap.createScaledBitmap(wall, xScale, 10, false);
        this.wallX=wallX*xScale;
        if(wallX==5&&vertical)
            this.wallX-=11;
        if(wallX==4&&!vertical)
            this.wallX-=11;
        if(wallX==1&&vertical)
            this.wallX+=11;

        this.wallY=wallY*yScale;
    }
}

