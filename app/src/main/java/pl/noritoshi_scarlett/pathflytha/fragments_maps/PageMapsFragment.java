package pl.noritoshi_scarlett.pathflytha.fragments_maps;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import pl.noritoshi_scarlett.pathflytha.Pathflytha;
import pl.noritoshi_scarlett.pathflytha.R;
import pl.noritoshi_scarlett.pathflytha.activities.MapsActivity;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.small_quests.LatLongConverter;


/**
 * A simple {@link Fragment} subclass.
 */
public class PageMapsFragment extends Fragment implements PageMapsFragmentAdapter.InterfaceLongLatListener {

    private TextView txtLongitude;
    private TextView txtLalitude;
    private EditText editLongitude;
    private EditText editLatitude;
    private Button btnClear;

    private int currentPage;
    private View.OnKeyListener mOnKeyListener;

    public PageMapsFragment() {
        // Required empty public constructor
    }

    public static PageMapsFragment init(int page) {
        Bundle args = new Bundle();
        args.putInt(Pathflytha.ARG_PAGE_NUMBER, page);
        PageMapsFragment fragment = new PageMapsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentPage = getArguments().getInt(Pathflytha.ARG_PAGE_NUMBER);

        // OnKeyListener dla pol edycji
        mOnKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    final String txtLong = editLongitude.getText().toString();
                    final String txtLat = editLatitude.getText().toString();

                    if (txtLong.length() > 0 && txtLat.length() > 0 && getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Double longitude = Double.parseDouble(txtLong);
                                    Double latitude = Double.parseDouble(txtLat);
                                    ((MapsActivity) getActivity()).markNewPoint(latitude, longitude);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
                return true;
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_page_maps, container, false);

        txtLongitude = view.findViewById(R.id.txtLongitude);
        txtLalitude = view.findViewById(R.id.txtLalitude);
        editLongitude = view.findViewById(R.id.editLongitude);
        editLatitude = view.findViewById(R.id.editLatitude);

        editLongitude.setOnKeyListener(mOnKeyListener);
        editLatitude.setOnKeyListener(mOnKeyListener);

        return view;
    }

    /**
     * Zmiana wartosci w polach tekstowych na zgodne z punktem zaznacoznym na mapie
     * @param longitude dlugosc geograficzna
     * @param latitude szerokosc geograficzna
     */
    @Override
    public void changeValues(Double longitude, Double latitude) {
        if (editLongitude != null && editLatitude != null) {
            String txtValue;
            int currentCursor;

            txtValue = String.valueOf(longitude);
            currentCursor = editLongitude.getSelectionStart();
            editLongitude.setText(txtValue);
            if (currentCursor > txtValue.length()) {
                editLongitude.setSelection(txtValue.length());
            } else {
                editLongitude.setSelection(currentCursor);
            }

            txtValue = String.valueOf(latitude);
            currentCursor = editLatitude.getSelectionStart();
            editLatitude.setText(txtValue);
            if (currentCursor > txtValue.length()) {
                editLatitude.setSelection(txtValue.length());
            } else {
                editLatitude.setSelection(currentCursor);
            }
        }
        if (txtLongitude != null && txtLalitude != null) {
            txtLongitude.setText(LatLongConverter.convertLongitudeToDegrees(longitude));
            txtLalitude.setText(LatLongConverter.convertLatitudeToDegrees(latitude));
        }
    }

    /**
     * usuniecie wpisanych wspolrzednych geograficznych dla tego punktu
     */
    @Override
    public void removeValues() {
        if (txtLongitude != null && txtLalitude != null) {
            txtLongitude.setText("");
            txtLalitude.setText("");
        }
        if (editLongitude != null && editLatitude != null) {
            editLongitude.setText("");
            editLatitude.setText("");
        }
    }

}
