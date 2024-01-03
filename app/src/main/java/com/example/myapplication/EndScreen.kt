package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlin.math.roundToInt

class EndScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = intent.extras
        val framecount = extras!!.getInt("framecount")
        val level = extras!!.getInt("level")
        val name = extras!!.getString("name")
        setContent {
            MyApplicationTheme {
                    // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.wrapContentSize(Alignment.Center)) {

                        Text(
                            text = "Level Finished!"
                        )
                        if(name!=null)
                            Text(
                                text = name
                            )
                        Text(
                            text = timerString(framecount)
                        )
                        if(level!=8)
                            name?.let { SaveTimeButton(it,level,framecount) };
                        ExitButton(this@EndScreen);
                    }
                }
            }
        }
    }




    @Composable
    fun SaveTimeButton(name: String,level:Int, framecount: Int)
    {
        Row{
            Button(
                onClick = {
                    val appDb=AppDatabase.getInstance(this@EndScreen);
                    val st=SavedTime(name,level,framecount)
                    appDb.userDao().insert(st);
                },
            )
            {
                Text(text="Save Time")
            }
        }
    }
}

fun timerString(framecount: Int): String
{
    val miliseconds:Double= (framecount*30).toDouble();
    val seconds: Double=(miliseconds/1000);
    var minutes=0;
    if (seconds>60) minutes= (seconds/60).toInt();
    return "time: $minutes:$seconds"
}

