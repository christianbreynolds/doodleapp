package com.example.chris.doodleapp;
//Author: Chris Reynolds
//christian.b.reynolds@vanderbilt.edu
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Chris on 4/14/2015.
 */
public class Drawspace extends View{
    //some options specific to Drawspace
    public static enum PenColor{BLACK, WHITE, RED, BLUE};
    public final static int PEN_SIZE = 10;
    public final static int PAINT_COLORS = 4;
    private final int[] colors = {Color.BLACK, Color.WHITE, Color.RED, Color.BLUE};
    private final static long DRAW_RATE = 10; //how often the View Refreshes
    private Paint[] paint;
    private Thread drawThread;
    private PenColor curColor;
    private ArrayList<DrawnPoint> dotList;
    


    public Drawspace(Context context) {
        super(context);
        paint = new Paint[PAINT_COLORS];
        initializePaints();
        curColor = PenColor.BLACK;
        dotList = new ArrayList<DrawnPoint>();
        drawThread = null;
    }

    public void initializePaints(){
        for(int i = 0; i<PAINT_COLORS; i++){
            paint[i] = new Paint();
            paint[i].setColor(colors[i]);
        }
    }

    public void setPenColor(PenColor color){
        curColor = color;
    }


    public void drawDot(DrawnPoint d){
        synchronized(dotList){
            dotList.add(d);
        }
    }

    public void startDrawing(){
        drawThread = new Thread(new Runnable(){
            public void run(){
                while(true){
                    Drawspace.this.postInvalidate(); //causes it to redraw
                    try{
                        drawThread.sleep(DRAW_RATE);
                    }catch(InterruptedException e){
                        break; //stops drawing
                    }
                }
            }
        });
        drawThread.start();
    }

    public void stopDrawing(){
        if(drawThread != null){
            drawThread.interrupt();
            drawThread = null;
        }
    }

    //called whenever the View is refreshed, i.e. after postInvalidate
    protected void onDraw(Canvas canvas){
        int height = canvas.getHeight();
        int width = canvas.getWidth();

        synchronized(dotList){
            for(DrawnPoint p:dotList){
                if(p.mX<width && p.mY<height){
                    canvas.drawCircle(p.mX, p.mY, PEN_SIZE, paint[p.mColor]);
                }
            }
        }
    }

    public PenColor getColor(){
        return curColor;
    }

    private Paint getPaint(PenColor pc){
        return paint[pc.ordinal()];
    }
        
}
