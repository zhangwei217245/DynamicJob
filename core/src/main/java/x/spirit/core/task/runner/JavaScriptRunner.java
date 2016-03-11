package x.spirit.core.task.runner;

import x.spirit.core.App;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
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
            try {
                result = (V)engine.eval(new FileReader(jsFilePath));
            } catch (ScriptException | FileNotFoundException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }catch(Throwable t) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, t);
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
