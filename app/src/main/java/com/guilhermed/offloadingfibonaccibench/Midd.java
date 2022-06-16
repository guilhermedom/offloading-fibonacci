package com.guilhermed.offloadingfibonaccibench;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import eu.hgross.blaubot.core.Blaubot;
import eu.hgross.blaubot.core.IBlaubotDevice;
import eu.hgross.blaubot.core.ILifecycleListener;
import eu.hgross.blaubot.core.statemachine.IBlaubotConnectionStateMachineListener;
import eu.hgross.blaubot.core.statemachine.states.IBlaubotState;
import eu.hgross.blaubot.messaging.BlaubotMessage;
import eu.hgross.blaubot.messaging.IBlaubotChannel;
import eu.hgross.blaubot.messaging.IBlaubotMessageListener;

public class Midd extends Service implements ILifecycleListener, IBlaubotConnectionStateMachineListener {
    private final String TAG = "MIDDD";

    private IBlaubotChannel channel;
    private Blaubot mBlaubot;

    private String valueFromActivity;
    private ServiceCallbacks serviceCallbacks;

    //Declaring the handler
    private Handler handler;
    //Declaring your implementation of Runnable
    private Runner runner;

    private int ackCounter;

    IBinder mBinder = new LocalBinder();

    public void onCreate() {
        super.onCreate();

        ackCounter = 0;

        //debugView = (DebugView) findViewById(R.id.debugView);

        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        handler = new Handler();
        runner = new Runner();
    }

    public void onNewIntent(Intent intent) {
        Log.d("MESSAGE IN8", "Hello world!");
    }

    @Override
    public void onDestroy() {
        //Log.d("MESSAGE IN9", "Hello world!");
        handler.removeCallbacks(runner);
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        stopSelf();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public Midd getMiddlewareInstance() {
            return Midd.this;
        }
    }

    public class Runner implements Runnable {
        @Override
        public void run() {
            Log.d("AndroidClarified", "Running");
            handler.postDelayed(this, 1000 * 5);
        }
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }

    // Send messages to all subscribers of this channel
    public void publish (String message) {
        //channel.clearMessageQueue();
        channel.publish(message.getBytes());
        Log.d("MESSAGE OUT", message);
    }

    public void publishFile (String filePath) {
        File file = new File(filePath);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        channel.publish(bytes, true);
        Log.d("MESSAGE OUT", filePath);
    }

    public void inputBenchmarkStartingMessageToMid(String message) {
        publish(message);
    }

    public void inputToMid(String input) {
        valueFromActivity = input;
        //publish(valueFromActivity);
        send(valueFromActivity);
    }

    public void inputFilePathToMid(String filePath) {
        publishFile(filePath);
        //send(valueFromActivity);
    }

    public void midPublish(String input) {
        valueFromActivity = input;
        publish(valueFromActivity);
    }

