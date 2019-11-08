package parser

data class PipelineDescriptor(
    val steps: List<Step>
)

data class Step(
    val processor: String,
    val configuration: Map<String, String>
)

enum class State {
    BEFORE_FIELD,
    FIELD,
    FIELD_COMPLETED,
    BEFORE_VALUE,
    BEFORE_OBJECT,
    VALUE,
    OBJECT,
    ARRAY
}

class Parser {
    fun parseMap(json: String): Map<String, JsonNode> {
        var state = State.BEFORE_FIELD
        var fieldName = ""
        var fieldValue = ""
        var jsonObject = ""

        val map = mutableMapOf<String, JsonNode>()
        for (char in json) {
            when {
                char == '"' && state == State.BEFORE_FIELD -> state = State.FIELD
                char == '"' && state == State.FIELD -> state = State.FIELD_COMPLETED
                char == ':' && state == State.FIELD_COMPLETED -> state = State.BEFORE_VALUE
                char == '"' && state == State.BEFORE_VALUE -> state = State.VALUE
                char == '{' && state == State.BEFORE_VALUE -> state = State.OBJECT
                char == '}' && state == State.OBJECT -> {
                    val m = parseMap(jsonObject)
                    map[fieldName] = JsonNode.Mapp(m)
                    fieldName = ""
                    fieldValue = ""
                    jsonObject = ""
                    state = State.BEFORE_FIELD
                }
                char == '[' && state == State.BEFORE_VALUE -> state = State.ARRAY
                char == ']' && state == State.ARRAY -> {
                    map[fieldName] = JsonNode.Listt(parseArray(jsonObject))
                }

                char == '"' && state == State.VALUE -> {
                    map[fieldName] = JsonNode.Stringg(fieldValue)
                    fieldName = ""
                    fieldValue = ""
                    jsonObject = ""
                    state = State.BEFORE_FIELD
                }
                state == State.FIELD -> fieldName += char
                state == State.VALUE -> fieldValue += char
                state == State.OBJECT -> jsonObject += char
                state == State.ARRAY -> jsonObject += char
            }
        }
        return map
    }

    fun parseArray(json: String): List<Map<String, JsonNode>> {
        var state = State.BEFORE_OBJECT
        var objectJson = ""
        var deep = 0
        val result = mutableListOf<Map<String, JsonNode>>()
        for (char in json) {
            when {
                char == '{' && state == State.BEFORE_OBJECT -> state = State.OBJECT
                char == '{' && state == State.OBJECT -> deep += 1
                char == '}' && state == State.OBJECT && deep == 0 -> result += parseMap(objectJson)
                char == '}' && state == State.OBJECT -> deep -= 1
                state == State.OBJECT -> objectJson += char
            }
        }
        return result
    }

}

sealed class JsonNode {
    data class Stringg(val value: String) : JsonNode()
    data class Mapp(val value: Map<String, JsonNode>) : JsonNode()
    data class Listt(val value: List<Map<String, JsonNode>>): JsonNode()
}

class StepMapper {
    fun map(json: Map<String, JsonNode>): Step {
        return Step(
            processor = (json["processor"] as JsonNode.Stringg).value,
            configuration = (json["configuration"] as JsonNode.Mapp).value
                .map { (key, value) -> key to (value as JsonNode.Stringg).value }
                .toMap()
        )
    }
}

class PipileneMapper {
    fun map(json: Map<String, JsonNode>): PipelineDescriptor {
        return PipelineDescriptor(
            steps = (json["steps"] as JsonNode.Listt).value.map { StepMapper().map(it) }
        )
    }
}