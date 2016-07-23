package com.dimafeng.containerizedtasks;

import org.gradle.api.tasks.Input;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

public class NpmContainerizedTask extends ContainerizedTask {

    @Input
    private String sourcesDir = "";

    @Input
    private String imageName = "monostream/nodejs-gulp-bower";

    @Input
    private String scriptBody = "npm install\ngulp";

    private Path nodeModulesCache;

    @Override
    public String getSourcesDir() {
        return sourcesDir;
    }

    @Override
    public String getImageName() {
        return imageName;
    }

    @Override
    public String getScriptBody() {
        return scriptBody;
    }

    public void setSourcesDir(String sourcesDir) {
        this.sourcesDir = sourcesDir;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setScriptBody(String scriptBody) {
        this.scriptBody = scriptBody;
    }

    @Override
    public Map<String, String> getAdditionalVolumeBinds() {
        return Collections.singletonMap(nodeModulesCache.toAbsolutePath().toString(), DockerRunner.WORKING_DIR + "/node_modules");
    }

    @Override
    public void preRun(Path sources, Path buildDir) {
        nodeModulesCache = buildDir.resolve("node_modules_cache");
    }
}
