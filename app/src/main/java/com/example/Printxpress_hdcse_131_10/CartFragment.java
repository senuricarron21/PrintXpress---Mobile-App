package com.example.Printxpress_hdcse_131_10;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
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
import java.util.List;

public class CartFragment extends Fragment {
    RecyclerView rvCart;
    TextView tvEmptyCart, tvTotal;
    Button btCheckout, btnUploadArtwork;
    DbHelper dbHelper;
    List<CartItem> cartItemsList;
    String cusEmail;
    CartAdapter c_adapter;
    String artworkImagePath = "";
    ActivityResultLauncher<Intent> artWork;
    ImageView ivArtwork;
    EditText etCustomText;

    private final ActivityResultLauncher<Intent> artworkLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            try {

                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                        requireActivity().getContentResolver(), imageUri
                                );
                                artworkImagePath = saveArtwork(bitmap);

                                if (ivArtwork != null && !artworkImagePath.isEmpty()) {
                                    ivArtwork.setVisibility(View.VISIBLE);
                                    ivArtwork.setImageBitmap(BitmapFactory.decodeFile(artworkImagePath));
                                }
                                Toast.makeText(getContext(), "Art Work updated!", Toast.LENGTH_SHORT).show();

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
            cusEmail = getArguments().getString("email");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        rvCart = view.findViewById(R.id.rvCart);
        tvEmptyCart = view.findViewById(R.id.tvEmptyCart);
        btCheckout = view.findViewById(R.id.btnCheckOut);


        dbHelper = new DbHelper(getContext());

        if (getArguments() != null) {
            cusEmail = getArguments().getString("email");
        }

        rvCart.setLayoutManager(new LinearLayoutManager(getActivity()));
        loadCart();

        btCheckout.setOnClickListener(v -> {
//new part
            if (cusEmail == null || cusEmail.isEmpty()) {
                Toast.makeText(getContext(), "Please login to place an order.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.putExtra("redirectToCart", true);
                startActivity(intent);
                return;
            }

            if (cartItemsList == null || cartItemsList.isEmpty()){
                Toast.makeText(getContext(),"Your cart is empty!" , Toast.LENGTH_SHORT).show();
                return;
            }

         //calculate the total
          double tot = 0;
            for (CartItem item : cartItemsList){
                 tot += item.getPrice() * item.getQuantity();
            }

         //calculating promotion
            Promotion promo = dbHelper.getBestPromo(tot);
            double discount = 0;
            double finalTot;
            String promoMsg = "";

            if (promo != null){
                discount = tot * promo.getDisRate();
                finalTot = tot - discount;
                promoMsg = "🎉 \"" + promo.getPromTitle() + "\" applied! You save Rs. " + String.format("%.2f", discount);
            } else {
                finalTot = tot;
            }


            final double chargedTot = finalTot;
            final String appliedPromoMsg = promoMsg;

            View customView = LayoutInflater.from(getContext())
                    .inflate(R.layout.custom_textbox, null);

            etCustomText = customView.findViewById(R.id.etCustomText);
            tvTotal = customView.findViewById(R.id.tvDialogTotal);
            btnUploadArtwork = customView.findViewById(R.id.btnUploadArtwork);
            ivArtwork = customView.findViewById(R.id.ivArtworkDesign);

            if (!appliedPromoMsg.isEmpty()){
                tvTotal.setText((appliedPromoMsg + "\nTotal: Rs. " + String.format("%.2f", chargedTot)));
            }else {
                tvTotal.setText("Total: Rs. " + String.format("%.2f", chargedTot));
            }

            btnUploadArtwork.setOnClickListener(upload -> {
                openArtworkGallery();
            });

            new AlertDialog.Builder(getContext())
                    .setTitle("Confirm Your Order")
                    .setView(customView)
                    .setPositiveButton("OK", (dialog, which) ->{

                        String customText = etCustomText.getText().toString().trim();

                        long newOrderId = dbHelper.placeOrder(cusEmail, finalTot, "", customText, artworkImagePath);

                        if (newOrderId != -1){
                            dbHelper.insertOrderItems(newOrderId,  cartItemsList, customText);
                            dbHelper.clearCart(cusEmail);
                            artworkImagePath = "";

                            if (!appliedPromoMsg.isEmpty()) {
                                Toast.makeText(getContext(), "Order Placed." + appliedPromoMsg, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), "Order Placed.", Toast.LENGTH_SHORT).show();
                            }

                            Intent intent = new Intent(getActivity(), OrderActivity.class);
                            intent.putExtra("email", cusEmail);
                            startActivity(intent);
                            loadCart();
                        }

                    })
                    .setNegativeButton("Cancel" , null).show();
        });

        return view;
}

    private void loadCart() {
        cartItemsList = dbHelper.getCartItems(cusEmail);

        if (cartItemsList.isEmpty()){
            rvCart.setVisibility(View.GONE);
            tvEmptyCart.setVisibility(View.VISIBLE);
            }
        else {
            rvCart.setVisibility((View.VISIBLE));
            tvEmptyCart.setVisibility(View.GONE);

            c_adapter = new CartAdapter(getContext(), cartItemsList, new CartAdapter.OnRemoveListener() {
                @Override
                public void onRemove(int cartId) {
                    dbHelper.removeFromCart(cartId);
                    loadCart();
                }
            });

            rvCart.setAdapter(c_adapter);
        }

    }

    private void openArtworkGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        artworkLauncher.launch(intent);
    }

    private String saveArtwork(Bitmap bitmap) {
        File directory = requireActivity().getDir("designs", Activity.MODE_PRIVATE);
        File file = new File(directory, "design_" + System.currentTimeMillis() + ".jpg");

        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();
    }

    @Override
    public void onResume(){

        super.onResume();
        loadCart();
    }
}