package x.spirit.dynamicjob.core.task.runner;


import x.spirit.dynamicjob.core.task.Task;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Created by zhangwei on 3/11/16.
 */
public class NormalTaskRunner<V, S> implements Callable<V> {

    private Class taskClass;
    private S input;

    public Task<V, S> initializeTask() {
        Task task = null;
        try {
            task = (Task) taskClass.newInstance();
            System.out.println("Task initialized! " + task.getClass());
            task.setInput(input);
        } catch (Throwable ex) {
            ex.printStackTrace();
            Logger.getLogger(NormalTaskRunner.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return task;
        }
    }

    @Override
    public V call() throws Exception {
        V result = null;
        Task<V, S> task = initializeTask();
        if (task != null) {
            try {
                result = task.execute();
            } catch (Throwable t) {
                t.printStackTrace();
                Logger.getLogger(NormalTaskRunner.class.getName()).log(Level.SEVERE, null, t);
            }
        }
        return result;
    }

    public S getInput() {
        return input;
    }

    public void setInput(S input) {
        this.input = input;
    }

    public void setTaskClass(Class taskClass) {
        this.taskClass = taskClass;
    }

    public Class<Task> getTaskClass() {
        return taskClass;
    }
}
