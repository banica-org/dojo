package com.dojo.codeexecution.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Component
@PropertySource("classpath:application.properties")
public class RequestExecutor {

    final static Logger logger = LoggerFactory.getLogger(RequestExecutor.class);

    static String participantClassName;
    static String participantClassPath;
    static String participantMethodName;

    @Autowired
    public void setParticipantClassName(@Value("${participant.class.name}") String participantClassName){
        RequestExecutor.participantClassName = participantClassName;
    }
    @Autowired
    public void setParticipantClassPath(@Value("${participant.class.path}") String participantClassPath){
        RequestExecutor.participantClassPath = participantClassPath;
    }
    @Autowired
    public void setParticipantMethodName(@Value("${participant.method.name}") String participantMethodName){
        RequestExecutor.participantMethodName = participantMethodName;
    }

    private final static String[] EMPTY_STRING_ARRAY = {};

    public static void invokeKataClient(final String participantName) {
        //DynamicCompiler.compileParticipantCode(participantName);

        try {
            reloadAndRunClass();
        } catch (ClassNotFoundException e) {
            logger.error("New class not found at : " + participantClassPath + ". Error Message : " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.error("Could not access new file : " + e.getMessage());
        } catch (InstantiationException e) {
            logger.error("Could not instantiate new class : " + e.getMessage());
        } catch (InvocationTargetException e) {
            logger.error("Invocation target exception : " + e.getMessage());
        }
    }

    private static void reloadAndRunClass() throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
        DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(DynamicClassLoader.class.getClassLoader());
        Class<?> participantSolverClass = dynamicClassLoader.loadClass(participantClassName);

        //todo - combine all incoming user files into kata client solver before sending request to kata server

        Method method = null;
        try {
            method = participantSolverClass.getMethod(participantMethodName, String[].class);
        } catch (NoSuchMethodException e) {
            logger.error(participantMethodName + " method missing from class " + participantSolverClass.getName());
        }

        if (method != null) {
//            method.invoke(null, (Object) EMPTY_STRING_ARRAY); // static method doesn't have an instance
        } else {
            logger.error("New class " + participantMethodName + " method could not be obtained properly.");
        }

    }

}
