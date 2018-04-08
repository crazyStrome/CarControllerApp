package crazystrome.carcontroller;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ControlActivity extends Activity {

    private String address;
    private int port;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.control_layout);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        address = bundle.getString("addr");
        port = bundle.getInt("port");

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
                    socket.write(ByteBuffer.wrap(Protocol.START.getBytes(utf))).get();

                    System.out.println("from server: " + utf.decode(buffer));
                    buffer.clear();

                } catch (TimeoutException e) {
                    e.printStackTrace();
                    System.out.println("Server might not opened...");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Server might be wrong...");
                }
            }
        }).start();
    }
}
