package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class SavedTimesSelect: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Cyan
                ) {
                    SavedLevels(this)
                }
            }
        }
    }
}
@Composable
fun SavedLevels(context: Context) {
    Column(modifier = Modifier.wrapContentSize(Alignment.Center)) {
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            val txt="Saved Times"
            Text(
                text = txt
            )
        }
        for(i in 1..7)
            LevelTimesButton(context, i)
        ExitButton(context)
    }
}


@Composable
fun LevelTimesButton(context: Context, level: Int)
{
    var txt by remember { mutableStateOf("level $level") }
    val basetxt="level $level"
    var extendedtext="level $level\n"
    var clicked by remember{ mutableStateOf(false)}
    var downloaded=false
    val appDb=AppDatabase.getInstance(context)
    var st by remember{ mutableStateOf(emptyList<SavedTime>()) }
    Column(modifier = Modifier.clickable {
        if(!downloaded)
        {
            st=appDb.userDao().loadLevelTimes(level)
            for(i in st)
            {
                extendedtext+="Name: "+i.player+" Level: "+i.level+" "+ timerString( i.time)+"\n"
            }
            downloaded=true
        }
        if(!clicked) {
            txt = extendedtext
            clicked=true
        }
        else
        {
            txt=basetxt
            clicked=false
        }
    }
    ){
        Text(txt)
        AnimatedVisibility(clicked) {
            DeleteAllTimesButton(appDb = appDb, st = st)
        }
    }
    Spacer(modifier = Modifier.height(4.dp))

}

@Composable
fun DeleteAllTimesButton(appDb: AppDatabase, st: List<SavedTime>)
{
    Row()
    {

        Button(
            onClick = {
                deleteAllTimes(appDb,st)
            },
            colors = ButtonDefaults.buttonColors(Color.Red)

        )

        {
            Text(text="Delete all times for this level")
        }
    }
}

fun deleteAllTimes(appDb: AppDatabase, st: List<SavedTime>)
{
    for(i in st)
    {
        appDb.userDao().delete(i)
    }
}