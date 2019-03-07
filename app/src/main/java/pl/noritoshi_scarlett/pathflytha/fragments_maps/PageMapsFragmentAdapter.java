package pl.noritoshi_scarlett.pathflytha.fragments_maps;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import pl.noritoshi_scarlett.pathflytha.R;
import pl.noritoshi_scarlett.pathflytha.fragments_maps.PageMapsFragment;


public class PageMapsFragmentAdapter extends FragmentPagerAdapter {

    final private int PAGE_NUMBER = 4;

    private int[] tabText = {
            R.string.maps_startCoordinatesTitle,
            R.string.maps_start_out_CoordinatesTitle,
            R.string.maps_end_target_CoordinatesTitle,
            R.string.maps_endCoordinatesTitle
    };

    private Context context;
    private SparseArray<Fragment> fragments = new SparseArray<>();

    public interface InterfaceLongLatListener {
        void changeValues(Double longitude, Double latitude);
        void removeValues();
    }

    public interface InterfaceEditLongLatListener {
        void markNewPoint(Double longitude, Double latitude);
    }

    // tworzenie/pobieranie fragmentu
    @Override
    public Fragment getItem(int position) {
        // pobierz jeśli istnieje (by nie tworzyć niepotrzebnie nowego)
        if (fragments.get(position) != null) {
            return fragments.get(position);
        }
        // nie istnieje -> utwórz i zwróć
        fragments.put(position, PageMapsFragment.init(position));
        return fragments.get(position);
    }

    public PageMapsFragmentAdapter(FragmentManager fm, Context context) {
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
        ViewGroup layout = view.findViewById(R.id.layout);
        title.setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/Dosis-Light.ttf"));
        title.setText(getPageTitle(position));
        if (viewIcon) {
            icon.setVisibility(View.VISIBLE);
            //icon.setImageDrawable(context.getDrawable( id ));
        }
        return view;
    }
}
