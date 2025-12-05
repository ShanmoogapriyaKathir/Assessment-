package com.example.assessment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.assessment.ui.theme.AssessmentTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AssessmentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigator()
                }
            }
        }
    }
}

@Composable
fun AppNavigator() {
    var currentScreen by remember { mutableStateOf("login") }
    var userName by remember { mutableStateOf("") }

    when (currentScreen) {
        "login" -> LoginScreen(
            onLogin = { name ->
                userName = name
                currentScreen = "welcome"
            }
        )

        "welcome" -> WelcomeScreen(
            name = userName,
            onFinish = { currentScreen = "quiz" }
        )

        "quiz" -> QuizScreen(
            onAssessmentDone = { currentScreen = "done" }
        )

        "done" -> AssessmentDoneScreen()
    }
}

@Composable
fun LoginScreen(onLogin: (String) -> Unit) {
    var name by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(text = "Enter your name", color = Color.Black) },
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (name.text.isNotEmpty()) {
                    onLogin(name.text)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
        ) {
            Text("Login", color = Color.White, fontSize = 18.sp)
        }
    }
}

@Composable
fun WelcomeScreen(name: String, onFinish: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🎉 Welcome, $name 🎉",
            color = Color.Green,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

data class Question(
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

@Composable
fun QuizScreen(onAssessmentDone: () -> Unit) {
    val questions = remember { pythonQuestions() }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    var assessmentDone by remember { mutableStateOf(false) }
    val selectedAnswers = remember { mutableStateListOf<Int>().apply { repeat(questions.size) { add(-1) } } }

    when {
        assessmentDone -> AssessmentDoneScreen()
        showResult -> {
            val score = selectedAnswers.withIndex().count { (i, answer) ->
                answer == questions[i].correctAnswerIndex
            }
            ResultScreen(score = score, total = questions.size) {
                assessmentDone = true
                onAssessmentDone()
            }
        }
        else -> {
            val question = questions[currentQuestionIndex]
            val selectedOption = selectedAnswers[currentQuestionIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = question.questionText,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Blue
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        question.options.forEachIndexed { index, option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedAnswers[currentQuestionIndex] = index }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedOption == index,
                                    onClick = { selectedAnswers[currentQuestionIndex] = index },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color.Blue,
                                        unselectedColor = Color.Black
                                    )
                                )
                                Text(
                                    text = option,
                                    fontSize = 16.sp,
                                    color = if (selectedOption == index) Color.Blue else Color.Black,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { if (currentQuestionIndex > 0) currentQuestionIndex-- },
                        enabled = currentQuestionIndex > 0,
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(1.dp, Color.Blue),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Blue
                        )
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Blue)
                        Spacer(Modifier.width(6.dp))
                        Text("Previous", color = Color.Blue)
                    }

                    Button(
                        onClick = {
                            if (currentQuestionIndex < questions.lastIndex) {
                                currentQuestionIndex++
                            } else {
                                showResult = true
                            }
                        },
                        enabled = selectedOption != -1,
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                    ) {
                        Text(
                            text = if (currentQuestionIndex == questions.lastIndex) "Submit" else "Next",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResultScreen(score: Int, total: Int, onAssessmentDone: () -> Unit) {
    val context = LocalContext.current
    val emailAddresses = arrayOf("shanmoogapriyak06@gmail.com")
    val subject = "Quiz Result from Assessment App"

    var name by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    val mentor = "Shanmoogapriya K"
    var comment by remember { mutableStateOf("") }

    var hasLaunchedEmail by remember { mutableStateOf(false) }

    LaunchedEffect(hasLaunchedEmail) {
        if (hasLaunchedEmail) {
            delay(2000)
            onAssessmentDone()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF97DAF7))
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎉 Quiz Completed 🎉",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Your Score: $score / $total",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name", color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.Black)
        )
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = userEmail,
            onValueChange = { userEmail = it },
            label = { Text("Email ID", color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.Black)
        )
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = { Text("Mobile Number", color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.Black)
        )
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = college,
            onValueChange = { college = it },
            label = { Text("College Name", color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.Black)
        )
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = mentor,
            onValueChange = {},
            label = { Text("Mentor Name", color = Color.Black) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.Black)
        )
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Add your comments here", color = Color.Black) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            textStyle = LocalTextStyle.current.copy(color = Color.Black)
        )
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                if (name.isNotEmpty() && userEmail.isNotEmpty() && mobile.isNotEmpty() && college.isNotEmpty()) {
                    val body = """
                        🎓 Student Assessment Result 🎓
                        
                        --- Student Information ---
                        Name: $name
                        Email ID: $userEmail
                        Mobile No: $mobile
                        College Name: $college
                        Mentor Name: $mentor

                        --- Quiz Details ---
                        Score: $score / $total
                        
                        --- Comments ---
                        $comment
                        
                        Thank you!
                    """.trimIndent()

                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, emailAddresses)
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                        putExtra(Intent.EXTRA_TEXT, body)
                    }

                    context.startActivity(Intent.createChooser(intent, "Send Result via Email"))
                    hasLaunchedEmail = true
                }
            },
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Text("Send Result to Email", color = Color.White, fontSize = 18.sp)
        }
    }
}

