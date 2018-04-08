package crazystrome.carcontroller;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private ScrollView mScrollView;
    private AutoCompleteTextView mInetAddress;
    private AutoCompleteTextView mInetPort;
    private Button mConnectButton;

    private String address = "";
    private int port;

    //消息传递用于UI更新
    private static final int SERVER_ERROR = 0;
    private static final int SERVER_SUCCEE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Handler  mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case SERVER_ERROR:
                        mProgressBar.setVisibility(View.GONE);
                        mScrollView.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "Server might not be opened, try again later...", Toast.LENGTH_SHORT).show();
                        break;
                    case SERVER_SUCCEE:
                        mProgressBar.setVisibility(View.GONE);
                        mScrollView.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }
        };

        mProgressBar = findViewById(R.id.connect_progress);
        mScrollView = findViewById(R.id.scrollView);
        mInetAddress = findViewById(R.id.inet_address);
        mInetPort = findViewById(R.id.inet_port);
        mConnectButton = findViewById(R.id.connect_button);

        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                address = mInetAddress.getText().toString();
                port = Integer.parseInt(mInetPort.getText().toString());
                mScrollView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try (
                                AsynchronousSocketChannel socket = AsynchronousSocketChannel.open()
                        ) {
                            socket.connect(new InetSocketAddress(address, port)).get(2, TimeUnit.SECONDS);
                            ByteBuffer buffer = ByteBuffer.allocate(256);
                            Charset utf = Charset.forName("utf-8");
                            socket.read(buffer).get();
                            buffer.flip();
                            System.out.println("from server: " + utf.decode(buffer));
                            buffer.clear();
                            socket.close();

                            //打开控制界面，同时把参数传过去
                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this, ControlActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("addr", address);
                            bundle.putInt("port", port);
                            intent.putExtras(bundle);
                            startActivity(intent);

                            mHandler.sendEmptyMessage(SERVER_SUCCEE);

                        } catch (TimeoutException e) {
                            e.printStackTrace();
                            System.out.println("Server might not opened...");
                            mHandler.sendEmptyMessage(SERVER_ERROR);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Server might be wrong...");
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
