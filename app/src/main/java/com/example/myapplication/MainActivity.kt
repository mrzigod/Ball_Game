package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    val name= mutableStateOf("Player")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHolder.setData(true);
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Cyan
                ) {
                    Column(modifier = Modifier.wrapContentSize(Alignment.Center)) {
                        Text(text = "Ball Race",
                            Modifier
                                .scale(4F)
                                .padding(horizontal = 25.dp, vertical = 20.dp), Color.Blue,);
                        Image(painter=painterResource(R.drawable.ball),"logo",Modifier.padding(horizontal = 25.dp));
                        SimpleFilledTextFieldSample(::onQueryChanged , name)
                        LevelSelectButton(context = this@MainActivity,name.value)
                        OptionsButton(context = this@MainActivity)
                        SavedTimesButton(context = this@MainActivity)
                        BluetoothScreenButton(context = this@MainActivity,name.value)
                    }
                }
            }
        }
    }
    fun onQueryChanged(query: String){
        this.name.value = query
    }

}

@Composable
fun LevelSelectButton(context: Context,name: String) {
    Row {
        val intent = Intent(context, LevelSelect::class.java)
        intent.putExtra("name",name)
        Button(
            onClick = {
                context.startActivity(intent)
            },
        )
        {
            Text(text = "Level Select")
        }
    }
}


@Composable
fun OptionsButton(context: Context) {
    Row {
        val intent = Intent(context, OptionsScreen::class.java)
        Button(
            onClick = {
                context.startActivity(intent)
            },
        )
        {
            Text(text = "Options")
        }
    }
}



@Composable
fun SavedTimesButton(context: Context) {
    Row {
        val intent = Intent(context, SavedTimesSelect::class.java)
        Button(
            onClick = {
                context.startActivity(intent)
            },
        )
        {
            Text(text = "Saved Times")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleFilledTextFieldSample(name1: (String) -> Unit, name: MutableState<String>) {
    var text =name.value

    TextField(
        value = text,
        onValueChange = { name1(it) },
        label = { Text("Name") },
        singleLine=true,
        modifier=Modifier.width(120.dp)
    )
}

@Composable
fun BluetoothScreenButton(context: Context,name: String) {
    Row {
        val intent = Intent(context, BluetoothScreen::class.java)
        intent.putExtra("name",name)
        Button(
            onClick = {
                context.startActivity(intent)
            },
        )
        {
            Text(text = "Bluetooth")
        }
    }
}