@Composable
fun AssessmentDoneScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎉 Assessment Done 🎉", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Thank you for completing the assessment!",
            fontSize = 20.sp,
            color = Color.DarkGray,
            lineHeight = 28.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(30.dp))
        Text(
            text = "Your responses have been submitted successfully.",
            fontSize = 16.sp,
            color = Color.Green,
            textAlign = TextAlign.Center
        )
    }
}

fun pythonQuestions(): List<Question> {
    return listOf(
        Question(
            "Q1: What is Python?",
            listOf(
                "a) A type of snake",
                "b) A programming language",
                "c) A database",
                "d) A web server"
            ),
            1
        ),
        Question(
            "Q2: Which of these is a correct variable assignment in Python?",
            listOf(
                "a) x == 5",
                "b) x = 5",
                "c) 5 = x",
                "d) x := 5"
            ),
            1
        ),
        Question(
            "Q3: What is the correct way to start a Python function?",
            listOf(
                "a) function myFunc():",
                "b) def myFunc():",
                "c) func myFunc():",
                "d) function: myFunc()"
            ),
            1
        ),
        Question(
            "Q4: How do you write a comment in Python?",
            listOf(
                "a) // This is a comment",
                "b) <!-- This is a comment -->",
                "c) # This is a comment",
                "d) /* This is a comment */"
            ),
            2
        ),
        Question(
            "Q5: Which of these is a correct list in Python?",
            listOf(
                "a) [1, 2, 3]",
                "b) (1, 2, 3)",
                "c) {1, 2, 3}",
                "d) <1, 2, 3>"
            ),
            0
        ),
        Question(
            "Q6: How do you start a for loop in Python?",
            listOf(
                "a) for i in range(5):",
                "b) for (i = 0; i < 5; i++):",
                "c) loop i in range(5):",
                "d) foreach i in range(5):"
            ),
            0
        ),
        Question(
            "Q7: Which keyword is used for conditional statements in Python?",
            listOf(
                "a) if",
                "b) switch",
                "c) select",
                "d) case"
            ),
            0
        ),
        Question(
            "Q8: How do you import a module in Python?",
            listOf(
                "a) include math",
                "b) import math",
                "c) using math",
                "d) require math"
            ),
            1
        ),
        Question(
            "Q9: What does the len() function do?",
            listOf(
                "a) Returns the length of an object",
                "b) Converts a number to a string",
                "c) Adds elements to a list",
                "d) Deletes an object"
            ),
            0
        ),
        Question(
            "Q10: Which of these data types is mutable in Python?",
            listOf(
                "a) List",
                "b) Tuple",
                "c) String",
                "d) Integer"
            ),
            0
        )
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewLogin() {
    AssessmentTheme {
        AppNavigator()
    }
}
