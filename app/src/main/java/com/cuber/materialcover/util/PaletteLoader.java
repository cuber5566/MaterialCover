package com.cuber.materialcover.util;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.graphics.Palette;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.collections4.map.LRUMap;

import java.util.Collections;
import java.util.Map;

public class PaletteLoader {

    private static int TRUE = 1;
    private static int FALSE = 0;

    private static final int MSG_RENDER_PALETTE = 10;
    private static final int MSG_DISPLAY_PALETTE = 11;

    private static final int MAX_ITEMS_IN_CACHE = 100;
    private static final int MAX_CONCURRENT_THREADS = 5;

    private static Handler uiHandler;
    private static Handler backgroundHandler;

    private static Map<String, Palette> colorSchemeCache;

    private PaletteLoader() {
    }

    public static PaletteBuilder with(Context context, String id) {
        if (colorSchemeCache == null) {
            //使用Collections.synchronizedMap所建置的Map，可以保證1次只有1個Thread來存取這個Map，減少多執行緒的程式所發生的資料錯誤。不過在使用iterator時，必須要加上synchronized，以免發生不可預期的錯誤。
            colorSchemeCache = Collections.synchronizedMap(
                    //LRUMap如果數據超出限制，就刪除最近未使用的數據（MAX_ITEMS_IN_CACHE = 100）
                    new LRUMap<String, Palette>(MAX_ITEMS_IN_CACHE)
            );
        }
        if (uiHandler == null || backgroundHandler == null) {
            setupHandlers(context);
        }
        return new PaletteBuilder(id);
    }

