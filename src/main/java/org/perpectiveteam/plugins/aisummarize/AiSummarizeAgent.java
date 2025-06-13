package org.perpectiveteam.plugins.aisummarize;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

public final class AiSummarizeAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(org.perpectiveteam.plugins.aisummarize.AiSummarizeAgent.class);

    private AiSummarizeAgent() {
        super();
    }

    public static void premain(String args, Instrumentation instrumentation) throws UnmodifiableClassException, ClassNotFoundException {
        redefineAvailability(instrumentation, "org.perpectiveteam.plugins.aisummarize.AiSummarizePluginBootstrap", redefineIsAvailableFlag());
    }

    private static void redefineAvailability(Instrumentation instrumentation, String targetClassName, Redefiner redefiner) throws ClassNotFoundException, UnmodifiableClassException {
        instrumentation.addTransformer(new ClassFileTransformer() {

            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] byteCode) {

                String finalTargetClassName = targetClassName.replace(".", "/");

                if (!className.equals(finalTargetClassName)) {
                    return byteCode;
                }

                LOGGER.debug("Transforming class {}", targetClassName);
                try {
                    ClassPool cp = ClassPool.getDefault();
                    CtClass cc = cp.get(targetClassName);

                    redefiner.redefine(cc);

                    byteCode = cc.toBytecode();
                    cc.detach();
                } catch (NotFoundException | CannotCompileException | IOException e) {
                    LOGGER.error("Could not transform class {}}, will use default class definition", targetClassName, e);
                }

                return byteCode;
            }

        });

        instrumentation.retransformClasses(Class.forName(targetClassName));
    }


    private static Redefiner redefineIsAvailableFlag() {
        return ctClass -> {
            CtMethod ctMethod = ctClass.getDeclaredMethod("isAvailable");
            ctMethod.setBody("return true;");
        };
    }

    @FunctionalInterface
    private interface Redefiner {
        void redefine(CtClass ctClass) throws CannotCompileException, NotFoundException;
    }

}
