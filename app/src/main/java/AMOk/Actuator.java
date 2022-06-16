package AMOk;

import Offload.Dataset;

public class Actuator {
    private KnowledgeBase kBase;
    private Dataset dataset;

    public Actuator() {
        kBase = KnowledgeBase.getInstance();
        dataset = Dataset.getInstance();
    }

    public void act(){
        dataset.setShouldOffload(kBase.getShouldOffload());
    }
}