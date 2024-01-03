package com.example.myapplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

public class LevelLoader {

    GameView gameView;
    Context context;
    Point size;
    Bitmap ball;
    LevelLoader(GameView gameView, Context context, Point size, Bitmap ball)
    {
        this.gameView=gameView;
        this.context=context;
        this.size=size;
        this.ball=ball;
    }

    void loadLevel(int level,boolean multi) {
        try {
            BufferedReader reader=getBufferedReader(level);
            int wallcount = Integer.parseInt(reader.readLine());
            for(int i=0;i<wallcount;i++) {
                String[] split=reader.readLine().split(" ");
                gameView.walls.add(new Wall(context, Integer.parseInt(split[0]), Integer.parseInt(split[1]), Boolean.parseBoolean(split[2]), size.x / 6, size.y / 12));
            }

            gameView.ballX = Integer.parseInt(reader.readLine())*size.x/(float)6+10;
            gameView.ballY = Integer.parseInt(reader.readLine())*size.y /(float)12+10;
            if(multi){
                gameView.otherballX= gameView.ballX;
                gameView.otherballY= gameView.ballY;
            }
            String[] split=reader.readLine().split(" ");
            gameView.goal = new Goal(context, Integer.parseInt(split[0]), Integer.parseInt(split[1]), size.x / 6, size.y / 12);
        } catch (Exception exception){
            System.out.println(exception.getStackTrace());
        }
    }

    BufferedReader getBufferedReader(int level)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.level1)));
        switch (level) {
            case 2 ->
                    reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.level2)));
            case 3 ->
                    reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.level3)));
            case 4 ->
                    reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.level4)));
            case 5 ->
                    reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.level5)));
            case 6 ->
                    reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.level6)));
            case 7 ->
                    reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.level7)));
        }
        return reader;
    }


    void generateRandomLevel(boolean multi)
    {
        ArrayList<ArrayList<Integer>> graph=new ArrayList<>();
        ArrayList<Triple> triples = new ArrayList<>();
        int startx = 0,starty = 0,goalx=5,goaly=10;
        boolean valid=false;
        while(!valid) {
            graph = createGraph();
            triples = generateWalls();
            Random rand=new Random();
            startx=rand.nextInt(6);
            starty= rand.nextInt(11);
            goalx=rand.nextInt(6);
            goaly= rand.nextInt(11);
            valid=validate(graph,triples,startx,starty,goalx,goaly);
        }
        for(int i=0;i< triples.size();i++)
        {
            gameView.walls.add(new Wall(context, triples.get(i).x, triples.get(i).y, triples.get(i).vertical, size.x / 6, size.y / 12));
        }
        gameView.ballX = startx*size.x/6.f;
        gameView.ballY = starty*size.y/12.f;
        if(multi)
        {
            gameView.otherballX=startx;
            gameView.otherballY=starty;
        }
        gameView.goal = new Goal(context, goalx, goaly, size.x / 6, size.y / 12);

    }



    ArrayList <ArrayList<Integer> > createGraph()
    {
        ArrayList <ArrayList<Integer> > graph=new ArrayList<>();
        for(int i=0;i<72;i++)
        {
            ArrayList<Integer> v=new ArrayList<>();
            graph.add(v);
        }
        for(int i=0;i<66;i++)
        {
            if(i%6!=5)
                graph.get(i).add(i+1);
        }
        for(int i=0;i<66;i++)
        {
            if(i%6!=0)
                graph.get(i).add(i-1);
        }
        for(int i=0;i<60;i++)
        {
            graph.get(i).add(i+6);
        }
        for(int i=6;i<66;i++)
        {
            graph.get(i).add(i-6);
        }
        return graph;
    }

    ArrayList<Triple> generateWalls(){
        ArrayList <Triple> triples=new ArrayList<>();
        Random rand=new Random();
        int wallCount=rand.nextInt(30);
        wallCount+=15;
        for(int i=0;i<wallCount;i++)
        {
            boolean v=rand.nextBoolean();
            int x,y;
            if(v)
            {
                x=rand.nextInt(5)+1;
                y=rand.nextInt(11);
            }
            else
            {
                x=rand.nextInt(6);
                y=rand.nextInt(10)+1;
            }
            triples.add(new Triple(x,y,v));
        }
        return triples;
    }

    ArrayList<Triple> testWalls(){
        ArrayList <Triple> triples=new ArrayList<>();
        triples.add(new Triple(0,1,false));
        triples.add(new Triple(1,0,true));
        return triples;
    }
    private boolean validate(ArrayList<ArrayList<Integer>> graph, ArrayList<Triple> triples,int startx,int starty, int goalx, int goaly) {
        ArrayList <Integer> visited= new ArrayList<>();
        for(int i=0;i<66;i++)
        {
            visited.add(0);
        }
        for(int i=0;i<triples.size();i++)
        {
            Triple t=triples.get(i);
            int index=t.x+t.y*6;
            int index2=index-1;
            if(!t.vertical)
            {
                index2=index-6;
            }
            graph.get(index).remove(Integer.valueOf(index2));
            graph.get(index2).remove(Integer.valueOf(index));
        }
        ArrayList<Integer> l= new ArrayList<>();
        l.add(startx+6*starty);
        visited.set(startx+6*starty,1);
        while(l.size()!=0)
        {
            Integer i= l.get(0);
            System.out.println(i);
            l.remove(0);
            for(int j=0;j<graph.get(i).size();j++)
            {
                Integer k=graph.get(i).get(j);
                if(visited.get(k)==0)
                {
                    l.add(k);
                    visited.set(k,1);
                }
            }
        }
        if(visited.get(goalx+goaly*6)==1)
            return true;
        return false;
    }

    class Triple{
        int x;
        int y;
        boolean vertical;
        public Triple(int x, int y, boolean vertical)
        {
            this.x=x;
            this.y=y;
            this.vertical=vertical;
        }

    }


}
