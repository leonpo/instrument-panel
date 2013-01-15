package com.portman.mfd;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
 
public class MFDActivity extends Activity {
   ServerSocket ss = null;
   Thread myCommsThread = null;
   protected static final int MSG_ID = 0x1335;
   public static final int SERVERPORT = 6000;
   private static String mClientMsg = "";
   
   // UI controls
   private static VSD mVSD;

   @Override
   public void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   setContentView(R.layout.activity_mfd);
	   
	   Log.i("TEWSActivity", "onCreate");
	   
	   // find controls	   
	   mVSD = (VSD) findViewById(R.id.vsd);
	   	  	 
	   this.myCommsThread = new Thread(new CommsThread());
	   this.myCommsThread.start();
   }
   
 
   @Override
   protected void onStop() {
	   super.onStop();
	   
	   Log.i("MFDActivity", "OnStop");
	   
	   try {
		   // make sure you close the socket upon exiting
		   this.myCommsThread.interrupt();
		   ss.close();
	   } catch (IOException e) {
		   e.printStackTrace();
	   }
   }
 
   private static Handler myUpdateHandler = new Handler() {
	   public void handleMessage(Message msg) {
		   switch (msg.what) {
		   case MSG_ID:			   
			   try {
				   Log.i("MFDActivity", "handleMessage: " + mClientMsg);
				   // parse json
				   JSONObject object = (JSONObject) new JSONTokener(mClientMsg).nextValue();
				   
				   mVSD.showTargets(object);
				   
			   } catch (Exception e) {
				   // TODO Auto-generated catch block
				   e.printStackTrace();
			   }
			   break;
		   default:
			   break;
		   }
		   super.handleMessage(msg);
	   }
   };
   
   class CommsThread implements Runnable {
	   public void run() {
		   Socket s = null;
		   try {
			   ss = new ServerSocket(SERVERPORT );
			   Log.i("MFDActivity", "Socket created");
		   } catch (IOException e) {
			   e.printStackTrace();
		   }
        
		   while (!Thread.currentThread().isInterrupted()) {
			   Message m = new Message();
			   m.what = MSG_ID;
			   try {
				   if (s == null)
					   s = ss.accept();
				   Log.i("MFDActivity", "Socket after accept");
				   BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
				   String st = null;
				   st = input.readLine();
				   if (st == null) {
					   input.close();
					   s.close();
					   s = null;
				   } else {
					   mClientMsg = st;
					   myUpdateHandler.sendMessage(m);
				   }
			   } catch (IOException e) {
				   e.printStackTrace();
			   }
		   }
		   Log.i("MFDActivity", "Comms Thread is interrupted");
	   }
    }
}