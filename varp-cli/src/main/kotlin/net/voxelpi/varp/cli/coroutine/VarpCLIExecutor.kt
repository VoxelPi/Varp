package net.voxelpi.varp.cli.coroutine

import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue

class VarpCLIExecutor : Executor {

    private val taskQueue: BlockingQueue<Runnable> = LinkedBlockingQueue()

    override fun execute(command: Runnable) {
        taskQueue.offer(command)
    }

    fun runTasks() {
        while (true) {
            val task = taskQueue.poll() ?: break
            task.run()
        }
    }
}
