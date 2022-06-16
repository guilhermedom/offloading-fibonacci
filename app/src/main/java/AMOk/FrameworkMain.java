package AMOk;

public class FrameworkMain {
    private Monitor mMonitor;
    private Analyser mAnalyser;
    private Planner mPlanner;
    private Executor mExecutor;

    public FrameworkMain(){
        mMonitor = new Monitor();
        mAnalyser = new Analyser();
        mPlanner = new Planner();
        mExecutor = new Executor();
    }

    public void run(){
        mMonitor.monitor();
        mAnalyser.analyse();
        mPlanner.plan();
        mExecutor.execute();
    }
}