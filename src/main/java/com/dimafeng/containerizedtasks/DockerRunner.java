package com.dimafeng.containerizedtasks;

import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.output.FrameConsumerResultCallback;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.shaded.com.github.dockerjava.api.DockerClient;
import org.testcontainers.shaded.com.github.dockerjava.api.command.CreateContainerResponse;
import org.testcontainers.shaded.com.github.dockerjava.api.model.Bind;
import org.testcontainers.shaded.com.github.dockerjava.api.model.Volume;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class DockerRunner {

    public static final String WORKING_DIR = "/tmp/workspace";
    public static final String SCRIPTS_DIR = "/tmp/build";

    public static int runScript(String imageName, Path buildDir, Path workspaceDir, String scriptBody,
                                    Map<String, String> additionalBinds, Consumer<String> stdoutConsumer, Consumer<String> stderrConsumer) {
        Path script = buildDir.resolve(new Date().getTime() + ".sh");
        try {

            try {
                String scriptWithDebugInfo = Arrays.stream(scriptBody.split("\n"))
                        .collect(joining("\n"));

                Files.write(script, scriptWithDebugInfo.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Cannot create a script file in " + buildDir, e);
            }

            Map<String, String> binds = new HashMap<>();
            binds.put(buildDir.toAbsolutePath().toString(), SCRIPTS_DIR);
            binds.put(workspaceDir.toAbsolutePath().toString(), WORKING_DIR);
            if (additionalBinds != null) {
                binds.putAll(additionalBinds);
            }

            return DockerCommandExecutor.getInstance()
                    .run(imageName, WORKING_DIR,
                            new String[]{"sh", "-x", SCRIPTS_DIR + "/" + script.getFileName().toString()}, binds, stdoutConsumer, stderrConsumer);
        } finally {
            try {
                Files.delete(script);
            } catch (IOException e) {
                //TODO log properly
            }
        }
    }

    private interface DockerCommandExecutor {
        int run(String imageName, String workingDir, String[] command, Map<String, String> volumeBinds, Consumer<String> stdoutConsumer, Consumer<String> stderrConsumer);

        static DockerCommandExecutor getInstance() {
            return new TestcontainersCommandExecutor();
        }
    }

    private static class TestcontainersCommandExecutor implements DockerCommandExecutor {
        public int run(String imageName, String workingDir, String[] command, Map<String, String> volumeBinds, Consumer<String> stdoutConsumer, Consumer<String> stderrConsumer) {

            DockerClient dockerClient = DockerClientFactory.instance().client();

            CreateContainerResponse exec = dockerClient
                    .createContainerCmd(imageName)
                    .withWorkingDir(workingDir)
                    .withCmd(command)
                    .withBinds(volumeBinds.entrySet().stream()
                            .map(e -> new Bind(e.getKey(), new Volume(e.getValue())))
                            .collect(toList())
                    )
                    .exec();

            try {

                FrameConsumerResultCallback callback = new FrameConsumerResultCallback();
                callback.addConsumer(OutputFrame.OutputType.STDERR, fr -> {
                    if (fr.getBytes() != null) stderrConsumer.accept(fr.getUtf8String().trim());
                });
                callback.addConsumer(OutputFrame.OutputType.STDOUT, fr -> {
                    if (fr.getBytes() != null) stdoutConsumer.accept(fr.getUtf8String().trim());
                });
                callback.addConsumer(OutputFrame.OutputType.END, fr -> {
                    if (fr.getBytes() != null) stdoutConsumer.accept(fr.getUtf8String().trim());
                });
                dockerClient.startContainerCmd(exec.getId()).exec();
                dockerClient.logContainerCmd(exec.getId()).withFollowStream(true).withStdErr(true).withStdOut(true).exec(callback);
                while (dockerClient.inspectContainerCmd(exec.getId()).exec().getState().getRunning()) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                return dockerClient.inspectContainerCmd(exec.getId()).exec().getState().getExitCode();
            } finally {
                dockerClient.removeContainerCmd(exec.getId()).exec();
            }
        }
    }
}
