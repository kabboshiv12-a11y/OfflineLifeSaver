package com.example.offlinelifesaver

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*

class MainActivity : AppCompatActivity() {

    private val SERVICE_ID = "com.example.offlinelifesaver.mesh"
    private val TAG = "LifeSaverMesh"
    private var connectedEndpointId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI Buttons (Assume these exist in your layout)
        val btnStart = findViewById<Button>(R.id.btnStartNetwork)
        val btnSos = findViewById<Button>(R.id.btnSendSOS)

        btnStart.setOnClickListener {
            startAdvertising()
            startDiscovery()
        }

        btnSos.setOnClickListener {
            sendEmergencyMessage("🚨 SOS: I need help here!")
        }
    }

    // 📡 Step 1: Broadcast presence to nearby users
    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        
        Nearby.getConnectionsClient(this)
            .startAdvertising(
                "User_" + (1000..9999).random(), // Unique username
                SERVICE_ID,
                connectionLifecycleCallback,
                advertisingOptions
            )
            .addOnSuccessListener { Log.d(TAG, "Advertising started...") }
            .addOnFailureListener { e -> Log.e(TAG, "Advertising failed", e) }
    }

    // 📡 Step 2: Search for nearby users
    private fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()

        Nearby.getConnectionsClient(this)
            .startDiscovery(
                SERVICE_ID,
                endpointDiscoveryCallback,
                discoveryOptions
            )
            .addOnSuccessListener { Log.d(TAG, "Discovery started...") }
            .addOnFailureListener { e -> Log.e(TAG, "Discovery failed", e) }
    }

    // 🤝 Step 3: Handle Connection Requests
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Automatically accept connection for emergency app
            Nearby.getConnectionsClient(this@MainActivity)
                .acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                connectedEndpointId = endpointId
                Toast.makeText(this@MainActivity, "Connected to a user!", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onDisconnected(endpointId: String) {
            connectedEndpointId = null
            Log.d(TAG, "Disconnected from $endpointId")
        }
    }

    // 🔍 Step 4: When a user is found, request connection
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Nearby.getConnectionsClient(this@MainActivity)
                .requestConnection("MyDevice", endpointId, connectionLifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {}
    }

    // 📩 Step 5: Receive Messages (Chat / SOS)
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val message = String(payload.asBytes()!!)
                Log.d(TAG, "Message Received: $message")
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                
                // Add logic here: If message contains "SOS", trigger Alarm sound 🔊
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    // 🚨 Step 6: Send Message (SOS or Chat)
    private fun sendEmergencyMessage(text: String) {
        connectedEndpointId?.let { endpointId ->
            val bytesPayload = Payload.fromBytes(text.toByteArray())
            Nearby.getConnectionsClient(this).sendPayload(endpointId, bytesPayload)
        } ?: run {
            Toast.makeText(this, "No offline users connected yet", Toast.LENGTH_SHORT).show()
        }
    }
}

