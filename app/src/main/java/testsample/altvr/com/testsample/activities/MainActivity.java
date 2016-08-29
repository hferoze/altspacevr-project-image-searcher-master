package testsample.altvr.com.testsample.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import testsample.altvr.com.testsample.R;
import testsample.altvr.com.testsample.fragments.MainFragment;
import testsample.altvr.com.testsample.util.LogUtil;
import testsample.altvr.com.testsample.util.Utils;

public class MainActivity extends AppCompatActivity {
    private LogUtil log = new LogUtil(MainActivity.class);

    public static Fragment sCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_searcher_activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayFragment(R.string.toolbar_main_title);
        new CreateInternalFolder().execute();
    }

    private void displayFragment(int title) {
        setTitle(title);
        Fragment f = getSupportFragmentManager().findFragmentByTag(getString(R.string.main_view_pager_fragment_tag));
        if (f == null) {
            log.d("Adding fragment");
            try {
                if (!isFinishing()) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, MainFragment.getInstance(), getString(R.string.main_view_pager_fragment_tag))
                            .commit();
                }
            } catch (IllegalStateException ignored) {
                ignored.printStackTrace();
            }
            sCurrentFragment = MainFragment.getInstance();
        }
    }

    private class CreateInternalFolder extends AsyncTask<Void,Void,Boolean>{
        @Override
        protected Boolean doInBackground(Void... params) {
            return Utils.createPrivateDirs(MainActivity.this);
        }
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result) log.d("Folders created successfully");
            else log.e("Failed to create folders");
        }
    }
}
