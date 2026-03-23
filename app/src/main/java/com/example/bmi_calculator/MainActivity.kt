package com.example.bmi_calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val viewModel: BmiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                BmiScreen(viewModel)
            }
        }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = Color(0xFF818CF8),
        secondary = Color(0xFF34D399),
        surface = Color(0xFF1E293B),
        background = Color(0xFF0F172A)
    )
    MaterialTheme(colorScheme = colorScheme, content = content)
}

data class RobotParticle(
    val xProgress: Float,
    val yProgress: Float,
    val sizeProgress: Float,
    val alpha: Float,
    val rotationSpeed: Float,
    val xSpeed: Float,
    val ySpeed: Float
)

@Composable
fun AnimatedBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    // Use Icons.Default.Adb for the full "Bugdroid" robot body
    val robotPainter = rememberVectorPainter(Icons.Default.Adb)
    
    // Drive animation with a time value
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(80000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Larger glowing blobs in the background
    val blobOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob"
    )

    val robots = remember {
        List(12) {
            RobotParticle(
                xProgress = Random.nextFloat(),
                yProgress = Random.nextFloat(),
                sizeProgress = Random.nextFloat(),
                alpha = Random.nextFloat() * 0.3f + 0.2f, // Increased visibility (20% to 50%)
                rotationSpeed = (Random.nextFloat() - 0.5f) * 120f,
                xSpeed = (Random.nextFloat() - 0.5f) * 100f,
                ySpeed = (Random.nextFloat() - 0.5f) * 100f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawRect(color = Color(0xFF0F172A))

        // Large Background Glowing Blobs
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF4F46E5).copy(alpha = 0.15f), Color.Transparent),
                center = Offset(
                    width * 0.5f + (width * 0.3f * kotlin.math.cos(blobOffset)),
                    height * 0.5f + (height * 0.3f * kotlin.math.sin(blobOffset))
                ),
                radius = width * 1.2f
            ),
            radius = width * 1.2f
        )

        // Draw and animate Robots
        robots.forEach { robot ->
            // Current position calculation with wrapping
            var x = (width * robot.xProgress + robot.xSpeed * time) % width
            var y = (height * robot.yProgress + robot.ySpeed * time) % height
            
            // Fix negative modulo for wrapping
            if (x < 0) x += width
            if (y < 0) y += height
            
            val robotSize = 45.dp.toPx() + (55.dp.toPx() * robot.sizeProgress)
            val rotation = robot.rotationSpeed * time

            translate(x, y) {
                rotate(rotation, pivot = Offset(robotSize / 2, robotSize / 2)) {
                    with(robotPainter) {
                        draw(
                            size = Size(robotSize, robotSize),
                            alpha = robot.alpha,
                            colorFilter = ColorFilter.tint(Color(0xFF3DDC84)) // Full color tint, alpha handled by draw
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BmiScreen(viewModel: BmiViewModel) {
    val bmiState = viewModel.bmiState
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 32.dp)
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                SegmentedButton(
                    selected = viewModel.unitSystem == UnitSystem.METRIC,
                    onClick = { viewModel.toggleUnitSystem(UnitSystem.METRIC) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text(stringResource(R.string.unit_metric))
                }
                SegmentedButton(
                    selected = viewModel.unitSystem == UnitSystem.IMPERIAL,
                    onClick = { viewModel.toggleUnitSystem(UnitSystem.IMPERIAL) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text(stringResource(R.string.unit_imperial))
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    val weightHint = if (viewModel.unitSystem == UnitSystem.METRIC) 
                        R.string.weight_hint_metric else R.string.weight_hint_imperial
                    val heightHint = if (viewModel.unitSystem == UnitSystem.METRIC) 
                        R.string.height_hint_metric else R.string.height_hint_imperial

                    BmiInputField(
                        value = viewModel.weightInput,
                        onValueChange = { viewModel.onWeightChange(it) },
                        label = stringResource(R.string.weight_label),
                        suffix = stringResource(weightHint)
                    )

                    BmiInputField(
                        value = viewModel.heightInput,
                        onValueChange = { viewModel.onHeightChange(it) },
                        label = stringResource(R.string.height_label),
                        suffix = stringResource(heightHint)
                    )

                    Button(
                        onClick = { viewModel.calculateBmi() },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        Text(
                            stringResource(R.string.calculate_bmi).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(
                targetState = bmiState,
                transitionSpec = {
                    (fadeIn(tween(500)) + slideInVertically { it / 2 }).togetherWith(fadeOut(tween(500)))
                },
                label = "result"
            ) { state ->
                when (state) {
                    is BmiViewModel.BmiState.Success -> ResultSection(state)
                    is BmiViewModel.BmiState.InvalidInput -> {
                        Text(
                            stringResource(R.string.invalid_input),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> Unit
                }
            }
        }
    }
}

@Composable
fun BmiInputField(value: String, onValueChange: (String) -> Unit, label: String, suffix: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        suffix = { Text(suffix, fontWeight = FontWeight.Bold) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF818CF8),
            unfocusedBorderColor = Color(0xFF475569)
        )
    )
}

@Composable
fun ResultSection(state: BmiViewModel.BmiState.Success) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
            BmiGauge(percent = state.percent, color = colorResource(state.colorResId))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.bmi_result_format, state.bmi),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "BMI",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = colorResource(state.colorResId).copy(alpha = 0.2f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, colorResource(state.colorResId))
        ) {
            Text(
                text = stringResource(state.categoryResId).uppercase(),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = colorResource(state.colorResId)
            )
        }
    }
}

@Composable
fun BmiGauge(percent: Float, color: Color) {
    val animatedPercent by animateFloatAsState(
        targetValue = percent,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "gauge"
    )

    Canvas(modifier = Modifier.size(240.dp).padding(16.dp)) {
        val strokeWidth = 16.dp.toPx()
        
        drawArc(
            color = Color(0xFF1E293B),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        drawArc(
            color = color,
            startAngle = 135f,
            sweepAngle = 270f * animatedPercent,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
