package AMOk;

import android.util.Log;
import Offload.Dataset;

/**
 * Created by Vitor Bertozzi on 8/9/18.
 *
 */
public class Planner {
    public double threshold;

    public Planner() {
        threshold = 0.5;
    }

    public void plan(){
        KnowledgeBase kBase= KnowledgeBase.getInstance();
        Boolean isConnected = kBase.isConnected();

        Double coefficient = kBase.getCoefficient();

        Log.d("Planner data:","Coefficient: "+ String.valueOf(coefficient));

        /** If not connected, auto-deny offloading*/
        if (!isConnected){
            kBase.setShouldOffload(false);
        }

        if (coefficient>threshold) {
            kBase.setShouldOffload(true);
        }
        else{
            kBase.setShouldOffload(false);
        }
    }
}