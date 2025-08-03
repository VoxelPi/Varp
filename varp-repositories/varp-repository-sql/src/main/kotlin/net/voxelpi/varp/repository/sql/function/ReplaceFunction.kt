package net.voxelpi.varp.repository.sql.function

import org.jetbrains.exposed.v1.core.CustomFunction
import org.jetbrains.exposed.v1.core.ExpressionWithColumnType
import org.jetbrains.exposed.v1.core.TextColumnType
import org.jetbrains.exposed.v1.core.stringLiteral

class ReplaceFunction(expression: ExpressionWithColumnType<String>, from: String, to: String) : CustomFunction<String>(
    functionName = "REPLACE",
    columnType = TextColumnType(),
    expr = arrayOf(expression, stringLiteral(from), stringLiteral(to))
)
