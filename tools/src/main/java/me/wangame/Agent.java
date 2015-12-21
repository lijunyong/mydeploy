package me.wangame;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * Created by zzy
 */
public class Agent {

    public static void premain(String agentArgs, Instrumentation inst)
            throws ClassNotFoundException, UnmodifiableClassException {
        inst.addTransformer(new BugFixTransformer(0,agentArgs),true);
        System.out.println("premain finish!!!!");
    }

    public static void agentmain(String args, Instrumentation inst) throws Exception {
        System.out.println("args="+args);
        String[]ss = args.split("_");
        Class[] allLoadedClasses = inst.getAllLoadedClasses();
        BugFixTransformer transformer = new BugFixTransformer(Integer.valueOf(ss[0]),args);
        inst.addTransformer(transformer, true);
        for (Class clazz : allLoadedClasses) {
            if (clazz.getName().equals(ss[1])) {
                try {
                    inst.retransformClasses(clazz);
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        boolean remove = inst.removeTransformer(transformer);
        System.out.println("agentmain finish!! remove:"+remove);
    }


}
