package fr.shiningcat.binclockwidget.domain

import fr.shiningcat.binclockwidget.domain.model.WeatherCondition
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class WeatherGlyphMapperTest {
  @Test fun `code 0 is clear`() { assertEquals(WeatherCondition.CLEAR, WeatherGlyphMapper.toCondition(0)) }
  @Test fun `1 and 2 are partly cloudy`() {
    assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherGlyphMapper.toCondition(1))
    assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherGlyphMapper.toCondition(2))
  }
  @Test fun `3 overcast, 45 fog, 61 rain, 71 snow, 80 showers, 95 thunder`() {
    assertEquals(WeatherCondition.OVERCAST, WeatherGlyphMapper.toCondition(3))
    assertEquals(WeatherCondition.FOG, WeatherGlyphMapper.toCondition(45))
    assertEquals(WeatherCondition.RAIN, WeatherGlyphMapper.toCondition(61))
    assertEquals(WeatherCondition.SNOW, WeatherGlyphMapper.toCondition(71))
    assertEquals(WeatherCondition.RAIN_SHOWERS, WeatherGlyphMapper.toCondition(80))
    assertEquals(WeatherCondition.THUNDERSTORM, WeatherGlyphMapper.toCondition(95))
  }
  @Test fun `drizzle range 51 to 57 boundaries`() {
    assertEquals(WeatherCondition.DRIZZLE, WeatherGlyphMapper.toCondition(51))
    assertEquals(WeatherCondition.DRIZZLE, WeatherGlyphMapper.toCondition(57))
  }
  @Test fun `range upper boundaries 67 rain, 77 snow, 82 rain showers, 99 thunder`() {
    assertEquals(WeatherCondition.RAIN, WeatherGlyphMapper.toCondition(67))
    assertEquals(WeatherCondition.SNOW, WeatherGlyphMapper.toCondition(77))
    assertEquals(WeatherCondition.RAIN_SHOWERS, WeatherGlyphMapper.toCondition(82))
    assertEquals(WeatherCondition.THUNDERSTORM, WeatherGlyphMapper.toCondition(99))
  }
  @Test fun `48 is fog and 85, 86 are snow showers`() {
    assertEquals(WeatherCondition.FOG, WeatherGlyphMapper.toCondition(48))
    assertEquals(WeatherCondition.SNOW_SHOWERS, WeatherGlyphMapper.toCondition(85))
    assertEquals(WeatherCondition.SNOW_SHOWERS, WeatherGlyphMapper.toCondition(86))
  }
  @Test fun `unmapped code is UNKNOWN`() {
    assertEquals(WeatherCondition.UNKNOWN, WeatherGlyphMapper.toCondition(999))
    assertEquals(WeatherCondition.UNKNOWN, WeatherGlyphMapper.toCondition(58))
  }
}
