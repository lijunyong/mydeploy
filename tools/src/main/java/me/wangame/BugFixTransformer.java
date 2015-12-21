package me.wangame;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.ProtectionDomain;

/**
 * Created by zzy
 */
public class BugFixTransformer implements ClassFileTransformer {


    private int type;
    private String params;

    public BugFixTransformer(int type,String params) {
        this.type = type;
        this.params = params;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
//        if (BugManager.ins().hasBugFix(className)) {
//            byte[]data = BugManager.ins().getFixCode(className);
//            return data;
//        }

        String[] ss = params.split("_");
        System.out.println("type="+type);
        if (type == 0) {//modify
            System.out.println("modify className:"+className.replaceAll("/","."));
            if (className.replaceAll("/",".").equals(ss[1])) {
                System.out.println("transform  begin " + className +"begin content:"+ss[2]);
                ClassPool pool = ClassPool.getDefault();
                CtClass currentClass = null;
                try {
                    currentClass = pool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
                    CtMethod method = currentClass.getDeclaredMethod("execute");
                    method.insertBefore(ss[2]);
                    method.insertAfter(ss[3]);
                    currentClass.writeFile();
                    classfileBuffer = currentClass.toBytecode();
                } catch (Exception e) {
                    e.printStackTrace();

                } finally {
                    if (currentClass != null) {
                        currentClass.detach();
                    }
                }
            }
        } else if (type == 1) {//load
            System.out.println("className:"+className.replaceAll("/","."));
            if (className.replaceAll("/",".").equals(ss[1])) {
                System.out.println("-----------------------> load "+ss[1] +" file path:"+ss[2]);
                try {
                    byte[] data = Files.readAllBytes(Paths.get(ss[2]));
                    return data;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return classfileBuffer;
    }


}
