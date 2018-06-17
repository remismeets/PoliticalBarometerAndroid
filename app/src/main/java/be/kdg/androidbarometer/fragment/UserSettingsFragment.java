package be.kdg.androidbarometer.fragment;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import be.kdg.androidbarometer.R;
import be.kdg.androidbarometer.activity.MainActivity;

public class UserSettingsFragment extends Fragment {
    //Global attributes
    private View view;
    private ImageView ivProfilePicture;
    private EditText etFirstName;
    private EditText etLastName;
    private Button btnSave;

    public static final int PICK_IMAGE = 1;

    public UserSettingsFragment() {
        //Required empty public constructor
    }

    /**
     * Creates the UserSettingsFragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_settings, container, false);

        initialiseViews();
        addEventHandlers();
        return view;
    }

    /**
     * Handles the return after selecting a profile picture.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), uri);
                    ivProfilePicture.setImageBitmap(bitmap);
                } catch (IOException e) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_general), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Initialise all views.
     */
    private void initialiseViews() {
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        btnSave = view.findViewById(R.id.btnSave);
    }

    /**
     * Adds event handlers to views.
     */
    private void addEventHandlers() {
        btnSave.setOnClickListener((View v) -> saveData());

        ivProfilePicture.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), PICK_IMAGE);
        });
    }

    /**
     * Formats data for json compatibility and calls post api method for updating user profile.
     */
    private void saveData() {
        String firstName = etFirstName.getText().toString();
        String lastName = etLastName.getText().toString();
        String profilePictureString;

        if (ivProfilePicture.getDrawable() instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) ivProfilePicture.getDrawable()).getBitmap();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] profilePicture = byteArrayOutputStream.toByteArray();
            profilePictureString = Base64.encodeToString(profilePicture, Base64.NO_WRAP);
        } else {
            profilePictureString = "";
        }

       if (((MainActivity) Objects.requireNonNull(getActivity())).sendUserInfo(firstName, lastName, profilePictureString)) {
           Handler handler = new Handler();
           handler.postDelayed(() -> ((MainActivity)getActivity()).synchronizeViews(), 1000);
       }
    }
}
