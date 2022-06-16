package com.guilhermed.offloadingfibonaccibench;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.TrafficStats;
import android.os.BatteryManager;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

import AMOk.*;
import Offload.Monitor;
import eu.hgross.blaubot.android.BlaubotAndroid;
import eu.hgross.blaubot.android.BlaubotAndroidFactory;
import eu.hgross.blaubot.core.Blaubot;
import eu.hgross.blaubot.core.LifecycleListenerAdapter;

import static java.lang.Thread.sleep;

import Offload.*;

public class MainActivity extends AppCompatActivity implements ServiceCallbacks, Observer {
    private static final UUID APP_UUID = UUID.fromString("DE292C5B-34FB-4738-8C7E-D0291389DEC8");

    private Blaubot mBlaubot;
    //private DebugView mDebugView;
    private Midd midd;

    private static final int STOP = 0;
    private static final int GO = 1;

    private TextView outputPos;
    private TextView outputResult;
    private EditText inputPosition;
    private Button buttonCalculate;

    private long outputValue;

    private int waiter = 0;

    private boolean bounded;

    // variable for testing without going through decision algorithm. true = always offload, false = never offload
    private boolean offload = true;

    //private Middleware mid;
    private FrameworkMain framework;

    private Monitor mMonitor;
    private Planner planner;
    //private Extractor extractor;

    private long startTime;
    private long diffTime;

    private int currBatteryPct;
    private long currBatteryNanoWatt;
    private long startBattery;
    private long diffBattery;

    private long startBatteryNanoWatt;
    private long diffBatteryNanoWatt;

    private Long rxBytesStart = Long.valueOf(0);
    private Long txBytesStart = Long.valueOf(0);

    private Long rxBytesDiff;
    private Long txBytesDiff;

    private final int[] numbers = new int[]{5, 10, 15, 20, 25, 30, 35, 40, 45};
    private int fiboCount;

    static {
        eu.hgross.blaubot.util.Log.LOG_LEVEL = eu.hgross.blaubot.util.Log.LogLevel.DEBUG;
    }

    private int UID;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fiboCount = 0;

        UID = android.os.Process.myUid();

        setContentView(R.layout.activity_main);

        //startOffloadingMechanism();
        startOffloadingFramework();

        mBlaubot = BlaubotAndroidFactory.createEthernetBlaubot(APP_UUID);
//        mDebugView.registerBlaubotInstance(mBlaubot);

        mBlaubot.addLifecycleListener(new LifecycleListenerAdapter() {
            @Override
            public void onConnected() {
                Log.d("MAINACTIVITY", "CONNECTED");
            }

            @Override
            public void onDisconnected() {
                Log.d("MAINACTIVITY", "DISCONNECTED");
            }

        });

        startMyService();

        outputPos = (TextView) findViewById(R.id.idPosValue);
        outputResult = (TextView) findViewById(R.id.idResultValue);
        inputPosition = (EditText) findViewById(R.id.idInput);
        buttonCalculate = (Button) findViewById(R.id.idButton);

        String storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        String header = "Fibonacci Number;Time Spent;Current Battery Percentage;Battery Spent;Bytes Received;Bytes Transmitted\n";
        String filePath = storageDir + "/benchmarks_values_fibo.txt";

        File file = new File(filePath);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileOutputStream stream = new FileOutputStream(filePath, false);
            OutputStreamWriter osw = new OutputStreamWriter(stream);
            osw.write(header);
            osw.flush();
            osw.close();
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBenchmarkStart();
                /*for (int i = 25; i < 30; i++) {
                    startTime = System.currentTimeMillis();
                    startBattery = Math.round(Dataset.getInstance().getBatteryPct()*100);
                    calcFibonacci(i);
                    diffBattery = Math.round(Dataset.getInstance().getBatteryPct()*100) - startBattery;
                    diffTime = System.currentTimeMillis() - startTime;
                    Log.d("TESTING TIME ELAPSED " + Integer.toString(i), Long.toString(diffTime));
                    Toast.makeText(MainActivity.this, "TESTING TIME ELAPSED " + Integer.toString(i) + Long.toString(diffTime), Toast.LENGTH_SHORT).show();
                    Log.d("TESTING BATTERY USAGE % " + Integer.toString(i), Long.toString(diffBattery));
                    Toast.makeText(MainActivity.this, "TESTING BATTERY USAGE % " + Integer.toString(i) + Long.toString(diffBattery), Toast.LENGTH_SHORT).show();
                }*/

