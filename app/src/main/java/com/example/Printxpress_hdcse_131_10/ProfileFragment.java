package com.example.Printxpress_hdcse_131_10;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class ProfileFragment extends Fragment {

    TextView txtName, txtEmail, txtPhone, txtSave;
    Button btnEdit, btnOrders, btnLogout;
    ImageView imgProPic;
    EditText etAddress, etCity, etProvince, etPostalCode;
    String loginEmail;
    String savedImgPath = "";
    int currentUserId = -1;
    DbHelper dbHelper;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            try {

                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                        requireActivity().getContentResolver(), imageUri
                                );
                                imgProPic.setImageBitmap(bitmap);

                                savedImgPath = saveImageToInternalStorage(bitmap);
                                dbHelper.updateProPic(currentUserId, savedImgPath);
                                Toast.makeText(getContext(), "Photo updated!", Toast.LENGTH_SHORT).show();

                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

            );


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            loginEmail = getArguments().getString("email");
            Log.d("ProfileFragment", "Email received: " + loginEmail);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        txtName      = view.findViewById(R.id.tvName);
        txtEmail     = view.findViewById(R.id.tvEmail);
        txtPhone     = view.findViewById(R.id.tvContact);
        btnEdit      = view.findViewById(R.id.btnEdit);
        btnOrders    = view.findViewById(R.id.btnOrders);
        btnLogout    = view.findViewById(R.id.btnLogout);
        imgProPic    = view.findViewById(R.id.ivProPhoto);
        etAddress    = view.findViewById(R.id.edtAddress);
        etCity       = view.findViewById(R.id.edtCity);
        etProvince   = view.findViewById(R.id.edtProvince);
        etPostalCode = view.findViewById(R.id.edtCode);
        txtSave      = view.findViewById(R.id.tvSaveLocation);

        dbHelper = new DbHelper(getContext());


        if (loginEmail != null && !loginEmail.isEmpty()) {
            loadCustomerDetails();
            loadLocation();

        } else {
//            Log.e("ProfileFragment", "Email is null!");
//            Toast.makeText(getContext(),
//                    "Session error. Please login again.", Toast.LENGTH_LONG).show();
            view.findViewById(R.id.profileContent).setVisibility(View.GONE);
            view.findViewById(R.id.layoutLoginPrompt).setVisibility(View.VISIBLE);

            Button btnGoLogin = view.findViewById(R.id.btnGoLogin);
            Button btnGoRegister = view.findViewById(R.id.btnGoRegister);

            btnGoLogin.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), LoginActivity.class));
            });

            btnGoRegister.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), RegisterActivity.class));
            });
        }

        txtSave.setOnClickListener(v -> saveCustomerLocation());


        btnOrders.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrderActivity.class);
            intent.putExtra("email", loginEmail);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });


        btnEdit.setOnClickListener(v -> {
            openGallery();
        });

        imgProPic.setOnClickListener(v -> {
            openGallery();
        });

        return view;
    }

    private void loadCustomerDetails() {
        Customer cus = dbHelper.getCustomerDetails(loginEmail);
        if (cus != null) {
            currentUserId = cus.getId();
            txtName.setText(cus.getName());
            txtEmail.setText(cus.getEmail());
            txtPhone.setText(cus.getPhone());

            String imgPath = dbHelper.getProfilePhoto(currentUserId);
            if (imgPath != null && !imgPath.isEmpty()) {
                File imgFile = new File(imgPath);
                if (imgFile.exists()) {
                    imgProPic.setImageBitmap(BitmapFactory.decodeFile(imgPath));
                } else {
                    Log.e("ProfileFragment", "Profile image file not found at: " + imgPath);
                }
            }
        }else {
            Log.e("ProfileFragment", "Customer not found for email: " + loginEmail);
        }
    }

    private void loadLocation() {
        Location location = dbHelper.getLocation(loginEmail);
        if (location != null) {
            etAddress.setText(location.getAddress());
            etCity.setText(location.getCity());
            etProvince.setText(location.getProvince());
            etPostalCode.setText(location.getPostCode());
        }
    }

    private void saveCustomerLocation() {
        String address = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String province = etProvince.getText().toString().trim();
        String postCode = etPostalCode.getText().toString().trim();

        if (address.isEmpty() || city.isEmpty() || province.isEmpty() || postCode.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields to save",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        boolean saved = dbHelper.saveLocation(loginEmail, address, city, province, postCode);
        if (saved) {
            Toast.makeText(getContext(), "Location Saved.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to save location.", Toast.LENGTH_SHORT).show();
        }
    }

    //logic to open gallery
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        File directory = requireActivity().getDir("profile_images", Activity.MODE_PRIVATE);
        File file = new File(directory, "profile_" + System.currentTimeMillis() + ".jpg");

        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();
    }


}
