package com.guilhermed.offloadingfibonaccibench;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;

import java.io.IOException;
import java.io.RandomAccessFile;

import Offload.Dataset;

public class AppMonitor extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        monitorBatery(context,intent);
        monitorInternet(context);
        //monitorCPU();
        monitorMemory();

    }

    /**
     *
     * @param context
     * @param intent
     *
     * Get battery information from the system intent.
     * Monitor when battery status change and when batery level change
     */
    public void monitorBatery(Context context, Intent intent){
        if(intent!=null) {
            int batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            if (batteryStatus != -1) {
                Dataset.getInstance().setCharging(batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                        batteryStatus == BatteryManager.BATTERY_STATUS_FULL);
            }

            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            if (chargePlug != -1) {
                switch (chargePlug) {
                    case BatteryManager.BATTERY_PLUGGED_AC:
                        Dataset.getInstance().setCharginMethod("AC");
                        break;
                    case BatteryManager.BATTERY_PLUGGED_USB:
                        Dataset.getInstance().setCharginMethod("USB");
                        break;
                    case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                        Dataset.getInstance().setCharginMethod("Wireless");
                        break;
                    default:
                        break;
                }
            }
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int nanoWattHoursCounter = intent.getIntExtra(Integer.toString(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER), -1);
            /*if (level != -1 && scale != -1) {
                Dataset.getInstance().setBatteryPct(level / (float) scale);
            }*/

            if (nanoWattHoursCounter != -1) {
                Dataset.getInstance().setBatteryPct(nanoWattHoursCounter);
            }
        }
    }

    /**
     *
     * @param context
     *
     * Monitor connectivity from android system service
     */
    public void monitorInternet(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if(activeNetwork != null) {
            Dataset.getInstance().setConnected(activeNetwork != null && activeNetwork.isConnectedOrConnecting());
            Dataset.getInstance().setConnectionType(activeNetwork.getTypeName() + " " + activeNetwork.getSubtypeName());
        }
    }

    /**
     * Monitor cpu by reading first line of linux file /proc/stats
     */
    public void monitorCPU(){
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r"); //ONSERTAR
            String load = reader.readLine();

            String[] toks = load.split(" +");  // Split on one or more spaces

            float user = Float.parseFloat(toks[1]) + Float.parseFloat(toks[2]);
            float system = Float.parseFloat(toks[3]);
            float idle = Float.parseFloat(toks[4]);
            float wait = Float.parseFloat(toks[5]) + Float.parseFloat(toks[6]) + Float.parseFloat(toks[7]);

            float total = user + system + idle + wait;

            Dataset.getInstance().setCpuUser(user/total);
            Dataset.getInstance().setCpuSystem(system/total);
            Dataset.getInstance().setCpuWait(wait/total);
            Dataset.getInstance().setCpuIdle(idle/total);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Monitor available memory by using Java's Runtime class
     */
    public void monitorMemory(){
        final Runtime runtime = Runtime.getRuntime();
        final long usedMemInMB=(runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
        final long maxHeapSizeInMB=runtime.maxMemory() / 1048576L;
        final long availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB;

        Dataset.getInstance().setHeapSize(maxHeapSizeInMB);
        Dataset.getInstance().setHeapFree(availHeapSizeInMB);
        Dataset.getInstance().setHeapUsed(usedMemInMB);
    }

}
