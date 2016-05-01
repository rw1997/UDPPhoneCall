package at.rw.udpphonecall;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActionBarActivity{


    private EditText ip;
    private Button callBtn;
    private ListView contacts;
    private boolean STARTED = false;
    private boolean IN_CALL = false;
    private boolean LISTEN = false;
    static final String LOG_TAG = "UDPchat";
    private static final int LISTENER_PORT = 50003;
    private static final int BUF_SIZE = 1024;
    private Map<String,String> nameMap = new HashMap<>();
    private ArrayList<String> names = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.ip = (EditText)findViewById(R.id.call_IP);
        this.callBtn = (Button)findViewById(R.id.callBtn);
        this.contacts = (ListView)findViewById(R.id.contacts);
        this.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCall(ip.getText().toString());
            }
        });
        this.contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String contact =(String) (contacts.getItemAtPosition(position));
                names.add(contact);
                startCall(nameMap.get(contact));
            }
        });
        startCallListener();
    }

    private void startCall(String ip){
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra("ip",ip);
        Log.e(LOG_TAG,"calling: "+ip);
        startActivity(intent);
    }
    private void startCallListener() {
        // Creates the listener thread
        LISTEN = true;
        Thread listener = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    // Set up the socket and packet to receive
                    Log.i(LOG_TAG, "Incoming call listener started");
                    DatagramSocket socket = new DatagramSocket(LISTENER_PORT);
                    socket.setSoTimeout(1000);
                    byte[] buffer = new byte[BUF_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, BUF_SIZE);
                    while(LISTEN) {
                        // Listen for incoming call requests
                        try {
                            Log.i(LOG_TAG, "Listening for incoming calls");
                            socket.receive(packet);
                            String data = new String(buffer, 0, packet.getLength());
                            Log.i(LOG_TAG, "Packet received from "+ packet.getAddress() +" with contents: " + data);
                            String action = data.substring(0, 4);
                            if(action.equals("CAL:")) {
                                // Received a call request. Start the ReceiveCallActivity
                                String address = packet.getAddress().toString();
                                String name = data.substring(4, packet.getLength());
                                nameMap.put(name,address);
                                Intent intent = new Intent(MainActivity.this, CallActivity.class);
                                intent.putExtra("address",address);
                                intent.putExtra("name",name);
                                IN_CALL = true;
                                //LISTEN = false;
                                //stopCallListener();
                                startActivity(intent);
                            }
                            else {
                                // Received an invalid request
                                Log.w(LOG_TAG, packet.getAddress() + " sent invalid message: " + data);
                            }
                        }
                        catch(Exception e) {}
                    }
                    Log.i(LOG_TAG, "Call Listener ending");
                    socket.disconnect();
                    socket.close();
                }
                catch(SocketException e) {

                    Log.e(LOG_TAG, "SocketException in listener " + e);
                }
            }
        });
        listener.start();
    }
}
