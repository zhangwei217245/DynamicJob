package x.spirit.dynamicjob.core.task.type;


import x.spirit.dynamicjob.core.task.Task;
import x.spirit.dynamicjob.core.task.runner.NormalTaskRunner;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhangwei on 3/11/16.
 *
 */
public abstract class NormalTask<T, S> implements Task<T, S> {

    private S input;

    @Override
    public T execute() {
        T result = null;
        try{
            result = executeTask(input);
        }catch (Throwable t) {
            t.printStackTrace();
            Logger.getLogger(NormalTask.class.getName()).log(Level.SEVERE, null, t);
        }
        return result;
    }

    public abstract T executeTask(S input);

    public S getInput() {
        return input;
    }

    @Override
    public void setInput(S input) {
        this.input = input;
    }
}
