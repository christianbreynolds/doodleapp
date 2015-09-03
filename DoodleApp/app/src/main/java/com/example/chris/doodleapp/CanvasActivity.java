package com.example.chris.doodleapp;
//Author: Chris Reynolds
//christian.b.reynolds@vanderbilt.edu
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;


public class CanvasActivity extends Activity {
    private final static String TAG = CanvasActivity.class.getName();
    private LinearLayout layout;
    private Drawspace drawspace;
    private Button[] buttons;
    private ArrayList<DrawnPoint> pointBuffer;
    private boolean connected = false, cycle = false;
    private String GROUP_NAME = "", USER_NAME = "";
    Socket s;
    PrintWriter pw;
    BufferedReader br;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas);
        Bundle b = getIntent().getExtras();
        USER_NAME = b.getString("username");
        GROUP_NAME = b.getString("groupname"); //only really important for an AsyncTask later
        Log.d(TAG, USER_NAME + " in " + GROUP_NAME);

        layout = (LinearLayout) findViewById(R.id.canvasLayout);
        drawspace = new Drawspace(this);
        pointBuffer = new ArrayList<DrawnPoint>();
        drawspace.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(!cycle) {
                    cycleSend();  //begins the automatic send timer
                }
                cycle = true;
                int x =0, y=0;
                int hist = event.getHistorySize();
                int pc = event.getPointerCount();
                synchronized(pointBuffer) {
                    for (int i = 0; i < hist; i++) { //gets all points touched and draws them
                        for (int p = 0; p < pc; p++) {  //this block is for capturing motion
                            x = (int) event.getHistoricalX(p, i);
                            y = (int) event.getHistoricalY(p, i);
                            DrawnPoint d = new DrawnPoint(x,y,drawspace.getColor().ordinal());
                            drawspace.drawDot(d);
                            pointBuffer.add(d);
                        }
                    }
                    for (int i = 0; i < pc; i++) {  //this block captures taps, not dragging
                        x = (int) event.getX(i);
                        y = (int) event.getY(i);
                        DrawnPoint d = new DrawnPoint(x,y,drawspace.getColor().ordinal());
                        drawspace.drawDot(d);
                        pointBuffer.add(d);
                    }
                    if(pointBuffer.size()>Constants.MAX_BUFFER){
                        sendPoints();
                    }
                } //end synchronized(pointBuffer)
                return true;
            }
        });

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0 ,1);
        layout.addView(drawspace, 0, params); //draws the canvas


        buttons = new Button[Drawspace.PAINT_COLORS];
        buttons[0] = (Button) findViewById(R.id.BlackButton);
        buttons[1] = (Button) findViewById(R.id.WhiteButton);
        buttons[2] = (Button) findViewById(R.id.RedButton);
        buttons[3] = (Button) findViewById(R.id.BlueButton);
        buttons[0].setOnClickListener(new ColorClickListener(Drawspace.PenColor.BLACK));
        buttons[1].setOnClickListener(new ColorClickListener(Drawspace.PenColor.WHITE));
        buttons[2].setOnClickListener(new ColorClickListener(Drawspace.PenColor.RED));
        buttons[3].setOnClickListener(new ColorClickListener(Drawspace.PenColor.BLUE));
        //connecting to server
        connect();
    }

    //allows color change
    private class ColorClickListener implements View.OnClickListener{
        private Drawspace.PenColor pc;

        public ColorClickListener(Drawspace.PenColor penColor){
            pc = penColor;
        }
        @Override
        public void onClick(View v) {
            drawspace.setPenColor(pc);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        drawspace.startDrawing();
    }

    @Override
    public void onPause(){
        super.onPause();
        drawspace.stopDrawing();
        cycle = false; //stops the automatic timer, just in case
    }

    //since we call this inside the synchronized(pointBuffer) block, we shouldn't
    //need any more locking to prevent race conditions on pointBuffer
    private void sendPoints(){
        String msg = "@Point";
        DrawnPoint firstPoint = pointBuffer.remove(0);  //this allows the splitting by &
        msg += firstPoint.toString();
        for(DrawnPoint p:pointBuffer){
            msg+= "&" + p.toString();
        }
        pointBuffer.clear();
        send(msg);
    }

    //the famed automatic send timer
    //done in an AsyncTask so as not to waste UI thread time
    private void cycleSend(){
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                int count = 0;
                while(cycle){
                    synchronized(pointBuffer) {
                        if (pointBuffer.size() != 0) {
                            sendPoints();
                            count = 0;
                        }
                    }
                    if(count == 5) { //if no new points are added by now, it's time to stop
                        cycle = false;
                    }
                    count++;
                    try {
                        Thread.sleep(Constants.SEND_RATE);
                    }catch(InterruptedException e){
                        cycle = false;
                        return null;
                    }
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    //////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    /// NETWORKING STUFF
    ////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////

    //connects to the internet, stores the connection media
    public boolean connect(){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void ... args) {
                try {
                    s = new Socket(Constants.SERVER_SITE, Constants.PORT_NUMBER);
                    pw = new PrintWriter(s.getOutputStream(), true);
                    br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    return e.getMessage();
                }
                connected = true;
                return null;
            }
            @Override
            protected void onPostExecute(String msg){
                if(msg != null){
                    showToast(msg);
                }
                else{
                    //showToast("Connected to server");
                    listen(); //calling them here makes sure they aren't called too early
                    send("@JoinGroup" + GROUP_NAME);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return false;
    }

    //sends strings to the server to be dealt with
    public boolean send(String msg){

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String ... args) {
                if(connected)
                    pw.println(args[0]);
                return args[0];
            }

            @Override
            protected void onPostExecute(String str){
                //empty
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, msg);
        return true;
    }

    //waits for and interprets messages from the server
    public void listen(){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... args) {
                try {
                    String input;
                    while((input = br.readLine()) !=null){
                        processProtocol(input);
                    }
                    br.close();
                }catch(IOException e){
                    connected = false;
                    Log.e(TAG, e.getMessage());
                    return e.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String msg){
                if(msg != null){
                    showToast(msg);
                }
            }

            //where all of the magic happens from the server
            private void processProtocol(String input){
                //someone has joined the group
                if(input.startsWith("@Name")){
                    final String name = input.substring(5);
                    runOnUiThread(new Runnable(){
                       @Override
                       public void run(){
                           showToast(name + " is now Doodling!");
                       }
                    });
                }
                //someone sent points to draw
                else if(input.startsWith("@Point")){
                    input = input.substring(6);
                    String [] pointList = input.split("&");
                    //Log.d(TAG, "Received a point list: " + pointList[0]);
                    for(String point:pointList){
                        if(!"".equals(point))
                            drawspace.drawDot(new DrawnPoint(point));
                    }
                }
                //joining the group succeeded
                else if(input.equals("@Success")){
                    send("@Name" + USER_NAME);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("Joined Group " + GROUP_NAME);
                        }
                    });
                }
                else{
                    final String msg = input;
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            showToast(msg);
                        }
                    });
                    Log.e(TAG, msg);
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //helper method
    private void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
