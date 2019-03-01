package ru.yandex.money.gradle.plugins.grafana.dashboard;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Grafana dashboards plugin
 */
@SuppressWarnings("ClassWithoutConstructor")
public class GrafanaDashboardPlugin implements Plugin<Project> {

    /**
     * Actions when applying GrafanaDashboardPlugin to a project
     *
     * @param target project to apply to
     */
    @Override
    public void apply(Project target) {
        GrafanaDashboardExtension grafanaDashboardExtension = getGrafanaDashboardExtensionWithDefaults(target);
        target.getPluginManager().apply(JavaBasePlugin.class);
        Configuration grafanaConfiguration = configureSourceSets(target, grafanaDashboardExtension);
        target.afterEvaluate(project -> {
            createUploadGrafanaDashboardTask(project, grafanaConfiguration, grafanaDashboardExtension);
        });
    }

    private static GrafanaDashboardExtension getGrafanaDashboardExtensionWithDefaults(Project target) {
        GrafanaDashboardExtension grafanaDashboardExtension = target.getExtensions()
                .create("grafana", GrafanaDashboardExtension.class);
        grafanaDashboardExtension.classpath = target.files();
        return grafanaDashboardExtension;
    }

    private Configuration configureSourceSets(Project project, GrafanaDashboardExtension grafanaDashboardExtension) {

        SourceSetContainer sourceSets = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
        SourceSet grafanaSourceset = sourceSets.create("grafana");
        grafanaSourceset.getJava().srcDir(new File(grafanaDashboardExtension.dir));

        Configuration grafanaCompileConfiguration = project.getConfigurations().getByName("grafanaCompile");
        DependencySet grafanaCompileDependencies = grafanaCompileConfiguration.getDependencies();
        grafanaCompileDependencies.add(new DefaultExternalModuleDependency(
                "org.jetbrains.kotlin", "kotlin-stdlib", "1.2.61"));
        grafanaCompileDependencies.add(new DefaultExternalModuleDependency(
                "org.jetbrains.kotlin", "kotlin-reflect", "1.2.61"));
        return grafanaCompileConfiguration;
    }

    private static UploadGrafanaDashboardsTask createUploadGrafanaDashboardTask(
            Project target, Configuration grafanaConfiguration, GrafanaDashboardExtension grafanaDashboardExtension) {
        UploadGrafanaDashboardsTask task = target.getTasks()
                .create("uploadGrafanaDashboards", UploadGrafanaDashboardsTask.class);
        task.setGroup("other");
        task.setDescription("Upload Grafana dashboards");
        task.setGrafanaConfiguration(grafanaConfiguration);
        task.setGrafanaDashboardExtension(grafanaDashboardExtension);
        return task;
    }

}