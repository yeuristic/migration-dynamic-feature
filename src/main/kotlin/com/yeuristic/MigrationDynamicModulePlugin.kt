package com.yeuristic

import com.android.build.gradle.AppExtension
import com.android.build.gradle.DynamicFeaturePlugin
import com.yeuristic.migration.MigrateBaseR
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class MigrationDynamicModulePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("migrationDM", MigrationDynamicFeaturePluginExtension::class.java)
        project.afterEvaluate {
            val baseRFullName = extension.baseRFullName
                ?: throw GradleException("baseRFullName is not configured, add migrationDM.baseRFullName = '<your app package>.R', eg: com.example.R")
            if (project.plugins.hasPlugin(DynamicFeaturePlugin::class.java)) {
                var found = false
                project.extensions.getByType(AppExtension::class.java).applicationVariants.all { variant ->
                    if (!found) {
                        found = true
                        project.task("migrateDynamicModule") { task ->
                            task.dependsOn("process${variant.name.capitalize()}Resources")
                            task.doLast {
                                val rTxtPath =
                                    "${project.buildDir.path}/intermediates/runtime_symbol_list/${variant.name}/R.txt"
                                val mainJavaFolder = "./${project.name}/src/"
                                MigrateBaseR.migrateBaseR(
                                    mainJavaFolder,
                                    baseRFullName,
                                    ListModuleResources.list(rTxtPath)
                                )
                                println(ListModuleResources.list(rTxtPath).joinToString { it.fileName })
                            }
                        }
                    }
                }
            }
        }
    }
}