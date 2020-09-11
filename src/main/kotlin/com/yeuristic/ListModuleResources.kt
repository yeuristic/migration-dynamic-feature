package com.yeuristic

import com.yeuristic.shrink.FindUsedBaseResources
import java.io.File

object ListModuleResources {
    fun list(path: String): Set<ResourceData> {
        val resourceSet: MutableSet<ResourceData> = mutableSetOf()
        File(path).apply {
            val bufferedReader = bufferedReader()
            var line = bufferedReader.readLine()
            while (line != null) {
                val temp = line.split(" ")
                if (temp.size > 2) {
                    resourceSet.add(ResourceData(temp[1], temp[2]))
                }
                line = bufferedReader.readLine()
            }
        }
        println("Resource count: ${resourceSet.size}")
        return resourceSet
    }
}

//private typealias BaseR = FindUsedBaseResources