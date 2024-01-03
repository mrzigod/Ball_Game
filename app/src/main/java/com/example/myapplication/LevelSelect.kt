package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

class LevelSelect : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var intent = Intent(this, GameplayActivity::class.java)
        val extras = getIntent().extras
        val name= extras?.getString("name")
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Cyan
                ) {
                    if (name != null) {
                        Levels(intent, this, name)
                    }
                }
            }
        }
    }
}

@Composable
fun Levels(intent:Intent, context:Context,name: String) {
    Column(modifier = Modifier.wrapContentSize(Alignment.Center)) {
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            val txt="Level Select"
            Text(
                text = txt
            )
        }
        for(i in 1..8)
            LevelButton( intent,  context, i,name)
        ExitButton(context)
    }
}


@Composable
fun LevelButton(intent:Intent, context:Context, level:Int, name:String)
{
    Row{
        Button(
            onClick = {
                intent.putExtra("Level", level);
                intent.putExtra("name",name)
                intent.putExtra("multi",false)
                intent.putExtra("host",true)
                context.startActivity(intent)
            },
        )
        {
            Text(text= "level $level")
        }
    }
}


@Composable
fun ExitButton(context: Context)
{
    Row{
        Button(
            onClick = {
                (context as Activity).finish()
            },
        )
        {
            Text(text="Go back")
        }
    }
}