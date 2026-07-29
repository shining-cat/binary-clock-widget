package fr.shiningcat.binclockwidget.data

import fr.shiningcat.binclockwidget.data.weather.ForecastDto
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class ForecastDtoParseTest {
  private val json = Json { ignoreUnknownKeys = true }
  @Test fun `parses current and daily`() {
    val raw = """{"current":{"weather_code":3,"is_day":1},"daily":{"weather_code":[61,71]}}"""
    val dto = json.decodeFromString<ForecastDto>(raw)
    assertEquals(3, dto.current.weatherCode)
    assertEquals(1, dto.current.isDay)
    assertEquals(listOf(61, 71), dto.daily.weatherCode)
  }
}
