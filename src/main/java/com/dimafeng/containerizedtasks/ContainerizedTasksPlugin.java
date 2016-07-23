package com.dimafeng.containerizedtasks;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ContainerizedTasksPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().create("npmContainerizedTask", NpmContainerizedTask.class);
    }
}
