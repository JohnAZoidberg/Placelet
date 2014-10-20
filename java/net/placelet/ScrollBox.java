package net.placelet;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

public class ScrollBox extends RelativeLayout {
    private static final int TRANSLATE_DURATION_MILLIS = 200;
    private FabOnScrollListener mOnScrollListener;

    @IntDef({TYPE_NORMAL, TYPE_MINI})
    public @interface TYPE {
    }

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_MINI = 1;

    protected AbsListView mListView;

    private boolean mVisible;

    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    public ScrollBox(Context context) {
        this(context, null);
    }

    public ScrollBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ScrollBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        mVisible = true;
    }

    private int getMarginBottom() {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }

    protected AbsListView.OnScrollListener getOnScrollListener() {
        return mOnScrollListener;
    }

    public void show() {
        show(true);
    }

    public void hide() {
        hide(true);
    }

    public void show(boolean animate) {
        toggle(true, animate, false);
    }

    public void hide(boolean animate) {
        toggle(false, animate, false);
    }

    private void toggle(final boolean visible, final boolean animate, boolean force) {
        if (mVisible != visible || force) {
            mVisible = visible;
            int height = getHeight();
            if (height == 0 && !force) {
                ViewTreeObserver vto = getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            ViewTreeObserver currentVto = getViewTreeObserver();
                            if (currentVto.isAlive()) {
                                currentVto.removeOnPreDrawListener(this);
                            }
                            toggle(visible, animate, true);
                            return true;
                        }
                    });
                    return;
                }
            }
            int translationY = visible ? 0 : height + getMarginBottom();
            if (animate) {
                animate().setInterpolator(mInterpolator)
                        .setDuration(TRANSLATE_DURATION_MILLIS)
                        .translationY(translationY);
            } else {
                setTranslationY(translationY);
            }
        }
    }

    public void attachToListView(@NonNull AbsListView listView, @NonNull AbsListView.OnScrollListener onScrollListener) {
        mListView = listView;
        FabOnScrollListener onFabScrollListener = new FabOnScrollListener();
        mOnScrollListener = onFabScrollListener;
        onFabScrollListener.setScrollBox(this);
        onFabScrollListener.setListView(listView);

        CompositeScrollListener composite = new CompositeScrollListener();
        composite.registerListener(onFabScrollListener);
        composite.registerListener(onScrollListener);
        mListView.setOnScrollListener(composite);
    }

    public void attachToListView(@NonNull AbsListView listView) {
        mListView = listView;
        FabOnScrollListener onScrollListener = new FabOnScrollListener();
        mOnScrollListener = onScrollListener;
        onScrollListener.setScrollBox(this);
        onScrollListener.setListView(listView);
        mListView.setOnScrollListener(onScrollListener);
    }

    public static class FabOnScrollListener extends ScrollDirectionDetector {
        private ScrollBox mScrollBox;

        public FabOnScrollListener() {
            setScrollDirectionListener(new ScrollDirectionListener() {
                @Override public void onScrollDown() {
                    mScrollBox.show();
                }

                @Override public void onScrollUp() {
                    mScrollBox.hide();
                }
            });
        }

        public void setScrollBox(ScrollBox ScrollBox) {
            mScrollBox = ScrollBox;
        }
    }
}
