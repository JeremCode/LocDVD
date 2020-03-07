package com.example.locdvd;


import android.app.ProgressDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends AppCompatActivity implements
        ListDVDFragment.OnDVDSelectedListener {

    private static final String TAG_FRAGMENT_LISTDVD="FragementListDVD";

    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = (DrawerLayout) findViewById(R.id.main_Drawer);

        ListView listDrawer =
                (ListView) findViewById(R.id.main_DrawerList);
        String[] drawerItems =
                getResources().getStringArray(R.array.drawer_Items);
        listDrawer.setAdapter(new ArrayAdapter<String>(this,
                R.layout.listitem_drawer, drawerItems));

        listDrawer.setOnItemClickListener(
                new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View v, int pos,
                                            long id) {
                        if (pos == 0) {
                            Intent intent = new Intent(MainActivity.this,
                                    MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        if (pos == 1)
                            startActivity(new Intent(MainActivity.this,
                                    AddDVDActivity.class));

                        if (pos==2) {
                            SearchFragment searchFragment = new SearchFragment();
                            openDetailFragment(searchFragment);
                        }

                        drawerLayout.closeDrawer(Gravity.LEFT);
                    }

                });

        SharedPreferences sharedPreferences =
                getSharedPreferences("com.exemple.locDVD.prefs", Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("embeddedDataInserted", false))
            readEmbeddedData();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reinitialiser:
                // l’entrée Réinitialiser la base a été sélectionnée
                ensureReinitializeApp();
                return true;
            case R.id.menu_informations:
                // l’entrée Informations a été sélectionnée
                showInformations();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void ensureReinitializeApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirmer_reinitialisation_title);
        builder.setMessage(R.string.confirmer_reinitialisation_message);
        builder.setNegativeButton(R.string.non, null);
        builder.setPositiveButton(R.string.oui,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LocalSQLiteOpenHelper.deleteDatabase(MainActivity.this);
                        readEmbeddedData();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void showInformations(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.infos);
        builder.setPositiveButton(R.string.fermer, null);

        LayoutInflater inflater  = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_informations, null);
        TextView message =(TextView)view.findViewById(R.id.dialog_message);
        message.setText(R.string.informations_message);
        message.setMovementMethod(
                new android.text.method.ScrollingMovementMethod());

        builder.setView(view);

        builder.create().show();

    }


    @Override
    public void onResume() {
        super.onResume();
        ListDVDFragment listDVDFragment = new ListDVDFragment();
        openFragment(listDVDFragment,TAG_FRAGMENT_LISTDVD);
    }

    @Override
    public void onDVDSelected(long dvdId) {
        startViewDVDActivity(dvdId);
    }

    private void startViewDVDActivity(long dvdId) {
        ViewDVDFragment viewDVDFragment = new ViewDVDFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("dvdId", dvdId);
        viewDVDFragment.setArguments(bundle);
        openDetailFragment(viewDVDFragment);
    }

    private void openFragment(Fragment fragment , String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_placeHolder, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openDetailFragment(Fragment fragment) {
        FragmentManager fragmentManager =
                getSupportFragmentManager();

        FragmentTransaction transaction =
                fragmentManager.beginTransaction();

        if (findViewById(R.id.detail_placeHolder) == null)
            transaction.replace(R.id.main_placeHolder, fragment);
        else
            transaction.replace(R.id.detail_placeHolder, fragment);

        transaction.addToBackStack(null);

        transaction.commit();
    }


    private void readEmbeddedData() {
        AsyncReadEmbeddedData asyncReadEmbeddedData =
                new AsyncReadEmbeddedData();
        asyncReadEmbeddedData.execute("data.txt");
    }


    class AsyncReadEmbeddedData extends AsyncTask<String, Integer, Boolean> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute()  {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle(R.string.initialisation_de_la_base_de_donnees);
            progressDialog.setIndeterminate(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();

        }


        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = false;
            String dataFile = params[0];
            InputStreamReader reader = null;
            InputStream file=null;
            BufferedReader bufferedReader=null;
            try {
                int counter = 0;
                file = getAssets().open(dataFile);
                reader = new InputStreamReader(file);
                bufferedReader = new BufferedReader(reader);
                String line= null;
                while((line=bufferedReader.readLine())!=null) {
                    String [] data = line.split("\\|");
                    if(data!=null && data.length==4) {
                        DVD dvd = new DVD();
                        dvd.titre = data[0];
                        dvd.annee = Integer.decode(data[1]);
                        dvd.acteurs = data[2].split(",");
                        dvd.resume = data[3];
                        dvd.insert(MainActivity.this);
                        publishProgress(++counter);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(bufferedReader!=null) {
                    try {
                        bufferedReader.close();
                        reader.close();
                        SharedPreferences sharedPreferences =
                                getSharedPreferences("com.exemple.locDVD.prefs",
                                        Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor =
                                sharedPreferences.edit();
                        editor.putBoolean("embeddedDataInserted", true);
                        editor.commit();
                        result = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setMessage(
                    String.format(getString(R.string.x_dvd_inseres_dans_la_base),
                            values[0]));
        }

        @Override
        protected void onPostExecute(Boolean result)  {
            progressDialog.dismiss();
            FragmentManager fragmentManager =
                    getSupportFragmentManager();
            ListDVDFragment listDVDFragment =
                    (ListDVDFragment)fragmentManager
                            .findFragmentByTag(TAG_FRAGMENT_LISTDVD);
            if(listDVDFragment!=null)
                listDVDFragment.updateDVDList();
        }

    };


}