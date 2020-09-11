package com.yeuristic.shrink

import com.yeuristic.ListModuleResources
import com.yeuristic.ResourceData
import java.io.File

object WhitelistResources {
    fun whitelist(resultPath: String, baseResourcePath: String, dynamicModulePaths: ArrayList<String>) {
        val baseResources =
            ListModuleResources.list(baseResourcePath)

        val resourcesInCode = FindUsedBaseResources.findFromCode(dynamicModulePaths.toSet(), baseResources)

        val resourceInXml = FindUsedBaseResources.findFromXml(dynamicModulePaths.toSet(), baseResources)

        val resources: List<String> = mutableListOf<ResourceData>().apply {
            addAll(resourcesInCode)
            addAll(resourceInXml)
        }.map {
            "@${it.folderName}/${it.fileName}"
        }

        File(resultPath).apply {
            parentFile?.mkdirs()
            createNewFile()
//            @layout/l_used*_c,@layout/l_used_a,@layout/l_used_b*
            writeText(
                """<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools"
tools:keep="${resources.joinToString(separator = ", ")}" />"""
            )
        }
//        not recursive
//        File(path).listFiles()
    }
}