                final String inputText = inputPosition.getText().toString();

                inputPosition.setText(String.valueOf(numbers[fiboCount]));

                //offload = Dataset.getInstance().getShouldOffload();

                if (!offload) {
                    outputPos.setText("" + String.valueOf(numbers[fiboCount]));

                    if (midd == null) {
                        Log.d("SERVICE READY?", "NOT READY");
                    }


                    runOnUiThread(new Runnable() {
                        public void run() {
                            midd.inputBenchmarkStartingMessageToMid("started");
                            outputValue = calcFibonacci(numbers[fiboCount]-1);


                            outputResult.setText("" + outputValue);

                            midd.midPublish(String.valueOf(numbers[fiboCount]) + ";" + String.valueOf(outputValue));

                            setBenchmarkValues();

                            fiboCount++;
                        }
                    });

                }
                if (offload) {
                    //int inputValue = Integer.parseInt(inputText);
                    outputPos.setText("" + String.valueOf(numbers[fiboCount]));

                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (midd == null) {
                                Log.d("SERVICE READY?", "NOT READY");
                            }
                            //midd.registerBlaubot(mBlaubot, (short) 1);

                            midd.inputBenchmarkStartingMessageToMid("started");

                            midd.inputToMid(String.valueOf(numbers[fiboCount]));
                            fiboCount++;

//                            startTime = System.currentTimeMillis();
//                            startBattery = getBatteryPct();
//                            startBatteryNanoWatt = getBatteryNanoWatt();
//                            rxBytesStart = getUidRxBytes(UID);
//                            txBytesStart = getUidTxBytes(UID);
                        }
                    });
                }
            }
        });

