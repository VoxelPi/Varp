package net.voxelpi.varp.warp.repository

import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

public sealed interface RepositoryTypeData {

    public val id: String

    public data class NoArgs(
        override val id: String,
        public val generator: (id: String) -> Result<Repository>,
    ) : RepositoryTypeData

    public data class WithPath(
        override val id: String,
        public val generator: (id: String, path: Path) -> Result<Repository>,
    ) : RepositoryTypeData

    public data class WithConfig<C : RepositoryConfig>(
        override val id: String,
        public val configType: KClass<C>,
        public val generator: (id: String, config: C) -> Result<Repository>,
    ) : RepositoryTypeData

    public data class WithPathConfig<C : RepositoryConfig>(
        override val id: String,
        public val configType: KClass<C>,
        public val generator: (id: String, path: Path, config: C) -> Result<Repository>,
    ) : RepositoryTypeData

    public companion object {

        public inline fun <reified T : Repository> fromClass(): Result<RepositoryTypeData?> {
            return fromClass(T::class)
        }

        public fun fromClass(type: KClass<out Repository>): Result<RepositoryTypeData?> {
            val typeAnnotation = type.findAnnotation<RepositoryType>()
            if (typeAnnotation == null) {
                throw IllegalArgumentException("The given repository class is not marked as type.")
            }

            val id = typeAnnotation.id

            val constructors = type.constructors.filter { it.findAnnotation<RepositoryLoader>() != null }
            require(constructors.size == 1) { "There must be exactly one constructor marked with the RepositoryLoader annotation." }
            val constructorFunction = constructors.single()

            val constructorArgs = constructorFunction.parameters
            val argsMap = constructorArgs.map { it.name }.withIndex().associate { it.value to it.index }

            // Check that the constructor has an id argument.
            require(hasArgument(constructorArgs, "id", typeOf<String>())) { "The constructor must have an id argument" }

            if (hasArgument(constructorArgs, "path", typeOf<Path>())) {
                if (hasArgument(constructorArgs, "config", typeOf<RepositoryConfig>())) {
                    require(constructorArgs.size == 3) { "There must be exactly 3 constructor args." }

                    @Suppress("UNCHECKED_CAST")
                    val configType = constructorArgs.find { it.name == "config" }!!.type.classifier as KClass<RepositoryConfig>

                    val generator = { id: String, path: Path, config: RepositoryConfig ->
                        val args = arrayOf<Any?>(null, null, null)
                        args[argsMap["id"]!!] = id
                        args[argsMap["path"]!!] = path
                        args[argsMap["config"]!!] = config
                        Result.success(constructorFunction.call(*args))
                    }
                    return Result.success(WithPathConfig<RepositoryConfig>(id, configType, generator))
                } else {
                    require(constructorArgs.size == 2) { "There must be exactly 2 constructor args." }

                    val generator = { id: String, path: Path ->
                        val args = arrayOf<Any?>(null, null)
                        args[argsMap["id"]!!] = id
                        args[argsMap["path"]!!] = path
                        Result.success(constructorFunction.call(*args))
                    }
                    return Result.success(WithPath(id, generator))
                }
            } else {
                if (hasArgument(constructorArgs, "config", typeOf<RepositoryConfig>())) {
                    require(constructorArgs.size == 2) { "There must be exactly 2 constructor args." }

                    @Suppress("UNCHECKED_CAST")
                    val configType = constructorArgs.find { it.name == "config" }!!.type.classifier as KClass<RepositoryConfig>

                    val generator = { id: String, config: RepositoryConfig ->
                        val args = arrayOf<Any?>(null, null)
                        args[argsMap["id"]!!] = id
                        args[argsMap["config"]!!] = config
                        Result.success(constructorFunction.call(*args))
                    }
                    return Result.success(WithConfig<RepositoryConfig>(id, configType, generator))
                } else {
                    require(constructorArgs.size == 1) { "There must be exactly 1 constructor args." }

                    val generator = { id: String -> Result.success(constructorFunction.call(id)) }
                    return Result.success(NoArgs(id, generator))
                }
            }
        }

        private fun hasArgument(args: List<KParameter>, name: String, type: KType): Boolean {
            return args.any { it.name == name && it.type.isSubtypeOf(type) }
        }
    }
}