    public void send(final String inputValue) {
        //Log.d("Service running?", Boolean.toString(isMyServiceRunning(Middleware.class)));

        String server_url = "http://192.168.0.9:8080/FibonacciServer/FibonacciServlet";
        final RequestQueue requestQueue = Volley.newRequestQueue(Midd.this);
        Log.d("TRACKING", "TRACKING");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Resposta do servidor: ", response);

                        if (serviceCallbacks != null) {
                            serviceCallbacks.setResult(response);
                            serviceCallbacks.setBenchmarkValues();
                        }

                        publish(valueFromActivity + ";" + response);

                        requestQueue.stop();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Erro no servidor: ", error.toString());

                if (serviceCallbacks != null) {
                    serviceCallbacks.setResult(error.toString());
                }

                requestQueue.stop();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("fiboString", inputValue);

                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,  // before was 30000
                1000,       // before was 10000
                30000)); // before was 30000
        requestQueue.add(stringRequest);
    }

    private IBlaubotMessageListener mMessageListener = new IBlaubotMessageListener() {
        @Override
        public void onMessage(BlaubotMessage blaubotMessage) {
            //String msg = new String(blaubotMessage.getPayload());
            Log.d("MESSAGE RECEIVED", "HAHAHAHAH");

            if (blaubotMessage != null) {
                String message = new String(blaubotMessage.getPayload());

                if (message.equalsIgnoreCase("started")) {
                    serviceCallbacks.setBenchmarkStart();
                    Log.d("MESSAGE RECEIVED", message);
                } else if (message.equalsIgnoreCase("ack")) {
                    Log.d("ACK RECEIVED", message);
                    ackCounter++;

                    if (ackCounter == 1) {
                        ackCounter = 0;
                        serviceCallbacks.setBenchmarkValues();
                    }
                } else {
                    String resp = new String(blaubotMessage.getPayload());

                    Log.d("PAYLOAD RECEIVED", resp);

                    String[] inputAndOutput = resp.split(";");

                    if (serviceCallbacks != null) {
                        Log.d("MESSAGE IN FROM NET: ", blaubotMessage.toString());
                        serviceCallbacks.setResult(inputAndOutput[1]);
                        serviceCallbacks.setOutputPos(inputAndOutput[0]);
                    }

                    /*String storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();

                    Log.d("PATHH", storageDir);

                    String filePath = storageDir + "/midd_test_4096KB_result.txt";
                    Log.d("PATH + FILENAME", filePath);

                    byte[] fileContentInBytes = blaubotMessage.getPayload();

                    File file = new File(filePath);
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        FileOutputStream stream = new FileOutputStream(filePath);
                        stream.write(fileContentInBytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    publish("ack");
                    serviceCallbacks.setBenchmarkValues();
                }
            } else {
                Log.d("ATTENTION: ", "blaubotMessage is null");
            }
        }
    };

    private void registerMessageChannel(IBlaubotChannel blaubotChannel) {
        channel = blaubotChannel;
        channel.subscribe(mMessageListener);
    }

    private void unregisterChannels() {
        if (channel != null) {
            channel.removeMessageListener(mMessageListener);
        }
    }

    public void registerBlaubot(Blaubot blaubot, short messageChannelId) {
        if (mBlaubot != null) {
            unregisterBlaubot();
        }

        mBlaubot = blaubot;
        mBlaubot.addLifecycleListener(this);
        mBlaubot.getConnectionStateMachine().addConnectionStateMachineListener(this);

        // Create a channel for each of our messages
        channel = mBlaubot.createChannel(messageChannelId);
        channel.getChannelConfig().setTransmitIfNoSubscribers(true);
        channel.getChannelConfig().setTransmitReflexiveMessages(true);

        // register them with the chatroom
        registerMessageChannel(channel);
    }

    public void unregisterBlaubot() {
        unregisterChannels();
        if (mBlaubot != null) {
            mBlaubot.removeLifecycleListener(this);
            mBlaubot.getConnectionStateMachine().removeConnectionStateMachineListener(this);
            mBlaubot = null;
        }
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "CONNECTED");
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "DISCONNECTED");
    }

    @Override
    public void onDeviceJoined(IBlaubotDevice blaubotDevice) {
        Log.d(TAG, "JOINED");
    }

    @Override
    public void onDeviceLeft(IBlaubotDevice blaubotDevice) {
        Log.d(TAG, "LEFT");
    }

    @Override
    public void onPrinceDeviceChanged(IBlaubotDevice oldPrince, IBlaubotDevice newPrince) {
        Log.d(TAG, "PRINCE CHANGED");
    }

    @Override
    public void onKingDeviceChanged(IBlaubotDevice oldKing, IBlaubotDevice newKing) {
        Log.d(TAG, "KING CHANGED");
    }

    @Override
    public void onStateChanged(IBlaubotState oldState, IBlaubotState newState) {
        Log.d(TAG, "STATE CHANGED");
        channel.clearMessageQueue();
    }

    @Override
    public void onStateMachineStopped() {
        Log.d(TAG, "STATE MACHINE STOPPED");
    }

    @Override
    public void onStateMachineStarted() {
        Log.d(TAG, "STATE MACHINE STARTED");
    }
}
