package com.angelAndrei.HiitTimer

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.angelAndrei.HiitTimer.ui.theme.HiitTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HiitTimerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Counter(
                        modifier = Modifier.padding(innerPadding),
                        context = this
                    )
                }
            }
        }
    }
}

@Composable
fun Counter(modifier: Modifier = Modifier, context: Context) {
    var theCounter by remember { mutableStateOf(0L) }
    var countdownTime by remember { mutableStateOf(30) }
    var restTime by remember { mutableStateOf(10) }
    var cycleCount by remember { mutableStateOf(4) }
    var currentCycle by remember { mutableStateOf(1) }
    var isResting by remember { mutableStateOf(false) }
    var timerRunning by remember { mutableStateOf(false) }

    // Estados de visibilidad de las pantallas
    var mostrarPantallaConfiguracion by remember { mutableStateOf(true) }
    var mostrarPantallaGetReady by remember { mutableStateOf(false) }
    var mostrarPantallaWork by remember { mutableStateOf(false) }
    var mostrarPantallaRest by remember { mutableStateOf(false) }

    var miConterDown by remember {
        mutableStateOf(CounterDown(countdownTime) { newValue -> theCounter = newValue })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (mostrarPantallaConfiguracion) {
            PantallaConfiguracion(
                countdownTime = countdownTime,
                restTime = restTime,
                cycleCount = cycleCount,
                onConfigChange = { newWorkTime, newRestTime, newCycleCount ->
                    countdownTime = newWorkTime
                    restTime = newRestTime
                    cycleCount = newCycleCount
                },
                onStart = {
                    mostrarPantallaConfiguracion = false
                    mostrarPantallaGetReady = true
                }
            )
        }

        if (mostrarPantallaGetReady) {
            PantallaGetReady(
                currentCycle = currentCycle,
                cycleCount = cycleCount,
                time = theCounter,
                miConterDown = miConterDown,
                onStart = {
                    mostrarPantallaGetReady = false
                    mostrarPantallaWork = true
                    startWorkCycle(
                        context = context, // Pasamos el contexto aquí
                        countdownTime = countdownTime,
                        restTime = restTime,
                        currentCycle = currentCycle,
                        cycleCount = cycleCount,
                        onCounterUpdate = { miConterDown = it },
                        onRestingUpdate = { isResting = it },
                        onCurrentCycleUpdate = { currentCycle = it },
                        onCounterValueUpdate = { theCounter = it },
                        onTimerRunningUpdate = { timerRunning = it },
                        onMostrarPantallaWorkUpdate = { mostrarPantallaWork = it },
                        onMostrarPantallaRestUpdate = { mostrarPantallaRest = it },
                        onMostrarPantallaConfiguracionUpdate = { mostrarPantallaConfiguracion = it }
                    )
                }
            )
        }

        if (mostrarPantallaWork) {
            PantallaWork(
                currentCycle = currentCycle,
                cycleCount = cycleCount,
                time = theCounter,
                miConterDown = miConterDown
            )
        }

        if (mostrarPantallaRest) {
            PantallaRest(
                currentCycle = currentCycle,
                cycleCount = cycleCount,
                time = theCounter,
                miConterDown = miConterDown
            )
        }
    }
}

