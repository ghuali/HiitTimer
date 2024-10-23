package com.angelAndrei.HiitTimer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.angelAndrei.HiitTimer.ui.theme.HiitTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HiitTimerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Counter(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Counter(modifier: Modifier = Modifier) {
    var theCounter by remember { mutableStateOf(0L) }
    var countdownTime by remember { mutableStateOf(0) }
    var restTime by remember { mutableStateOf(0) }
    var cycleCount by remember { mutableStateOf(1) }
    var currentCycle by remember { mutableStateOf(1) }
    var isResting by remember { mutableStateOf(false) }
    var timerRunning by remember { mutableStateOf(false) }
    var isWorking by remember { mutableStateOf(false) }
    var isSettings by remember { mutableStateOf(false) }

    // Estado del temporizador
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
        Text(text = "Ciclo: $currentCycle / $cycleCount", modifier = Modifier.padding(bottom = 16.dp))
        Text(text = if (isResting) "Descanso: $theCounter s" else "Tiempo: $theCounter s", modifier = modifier)

        TimerAdjustmentControls(
            countdownTime = countdownTime,
            restTime = restTime,
            cycleCount = cycleCount,
            onCountdownChange = { countdownTime = it },
            onRestChange = { restTime = it },
            onCycleChange = { cycleCount = it }
        )

        TimerControlButtons(
            isResting = isResting,
            timerRunning = timerRunning,
            miConterDown = miConterDown,
            theCounter = theCounter,
            countdownTime = countdownTime,
            restTime = restTime,
            cycleCount = cycleCount,
            currentCycle = currentCycle,
            onCounterUpdate = { newCounterDown -> miConterDown = newCounterDown },
            onCurrentCycleUpdate = { currentCycle = it },
            onRestingUpdate = { isResting = it },
            onTimerRunningUpdate = { timerRunning = it },
            onCounterValueUpdate = { theCounter = it }
        )
    }
}

// Función para manejar los controles de ajuste de tiempo
@Composable
fun TimerAdjustmentControls(
    countdownTime: Int,
    restTime: Int,
    cycleCount: Int,
    onCountdownChange: (Int) -> Unit,
    onRestChange: (Int) -> Unit,
    onCycleChange: (Int) -> Unit
) {
    Column {
        // Controles para ajustar el tiempo de trabajo
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { if (countdownTime > 1) onCountdownChange(countdownTime - 1) }) {
                Text(text = "-1s")
            }
            Text(text = "Tiempo: $countdownTime s", modifier = Modifier.padding(horizontal = 8.dp))
            Button(onClick = { onCountdownChange(countdownTime + 1) }) {
                Text(text = "+1s")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controles para ajustar el tiempo de descanso
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { if (restTime > 1) onRestChange(restTime - 1) }) {
                Text(text = "-1s")
            }
            Text(text = "Descanso: $restTime s", modifier = Modifier.padding(horizontal = 8.dp))
            Button(onClick = { onRestChange(restTime + 1) }) {
                Text(text = "+1s")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controles para ajustar el número de ciclos
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { if (cycleCount > 1) onCycleChange(cycleCount - 1) }) {
                Text(text = "-1 ciclo")
            }
            Text(text = "Ciclos: $cycleCount", modifier = Modifier.padding(horizontal = 8.dp))
            Button(onClick = { onCycleChange(cycleCount + 1) }) {
                Text(text = "+1 ciclo")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Función para manejar los botones de control del temporizador
@Composable
fun TimerControlButtons(
    isResting: Boolean,
    timerRunning: Boolean,
    miConterDown: CounterDown,
    theCounter: Long,
    countdownTime: Int,
    restTime: Int,
    cycleCount: Int,
    currentCycle: Int,
    onCounterUpdate: (CounterDown) -> Unit,
    onCurrentCycleUpdate: (Int) -> Unit,
    onRestingUpdate: (Boolean) -> Unit,
    onTimerRunningUpdate: (Boolean) -> Unit,
    onCounterValueUpdate: (Long) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        // Botón para pausar/reanudar el temporizador
        Button(onClick = {
            if (!timerRunning) {
                startWorkCycle(
                    countdownTime = countdownTime,
                    restTime = restTime,
                    currentCycle = currentCycle,
                    totalCycles = cycleCount,
                    onCounterUpdate = onCounterUpdate,
                    onRestingUpdate = onRestingUpdate,
                    onCurrentCycleUpdate = onCurrentCycleUpdate,
                    onCounterValueUpdate = onCounterValueUpdate,
                    onTimerRunningUpdate = onTimerRunningUpdate
                )
            } else {
                miConterDown.toggle()
            }
        }) {
            Text(text = if (isResting) "Pausar Descanso" else "Pulsar")
        }

        // Botón para reiniciar el temporizador y manejar el ciclo completo
        Button(onClick = {
            miConterDown.cancel()
            onCounterValueUpdate(0L)
            onRestingUpdate(false)
            onCurrentCycleUpdate(1)
            onTimerRunningUpdate(false)
        }) {
            Text(text = "Reset")
        }
    }
}

// Función para iniciar el ciclo de trabajo y descanso
fun startWorkCycle(
    countdownTime: Int,
    restTime: Int,
    currentCycle: Int,
    totalCycles: Int,
    onCounterUpdate: (CounterDown) -> Unit,
    onRestingUpdate: (Boolean) -> Unit,
    onCurrentCycleUpdate: (Int) -> Unit,
    onCounterValueUpdate: (Long) -> Unit,
    onTimerRunningUpdate: (Boolean) -> Unit
) {
    onTimerRunningUpdate(true)

    val workCounter = CounterDown(countdownTime) { workValue ->
        onCounterValueUpdate(workValue)
        if (workValue == 0L) {
            onRestingUpdate(true)
            val restCounter = CounterDown(restTime) { restValue ->
                onCounterValueUpdate(restValue)
                if (restValue == 0L) {
                    onRestingUpdate(false)
                    val nextCycle = currentCycle + 1
                    if (nextCycle <= totalCycles) {
                        onCurrentCycleUpdate(nextCycle)
                        // Solo después de descansar, comienza el siguiente ciclo de trabajo
                        startWorkCycle(
                            countdownTime,
                            restTime,
                            nextCycle,
                            totalCycles,
                            onCounterUpdate,
                            onRestingUpdate,
                            onCurrentCycleUpdate,
                            onCounterValueUpdate,
                            onTimerRunningUpdate
                        )
                    } else {
                        onTimerRunningUpdate(false)
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HiitTimerTheme {
        Counter()
    }
}