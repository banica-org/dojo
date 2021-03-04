package com.dojo.codeexecution.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;


import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Component
@PropertySource("classpath:compiler.properties")
public class DynamicCompiler {

    final static Logger logger = LoggerFactory.getLogger(DynamicCompiler.class);


    private static String baseSourcePath;
    private static String baseOutputPath;

    @Autowired
    public void setBaseSourcePath(@Value("${base.source.path}") String baseSourcePath){
        DynamicCompiler.baseSourcePath = baseSourcePath;
    }
    @Autowired
    public void setBaseOutputPath(@Value("${base.output.path}") String baseOutputPath){
        DynamicCompiler.baseOutputPath = baseOutputPath;
    }

    public static void compileParticipantCode(String participantName) {
        final String systemFileSeparator = System.getProperty("file.separator");
        final File[] participantFiles = new File(baseSourcePath + systemFileSeparator + participantName).listFiles();

        if (participantFiles == null || participantFiles.length == 0) {
            logger.warn("Compilation initiated, but not files were found to be compiled.");
            return;
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
            String[] options = new String[]{"-d", baseOutputPath + systemFileSeparator + participantName};
            Iterable<? extends JavaFileObject> compilationUnit = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(participantFiles));

            JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    Arrays.asList(options),
                    null,
                    compilationUnit);

            if (!task.call()) {
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    logger.error("An error has been encountered during compilation. Line " + diagnostic.getLineNumber() + ", " + diagnostic.getSource().toUri());
                }
            } else {
                logger.info("Compilation has been successful.");
            }
        } catch (IOException e) {
            logger.error("Could not compile files - IOException: " + e.getMessage());
        }

    }
}

