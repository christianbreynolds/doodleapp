package com.example.chris.doodleapp;
//Author: Chris Reynolds
//christian.b.reynolds@vanderbilt.edu
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;


public class LoginScreenActivity extends ActionBarActivity {
    private LinearLayout loginLayout, groupLayout, createGroupLayout;
    private ListView groupList;
    private EditText nameEdit, groupEdit;
    private Button loginButton, refreshButton, changeNameButton, createGroupButton,
            groupSubmitButton, cancelGroupButton;
    private ArrayAdapter<String> adapt;
    private static final String TAG = LoginScreenActivity.class.getCanonicalName();
    private String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        loginLayout = (LinearLayout) findViewById(R.id.loginLayout);
        groupLayout = (LinearLayout) findViewById(R.id.groupLayout);
        createGroupLayout = (LinearLayout) findViewById(R.id.createGroupLayout);

        groupList = (ListView) findViewById(R.id.grouplist);

        nameEdit = (EditText) findViewById(R.id.nameEdit);
        groupEdit = (EditText) findViewById(R.id.groupEdit);

        loginButton = (Button) findViewById(R.id.loginButton);
        refreshButton = (Button) findViewById(R.id.refreshButton);
        changeNameButton = (Button) findViewById(R.id.changeNameButton);
        createGroupButton = (Button) findViewById(R.id.createGroupButton);
        groupSubmitButton = (Button) findViewById(R.id.groupSubmitButton);
        cancelGroupButton = (Button) findViewById(R.id.cancelGroupButton);

        adapt = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.my_list_item);
        groupList.setAdapter(adapt);

        //what happens when you click on a Group name in the List View
        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView clicked = (TextView) view;
                String groupName = clicked.getText().toString();

                startCanvasActivity(groupName);
            }
        });
        Log.d(TAG, "before click listener");

        //saves the login name, brings up the Group List View
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = nameEdit.getText().toString();
                if(username.equals("")){
                    showToast("Please enter a name");
                    return;
                }
                else{
                    loginLayout.setVisibility(View.GONE);
                    groupLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        //refreshes the groups
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGroups();
            }
        });

        //Brings back the original View
        changeNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupLayout.setVisibility(View.GONE);
                loginLayout.setVisibility(View.VISIBLE);
            }
        });

        //brings up the Create Group View
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupLayout.setVisibility(View.GONE);
                createGroupLayout.setVisibility(View.VISIBLE);
            }
        });

        //restores the Group List View
        cancelGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroupLayout.setVisibility(View.GONE);
                groupLayout.setVisibility(View.VISIBLE);
            }
        });

        //creates a group based on the EditText
        groupSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = groupEdit.getText().toString();
                if(groupName.equals("")){
                    showToast("Group must have a name");
                    return;
                }
                createGroup(groupName);
            }
        });

        getGroups();
    }

    //helper method to get everything prepared for the CanvasActivity
    public void startCanvasActivity(String groupName){
        Intent canvasIntent = new Intent(getApplicationContext(), CanvasActivity.class);
        Bundle b = new Bundle();
        b.putString("username", username); //two important things we can pass over for when we establish a good connection
        b.putString("groupname", groupName);
        canvasIntent.putExtras(b);
        startActivity(canvasIntent);
    }


    //this method populates the ListView with the correct groups on the server
    public boolean getGroups(){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void ... args) {
                try {
                    Socket s = new Socket(Constants.SERVER_SITE, Constants.PORT_NUMBER);
                    PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    pw.println("@Groups");
                    String groups = br.readLine();

                    if(groups!=null && groups.startsWith("@Groups")){
                        final String [] groupArr = groups.substring(7).split("&");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapt.clear(); //we clear all then re-read the groups
                                for(String str:groupArr) {
                                    adapt.add(str);
                                }
                                adapt.sort(new Comparator<String>() { //for aesthetics
                                    @Override
                                    public int compare(String lhs, String rhs) {
                                        return lhs.compareTo(rhs);
                                    }
                                });
                            }
                        });
                    }
                    s.close();
                } catch (IOException e) {
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
                else{
                    adapt.notifyDataSetChanged(); //updates the ListView
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return false;
    }


    //does the networking aspect of creating a group
    //if it successfully creates a group, it launches the CanvasActivity
    //if not, it toasts the error
    public boolean createGroup(String groupName){
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String ... args) {
                Socket s = null;
                try {
                    s = new Socket(Constants.SERVER_SITE, Constants.PORT_NUMBER);
                    PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String name = args[0];
                    pw.println("@CreaGroup" + name);
                    String groups = br.readLine();
                    s.close();
                    if(groups==null || !groups.equals("@Created")){ //if the message back isn't proper
                        return null;
                    }
                    else{
                        return name; //passes the groupName to onPostExecute
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    return null;
                }
                //return null;
            }
            @Override
            protected void onPostExecute(String msg){
                final String str = msg;
                if(str == null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast(str + " already exists");
                        }
                    });
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startCanvasActivity(str);
                        }
                    });
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, groupName);

        return false;
    }

    //helper method so I don't need that big long line everywhere
    private void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
