package net.voxelpi.varp.option

/**
 * Stores the state of an option.
 * @property option The option.
 * @property value The value of the option.
 */
@JvmRecord
public data class OptionValue<T>(
    val option: Option<T>,
    val value: T,
)

/**
 * Creates an option value.
 */
public infix fun <T> Option<T>.to(value: T): OptionValue<T> = OptionValue(this, value)
