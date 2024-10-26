package net.voxelpi.varp.repository.mysql.functions

import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.stringLiteral

class ReplaceFunction(expression: ExpressionWithColumnType<String>, from: String, to: String) : CustomFunction<String>(
    functionName = "REPLACE",
    columnType = VarCharColumnType(512),
    expr = arrayOf(expression, stringLiteral(from), stringLiteral(to))
)
