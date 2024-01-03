package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Goal {
    Bitmap goal;
    int goalX,goalY;
    public Goal(Context context, int goalX, int goalY, int xScale, int yScale){
        if(ThemeHolder.getData())
            goal= BitmapFactory.decodeResource(context.getResources(),R.drawable.goal);
        else
            goal= BitmapFactory.decodeResource(context.getResources(),R.drawable.newgoal);
        goal=Bitmap.createScaledBitmap(goal, xScale, yScale, false);
        this.goalX=goalX*xScale;
        this.goalY=goalY*yScale;
    }
}
