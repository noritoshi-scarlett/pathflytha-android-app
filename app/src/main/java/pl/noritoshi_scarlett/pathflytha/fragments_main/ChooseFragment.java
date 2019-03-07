package pl.noritoshi_scarlett.pathflytha.fragments_main;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import pl.noritoshi_scarlett.pathflytha.Pathflytha;
import pl.noritoshi_scarlett.pathflytha.R;
import pl.noritoshi_scarlett.pathflytha.activities.CalculateActivity;
import pl.noritoshi_scarlett.pathflytha.activities.MapsActivity;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.small_quests.LatLongConverter;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

import static android.app.Activity.RESULT_OK;

public class ChooseFragment extends Fragment {

    private Context context;

    private Button btnSelectCoordinates;
    private TextView txtPickedCoordinatesTitle;
    private TextView txtPickedCoordinates;

    private AppCompatSpinner spinnerObjectType;
    private AppCompatSpinner spinnerLicenseType;
    private AppCompatSpinner spinnerCalculateType;

    private FloatingActionButton floatBtnCalculateIt;

    private LatLng startLatLng;
    private LatLng startOutLatLng;
    private LatLng endTargetLatLng;
    private LatLng endLatLng;
    private TextInputEditText txtInputFlyNormal;

    public ChooseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_choose, container, false);

        context = getActivity().getApplicationContext();
        btnSelectCoordinates = view.findViewById(R.id.btnSelectCoordinates);
        btnSelectCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), MapsActivity.class);
                startActivityForResult(intent, Pathflytha.REQUEST_CODE_PICK_COORDINATIES);
            }
        });
        txtPickedCoordinatesTitle = view.findViewById(R.id.txtPickedCoordinatesTitle);
        txtPickedCoordinatesTitle.setVisibility(View.GONE);
        txtPickedCoordinates = view.findViewById(R.id.txtPickedCoordinates);
        spinnerObjectType = view.findViewById(R.id.spinnerObjectType);
        spinnerLicenseType = view.findViewById(R.id.spinnerLicenseType);
        spinnerCalculateType = view.findViewById(R.id.spinnerCalculateType);
        setupSpinnerObject();
        setupSpinnerLicense();
        setupSpinnerCalculate();
        txtInputFlyNormal = view.findViewById(R.id.inputFlyNormal);

        floatBtnCalculateIt = view.findViewById(R.id.floatBtnCalculateIt);
        floatBtnCalculateIt.setEnabled(false);
        floatBtnCalculateIt.setBackgroundTintList(getResources().getColorStateList(R.color.s_fab_colors_teal));
        floatBtnCalculateIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CalculateActivity.class);
                intent.putExtra(Pathflytha.START_MARKER_LONGITUDE,      startLatLng.longitude);
                intent.putExtra(Pathflytha.START_MARKER_LATITUDE,       startLatLng.latitude);
                intent.putExtra(Pathflytha.START_MARKER_OUT_LONGITUDE,  startOutLatLng.longitude);
                intent.putExtra(Pathflytha.START_MARKER_OUT_LATITUDE,   startOutLatLng.latitude);
                intent.putExtra(Pathflytha.END_MARKER_TARGET_LONGITUDE, endTargetLatLng.longitude);
                intent.putExtra(Pathflytha.END_MARKER_TARGET_LATITUDE,  endTargetLatLng.latitude);
                intent.putExtra(Pathflytha.END_MARKER_LONGITUDE,        endLatLng.longitude);
                intent.putExtra(Pathflytha.END_MARKER_LATITUDE,         endLatLng.latitude);
                intent.putExtra(Pathflytha.FLY_NORMAL, txtInputFlyNormal.getText().toString());
                intent.putExtra(Pathflytha.SETUP_ID, spinnerCalculateType.getSelectedItemPosition());
                startActivity(intent);
            }
        });

        return view;

    }

    private void setupSpinnerObject() {
        String[] objectArray = getResources().getStringArray(R.array.main_arrayObjectType);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, R.layout.item_spinner, objectArray) {
            // CUSTOM FONT
            final Typeface tf = TypefaceUtils.load(getResources().getAssets(), "fonts/Dosis-Light.ttf");
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTypeface(tf);
                return view;
            }
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View mView = super.getDropDownView(position, convertView, parent);
                TextView mTextView = (TextView) mView;
                mTextView.setTypeface(tf);
                return  mView;
            }
        };
        spinnerObjectType.setAdapter(spinnerArrayAdapter);
    }

    private void setupSpinnerLicense() {
        String[] objectArray = getResources().getStringArray(R.array.main_arrayLicenseType);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, R.layout.item_spinner, objectArray) {
            // CUSTOM FONT
            final Typeface tf = TypefaceUtils.load(getResources().getAssets(), "fonts/Dosis-Light.ttf");
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTypeface(tf);
                return view;
            }
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View mView = super.getDropDownView(position, convertView, parent);
                TextView mTextView = (TextView) mView;
                mTextView.setTypeface(tf);
                return  mView;
            }
        };
        spinnerLicenseType.setAdapter(spinnerArrayAdapter);
    }

    private void setupSpinnerCalculate() {
        String[] objectArray = getResources().getStringArray(R.array.main_arrayCalculateType);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, R.layout.item_spinner, objectArray) {
            // CUSTOM FONT
            final Typeface tf = TypefaceUtils.load(getResources().getAssets(), "fonts/Dosis-Light.ttf");
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTypeface(tf);
                return view;
            }
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View mView = super.getDropDownView(position, convertView, parent);
                TextView mTextView = (TextView) mView;
                mTextView.setTypeface(tf);
                return  mView;
            }
        };
        spinnerCalculateType.setAdapter(spinnerArrayAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK && requestCode == Pathflytha.REQUEST_CODE_PICK_COORDINATIES) {
            if (data != null) {

                startLatLng = new LatLng(
                        data.getDoubleExtra(Pathflytha.START_MARKER_LATITUDE, 0),
                        data.getDoubleExtra(Pathflytha.START_MARKER_LONGITUDE, 0));
                startOutLatLng = new LatLng(
                        data.getDoubleExtra(Pathflytha.START_MARKER_OUT_LATITUDE, 0),
                        data.getDoubleExtra(Pathflytha.START_MARKER_OUT_LONGITUDE, 0));
                endTargetLatLng = new LatLng(
                        data.getDoubleExtra(Pathflytha.END_MARKER_TARGET_LATITUDE, 0),
                        data.getDoubleExtra(Pathflytha.END_MARKER_TARGET_LONGITUDE, 0));
                endLatLng = new LatLng(
                        data.getDoubleExtra(Pathflytha.END_MARKER_LATITUDE, 0),
                        data.getDoubleExtra(Pathflytha.END_MARKER_LONGITUDE, 0));

                String[] textPickedCoordinates = LatLongConverter.writeAsString(context,
                        startLatLng, startOutLatLng, endTargetLatLng, endLatLng);

                // TODO -> use parameters
                if (txtPickedCoordinates != null) {
                    txtPickedCoordinates.setText(
                            String.format("%s%s%s%s",
                                    textPickedCoordinates[0], textPickedCoordinates[1],
                                    textPickedCoordinates[2], textPickedCoordinates[3]));
                }
                if (txtPickedCoordinatesTitle != null) {
                    txtPickedCoordinatesTitle.setVisibility(View.VISIBLE);
                }
                checkingData();
            }
        }
    }


    /**
     * sprawdzenie poprawnosci i wypelnienie danych -> jesli tak, pozwol wyliczac trase
     */
    public void checkingData() {

        // TODO -> checking other selections labels/spinners etc.

        if (startLatLng != null && startOutLatLng != null && endTargetLatLng != null && endLatLng != null) {
            floatBtnCalculateIt.setEnabled(true);
        } else {
            floatBtnCalculateIt.setEnabled(false);
        }
    }

}
