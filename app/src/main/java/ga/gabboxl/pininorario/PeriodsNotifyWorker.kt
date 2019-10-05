package ga.gabboxl.pininorario

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class PeriodsNotifyWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    override fun doWork(): Result {

        Log.e("tanta roba", "asd")

        return Result.success()
    }
}