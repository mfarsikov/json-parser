package parser

import io.kotlintest.shouldBe
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class ParserTest {
    @Test
    fun parseMap() {
        @Language("JSON")
        val json = """
        {
            "fieldName": "accountName",
            "fieldValue": "Facebook"
        }
        """
        val parsedMap = Parser().parseMap(
            json
        )
        parsedMap["fieldName"] shouldBe JsonNode.Stringg("accountName")
        parsedMap["fieldValue"] shouldBe JsonNode.Stringg("Facebook")
    }

    @Test
    fun parseStep() {
        @Language("JSON")
        val json = """
        {
          "processor": "AddField",
          "configuration": {
            "fieldName": "accountName",
            "fieldValue": "Facebook"
          }
        }
        """

        val step = Parser().parseMap(json)

        step["processor"] shouldBe JsonNode.Stringg("AddField")
        step["configuration"] shouldBe JsonNode.Mapp(
            mapOf(
                "fieldName" to JsonNode.Stringg("accountName"),
                "fieldValue" to JsonNode.Stringg("Facebook")
            )
        )
    }

    fun parseDescriptor(){
        @Language("JSON")
        val json = """
            {
              "steps": [
                {
                  "processor": "AddField",
                  "configuration": {
                    "fieldName": "accountName",
                    "fieldValue": "Facebook"
                  }
                },
                {
                  "processor": "RemoveField",
                  "configuration": {
                    "fieldName": "userName"
                  }
                },
                {
                  "processor": "countNumberOfFields",
                  "configuration": {
                    "targetFieldName": "numOfFields"
                  }
                }
              ]
            }
        """.trimIndent()
        val parsed = Parser().parseMap(json)
        with((parsed["steps"] as JsonNode.Listt).value){
            size shouldBe 3
            (first() as JsonNode.Mapp).value["processor"] shouldBe "AddField"
        }

        val pipeline = PipileneMapper().map(parsed)

        pipeline.steps.size shouldBe 3

        pipeline.steps.first().processor shouldBe "AddField"

    }

    @Test
    fun mapStep() {
        val jsonNode: Map<String, JsonNode> = mapOf(
            "processor" to JsonNode.Stringg("AddField"),
            "configuration" to JsonNode.Mapp(
                mapOf(
                    "fieldName" to JsonNode.Stringg("accountName"),
                    "fieldValue" to JsonNode.Stringg("Facebook")

                )
            )
        )


        val step = StepMapper().map(jsonNode)

        step.processor shouldBe "AddField"
        step.configuration shouldBe mapOf(
            "fieldName" to "accountName",
            "fieldValue" to "Facebook"
        )
    }

}