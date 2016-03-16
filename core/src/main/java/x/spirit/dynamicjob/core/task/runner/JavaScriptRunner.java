package x.spirit.dynamicjob.core.task.runner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhangwei on 3/11/16.
 *
 */
public class JavaScriptRunner<V> implements Callable<V>{

    private String jsFilePath;

    @Override
    public V call() throws Exception {
        V result = null;
        try {
            if (jsFilePath == null) {
                System.out.println("Please submit the location of your Task file:");
                Scanner stdin = new Scanner(System.in);
                while (stdin.hasNext()) {
                    jsFilePath = stdin.next();
                }
            }

            ScriptEngineManager engineManager
                    = new ScriptEngineManager();
            ScriptEngine engine
                    = engineManager.getEngineByName("nashorn");
            result = (V)engine.eval(new FileReader(jsFilePath));
        }catch(Throwable t) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, t);
            System.out.println(t);
        }finally {
            return result;
        }
    }

    public String getJsFilePath() {
        return jsFilePath;
    }

    public void setJsFilePath(String jsFilePath) {
        this.jsFilePath = jsFilePath;
    }
}
