package com.angelAndrei.HiitTimer

import android.os.CountDownTimer
import android.util.Log

class CounterDown(var segundos: Int, var loquehacealhacertick: (Long) -> Unit) {
    var counterState: Boolean = false
    private var myCounter: CountDownTimer? = null

    // Inicializa el CountDownTimer
    private fun createCounter() {
        myCounter = object : CountDownTimer((segundos * 1000L), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (counterState) loquehacealhacertick(millisUntilFinished / 1000)
            }

            override fun onFinish() {
                counterState = false
                Log.i("dam2", "El temporizador ha terminado")
            }
        }
    }

    init {
        createCounter()
    }

    fun toggle() {
        Log.i("dam2", "Toggle llamado, estado actual: $counterState")
        if (counterState) {
            cancel()
        } else {
            start()
        }
    }

    fun start() {
        if (!counterState) {
            createCounter()  // Crear un nuevo contador cada vez que comienza
            counterState = true
            myCounter?.start()
            Log.i("dam2", "Temporizador iniciado")
        }
    }

    fun cancel() {
        counterState = false
        myCounter?.cancel()
        Log.i("dam2", "Temporizador detenido")
    }
}