//        final CountDownTimer cdt = new CountDownTimer(30000 * 8 + 1, 30000) {
//            public void onTick(long millisUntilFinished) {
//                buttonCalculate.performClick();
//            }
//            public void onFinish() {
//            }
//        };
//
//        new CountDownTimer(10000, 10000) {
//            public void onTick(long millisUntilFinished) {
//                buttonCalculate.performClick();
//            }
//            public void onFinish() {
//                cdt.start();
//            }
//        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopMyService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBlaubot instanceof BlaubotAndroid) {
            final BlaubotAndroid blaubotAndroid = (BlaubotAndroid) mBlaubot;
            blaubotAndroid.unregisterReceivers(this);
            blaubotAndroid.onResume(this);
        }
    }

    @Override
    protected void onResume() {
        mBlaubot.startBlaubot();

        if (mBlaubot instanceof BlaubotAndroid) {
            final BlaubotAndroid blaubotAndroid = (BlaubotAndroid) mBlaubot;
            blaubotAndroid.setContext(this);
            blaubotAndroid.registerReceivers(this);
            blaubotAndroid.onResume(this);
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        mBlaubot.stopBlaubot();

        if (bounded) {
            midd.setCallbacks(null);
            unbindService(mConnection);
            bounded = false;
        }
        //stopMyService();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (mBlaubot instanceof BlaubotAndroid) {
            final BlaubotAndroid blaubotAndroid = (BlaubotAndroid) mBlaubot;
            blaubotAndroid.onNewIntent(intent);
        }
        super.onNewIntent(intent);
    }

    public long calcFibonacci(int n) {
        if (n <= 0)
            return 0;
        if (n == 1)
            return 1;
        return (calcFibonacci(n - 1) + calcFibonacci(n - 2));
    }

    /*public void send(final String inputValue) {
        Log.d("Service running?", Boolean.toString(isMyServiceRunning(Middleware.class)));

        /*for (InetAddress i : getLocalInetAddress()) {
            Log.d("IP: ", i.getCanonicalHostName());
            Log.d("IP: ", i.getHostName());
            Log.d("IP: ", i.getHostAddress());
            Log.d("IP: ", i.getAddress().toString());
        }

        String server_url = "http://192.168.0.9:8080/FibonacciServer/FibonacciServlet";
        final RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Resposta do servidor: ", response);
                        setResult(response);

                        //setWaiter(GO);

                        requestQueue.stop();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Erro no servidor: ", error.toString());

                setResult(error.toString());

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
                0,       // before was 10000
                30000)); // before was 30000
        requestQueue.add(stringRequest);
    }*/

    public String getResult() { return outputResult.getText().toString(); }
    public void setResult(final String response) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                outputResult.setText(response);
            }
        });
    }

    public String getOutputPos() { return outputPos.getText().toString(); }
    public void setOutputPos(final String response) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                outputPos.setText(response);
            }
        });
    }

    @SuppressLint("LongLogTag")
    @Override
    public void setBenchmarkValues() {

        diffTime = System.currentTimeMillis() - startTime;
        //currBatteryPct = Dataset.getInstance().getBatteryPct()*100;
        currBatteryPct = getBatteryPct();
        diffBattery = getBatteryPct() - startBattery;
        rxBytesDiff = getUidRxBytes(UID) - rxBytesStart;
        txBytesDiff = getUidTxBytes(UID) - txBytesStart;

        Log.d("BENCH TESTING TIME ELAPSED", Long.toString(diffTime));
        Log.d("BENCH CURRENT BATTERY PERCENTAGE", String.valueOf(currBatteryPct));
        Log.d("BENCH TESTING BATTERY USAGE %", Long.toString(diffBattery));
        Log.d("BENCH BYTES RECEIVED", Long.toString(rxBytesDiff));
        Log.d("BENCH BYTES SENT", Long.toString(txBytesDiff));

        String storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();

        String filePathBenchmark = storageDir + "/benchmarks_values_fibo.txt";

        String strNumber = String.valueOf(numbers[fiboCount])+";";
        String strDiffTime = Long.toString(diffTime)+";";
        String strCurrBatteryPct = String.valueOf(currBatteryPct)+";";
        String strDiffBatteryPct = Long.toString(diffBattery)+";";
        String strRxBytesDiff = Long.toString(rxBytesDiff)+";";
        // using '\n' as line break because it's faster and the code is only going to be tested on Android
        String strTxBytesDiff = Long.toString(txBytesDiff)+"\n";

        String strBenchmark = strNumber + strDiffTime + strCurrBatteryPct + strDiffBatteryPct + strRxBytesDiff + strTxBytesDiff;

        File file = new File(filePathBenchmark);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileOutputStream stream = new FileOutputStream(filePathBenchmark,  true);
            OutputStreamWriter osw = new OutputStreamWriter(stream);
            osw.write(strBenchmark);
            osw.flush();
            osw.close();
            stream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, "Calculation Done!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void setBenchmarkStart() {
        startTime = System.currentTimeMillis();
        startBattery = getBatteryPct();
        rxBytesStart = getUidRxBytes(UID);
        txBytesStart = getUidTxBytes(UID);
    }

    public int getWaiter() {
        return waiter;
    }
    public void setWaiter(int waiter) {
        this.waiter = waiter;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public int startMyService() {
        Intent serviceIntent = new Intent(this, Midd.class);

        if (!isMyServiceRunning(Midd.class)) {
            bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
            return 1;
        }
        return 0;
    }

    public int stopMyService() {
        Intent serviceIntent = new Intent(this, Midd.class);

        if (!isMyServiceRunning(Midd.class)) {
            if (bounded) {
                midd.setCallbacks(null);
                unbindService(mConnection);
                bounded = false;
            }

            return 1;
        }

        return 0;
    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "Service is disconnected!", Toast.LENGTH_LONG).show();
            bounded = false;
            midd = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(MainActivity.this, "Service is connected!", Toast.LENGTH_LONG).show();
            bounded = true;
            Midd.LocalBinder mLocalBinder = (Midd.LocalBinder)service;
            midd = mLocalBinder.getMiddlewareInstance();
            midd.setCallbacks(MainActivity.this);
            midd.registerBlaubot(mBlaubot, (short) 1);
        }
    };

    /// OLD OFFLOADING MECHANISM
    /*private static startOffloadingMechanism() {
        Monitor mMonitor = new Monitor();
        Timer timer = new Timer();

        final Extractor extractor = new Extractor(this);
        Dataset.getInstance().addObserver(this);

        if(mMonitor != null) {
            registerReceiver(mMonitor, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            registerReceiver(mMonitor, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
            registerReceiver(mMonitor, new IntentFilter(Intent.ACTION_POWER_CONNECTED));

            final IntentFilter filters = new IntentFilter();
            filters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            filters.addAction("android.net.wifi.STATE_CHANGE");
            registerReceiver(mMonitor, filters);

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMonitor.onReceive(getApplicationContext(),null);
                        }
                    });
                }
            },500,500);

            final boolean shouldExecuteLocally = Decider.shouldExecuteLocally();

            if (shouldExecuteLocally) {
                Log.d("Offloading Status: ", "Classifying locally...");
            } else {
                Log.d("Offloading Status: ","Classifying Remotely...");
            }
        }
    }*/

    public void startOffloadingFramework() {
        mMonitor = new Offload.Monitor();
        planner = new Planner();

        //extractor = new Extractor(this);
        //final long tamanho = extractor.getFileSize();
        //Dataset.getInstance().setFileSize(tamanho);

        Dataset.getInstance().addObserver(this);

        framework = new FrameworkMain();

        framework.run();
    }

    public static InetAddress[] getLocalInetAddress() {
        ArrayList<InetAddress> addresses = new ArrayList<InetAddress>();
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while( en.hasMoreElements()) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        addresses.add(inetAddress);
                    }
                }
            }
        } catch (SocketException ex) {
            String LOG_TAG = null;
            System.out.println(LOG_TAG + " : " + ex.toString());
        }
        return addresses.toArray(new InetAddress[0]);
    }

    @Override
    public void update(Observable observable, Object o) {
        /*bateryLevel.setText(String.valueOf(Math.round(Dataset.getInstance().getBatteryPct()*100))+"%");
        baterySource.setText(Dataset.getInstance().isCharging() ? "Status: Carregando via " + Dataset.getInstance().getCharginMethod() : "Status: Não está carregando");
        internetStatus.setText(Dataset.getInstance().isConnected() ? "Conectado" : "Offline");
        internetSource.setText("Via "+Dataset.getInstance().getConnectionType());

        memoryHeap.setText("Tamanho total do HEAP: "+Dataset.getInstance().getHeapSize()+"MB");
        heapUsing.setText("Usando no HEAP: "+Dataset.getInstance().getHeapUsed()+"MB");
        heapFree.setText("Disponível no HEAP: "+Dataset.getInstance().getHeapFree()+"MB");

        cpuUser.setText("User: "+String.valueOf(Math.round(Dataset.getInstance().getCpuUser()*100))+"%");
        cpuSystem.setText("System: "+String.valueOf(Math.round(Dataset.getInstance().getCpuSystem()*100))+"%");
        cpuWait.setText("IO: "+String.valueOf(Math.round(Dataset.getInstance().getCpuWait()*100))+"%");
        cpuIdle.setText("Idle: "+String.valueOf(Math.round(Dataset.getInstance().getCpuIdle()*100))+"%");

        runtimeOffline.setText("Tempo da última execução offine: "+String.valueOf(Math.round(Dataset.getInstance().getLocalTime()/1000.0))+"s");
        runtimeOnline.setText("Tempo da última execução online: "+String.valueOf(Math.round(Dataset.getInstance().getRemoteTime()/1000.0))+"s");*/
    }


    /**
     * Read UID Rx Bytes
     *
     * @param uid
     * @return rxBytes
     */
    public Long getUidRxBytes(int uid) {
        BufferedReader reader;
        Long rxBytes = 0L;
        try {
            reader = new BufferedReader(new FileReader("/proc/uid_stat/" + uid
                    + "/tcp_rcv"));
            rxBytes = Long.parseLong(reader.readLine());
            reader.close();
        }
        catch (FileNotFoundException e) {
            rxBytes = TrafficStats.getUidRxBytes(uid);
            //e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return rxBytes;
    }

    /**
     * Read UID Tx Bytes
     *
     * @param uid
     * @return txBytes
     */
    public Long getUidTxBytes(int uid) {
        BufferedReader reader;
        Long txBytes = 0L;
        try {
            reader = new BufferedReader(new FileReader("/proc/uid_stat/" + uid
                    + "/tcp_snd"));
            txBytes = Long.parseLong(reader.readLine());
            reader.close();
        }
        catch (FileNotFoundException e) {
            txBytes = TrafficStats.getUidTxBytes(uid);
            //e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return txBytes;
    }

    public int getBatteryPct() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;

        return level;
    }

    public long getBatteryNanoWatt() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

        long nanoWatt = batteryStatus.getLongExtra(Integer.toString(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER), -1);

        return nanoWatt;
    }
}
