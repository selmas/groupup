package ch.epfl.sweng.groupup.activity.event.files;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import ch.epfl.sweng.groupup.R;

/**
 * FullScreenFile class.
 * Put the image given in extra in full screen, a simple tap on screen makes the user go back
 * to file management.
 */
public class FullScreenFile extends AppCompatActivity {

    //TODO a little zoom-in / zoom-out method could be nice.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_file);

        Intent intent = getIntent();
        byte[] data = intent.getByteArrayExtra(FileManagementActivity.FILE_EXTRA_NAME);
        Bitmap fileToShow = BitmapFactory.decodeByteArray(data, 0, data.length);

        final int eventIndex = intent.getIntExtra(FileManagementActivity.EVENT_INDEX, -1);

        findViewById(R.id.container)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent back = new Intent(getApplicationContext(), FileManagementActivity.class);
                        back.putExtra(FileManagementActivity.EVENT_INDEX, eventIndex);
                        startActivity(back);
                    }
                });

        ((ImageView)findViewById(R.id.image_to_display))
                .setImageBitmap(fileToShow);
    }
}