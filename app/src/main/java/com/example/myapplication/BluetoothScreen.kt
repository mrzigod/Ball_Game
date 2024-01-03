package com.example.myapplication

import android.Manifest.permission.BLUETOOTH
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.util.UUID


class BluetoothScreen: ComponentActivity() {

    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var takePermission:ActivityResultLauncher<String>
    lateinit var takeResultLauncher: ActivityResultLauncher<Intent>

    lateinit var BTdevice: BluetoothDevice
    var MY_UUID = UUID.fromString("9c848f56-1986-46bb-98b2-13e94e21f62c")
    lateinit var handler: Handler

    private val sentLevel= mutableStateOf(1)
    private val playedLevel= mutableStateOf(1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = getIntent().extras
        val name= extras?.getString("name")
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter= bluetoothManager.adapter
        takePermission=registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if(it)
            {
                val intent=Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                takeResultLauncher.launch(intent);
            }
        }
        takeResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback { result ->
                if(result.resultCode== RESULT_OK){
                    Toast.makeText(applicationContext,"Bluetooth ON",Toast.LENGTH_SHORT).show()
                    this.recreate()
                }
            }
        )
        var bluetoothOn=false;
        if (bluetoothAdapter.isEnabled){
            bluetoothOn=true;
        }


        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(message: Message) {
                if(message.what==1) {
                    val appDb = AppDatabase.getInstance(this@BluetoothScreen)
                    val st = SavedTime.getSavedTimeFromString(message.obj as String?)
                    appDb.userDao().insert(st)
                }
                if(message.what==2){
                    val level=message.obj as Int
                    val intentt = Intent(this@BluetoothScreen, GameplayActivity::class.java)
                    intentt.putExtra("Level", level);
                    intentt.putExtra("name", name)
                    intentt.putExtra("multi",true)
                    intentt.putExtra("host",false)
                    this@BluetoothScreen.startActivity(intentt);
                }
            }
        }

        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Cyan
                ) {
                    Column(modifier = Modifier.wrapContentSize(Alignment.Center)) {
                        if(bluetoothOn)
                            BluetoothStuff()
                        else
                            NoBluetoothStuff()
                        BluetoothButton()
                        AnimatedVisibility(bluetoothOn) {
                            Column() {
                                DisplayPairedDevicesButton()
                                StartBTConnectionButton()
                                SimpleDropdownMenu(::onSentLevelChanged , sentLevel,false)
                                SendSomethingButton(sentLevel)

                                SimpleDropdownMenu(::onPlayedLevelChanged, playedLevel, true)
                                StartMultiplayerButton(this@BluetoothScreen , playedLevel,name!!)

                            }
                        }
                        ExitButton(context = this@BluetoothScreen)
                    }
                }
            }
        }

    }

    @Composable
    fun BluetoothButton(){
        Button(onClick = {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                takePermission.launch(BLUETOOTH_CONNECT)
            }
            else
                takePermission.launch(BLUETOOTH)
        }) {
            Text("Enable Bluetooth")
        }
    }


    @Composable
    fun DisplayPairedDevicesButton(){
        val data= StringBuffer()
        val pairedDevices:Set<BluetoothDevice>
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pairedDevices = if (ActivityCompat.checkSelfPermission(
                    this,
                    BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                bluetoothAdapter.bondedDevices
            } else {
                bluetoothAdapter.bondedDevices
            }
        }
        else {
            pairedDevices = if (ActivityCompat.checkSelfPermission(
                    this,
                    BLUETOOTH
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                bluetoothAdapter.bondedDevices
            } else {
                bluetoothAdapter.bondedDevices
            }
        }

        Text(text = "Paired Devices - click on one")
        for (device in pairedDevices)
        {
            Button(onClick = {
                BTdevice=device
                BCSHolder.startBCS(this,handler)
            }) {
                Text(text=device.name)
            }
        }
    }

    fun startBTConnection(device: BluetoothDevice,uuid: UUID){
            BCSHolder.getData().startClient(device,uuid)

    }


    @Composable
    fun StartBTConnectionButton() {
        Button(onClick = {
            if (this::BTdevice.isInitialized)
                startBTConnection(BTdevice,MY_UUID)
            else{
                Toast.makeText(applicationContext,"Didn't choose a device",Toast.LENGTH_SHORT).show()
            }
        }) {

            Text(text = "Start Connection")
        }
    }

    @Composable
    fun SendSomethingButton(level: MutableState<Int>){
        Button(onClick = {
            if(BCSHolder.isStarted()) {
                val appDb = AppDatabase.getInstance(this@BluetoothScreen)
                var st = appDb.userDao().loadLevelTimes(level.value)
                if (st.isEmpty()) {
                    Toast.makeText(
                        applicationContext,
                        "No saved times for that level",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val test = "1\n"+SavedTime.getStringFromSavedTime(st[0])
                    write(test.toByteArray())
                }
            }
            else{
                Toast.makeText(applicationContext, "Didn't choose a device", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Share your best time from chosen level")
        }
    }

    fun write(byteArray: ByteArray){
        if(BCSHolder.getData().isConnected)
            BCSHolder.getData().write(byteArray)
        else
            Toast.makeText(applicationContext, "Not connected", Toast.LENGTH_SHORT).show()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SimpleDropdownMenu(name1: (Int) -> Unit, name: MutableState<Int>,haseight: Boolean) {
        var expanded by remember { mutableStateOf(false) }
        var selectedLevel by remember { mutableStateOf(1) }
        ExposedDropdownMenuBox(expanded = false, onExpandedChange = {expanded=!expanded}) {
            TextField(value = selectedLevel.toString(), onValueChange ={},label={Text("Level")}, readOnly = true, trailingIcon = {ExposedDropdownMenuDefaults.TrailingIcon(
                expanded = expanded
            )},modifier = Modifier.menuAnchor() )
            ExposedDropdownMenu(expanded=expanded, onDismissRequest = {expanded=false}) {
                for (i in 1..7)
                    DropdownMenuItem(
                        text = { Text(i.toString()) },
                        onClick = {
                            selectedLevel=i
                            name1(i)
                            expanded=false
                        }
                    )
                if(haseight)
                {
                    DropdownMenuItem(
                        text = { Text(8.toString()) },
                        onClick = {
                            selectedLevel=8
                            name1(8)
                            expanded=false
                        }
                    )
                }
            }
        }
    }

    private fun onSentLevelChanged(int: Int){
        this.sentLevel.value = int
    }

    private fun onPlayedLevelChanged(int: Int){
        this.playedLevel.value=int;
    }

    @Composable
    fun StartMultiplayerButton(context: Context, level:MutableState<Int>,name:String){
        Button(onClick = {
            if(BCSHolder.isStarted()) {
                if (BCSHolder.getData().isConnected){
                    System.out.println("Clicked on start multiplayer button")
                    val intentt = Intent(this, GameplayActivity::class.java)
                    intentt.putExtra("Level", level.value);
                    intentt.putExtra("name", name)
                    intentt.putExtra("multi",true)
                    intentt.putExtra("host",true)
                    val sstring="2\n"+level.value.toString()
                    write(sstring.toByteArray())
                    context.startActivity(intentt);
                }
                else
                    Toast.makeText(applicationContext, "Not connected", Toast.LENGTH_SHORT).show()

            }
            else{
                Toast.makeText(applicationContext, "Didn't choose a device", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Play chosen level via Bluetooth")
        }
    }


}




@Composable
fun BluetoothStuff() {
    Text("Bluetooth on")
}

@Composable
fun NoBluetoothStuff(){
    Text("Bluetooth off")
}
