package add.bloodconnection.common.misc;

import android.view.View;
import android.view.animation.*;

/**
 * Created by Alexzander on 14.11.2017.
 */

public final class GraphicsProcessing {
    public static <V extends View> V CycleFadeAnimation(final V element, int fadingDuration, int timeout) {
        if(!element.getClass().equals(View.class)) {
            final AlphaAnimation animation1 = new AlphaAnimation(1f, 0f);
            animation1.setDuration(fadingDuration);
            animation1.setStartOffset(timeout);

            final AlphaAnimation animation2 = new AlphaAnimation(0f, 1f);
            animation2.setDuration(fadingDuration);
            animation2.setStartOffset(timeout);

            animation1.setAnimationListener(new Animation.AnimationListener(){

                @Override
                public void onAnimationEnd(Animation arg0) {
                    // start animation2 when animation1 ends (continue)
                    element.startAnimation(animation2);
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onAnimationStart(Animation arg0) {
                    // TODO Auto-generated method stub
                }

            });

            //animation2 AnimationListener
            animation2.setAnimationListener(new Animation.AnimationListener(){

                @Override
                public void onAnimationEnd(Animation arg0) {
                    // start animation1 when animation2 ends (repeat)
                    element.startAnimation(animation1);
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onAnimationStart(Animation arg0) {
                    // TODO Auto-generated method stub
                }

            });

            element.startAnimation(animation1);
        }

        return element;
    }

    public static <V extends View> V FadeAnimation(final V element, int fadingDuration, float from, float to) {
        if(!element.getClass().equals(View.class)) {
            final AlphaAnimation animation1 = new AlphaAnimation(from, to);
            animation1.setDuration(fadingDuration);
            element.startAnimation(animation1);
        }
        return element;
    }

    public static <V extends View> V RotateAnimation(final V element, int duration) {
        if(!element.getClass().equals(View.class)) {
            RotateAnimation rotate = new RotateAnimation(0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);

            rotate.setDuration(duration);
            rotate.setRepeatCount(Animation.INFINITE);
            rotate.setInterpolator(new LinearInterpolator());

            element.startAnimation(rotate);
        }

        return element;
    }

}
