package calculator

import java.math.BigInteger

class Calculator {
    private val action = mapOf("/help" to ::help, "/exit" to ::exit)
    private val variables = mutableMapOf<String, BigInteger>()
    private var exit = true

    fun start() {
        while (exit) {
            val input = readln()

            when  {
                input == "" -> continue
                Regex("""/.*""").matches(input) -> checkCommand(input)
                Regex(""".+=.+""").matches(input) -> checkVariable(input)
                Regex("""\s*[+-]?(\d+|\p{Alpha}+)\s*""").matches(input) -> showVariable(input)
                Regex(""".*([+-/*]+.*)+""").matches(input) -> checkOperation(input)
            }
        }
        println("Bye!")
    }



    private fun checkCommand(input: String) {
        val command = Regex("""/(exit|help)""")
        if (command.matches(input))  action[input]!!.invoke() else println("Unknown command")
    }

    private fun help() = println("The program calculates the sum or diff of numbers")

    private fun exit() {
        exit = false
    }



    private fun checkVariable(input: String) {
        val variableLeftPart = Regex("""\s*\p{Alpha}+\s*=.*""")
        val variableRightPart = Regex(""".*=\s*(\p{Alpha}+|-?\d+)\s*""")
        val wholeAssignment = Regex("""\s*\p{Alpha}+\s*=\s*(\p{Alpha}+|-?\d+)\s*""")

        val rightValue = input.split("=").last().trim()
        when {
            !variableLeftPart.matches(input) -> println("Invalid identifier")
            !variableRightPart.matches(input) || !wholeAssignment.matches(input) -> println("Invalid assignment")
            Regex("""\p{Alpha}+""").matches(rightValue) -> checkVariableExistence(rightValue, input)
            else -> assignVariable(input)
        }
    }

    private fun checkVariableExistence(variable: String, input: String) {
        if (variables.keys.contains(variable)) assignVariable(input, true) else println("Unknown variable")
    }

    private fun assignVariable(input: String, otherVariable: Boolean = false) {
        val (leftValue, rightValue) =  input.split("=").map { it.trim() }
        variables[leftValue] = if (otherVariable) variables[rightValue]!! else rightValue.toBigInteger()
    }

    private fun showVariable(input: String) {
        val integer = Regex("""[+-]?\d+""")
        val variable = Regex("""\p{Alpha}+""")
        when {
            integer.matches(input) -> println(input.toInt())
            !variable.matches(input.trim()) -> println("Invalid identifier")
            !variables.keys.contains(input.trim()) -> println("Unknown variable")
            else -> println(variables[input.trim()])
        }
    }



    private fun checkOperation(input: String) {
        val operation = Regex("""\s*\(*[-+]?(\d+|\p{Alpha}+)(\s*([/*]|[+-]+)\s*\(*(\d+|\p{Alpha}+)\)*)*""")
        val goodNumberOfParentheses = input.filter { it == '(' || it == ')'}.length % 2 == 0
        if (operation.matches(input) && goodNumberOfParentheses) transformInfixToPostfix(input) else println("Invalid expression")
    }

    private fun transformInfixToPostfix(expression: String) {
        val myExpression = formatExpression(expression)

        val operators = ArrayDeque<String>()
        val result = mutableListOf<String>()


        for (element in myExpression) {
            when {
                Regex("""\d+|\p{Alpha}+""").matches(element) -> result.add(element)
                operators.isEmpty() || element == "(" -> operators.addFirst(element )
                element == ")" -> {
                    while (operators.isNotEmpty() && operators.first() != "(") result.add(operators.removeFirst())
                    if (operators.isNotEmpty()) operators.removeFirst()
                }
                Regex("""[+-]+""").matches(element) -> {
                    while (operators.isNotEmpty() && operators.first() != "(" ) result.add(operators.removeFirst())
                    operators.addFirst(element)
                }
                else -> operators.addFirst(element)
            }
        }
        result.addAll(operators)
//        println(result.joinToString(" "))
        calculate(result.joinToString(" "))
    }

    private fun formatExpression(input: String): List<String> {
        var myExpression = input.replace(" ", "")
        myExpression = Regex("""[+\-*/]+""").replace(myExpression) {" ${it.value} "}
        myExpression = Regex("""\(""").replace(myExpression) {"${it.value} "}
        myExpression = Regex("""\)""").replace(myExpression) {" ${it.value}"}
        return myExpression.split(" ").filter { it.isNotEmpty() }
    }

    private fun calculate(input: String) {
        val expression = input.split(" ")
        val result = ArrayDeque<BigInteger>()

        for (element in expression) {
            when {
                Regex("""\d+|\p{Alpha}+""").matches(element) -> result.addFirst(getValue(element))
                else -> {
                    val sign = checkSign(element)
                    val numbers = result.take(2).reversed()
                    repeat(2) {result.removeFirst()}
                    result.addFirst(performOperation(numbers, sign))
                }
            }
        }
        println(result.first())
    }

    private fun checkSign(sign: String): String {
        var newSign = sign
        while (newSign.length > 1) {
            when (newSign.take(2)) {
                "--", "++" -> newSign = "+" + newSign.drop(2)
                "-+", "+-" -> newSign = "-" + newSign.drop(2)
            }
        }
        return newSign
    }

    private fun getValue(value: String): BigInteger {
        return variables[value]?: value.toBigInteger()
    }

    private fun performOperation(numbers: List<BigInteger>, symbol: String): BigInteger {
        return when (symbol) {
            "+" -> numbers.first() + numbers.last()
            "-" -> numbers.first() - numbers.last()
            "/" -> numbers.first() / numbers.last()
            else -> numbers.first() * numbers.last()
        }
    }
}

fun main() {
    val myCalculator = Calculator()
    myCalculator.start()
}