package com.example.Printxpress_hdcse_131_10;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    public interface OnRemoveListener {
        void onRemove(int cartId);
    }

    Context context;
    List<CartItem> cartItems;
    DbHelper dbHelper;
    OnRemoveListener onRemoveListener;

    public CartAdapter(Context context, List<CartItem> cartItems, OnRemoveListener onRemoveListener) {
        this.context = context;
        this.cartItems = cartItems;
        this.onRemoveListener = onRemoveListener;
        this.dbHelper = new DbHelper(context);
    }

    @NonNull
    @Override
    public CartAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.ViewHolder holder, int i) {
        CartItem item = cartItems.get(i);

        //load cart items
        holder.tvName.setText(item.getProductName());
        holder.tvCat.setText(item.getCategoryName());
        holder.tvPrice.setText("Rs. " + String.format("%.2f", item.getPrice()));
        holder.tvQty.setText("Qty: " + item.getQuantity());

        int imgId = context.getResources().getIdentifier(item.getImage(),
                "drawable", context.getPackageName());

        if (imgId != 0) holder.imgCartProd.setImageResource(imgId);

        //increase qty by 1
        holder.btnPlus.setOnClickListener(v -> {
            int newQty = item.getQuantity() + 1;
            item.setQuantity(newQty);

            dbHelper.updateCartQuantity(item.getCartId(), newQty);
            holder.tvQty.setText(String.valueOf(newQty));
            holder.tvSubtot.setText("Rs. " + String.format("%.2f", item.getPrice() * newQty));

        });

        //decrease qty by 1
        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                int newQty = item.getQuantity() - 1;
                item.setQuantity(newQty);

                dbHelper.updateCartQuantity(item.getCartId(), newQty);
                holder.tvQty.setText(String.valueOf(newQty));
                holder.tvSubtot.setText("Rs. " + String.format("%.2f", item.getPrice() * newQty));
            }
        });

        //remove item
        holder.btnRemove.setOnClickListener(v -> {
            if (onRemoveListener != null) {
                onRemoveListener.onRemove(item.getCartId());
            }
        });

        //Subtotal display
        holder.tvSubtot.setText("Rs. " + String.format("%.2f", item.getPrice() * item.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgCartProd;
        TextView tvName, tvCat, tvPrice, tvQty, tvSubtot;
        Button btnPlus, btnMinus, btnRemove;

        public ViewHolder(@NonNull View view) {
            super(view);

            imgCartProd = view.findViewById(R.id.ivCartProduct);
            tvName = view.findViewById(R.id.tvCartProductName);
            tvCat = view.findViewById(R.id.tvCartCategory);
            tvQty = view.findViewById(R.id.tvCartQty);
            tvPrice = view.findViewById(R.id.tvCartPrice);
            tvSubtot = view.findViewById(R.id.tvCartSubtotal);
            btnPlus = view.findViewById(R.id.btnQtyPlus);
            btnMinus = view.findViewById(R.id.btnQtyMinus);
            btnRemove = view.findViewById(R.id.btnRemoveCart);
        }
    }
}
