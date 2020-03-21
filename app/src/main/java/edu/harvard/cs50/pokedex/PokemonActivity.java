package edu.harvard.cs50.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;

public class PokemonActivity extends AppCompatActivity {
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private String url;
    private RequestQueue requestQueue;
    private String pokemonName;
    private ImageView pokemonImageView;
    private String spriteURL;
    private TextView descriptionTextView;
    private String urlDESC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        pokemonImageView = findViewById(R.id.pokemon_sprite);
        descriptionTextView = findViewById(R.id.pokemon_description);


        load();
    }

    public void load() {
        type1TextView.setText("");
        type2TextView.setText("");
        descriptionTextView.setText("");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    nameTextView.setText(response.getString("name"));
                    pokemonName = (String) nameTextView.getText();
                    numberTextView.setText(String.format("#%03d", response.getInt("id")));
                    JSONObject spritePictures = response.getJSONObject("sprites");
                    spriteURL = spritePictures.getString("front_default");
                    Log.d("cs50", spriteURL);


                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");

                        if (slot == 1) {
                            type1TextView.setText(type);
                        }
                        else if (slot == 2) {
                            type2TextView.setText(type);
                        }
                    }

                    JSONObject descriptionURL = response.getJSONObject("species");
                    urlDESC = descriptionURL.getString("url");

                    Log.d("cs50", urlDESC);

                    DownloadSpriteTask xyz = new DownloadSpriteTask();
                    xyz.execute(spriteURL);

                    JsonObjectRequest requestDesc = new JsonObjectRequest(Request.Method.GET, urlDESC, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray flavorEntries = response.getJSONArray("flavor_text_entries");
                                for (int i = 0; i < flavorEntries.length(); i++) {
                                    JSONObject flavorEntry = flavorEntries.getJSONObject(i);
                                    String flavorLang = flavorEntry.getJSONObject("language").getString("name");
                                    String flavorText = flavorEntry.getString("flavor_text");
                                    if (flavorLang.equals("en")) {
                                        descriptionTextView.setText(flavorText);
                                        break;
                                    }
                                }

                            } catch (JSONException e) {
                                Log.e("cs50", "Pokemon json error", e);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("cs50", "Pokemon details error");
                        }
                    });

                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }

                JsonObjectRequest requestDesc = new JsonObjectRequest(Request.Method.GET, urlDESC, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray flavorEntries = response.getJSONArray("flavor_text_entries");
                            for (int i = 0; i < flavorEntries.length(); i++) {
                                JSONObject flavorEntry = flavorEntries.getJSONObject(i);
                                String flavorLang = flavorEntry.getJSONObject("language").getString("name");
                                String flavorText = flavorEntry.getString("flavor_text");
                                if (flavorLang.equals("en")) {
                                    descriptionTextView.setText(flavorText);
                                    break;
                                }
                            }

                        } catch (JSONException e) {
                            Log.e("cs50", "Pokemon json error", e);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("cs50", "Pokemon details error");
                    }
                });

                requestQueue.add(requestDesc);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error");
            }
        });

        /*JsonObjectRequest requestDesc = new JsonObjectRequest(Request.Method.GET, urlDESC, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray flavorEntries = response.getJSONArray("flavor_text_entries");
                    for (int i = 0; i < flavorEntries.length(); i++) {
                        JSONObject flavorEntry = flavorEntries.getJSONObject(i);
                        String flavorLang = flavorEntry.getJSONObject("language").getString("name");
                        String flavorText = flavorEntry.getString("flavor_text");
                        if (flavorLang.equals("en")) {
                            descriptionTextView.setText(flavorText);
                            break;
                        }
                    }

                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error");
            }
        });*/

        //requestQueue.add(requestDesc);
        requestQueue.add(request);
    }

    public void toggleCatch(View view) {

        Log.d("cs50", pokemonName);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(pokemonName, (String) ((TextView)findViewById(R.id.button)).getText());
        editor.commit();

        String statusOfPokemon = sharedPref.getString(pokemonName, "Catch");

        if (statusOfPokemon.equals("Catch")) {
            ((TextView)findViewById(R.id.button)).setText("Caught");
            editor.putString(pokemonName, (String) ((TextView)findViewById(R.id.button)).getText());
            editor.commit();
        } else {
            ((TextView)findViewById(R.id.button)).setText("Catch");
            editor.putString(pokemonName, (String) ((TextView)findViewById(R.id.button)).getText());
            editor.commit();
        }
    }

    public class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            }
            catch (IOException e) {
                Log.e("cs50", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            pokemonImageView.setImageBitmap(bitmap);
        }
    }

}
