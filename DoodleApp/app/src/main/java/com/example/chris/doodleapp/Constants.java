package com.example.chris.doodleapp;

/**
 * Created by Chris on 4/15/2015.
 */
//Author: Chris Reynolds
//christian.b.reynolds@vanderbilt.edu

    //some pre-defined Constants/Options
public class Constants {
    //makes life easy
    public static final String SERVER_SITE = "ec2-54-200-108-87.us-west-2.compute.amazonaws.com";
    public static final int PORT_NUMBER = 7777;

    //how many points will be stored before automatically sending
    public static final int MAX_BUFFER = 50;

    //how much time can elapse before automatically sending
    public static final long SEND_RATE = 500;
}
