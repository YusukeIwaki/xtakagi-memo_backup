package io.github.yusukeiwaki.xtakagi_memo_backup;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    private static final Uri URI = Uri.parse("content://com.xtakagi.provider.MemoPad/notes");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_backup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backup();
                Toast.makeText(MainActivity.this, "たぶんバックアップしました", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.btn_restore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restore();
                Toast.makeText(MainActivity.this, "たぶんレストアしました", Toast.LENGTH_SHORT).show();
            }
        });

        TextView textCaption = findViewById(R.id.caption);
        textCaption.setText(getOutputFile() + "にバックアップします。");
    }

    public void backup() {
        JSONArray notes = new JSONArray();
        try(Cursor c = getContentResolver().query(URI, null, null, null, null)) {
            while(c.moveToNext()) {
                JSONObject note = new JSONObject()
                        .put("id", c.getInt(c.getColumnIndex("_id")))
                        .put("title", c.getString(c.getColumnIndex("title")))
                        .put("note", c.getString(c.getColumnIndex("note")))
                        .put("created", c.getLong(c.getColumnIndex("created")))
                        .put("modified", c.getLong(c.getColumnIndex("modified")));

                Log.d("MainActivity", "note = " + note);

                notes.put(note);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        backup(notes);
    }

    private File getOutputFile() {
        return new File(getExternalFilesDir(null), "xtakagi-memo-backup.json");
    }

    private void backup(JSONArray notes) {
        try (FileOutputStream outputStream = new FileOutputStream(getOutputFile())) {
            outputStream.write(notes.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONArray restoreFromFile() {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(getOutputFile())))) {
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) break;

                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            return new JSONArray(sb.toString());
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    public void restore() {
        JSONArray notes = restoreFromFile();

        for (int i = 0; i < notes.length(); i++) {
            try {
                JSONObject note = notes.getJSONObject(i);

                ContentValues contentValues = new ContentValues();
                contentValues.put("_id", note.getInt("id"));
                contentValues.put("title", note.getString("title"));
                contentValues.put("note", note.getString("note"));
                contentValues.put("created", note.getLong("created"));
                contentValues.put("modified", note.getLong("modified"));
                getContentResolver().insert(URI, contentValues);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
