package fi.jesunmaailma.tvapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import fi.jesunmaailma.tvapp.R;
import fi.jesunmaailma.tvapp.models.Channel;
import fi.jesunmaailma.tvapp.ui.activities.Details;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ViewHolder> {

    List<Channel> channels;
    String type;
    View view;

    public ChannelAdapter(List<Channel> channels, String type) {
        this.channels = channels;
        this.type = type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (type.equals("slider")) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.big_slider_view, parent, false);
        } else if (type.equals("category_details")) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_details_view, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_view_layout, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelAdapter.ViewHolder holder, int position) {
        Channel channel = channels.get(position);

        holder.channelName.setText(channel.getName());

        Picasso.get()
                .load(channel.getThumbnail())
                .into(holder.channelThumbnail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Details.class);
                intent.putExtra("channel", channel);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView channelThumbnail;
        TextView channelName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            channelThumbnail = itemView.findViewById(R.id.channelThumbnail);
            channelName = itemView.findViewById(R.id.channelName);
        }
    }
}
