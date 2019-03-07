package pl.noritoshi_scarlett.pathflytha.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import pl.noritoshi_scarlett.pathflytha.fragments_calculate.SettingsFragment;
import pl.noritoshi_scarlett.pathflytha.fragments_main.ChooseFragment;
import pl.noritoshi_scarlett.pathflytha.fragments_main.InfoFragment;
import pl.noritoshi_scarlett.pathflytha.fragments_main.ObstaclesListFragment;
import pl.noritoshi_scarlett.pathflytha.fragments_main.PatchesFragment;
import pl.noritoshi_scarlett.pathflytha.R;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    @Override
    protected void attachBaseContext(Context newBase) {
        // FONT -> Ustawienie domyślnej czcionki w aplikacji
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.main_bar_title, R.string.main_bar_title);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView = findViewById(R.id.navigationView);
        mNavigationView.setNavigationItemSelectedListener(this);

        //wybranie pierwszego elementu
        onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.navigation_item_geting_data));

        // Zmiana nazwy aktywnego elementu zgodnie z fragmentem zbieranym ze stosu
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment f = getFragmentManager().findFragmentById(R.id.content_frame);
                if (f != null) {
                    updateSubtitleAndSelectedDrawer(f);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // ustawinie subtytulu dla pierwszego elementu, o ile faktycznie jest zaznaczony
        if (mToolbar != null && mNavigationView.getMenu() != null) {
            if (mNavigationView.getMenu().findItem(R.id.navigation_item_geting_data).isChecked()) {
                mToolbar.setSubtitle(R.string.main_drawer_menu_geting_data);
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        // zaznaczenie aktywnego na drawerze
        menuItem.setChecked(true);
        // zmiana podtytulu
        if (mToolbar != null) {
            mToolbar.setSubtitle(menuItem.getTitle());
        }
        String fragmentTag;
        switch (menuItem.getItemId()) {
            case R.id.navigation_item_geting_data:
                fragmentTag = ChooseFragment.class.getSimpleName();
                if (! findFragment(fragmentTag)) {
                    replaceFragment(new ChooseFragment(), fragmentTag);
                }
                break;
            case R.id.navigation_item_patches:
                fragmentTag = PatchesFragment.class.getSimpleName();
                if (! findFragment(fragmentTag)) {
                    replaceFragment(new PatchesFragment(), fragmentTag);
                }
                break;
            case R.id.navigation_item_obstacles:
                fragmentTag = ObstaclesListFragment.class.getSimpleName();
                if (! findFragment(fragmentTag)) {
                    replaceFragment(new ObstaclesListFragment(), fragmentTag);
                }
                break;
            case R.id.navigation_item_settings:
                fragmentTag = SettingsFragment.class.getSimpleName();
                if (! findFragment(fragmentTag)) {
                    replaceFragment(new SettingsFragment(), fragmentTag);
                }
                break;
            case R.id.navigation_item_info:
                fragmentTag = InfoFragment.class.getSimpleName();
                if (! findFragment(fragmentTag)) {
                    replaceFragment(new InfoFragment(), fragmentTag);
                }
                break;
        }
        // zamknięcie NavigationDravera
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    /**
     * kliknięcie na przycisk cofania
     */
    @Override
    public void onBackPressed() {
        // zamykanie Drawera
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        // wyświetlenie informacji o braku pozostałych fragmentow na stosie
        if (getFragmentManager().getBackStackEntryCount() == 1) {
            Toast toast = Toast.makeText(this, getResources().getString(R.string.touchForClose), Toast.LENGTH_LONG);
            View view = toast.getView();
            view.setBackgroundColor(getResources().getColor(R.color.transparentBackgroundBaseBlueLight));
            view.setPaddingRelative(15, 10, 10, 15);
            toast.show();
        }
        super.onBackPressed();
    }

    /**
     * Szukanie fragmentu na stosie
     * @param fragmentTag nazwa fragmentu
     * @return czyZnaleziono?
     */
    private boolean findFragment(String fragmentTag){
        FragmentManager manager = getFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(fragmentTag, 0);
        return (fragmentPopped || manager.findFragmentByTag(fragmentTag) != null);
    }

    /**
     * podmiana widocznego fragmentu oraz koloru jego tła
     * @param fragment fragment który ma być pokazany
     * @param fragmentTag nazwa fragmentu, który ma być pokazany
     */
    private void replaceFragment(Fragment fragment, String fragmentTag) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.content_frame, fragment, fragmentTag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(fragmentTag);
        ft.commit();
    }

    /**
     * zmiana nazwy podtytulu
     * zmiana zaznacznego elementu w NavigationDraver
     */
    private void updateSubtitleAndSelectedDrawer(Fragment fragment) {
        String currentSubtitle = "";
        int currentPosition = R.id.navigation_item_geting_data;
        // zobacz jaki fragment jest aktywny i na tej podstawie zaznacz
        if (fragment.getClass().getName().equals(ChooseFragment.class.getName())) {
            currentPosition = R.id.navigation_item_geting_data;
            currentSubtitle = getResources().getString(R.string.main_drawer_menu_geting_data);
        }
        else if (fragment.getClass().getName().equals(PatchesFragment.class.getName())) {
            currentPosition = R.id.navigation_item_patches;
            currentSubtitle = getResources().getString(R.string.main_drawer_menu_patches);
        }
        else if (fragment.getClass().getName().equals(ObstaclesListFragment.class.getName())) {
            currentPosition = R.id.navigation_item_obstacles;
            currentSubtitle = getResources().getString(R.string.main_drawer_menu_obstacles);
        }
        else if (fragment.getClass().getName().equals(SettingsFragment.class.getName())) {
            currentPosition = R.id.navigation_item_settings;
            currentSubtitle = getResources().getString(R.string.main_drawer_menu_settings);
        }
        else if (fragment.getClass().getName().equals(InfoFragment.class.getName())) {
            currentPosition = R.id.navigation_item_info;
            currentSubtitle = getResources().getString(R.string.main_drawer_menu_info);
        }
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            if (mToolbar != null) {
                mToolbar.setSubtitle("");
            }
            mNavigationView.getMenu().findItem(currentPosition).setChecked(true);
            mNavigationView.getMenu().findItem(currentPosition).setChecked(false);
            return;
        }
        if (mToolbar != null) {
            mToolbar.setSubtitle(currentSubtitle);
        }
        mNavigationView.getMenu().findItem(currentPosition).setChecked(true);
    }

//    /**
//     * zmiana wielkosci ekranu ze zwyklego na fullscreen lub odwrotnie
//     * @param item klikniety przycisk
//     */
//    private void changeScreenSize(MenuItem item) {
//        // animated icon
//        View v = mToolbar.findViewById(item.getItemId());
//        ObjectAnimator anim = ObjectAnimator.ofFloat(v, "rotation",
//                rotationAngleForShowHideIcon, rotationAngleForShowHideIcon + 180);
//        anim.setDuration(500).start();
//        rotationAngleForShowHideIcon += 180;
//        rotationAngleForShowHideIcon = rotationAngleForShowHideIcon % 360;
//        // fullscreen and visible viewPager
//        WindowManager.LayoutParams attrs = getWindow().getAttributes();
//        if (isFuulscreen) {
//            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
//        } else {
//            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
//        }
//        isFuulscreen = ! isFuulscreen;
//        getWindow().setAttributes(attrs);
//    }
}
