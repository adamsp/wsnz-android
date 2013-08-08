package speakman.whatsshakingnz.views;

import android.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

/**
 * Created by Adam on 8/08/13.
 * http://stackoverflow.com/a/9253297/1217087
 */
public class AnimatingLinearLayout extends LinearLayout {
    Context context;
    Animation inAnimation;
    Animation outAnimation;

    public AnimatingLinearLayout(Context context) {
        super(context);
        this.context = context;
        initAnimations();

    }

    public AnimatingLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initAnimations();
    }

    private void initAnimations() {
        inAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        outAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_out);
    }

    public void show() {
        if (isVisible()) return;
        show(true);
    }

    public void show(boolean withAnimation) {
        if (withAnimation) this.startAnimation(inAnimation);
        this.setVisibility(View.VISIBLE);
    }

    public void hide() {
        if (!isVisible()) return;
        hide(true);
    }

    public void hide(boolean withAnimation) {
        if (withAnimation) this.startAnimation(outAnimation);
        this.setVisibility(View.GONE);
    }

    public boolean isVisible() {
        return (this.getVisibility() == View.VISIBLE);
    }

    public void overrideDefaultInAnimation(Animation inAnimation) {
        this.inAnimation = inAnimation;
    }

    public void overrideDefaultOutAnimation(Animation outAnimation) {
        this.outAnimation = outAnimation;
    }
}
