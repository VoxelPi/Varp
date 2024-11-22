package net.voxelpi.varp.option

/**
 * Stores the values of options.
 */
public class OptionsContext internal constructor(
    private val options: Map<Option<*>, *>,
) {

    internal constructor(optionValues: Collection<OptionValue<*>>) : this(optionValues.associate { it.value as Option<*> to it.value })

    /**
     * Gets the current value of the given [option] or null, if the option is not set.
     */
    public fun <T> getOrNull(option: Option<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return options[option] as T?
    }

    /**
     * Gets the current value of the given [option] or the default value of the option.
     */
    public fun <T> getOrDefault(option: Option<T>): T {
        return getOrNull(option) ?: option.default
    }

    /**
     * Gets the current value of the given [option] or [default], if the option is not set.
     */
    public fun <T> getOrElse(option: Option<T>, default: T): T {
        return getOrNull(option) ?: default
    }
}
