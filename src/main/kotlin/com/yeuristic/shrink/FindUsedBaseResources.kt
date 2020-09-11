package com.yeuristic.shrink

import com.yeuristic.ResourceData
import java.io.File
import java.lang.StringBuilder

//com.yeuristic.dynamicfeature.base_resource
object FindUsedBaseResources {
    fun findFromXml(
        dynamicModulePaths: Set<String>,
        baseResources: Set<ResourceData>
    ): Set<ResourceData> {
        val result: MutableSet<ResourceData> = mutableSetOf()

        dynamicModulePaths.map {
            getResourceFolder(it)
        }.forEach { dynamicModuleRes ->
            dynamicModuleRes.listFiles()
                ?.filter { it.filterResFolder() }
                ?.forEach { folder ->
                    folder.listFiles()
                        ?.filter { it.isFile }
                        ?.forEach {
                            result.addAll(
                                findBaseResourceUsage(
                                    it,
                                    baseResources
                                )
                            )
                        }
                }
        }

        return result
    }

    fun findFromCode(
        dynamicModulePaths: Set<String>,
        baseResources: Set<ResourceData>
    ): Set<ResourceData> {
        val result: MutableSet<ResourceData> = mutableSetOf()

        dynamicModulePaths.map {
            File(getResourceFolder(it).path + "/raw")
        }.forEach { dynamicModuleResBase ->
            dynamicModuleResBase.listFiles()
                ?.filter { it.isFile && it.extension == "txt"}
                ?.forEach {
                    val reader = it.bufferedReader()
                    var line = reader.readLine()
                    while (line != null) {
                        val temp = line.split(".")
                        if (temp.size == 3 && temp[0] == "R") {
                            result.add(
                                ResourceData(
                                    temp[1],
                                    temp[2]
                                )
                            )
                        }
                        line = reader.readLine()
                    }
                }

        }
        return result
    }

    private fun getResourceFolder(dynamicModulePath: String): File =
        File("$dynamicModulePath/src/main/res")

    private fun findBaseResourceUsage(
        file: File,
        baseResources: Set<ResourceData>
    ): Set<ResourceData> {
        val result: MutableSet<ResourceData> = mutableSetOf()
        val reader = file.bufferedReader()
        var line = reader.readLine()
        var inComment = false
        var stringBuilder: StringBuilder = StringBuilder()
        while (line != null) {
            val startComment = "<!--"
            val endComment = "-->"
            var i = 0
            while (i in line.indices) {
                val char = line[i]
                if (inComment) {
                    if (char == '-') {
                        if (line.length - i >= endComment.length) {
                            inComment = false
                            for (j in endComment.indices) {
                                if (line[i + j] != endComment[j]) {
                                    inComment = true
                                    break
                                }
                            }
                            if (!inComment) {
                                i += endComment.length - 1
                            }
                        }
                    }
                } else {
                    if (char == '<') {
                        if (line.length - i >= startComment.length) {
                            inComment = true
                            for (j in startComment.indices) {
                                if (line[i + j] != startComment[j]) {
                                    inComment = false
                                    break
                                }
                            }
                            if (inComment) {
                                i += startComment.length - 1
                            }
                        }
                    } else if (char == '@') {
                        val validChar: MutableSet<Char> = mutableSetOf<Char>().apply {
                            addAll(('a'..'z').toHashSet())
                            addAll(('A'..'Z').toHashSet())
                            addAll(('0'..'9').toHashSet())
                            add('_')
                        }
                        var folderName = ""
                        var fileName = ""
                        stringBuilder.clear()
                        var j = i + 1
                        while (j in line.indices) {
                            val c = line[j]
                            if (c in validChar) {
                                stringBuilder.append(c)
                            } else if (c == '/') {
                                folderName = stringBuilder.toString()
                                stringBuilder.clear()
                            } else {
                                fileName = stringBuilder.toString()
                                stringBuilder.clear()
                                break
                            }
                            j++
                        }
                        if (folderName.isNotEmpty() && fileName.isNotEmpty()) {
                            val resourceData =
                                ResourceData(folderName, fileName)
                            if (baseResources.contains(resourceData)) {
                                result.add(resourceData)
                            }
                            i += folderName.length + "/".length + fileName.length
                        }

                    }
                }
                i++
            }
            line = reader.readLine()
        }
        return result
    }
}