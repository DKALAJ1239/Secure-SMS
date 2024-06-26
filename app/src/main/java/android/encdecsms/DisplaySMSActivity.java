package android.encdecsms;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;

import android.util.Log;

public class DisplaySMSActivity extends Activity
{
    EditText secretKey;
    TextView senderNum;
    TextView encryptedMsg;
    TextView decryptedMsg;
    Button submit;
    Button cancel;
    String originNum = "";
    String msgContent = "";

    @Override
    public void onStop()
    {
        super.onStop();
        EncDecSMSActivity.intent = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Toast.makeText(DisplaySMSActivity.this, "SMS Received", Toast.LENGTH_LONG).show();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.onreceive);

        senderNum = (TextView) findViewById(R.id.senderNum);
        encryptedMsg = (TextView) findViewById(R.id.encryptedMsg);
        decryptedMsg = (TextView) findViewById(R.id.decryptedMsg);
        secretKey = (EditText) findViewById(R.id.secretKey);
        submit = (Button) findViewById(R.id.submit);
        cancel = (Button) findViewById(R.id.back);
        Bundle extras = getIntent().getExtras();

        if (extras != null)
        {
            // get the sender phone number from extra
            originNum = extras.getString("originNum");

            // get the encrypted message body from extra
            msgContent = extras.getString("msgContent");

            // set the text fields in the UI
            senderNum.setText(originNum);
            encryptedMsg.setText(msgContent);

            Log.d("success", "Message Displayed!\nNum: " + senderNum + "\nContent: " + encryptedMsg);
        }
        else
        {
            // if the Intent is null, there should be something wrong
            Toast.makeText(getBaseContext(), "Error Occurs!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // when click on the cancel button, return
        cancel.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                finish();

                Intent in = new Intent(DisplaySMSActivity.this, EncDecSMSActivity.class);
                in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                in.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                // start the DisplaySMSActivity.java
                startActivity(in);
            }
        });

        // when click on the submit button decrypt the message body
        submit.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // user input the AES secret key
                String secretKeyString = secretKey.getText().toString();

                // key length should be 16 characters as defined by AES-128-bit
                if (secretKeyString.length() > 0 && secretKeyString.length() == 16)
                {
                    try
                    {
                        // convert the encrypted String message body to a byte
                        // array
                        byte[] msg = hex2byte(msgContent.getBytes());

                        // decrypt the byte array
                        byte[] result = decryptSMS(secretKey.getText().toString(), msg);

                        // set the text view for the decrypted message
                        decryptedMsg.setText(new String(result));
                    }
                    catch (Exception e)
                    {
                        // in the case of message corrupted or invalid key
                        // decryption cannot be carried out
                        decryptedMsg.setText("Message Cannot Be Decrypted!");
                    }
                }
                else
                    Toast.makeText(getBaseContext(),
                            "You must provide a 16-character secret key!",
                            Toast.LENGTH_SHORT).show();
            }
        });
    }

    // utility function: convert hex array to byte array
    public static byte[] hex2byte(byte[] b)
    {
        if ((b.length % 2) != 0)
            throw new IllegalArgumentException("hello");

        byte[] b2 = new byte[b.length / 2];

        for (int n = 0; n < b.length; n += 2)
        {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }

    // decryption function
    public static byte[] decryptSMS(String secretKeyString, byte[] encryptedMsg) throws Exception
    {
        // generate AES secret key from the user input secret key
        Key key = generateKey(secretKeyString);

        // get the cipher algorithm for AES
        Cipher c = Cipher.getInstance("AES");

        // specify the decryption mode
        c.init(Cipher.DECRYPT_MODE, key);

        // decrypt the message
        byte[] decValue = c.doFinal(encryptedMsg);
        return decValue;
    }

    private static Key generateKey(String secretKeyString) throws Exception
    {
        // generate AES secret key from a String
        Key key = new SecretKeySpec(secretKeyString.getBytes(), "AES");
        return key;
    }
}
