package ch.mno.copper;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;

/**
 * Created by dutoitc on 23.04.2019.
 */
public class GroovyPoc {


    public abstract static class API extends Script {
        static int value=1;

        public void inc() {
            value++;
        }

        public int getValue() {
            return value;
        }

        @Override
        public Object run() {
            return null;
        }
    }

    public static void main(String[] args) {
//        ScriptEngineManager factory = new ScriptEngineManager(); // JSR 223
//        ScriptEngine engine = factory.getEngineByName("groovy");

        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass("ch.mno.copper.GroovyPoc.API");

        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(GroovyPoc.class.getClassLoader(), binding, config);

        // Simple
        Object obj = shell.evaluate("a=1;b=2;System.out.println(a+b); return a+b;");
        System.out.println(obj);

        // API
        // Simple
        obj = shell.evaluate("System.out.println(getValue());inc();System.out.println(getValue());inc();getValue()");
        System.out.println(obj);
        System.out.println(API.value);

        /*
        trigger=[CronTrigger('* * * * *')]
        filter=['x<10']
        collectors=[
            jmx(....).keep('y', 'x').keep('b','c')
        ]
        reporter=[mail('...','...')]

         */
    }

}
