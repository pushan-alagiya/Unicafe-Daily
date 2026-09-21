package fi.pushan.unicafedaily

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import fi.pushan.unicafedaily.domain.model.RestaurantStatus
import fi.pushan.unicafedaily.ui.components.StatusBadge
import fi.pushan.unicafedaily.ui.theme.UniCafeDailyTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun status_badge_screenshot() {
        composeTestRule.setContent {
            UniCafeDailyTheme {
                Box(modifier = Modifier.padding(16.dp)) {
                    StatusBadge(
                        status = RestaurantStatus(
                            isOpenNow = true,
                            displayText = "OPEN • Lunch until 14:00",
                            shortStatus = "OPEN",
                            hoursDescription = "Lunch 11:00 – 14:00"
                        )
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/status_badge.png")
    }
}
