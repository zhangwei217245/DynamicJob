package x.spirit.dynamicjob.core.task;

/**
 * Created by zhangwei on 3/11/16.
 */
public interface Task<V, S> {


    public V execute();

    public void setInput(S input);
}
