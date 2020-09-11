package com.yeuristic.shrink

import java.io.File

fun File.filterResFolder() =  isDirectory && nameWithoutExtension != "values"