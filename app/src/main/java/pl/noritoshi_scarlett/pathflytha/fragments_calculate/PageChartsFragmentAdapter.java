package pl.noritoshi_scarlett.pathflytha.fragments_calculate;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import pl.noritoshi_scarlett.pathflytha.R;


public class PageChartsFragmentAdapter extends FragmentPagerAdapter {

    final private int PAGE_NUMBER = 3;

    private int[] tabText = {
            R.string.calc_chartGraph,
            R.string.calc_chartMapView,
            R.string.calc_chartHeight,
    };

    private Context context;
    private SparseArray<Fragment> fragments = new SparseArray<>();

    public interface InterfaceChartListener {
        void changeValues(ArrayList<?> data, String type);
        void removeValues();
    }

    // tworzenie/pobieranie fragmentu
    @Override
    public Fragment getItem(int position) {
        // pobierz jeśli istnieje (by nie tworzyć niepotrzebnie nowego)
        if (fragments.get(position) != null) {
            return fragments.get(position);
        }
        // nie istnieje -> utwórz i zwróć
        if (position == 0) {
            fragments.put(position, TerrainShapeFragment.init(position));
        } else if (position == 1) {
            fragments.put(position, MapsWithPathFragment.init(position));
        } else {
            fragments.put(position, HeightChartFragment.init(position));
        }
        return fragments.get(position);
    }

    public PageChartsFragmentAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_NUMBER;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return context.getResources().getString(tabText[position]);
    }

    /**
     * Customizacja layutu: czcionka, kolor, tekst
     * @param position pozycja w tablayout
     * @return custom view
     */
    public View getTabIconView(int position, boolean viewIcon) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
        TextView title = view.findViewById(R.id.title);
        ImageView icon = view.findViewById(R.id.icon);
        title.setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/Dosis-Light.ttf"));
        title.setText(getPageTitle(position));
        if (viewIcon) {
            icon.setVisibility(View.VISIBLE);
        }
        return view;
    }
}
