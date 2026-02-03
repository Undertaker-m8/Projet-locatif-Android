package com.example.miniprojet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.miniprojet.model.Advertisement;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

public class AdvertisementAdapter extends RecyclerView.Adapter<AdvertisementAdapter.ViewHolder> {

    private Context context;
    private List<Advertisement> advertisements;
    private OnAdvertisementClickListener listener;
    private boolean isFavoriteMode = false;

    // Interface pour les clics
    public interface OnAdvertisementClickListener {
        void onAdvertisementClick(Advertisement advertisement);
        void onFavoriteClick(Advertisement advertisement);
    }

    // CONSTRUCTEUR CORRIGÉ - Accepte 3 paramètres
    public AdvertisementAdapter(Context context, List<Advertisement> advertisements,
                                OnAdvertisementClickListener listener) {
        this.context = context;
        this.advertisements = advertisements;
        this.listener = listener;
    }

    // CONSTRUCTEUR ALTERNATIF - Pour compatibilité avec votre code existant
    public AdvertisementAdapter(List<Advertisement> advertisements, OnAdvertisementClickListener listener,
                                boolean isFavoriteMode) {
        this.advertisements = advertisements;
        this.listener = listener;
        this.isFavoriteMode = isFavoriteMode;
    }

    // MÉTHODES SETTER POUR LA COMPATIBILITÉ
    public void setOnItemClickListener(OnAdvertisementClickListener listener) {
        this.listener = listener;
    }

    public void setOnFavoriteClickListener(OnAdvertisementClickListener listener) {
        this.listener = listener;
    }

    public void setFavoriteMode(boolean isFavoriteMode) {
        this.isFavoriteMode = isFavoriteMode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_advertisement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Advertisement ad = advertisements.get(position);

        // Titre
        holder.tvTitle.setText(ad.getTitle());

        // Adresse
        holder.tvAddress.setText(String.format("%s, %s", ad.getAddress(), ad.getCity()));

        // Prix
        holder.tvPrice.setText(formatPrice(ad.getPrice()));

        // Surface
        holder.tvSurface.setText(formatSurface(ad.getSurface()));

        // Pièces
        holder.tvRooms.setText(formatRooms(ad.getRooms()));

        // Équipements
        StringBuilder amenities = new StringBuilder();
        if (ad.isHasParking()) {
            amenities.append("🏠 ");
        }
        if (ad.isHasElevator()) {
            amenities.append("🛗 ");
        }
        if (ad.isHasBalcony()) {
            amenities.append("🌿 ");
        }
        holder.tvAmenities.setText(amenities.toString().trim());

        // Note
        if (ad.getRating() > 0 && ad.getReviewCount() > 0) {
            holder.tvRating.setVisibility(View.VISIBLE);
            holder.tvRatingCount.setVisibility(View.VISIBLE);
            holder.tvRating.setText(String.format(Locale.FRANCE, "%.1f", ad.getRating()));
            holder.tvRatingCount.setText(String.format("(%d)", ad.getReviewCount()));
        } else {
            holder.tvRating.setVisibility(View.GONE);
            holder.tvRatingCount.setVisibility(View.GONE);
        }

        // Image
        if (ad.getImageUrls() != null && !ad.getImageUrls().isEmpty()) {
            holder.ivImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(ad.getImageUrls().get(0))
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .centerCrop()
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setVisibility(View.GONE);
        }

        // Favori (icône différente en mode favori)
        if (isFavoriteMode) {
            holder.btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            holder.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
        }

        holder.btnFavorite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(ad);
            }
        });

        // Clic sur l'annonce
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAdvertisementClick(ad);
            }
        });
    }

    @Override
    public int getItemCount() {
        return advertisements != null ? advertisements.size() : 0;
    }

    public void updateData(List<Advertisement> newAdvertisements) {
        this.advertisements = newAdvertisements;
        notifyDataSetChanged();
    }

    // Méthodes utilitaires de formatage
    private String formatPrice(double price) {
        return String.format(Locale.FRANCE, "%.0f €", price);
    }

    private String formatSurface(double surface) {
        return String.format(Locale.FRANCE, "%.0f m²", surface);
    }

    private String formatRooms(int rooms) {
        if (rooms == 1) {
            return "1 pièce";
        } else {
            return rooms + " pièces";
        }
    }

    // Classe ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView ivImage;
        TextView tvTitle;
        TextView tvAddress;
        TextView tvPrice;
        TextView tvSurface;
        TextView tvRooms;
        TextView tvAmenities;
        TextView tvRating;
        TextView tvRatingCount;
        ImageView btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_advertisement);
            ivImage = itemView.findViewById(R.id.iv_ad_image);
            tvTitle = itemView.findViewById(R.id.tv_ad_title);
            tvAddress = itemView.findViewById(R.id.tv_ad_address);
            tvPrice = itemView.findViewById(R.id.tv_ad_price);
            tvSurface = itemView.findViewById(R.id.tv_ad_surface);
            tvRooms = itemView.findViewById(R.id.tv_ad_rooms);
            tvAmenities = itemView.findViewById(R.id.tv_ad_amenities);
            tvRating = itemView.findViewById(R.id.tv_ad_rating);
            tvRatingCount = itemView.findViewById(R.id.tv_ad_rating_count);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }
    }
}