    private static void setupHandlers(Context context) {
        HandlerThread handlerThread = new HandlerThread("palette-loader-background");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper(), sCallback);
        uiHandler = new Handler(context.getMainLooper(), sCallback);
    }

    private static Handler.Callback sCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            switch (message.what) {

                case MSG_RENDER_PALETTE:
                    Pair<Bitmap, PaletteTarget> pair = (Pair<Bitmap, PaletteTarget>) message.obj;
                    if (pair != null && !pair.first.isRecycled()) {
                        Palette palette = Palette.generate(pair.first);

                        colorSchemeCache.put(pair.second.getId(), palette);

                        Message uiMessage = uiHandler.obtainMessage();
                        uiMessage.what = MSG_DISPLAY_PALETTE;
                        uiMessage.obj = new Pair<>(palette, pair.second);
                        uiMessage.arg1 = FALSE;

                        uiHandler.sendMessage(uiMessage);
                    }
                    break;

                case MSG_DISPLAY_PALETTE:
                    Pair<Palette, PaletteTarget> pairDisplay = (Pair<Palette, PaletteTarget>) message.obj;
                    boolean fromCache = message.arg1 == TRUE;
                    applyColorToView(pairDisplay.second, pairDisplay.first, fromCache);
                    break;

            }
            return false;
        }
    };

    public static class PaletteBuilder {

        private String id;
        private Bitmap bitmap;
        private boolean maskDrawable;
        private int fallbackColor = Color.TRANSPARENT;
        private PaletteRequest paletteRequest = new PaletteRequest(PaletteRequest.SwatchType.REGULAR_VIBRANT, PaletteRequest.SwatchColor.BACKGROUND);
        private Palette palette;

        public PaletteBuilder(String id) {
            this.id = id;
        }

        public PaletteBuilder load(Bitmap bitmap) {
            this.bitmap = bitmap;
            return this;
        }

        public PaletteBuilder load(Palette colorScheme) {
            this.palette = colorScheme;
            return this;
        }

        public PaletteBuilder mask() {
            maskDrawable = true;
            return this;
        }

        public PaletteBuilder fallbackColor(int fallbackColor) {
            this.fallbackColor = fallbackColor;
            return this;
        }

        public PaletteBuilder setPaletteRequest(PaletteRequest paletteRequest) {
            this.paletteRequest = paletteRequest;
            return this;
        }

        public void into(View view) {
            final PaletteTarget paletteTarget = new PaletteTarget(id, paletteRequest, view, maskDrawable, fallbackColor);
            if (palette != null) {
                colorSchemeCache.put(paletteTarget.getId(), palette);
                applyColorToView(paletteTarget, palette, false);
            } else {
                if (colorSchemeCache.get(id) != null) {
                    Palette palette = colorSchemeCache.get(id);
                    applyColorToView(paletteTarget, palette, true);
                } else {
                    if (Build.VERSION.SDK_INT >= 21) {
//                        executorService.submit(new PaletteRenderer(bitmap, paletteTarget));
                    } else {
                        Message bgMessage = backgroundHandler.obtainMessage();
                        bgMessage.what = MSG_RENDER_PALETTE;
                        bgMessage.obj = new Pair<>(bitmap, paletteTarget);
                        backgroundHandler.sendMessage(bgMessage);
                    }
                }
            }
        }
    }

    private static void applyColorToView(PaletteTarget target, Palette palette, boolean fromCache) {
        if (!isViewRecycled(target)) {
            applyColorToView(target, target.getPaletteRequest().getColor(palette), fromCache);
        }
    }

    private static void applyColorToView(final PaletteTarget target, int color, boolean fromCache) {
        if (target.getView() instanceof TextView) {
            applyColorToView((TextView) target.getView(), color, fromCache);
            return;
        }
        if (fromCache) {
            if (target.getView() instanceof ImageView && target.shouldMaskDrawable()) {
                ((ImageView) target.getView()).getDrawable().mutate()
                        .setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            } else {
                target.getView().setBackgroundColor(color);
            }
        } else {
            if (target.getView() instanceof ImageView && target.shouldMaskDrawable()) {
                Integer colorFrom;
                ValueAnimator.AnimatorUpdateListener imageAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ((ImageView) target.getView()).getDrawable().mutate()
                                .setColorFilter((Integer) valueAnimator
                                        .getAnimatedValue(), PorterDuff.Mode.MULTIPLY);
                    }
                };
                ValueAnimator.AnimatorUpdateListener animatorUpdateListener;

                PaletteTag paletteTag = (PaletteTag) target.getView().getTag();
                animatorUpdateListener = imageAnimatorUpdateListener;
                colorFrom = paletteTag.getColor();
                target.getView().setTag(new PaletteTag(paletteTag.getId(), color));

                Integer colorTo = color;
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                colorAnimation.addUpdateListener(animatorUpdateListener);
                colorAnimation.setStartDelay(300);
                colorAnimation.start();
            } else {

                Drawable preDrawable;

                if (target.getView().getBackground() == null) {
                    preDrawable = new ColorDrawable(Color.TRANSPARENT);
                } else {
                    preDrawable = target.getView().getBackground();
                }

                TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{
                        preDrawable,
                        new ColorDrawable(color)
                });
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    target.getView().setBackground(transitionDrawable);
                } else {
                    target.getView().setBackgroundDrawable(transitionDrawable);
                }
                transitionDrawable.startTransition(300);
            }
        }
    }

    //for TextView
    private static void applyColorToView(final TextView textView, int color, boolean fromCache) {
        if (fromCache) {
            textView.setTextColor(color);
        } else {
            Integer colorFrom = textView.getCurrentTextColor();
            Integer colorTo = color;
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    textView.setTextColor((Integer) animator.getAnimatedValue());
                }
            });
            colorAnimation.start();
        }
    }

    private static boolean isViewRecycled(PaletteTarget target) {

        if (target != null && target.getId() != null && target.getView() != null
                && target.getView().getTag() != null) {
            if (target.getView().getTag() instanceof PaletteTag) {
                return !target.getId().equals(((PaletteTag) target.getView().getTag()).getId());
            } else {
               return true;
            }
        } else {
            return false;
        }
    }

    private static class PaletteTarget {
        private String id;
        private PaletteRequest paletteRequest;
        private View view;
        private boolean maskDrawable;

        private PaletteTarget(String id, PaletteRequest paletteRequest, View view, boolean maskDrawable, int fallbackColor) {
            this.id = id;
            this.paletteRequest = paletteRequest;
            this.view = view;
            this.view.setTag(new PaletteTag(this.id, fallbackColor));
            this.maskDrawable = maskDrawable;
        }

        public String getId() {
            return id;
        }

        public PaletteRequest getPaletteRequest() {
            return paletteRequest;
        }

        public View getView() {
            return view;
        }

        public boolean shouldMaskDrawable() {
            return maskDrawable;
        }
    }

    private static class PaletteTag {
        private String id;
        private Integer color;

        private PaletteTag(String id, Integer color) {
            this.id = id;
            this.color = color;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getColor() {
            return color;
        }

        public void setColor(Integer color) {
            this.color = color;
        }
    }

    public static class NoPaletteTagFoundException extends NullPointerException {
        public NoPaletteTagFoundException() {
            super();
        }

        public NoPaletteTagFoundException(String message) {
            super(message);
        }
    }

}
