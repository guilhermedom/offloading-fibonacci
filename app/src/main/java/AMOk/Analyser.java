package AMOk;

import android.util.Log;

import Offload.Dataset;

public class Analyser {

    public void analyse(){
        KnowledgeBase kBase = KnowledgeBase.getInstance();

        final String toastText = "fileSizeValue[0,1]: "+ Math.min((double)KnowledgeBase.getInstance().getFileSize()/(double)30,(double)1)
                + "\nMemoValue[0,1]: "+ (double)KnowledgeBase.getInstance().getHeapUsed()/(double)KnowledgeBase.getInstance().getHeapSize()
                + "\nBattery[0,1]:" + (double)KnowledgeBase.getInstance().getBatteryPct();
        Log.d("Analyser data:",toastText);

        /** Specialist defined parameters due to application requests*/
        double batteryWeight = 0.5;
        double memoWeight = 0.3;
        double sizeWeight = 0.2;


        double memo = memoWeight*((double)kBase.getHeapUsed()/(double)kBase.getHeapSize());

        /** Using 30KB as size reference*/
        double size = sizeWeight* Math.min((double)kBase.getFileSize()/(double)30,(double)1);

        double battery;

        if(kBase.isCharging())
            battery = batteryWeight;
        else
            battery = batteryWeight*(double)kBase.getBatteryPct();

        double coef = size + battery + memo;

        kBase.setCoefficient(coef);
    }
}