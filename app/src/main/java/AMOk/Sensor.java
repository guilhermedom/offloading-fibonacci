package AMOk;

import Offload.Dataset;

public class Sensor {

    private KnowledgeBase kBase;
    private Dataset dataset;

    public Sensor() {
        kBase = KnowledgeBase.getInstance();
        dataset = Dataset.getInstance();
    }

    public void sense(){
        kBase.setBatteryPct(dataset.getBatteryPct());
        kBase.setCharging(dataset.isCharging());

        kBase.setConnected(dataset.isConnected());

        kBase.setHeapFree(dataset.getHeapFree());
        kBase.setHeapUsed(dataset.getHeapUsed());
        kBase.setHeapSize(dataset.getHeapSize());

        kBase.setFileSize(dataset.getFileSize());
    }
}