fun startWorkCycle(
    context: Context,
    countdownTime: Int,
    restTime: Int,
    currentCycle: Int,
    cycleCount: Int,
    onCounterUpdate: (CounterDown) -> Unit,
    onRestingUpdate: (Boolean) -> Unit,
    onCurrentCycleUpdate: (Int) -> Unit,
    onCounterValueUpdate: (Long) -> Unit,
    onTimerRunningUpdate: (Boolean) -> Unit,
    onMostrarPantallaWorkUpdate: (Boolean) -> Unit,
    onMostrarPantallaRestUpdate: (Boolean) -> Unit,
    onMostrarPantallaConfiguracionUpdate: (Boolean) -> Unit
) {
    onTimerRunningUpdate(true)

    val mediaPlayer = MediaPlayer.create(context, R.raw.audio) // Reproduce el sonido cuando queden 3 segundos

    val workCounter = CounterDown(countdownTime) { workValue ->
        onCounterValueUpdate(workValue)

        // Inicia el sonido cuando queden 3 segundos
        if (workValue == 3L) {
            mediaPlayer.start()
        }

        if (workValue == 0L) {
            onRestingUpdate(true)
            onMostrarPantallaWorkUpdate(false)
            onMostrarPantallaRestUpdate(true)
            mediaPlayer.stop()  // Detenemos el sonido después de que termine el trabajo

            val restCounter = CounterDown(restTime) { restValue ->
                onCounterValueUpdate(restValue)
                if (restValue == 0L) {
                    onRestingUpdate(false)
                    onMostrarPantallaRestUpdate(false)

                    val nextCycle = currentCycle + 1
                    if (nextCycle <= cycleCount) {
                        onCurrentCycleUpdate(nextCycle)
                        onMostrarPantallaWorkUpdate(true)
                        startWorkCycle(
                            context = context,
                            countdownTime = countdownTime,
                            restTime = restTime,
                            currentCycle = nextCycle,
                            cycleCount = cycleCount,
                            onCounterUpdate = onCounterUpdate,
                            onRestingUpdate = onRestingUpdate,
                            onCurrentCycleUpdate = onCurrentCycleUpdate,
                            onCounterValueUpdate = onCounterValueUpdate,
                            onTimerRunningUpdate = onTimerRunningUpdate,
                            onMostrarPantallaWorkUpdate = onMostrarPantallaWorkUpdate,
                            onMostrarPantallaRestUpdate = onMostrarPantallaRestUpdate,
                            onMostrarPantallaConfiguracionUpdate = onMostrarPantallaConfiguracionUpdate
                        )
                    } else {
                        onTimerRunningUpdate(false)
                        onMostrarPantallaConfiguracionUpdate(true)
                    }
                }
            }
            onCounterUpdate(restCounter)
            restCounter.start()
        }
    }
    onCounterUpdate(workCounter)
    workCounter.start()
}

@Composable
fun PantallaConfiguracion(
    countdownTime: Int,
    restTime: Int,
    cycleCount: Int,
    onConfigChange: (Int, Int, Int) -> Unit,
    onStart: () -> Unit
) {
    var workTimeInput by remember { mutableStateOf(countdownTime.toString()) }
    var restTimeInput by remember { mutableStateOf(restTime.toString()) }
    var cycleCountInput by remember { mutableStateOf(cycleCount.toString()) }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Configurar HiitTimer", fontSize = 30.sp, modifier = Modifier.padding(bottom = 16.dp))

        Text(text = "Tiempo de trabajo (s):", fontSize = 20.sp)
        BasicTextField(
            value = workTimeInput,
            onValueChange = { workTimeInput = it },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )

        Text(text = "Tiempo de descanso (s):", fontSize = 20.sp)
        BasicTextField(
            value = restTimeInput,
            onValueChange = { restTimeInput = it },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )

        Text(text = "Número de ciclos:", fontSize = 20.sp)
        BasicTextField(
            value = cycleCountInput,
            onValueChange = { cycleCountInput = it },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )

        Button(
            onClick = {
                onConfigChange(
                    workTimeInput.toIntOrNull() ?: countdownTime,
                    restTimeInput.toIntOrNull() ?: restTime,
                    cycleCountInput.toIntOrNull() ?: cycleCount
                )
                onStart()
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Comenzar")
        }
    }
}

@Composable
fun PantallaGetReady(
    currentCycle: Int,
    cycleCount: Int,
    time: Long,
    miConterDown: CounterDown,
    onStart: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ciclo: $currentCycle / $cycleCount",
            fontSize = 30.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Prepararse: ${time}s",
            fontSize = 40.sp
        )
        Button(onClick = { onStart() }) {
            Text(text = "Iniciar")
        }
    }
}

@Composable
fun PantallaWork(
    currentCycle: Int,
    cycleCount: Int,
    time: Long,
    miConterDown: CounterDown
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ciclo: $currentCycle / $cycleCount",
            fontSize = 30.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Trabajo: ${time}s",
            fontSize = 40.sp
        )
        Button(onClick = { miConterDown.toggle() }) {
            Text(text = if (miConterDown.counterState) "Pausar" else "Reanudar")
        }
    }
}

@Composable
fun PantallaRest(
    currentCycle: Int,
    cycleCount: Int,
    time: Long,
    miConterDown: CounterDown
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ciclo: $currentCycle / $cycleCount",
            fontSize = 30.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Descanso: ${time}s",
            fontSize = 40.sp
        )
        Button(onClick = { miConterDown.toggle() }) {
            Text(text = if (miConterDown.counterState) "Pausar" else "Reanudar")
        }
    }
}
