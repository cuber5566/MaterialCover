package com.cuber.materialcover.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cuber.materialcover.R;
import com.cuber.materialcover.util.PaletteLoader;
import com.cuber.materialcover.util.PaletteRequest;

/**
 * Created by cuber on 2015/1/30.
 */
public class CustomGridViewAdapter extends RecyclerView.Adapter<CustomGridViewAdapter.ViewHolder> {

    private int[] images = {R.drawable.kodaline, R.drawable.fitz, R.drawable.jamie, R.drawable.yuna};
    private String[] line1 = {"In a Perfect World", "More Than Just A Dream", "Jamie Lidell", "Nocturnal"};
    private String[] line2 = {"Kodaline", "Fitz & the Tantrums", "Jamie Lidell", "Yuna"};

    private OnItemClickListener onItemClickListener;
    private int barHeight;
    private int itemSize;

    public CustomGridViewAdapter(int itemSize) {
        this.itemSize = itemSize;
    }

    @Override
    public CustomGridViewAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_cell_thumb_tile, viewGroup, false);
        if (barHeight == 0) {
            barHeight = view.getResources().getDimensionPixelSize(R.dimen.grid_item_footer_two_line_height);
        }
        view.getLayoutParams().height = itemSize+barHeight;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomGridViewAdapter.ViewHolder viewHolder, int i) {

        viewHolder.imageView.getLayoutParams().height = itemSize;
        viewHolder.imageView.getLayoutParams().width = itemSize;
        viewHolder.imageView.setImageResource(images[i % 4]);


        Bitmap bitmap = ((BitmapDrawable) viewHolder.imageView.getDrawable()).getBitmap();
        Context context = viewHolder.bottomBar.getContext();

        viewHolder.bottomBar.setBackgroundResource(R.color.gery_dark);
        viewHolder.textLine1.setTextColor(context.getResources().getColor(R.color.white));
        viewHolder.textLine2.setTextColor(context.getResources().getColor(R.color.white));

        PaletteLoader.with(context, "" + i)
                .load(bitmap)
                .setPaletteRequest(new PaletteRequest(PaletteRequest.SwatchType.REGULAR_VIBRANT, PaletteRequest.SwatchColor.BACKGROUND))
                .into(viewHolder.bottomBar);
        PaletteLoader.with(context, "" + i)
                .load(bitmap)
                .setPaletteRequest(new PaletteRequest(PaletteRequest.SwatchType.LIGHT_MUTED, PaletteRequest.SwatchColor.TEXT_TITLE))
                .into(viewHolder.textLine1);
        PaletteLoader.with(context, "" + i)
                .load(bitmap)
                .setPaletteRequest(new PaletteRequest(PaletteRequest.SwatchType.LIGHT_MUTED, PaletteRequest.SwatchColor.TEXT_TITLE))
                .into(viewHolder.textLine2);
        PaletteLoader.with(context, "" + i)
                .load(bitmap)
                .fallbackColor(viewHolder.textLine1.getCurrentTextColor())
                .setPaletteRequest(new PaletteRequest(PaletteRequest.SwatchType.LIGHT_MUTED, PaletteRequest.SwatchColor.TEXT_TITLE))
                .mask()
                .into(viewHolder.imageButton);


        viewHolder.textLine1.setText(line1[i % 4]);
        viewHolder.textLine2.setText(line2[i % 4]);


    }

    @Override
    public int getItemCount() {
        return 120;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        ImageView imageView;
        ViewGroup bottomBar;
        ImageButton imageButton;
        TextView textLine1;
        TextView textLine2;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            imageView = (ImageView) itemView.findViewById(R.id.thumb_image_view);
            imageButton = (ImageButton) itemView.findViewById(R.id.thumb_button_heart);
            textLine1 = (TextView) itemView.findViewById(R.id.thumb_text_line1);
            textLine2 = (TextView) itemView.findViewById(R.id.thumb_text_line2);
            bottomBar = (ViewGroup) itemView.findViewById(R.id.thumb_bottom_view);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v, getPosition());
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        abstract void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setItemSize(int itemSize) {
        this.itemSize = itemSize;
    }
}
