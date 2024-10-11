package net.voxelpi.varp.cli.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.CoroutineContext

class VarpCLIDispatcher : CoroutineDispatcher() {

    val executor = VarpCLIExecutor()

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        executor.execute(block)
    }
}
