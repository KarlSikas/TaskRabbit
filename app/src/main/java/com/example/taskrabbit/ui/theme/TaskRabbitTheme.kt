import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import com.example.taskrabbit.viewmodel.SettingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import com.example.taskrabbit.ui.theme.BackgroundChoice
import com.example.taskrabbit.ui.theme.Typography // Import Typography from your Typography.kt


private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun TaskRabbitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Get the SettingsViewModel *outside* the MaterialTheme
    val settingsViewModel: SettingsViewModel = viewModel()
    val themeSettings by settingsViewModel.themeSettings.collectAsState()
    val backgroundChoice = themeSettings.backgroundChoice

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Use the imported Typography object
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(getBackgroundColor(backgroundChoice))
        ) {
            content()
        }
    }
}

@Composable
fun getBackgroundColor(backgroundChoice: BackgroundChoice): Color {
    return when (backgroundChoice) {
        BackgroundChoice.WHITE -> Color.White
        BackgroundChoice.BUTTERFLY -> Color.LightGray
        BackgroundChoice.COLORFUL -> Color.Cyan
        BackgroundChoice.CUTE -> Color.Green
        BackgroundChoice.FLOWERS -> Color.Yellow
        BackgroundChoice.RAINBOW -> Color.Red
        BackgroundChoice.SHOOTING_STAR -> Color.Magenta
        BackgroundChoice.SKELETON_HEAD -> Color.Gray
        else -> Color.Black // Add an 'else' branch
    }
}