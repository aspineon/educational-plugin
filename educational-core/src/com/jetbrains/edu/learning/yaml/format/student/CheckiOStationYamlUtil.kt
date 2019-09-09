package com.jetbrains.edu.learning.yaml.format.student

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder
import com.jetbrains.edu.learning.checkio.courseFormat.CheckiOStation
import com.jetbrains.edu.learning.yaml.format.LessonBuilder
import com.jetbrains.edu.learning.yaml.format.LessonYamlMixin
import com.jetbrains.edu.learning.yaml.format.NotImplementedInMixin

private const val TYPE_PROPERTY_NAME = "type"

@JsonDeserialize(builder = StationBuilder::class)
@JsonPropertyOrder(TYPE_PROPERTY_NAME, LessonYamlMixin.CUSTOM_NAME, LessonYamlMixin.CONTENT)
class CheckiOStationYamlMixin : LessonYamlMixin() {
  @JsonProperty(TYPE_PROPERTY_NAME)
  private fun getItemType(): String {
    throw NotImplementedInMixin()
  }
}

@JsonPOJOBuilder(withPrefix = "")
private class StationBuilder(@JsonProperty(LessonYamlMixin.CONTENT) content: List<String?>) : LessonBuilder(content) {
  override fun createLesson() = CheckiOStation()
}