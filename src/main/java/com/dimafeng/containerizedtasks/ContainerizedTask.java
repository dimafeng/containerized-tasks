package com.dimafeng.containerizedtasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

public abstract class ContainerizedTask extends DefaultTask {

    private final String TASK_BUILD_DIR = "containerized-task";

    @Input
    public String buildDirPostfix = "";

    public abstract String getSourcesDir();

    public abstract String getImageName();

    public abstract String getScriptBody();

    public Map<String, String> getAdditionalVolumeBinds() {
        return Collections.emptyMap();
    }

    public void preRun(Path sources, Path buildDir) {

    }

    @TaskAction
    public void run() throws Exception {
        check(getSourcesDir() != null && !getSourcesDir().isEmpty(), "Source dir is not set");
        check(getImageName() != null && !getImageName().isEmpty(), "Docker image is not set");
        check(getScriptBody() != null && !getScriptBody().isEmpty(), "Script body dir is not set");

        Path sources = getProject().getProjectDir().toPath().resolve(getSourcesDir());
        Path buildDir = getProject().getBuildDir().toPath().resolve(TASK_BUILD_DIR + buildDirPostfix);
        Files.createDirectories(buildDir);

        preRun(sources, buildDir);

        int exitCode = DockerRunner.runScript(
                getImageName(),
                buildDir,
                sources,
                getScriptBody(),
                getAdditionalVolumeBinds(),
                m -> getLogger().info(m),
                m -> getLogger().error(m));

        if (exitCode > 0) {
            throw new GradleException("Script finished with exit code " + exitCode);
        }
    }

    protected void check(boolean check, String message) {
        if (!check) {
            throw new InvalidUserDataException(message);
        }
    }
}