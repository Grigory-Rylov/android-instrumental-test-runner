package com.github.grishberg.tests

import com.github.grishberg.tests.adb.AdbWrapper
import com.github.grishberg.tests.common.DefaultGradleLogger
import com.github.grishberg.tests.common.RunnerLogger
import com.github.grishberg.tests.planner.InstrumentalTestPlanProvider
import com.github.grishberg.tests.planner.PackageTreeGenerator
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for testing.
 */
class InstrumentalTestPlugin implements Plugin<Project> {

    public static final String INSTRUMENTAL_PLUGIN_EXTENSION = 'instrumentalPluginConfig'

    @Override
    void apply(Project project) {
        project.getLogger().info("InstrumentalTestPlugin apply")

        InstrumentalExtension extension = project.extensions
                .create(INSTRUMENTAL_PLUGIN_EXTENSION, InstrumentalExtension)

        InstrumentationTestTask task = project.getTasks()
                .create(InstrumentationTestTask.NAME, InstrumentationTestTask.class)

        task.setGroup("verification")
        task.setDescription("Plugin for running instrumental tests on multiple devices")

        RunnerLogger logger = new DefaultGradleLogger(project.getLogger())
        PackageTreeGenerator packageTreeGenerator = new PackageTreeGenerator()
        InstrumentalTestPlanProvider testPlanProvider = new InstrumentalTestPlanProvider(
                propertiesFromGradleArguments(project), extension, packageTreeGenerator, logger)
        DeviceCommandsRunnerFabric deviceCommandsRunnerFabric = new DeviceCommandsRunnerFabric(
                testPlanProvider)
        AdbWrapper adbWrapper = new AdbWrapper()
        task.initAfterApply(adbWrapper, deviceCommandsRunnerFabric, logger)
    }

    private static Map<String, String> propertiesFromGradleArguments(Project project) {
        HashMap<String, String> properties = new HashMap<>()
        if (project.properties["testClass"]) {
            properties.put("testClass", (String) project.properties["testClass"])
        }
        if (project.properties["testPackage"]) {
            properties.put("testPackage", (String) project.properties["testPackage"])
        }
        return properties
    }
}
