package at.rw.udpphonecall;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import lib.AudioCall;

public class CallActivity extends ActionBarActivity {

    private TextView callerName;
    private TextView time;
    private ImageView acceptCall;
    private ImageView declineCall;
    private AudioCall call;
    private String ip;
    private String caller;
    private static final int LISTENER_PORT = 50003;
    private static final int BUF_SIZE = 1024;
    private boolean IN_CALL = false;
    private int timeElapsed = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Timer timer = new Timer();
        setContentView(R.layout.activity_call);
        ImageView endCall = (ImageView) findViewById(R.id.declineCall);
        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call.endCall();
                finish();
                timer.cancel();
            }
        });
        this.callerName = (TextView)findViewById(R.id.callerName);
        this.time = (TextView)findViewById(R.id.time);
        this.acceptCall = (ImageView)findViewById(R.id.acceptCall);
        this.declineCall = (ImageView)findViewById(R.id.declineCall);
        ip = this.getIntent().getExtras().getString("address");
        caller = this.getIntent().getExtras().getString("name");
        callerName.setText(caller);
        final Handler updateTime = new Handler();
        acceptCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IN_CALL = true;
                try {
                    call = new AudioCall(InetAddress.getByName(ip));
                    call.startCall();
                    acceptCall.setVisibility(View.INVISIBLE);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        updateTime.post(new Runnable() {
                            @Override
                            public void run() {
                                timeElapsed++;
                                time.setText((timeElapsed/60)+":"+timeElapsed%60);
                            }
                        });
                    }
                },0,1000);
            }
        });
    }

}
