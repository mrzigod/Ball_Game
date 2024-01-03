package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.myapplication.ui.theme.MyApplicationTheme

class OptionsScreen: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Cyan
                ) {
                    Column(modifier = Modifier.wrapContentSize(Alignment.Center)) {
                        standardThemeButton()
                        alternateThemeButton()
                        ExitButton()
                    }
                }
            }
        }
    }
    @Composable
    fun ExitButton()
    {
        Row{
            Button(
                onClick = {
                    finish()
                },
            )
            {
                Text(text="Go back")
            }
        }
    }
}

@Composable
fun standardThemeButton()
{
    Row{
        Button(onClick = {
            ThemeHolder.setData(true)
        },)
        {
            Text(text= "Standard Theme")
        }
    }
}

@Composable
fun alternateThemeButton()
{
    Row{
        Button(onClick = {
            ThemeHolder.setData(false)
        },)
        {
            Text(text= "Alternate Theme")
        }
    }
}




