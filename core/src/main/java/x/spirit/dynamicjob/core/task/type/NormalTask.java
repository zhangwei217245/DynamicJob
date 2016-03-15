package x.spirit.dynamicjob.core.task.type;


import x.spirit.dynamicjob.core.task.Task;

/**
 * Created by zhangwei on 3/11/16.
 *
 */
public abstract class NormalTask<T, S> implements Task<T, S> {

    private S input;

    @Override
    public T execute() {
        return executeTask(input);
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
