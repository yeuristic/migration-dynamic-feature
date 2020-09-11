package com.yeuristic.shrink

import com.yeuristic.ResourceData
import java.io.File
import java.util.regex.Pattern


object GenerateBaseR {
    fun generate(destinationPath: String, baseRFullName: String) {
        val resourceSet: MutableSet<ResourceData> = mutableSetOf()

        File("./dynamicfeature/base-resources.gradle").let {
            val reader = it.bufferedReader()
            var line = reader.readLine()
            val p = Pattern.compile("R\\.([a-zA-Z0-9_]+)\\.([a-zA-Z0-9_]+)")

            while (line != null) {
                val m = p.matcher(line)
                while (m.find()) {
                    val folderName = m.group(1)
                    val fileName = m.group(2)
                    resourceSet.add(
                        ResourceData(
                            folderName,
                            fileName
                        )
                    )
                }
                line = reader.readLine()
            }
        }


        val resourceList: MutableList<ResourceData> = resourceSet.toMutableList()
        resourceList.sortWith (Comparator { o1, o2 ->
            val folderDiff = o1.folderName.compareTo(o2.folderName)
            if (folderDiff != 0) {
                folderDiff
            } else {
                o1.fileName.compareTo(o2.fileName)
            }
        })
//        val innerClassString = ""

        File(destinationPath+"/BaseR.java").apply {
            parentFile?.mkdirs()
            createNewFile()
            val writer = bufferedWriter()
            val spacing = "    "

            writer.run {
                val indexOfCom = destinationPath.indexOf("com/")
                val packageName = destinationPath.substring(indexOfCom).replace('/', '.')
                write("package $packageName;")
                newLine()
                newLine()
                write("import $baseRFullName;")
                newLine()
                newLine()
                write("public final class BaseR {")
                newLine()
            }

            var lastFolderName = ""
            resourceList.forEach {
                if (it.folderName != lastFolderName) {
                    if (lastFolderName.isNotEmpty()) {
                        writer.run {
                            write(spacing)
                            write("}")
                            newLine()
                        }
                    }
                    lastFolderName = it.folderName
                    writer.run {
                        write(spacing)
                        write("public static final class $lastFolderName {")
                        newLine()
                    }
                }
                writer.run {
                    write(spacing)
                    write(spacing)
                    write("public static final int ${it.fileName} = R.$lastFolderName.${it.fileName};")
                    newLine()
                }
            }
            if (lastFolderName.isNotEmpty()) {
                writer.run {
                    write(spacing)
                    write("}")
                    newLine()
                }
            }
            writer.write("}")
            writer.flush()
            writer.close()
        }
    }
}