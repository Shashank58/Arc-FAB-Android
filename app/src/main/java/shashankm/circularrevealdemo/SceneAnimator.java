package shashankm.circularrevealdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.TransitionRes;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AlertDialog;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

/**
 * Created by shashankm on 12/07/16.
 */
final class SceneAnimator {
    private static final String TAG = "SceneAnimator";
    private final TransitionManager transitionManager;
    private Scene scene1;
    private Scene scene2;
    private Context context;

    @TargetApi(VERSION_CODES.KITKAT)
    public static SceneAnimator newInstance(@NonNull Context context, @NonNull ViewGroup container,
                                            @LayoutRes int layout1Id, @LayoutRes int layout2Id, @TransitionRes int transitionId) {
        TransitionManager transitionManager = new TransitionManager();
        SceneAnimator sceneAnimator = new SceneAnimator(transitionManager, context);
        Scene scene1 = createScene(sceneAnimator, context, container, layout1Id);
        Scene scene2 = createScene(sceneAnimator, context, container, layout2Id);
        Transition transition = TransitionInflater.from(context).inflateTransition(transitionId);
        transitionManager.setTransition(scene1, scene2, transition);
        transitionManager.setTransition(scene2, scene1, transition);
        transitionManager.transitionTo(scene1);
        sceneAnimator.scene1 = scene1;
        sceneAnimator.scene2 = scene2;
        return sceneAnimator;
    }

    @TargetApi(VERSION_CODES.KITKAT)
    private static Scene createScene(@NonNull SceneAnimator sceneAnimator, @NonNull Context context,
                                     @NonNull ViewGroup container, @LayoutRes int layoutId) {
        Scene scene = Scene.getSceneForLayout(container, layoutId, context);
        scene.setEnterAction(new EnterAction(sceneAnimator, scene));
        return scene;
    }

    private SceneAnimator(TransitionManager transitionManager, Context context) {
        this.transitionManager = transitionManager;
        this.context = context;
    }

    @TargetApi(VERSION_CODES.KITKAT)
    private void sceneTransition(Scene from) {
        if (from == scene1) {
            transitionManager.transitionTo(scene2);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showAddTaskDialog(context);
                }
            }, 150);
        } else {
            transitionManager.transitionTo(scene1);
        }
    }

    private void showAddTaskDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.add_task, null);
        builder.setView(dialogView).setCancelable(false);
        final AlertDialog dialog = builder.create();
        if (VERSION.SDK_INT >= 21) {
            dialog.setOnShowListener(new OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    revealShow(dialogView, true, null);
                }
            });

            hideDialog(dialog, R.id.done, dialogView);
            hideDialog(dialog, R.id.cancel, dialogView);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        dialog.show();
    }

    private void hideDialog(final AlertDialog dialog, final int resId, final View dialogView) {
        dialogView.findViewById(resId).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                revealShow(dialogView, false, dialog);
            }
        });
    }

    private void revealShow(View rootView, boolean reveal, final AlertDialog dialog) {
        final View view = rootView.findViewById(R.id.root_view);
        int w = view.getMeasuredWidth() / 2;
        int h = view.getMeasuredHeight() / 2;

        // get the initial radius for the clipping circle
        int maxRadius = view.getWidth() / 2;

        if (reveal) {
            showDialog(view, w, h, maxRadius);
        } else {
            hideDialog(dialog, view, w, h, maxRadius);
        }
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private void hideDialog(final AlertDialog dialog, final View view, int w, int h, float maxRadius) {
        Animator anim = ViewAnimationUtils.createCircularReveal(view, w, h, maxRadius, 0);
        anim.setInterpolator(new FastOutLinearInInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sceneTransition(scene2);
                    }
                }, 150);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                dialog.dismiss();
            }
        });
        anim.setDuration(200);

        anim.start();
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private void showDialog(View view, int w, int h, float maxRadius) {
        Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view,
                w, h, 0, maxRadius);
        revealAnimator.setDuration(200);
        revealAnimator.setInterpolator(new FastOutLinearInInterpolator());
        view.setVisibility(View.VISIBLE);
        revealAnimator.start();
    }



    private static final class EnterAction implements Runnable, View.OnClickListener {
        private final SceneAnimator sceneAnimator;
        private final Scene scene;

        private EnterAction(@NonNull SceneAnimator sceneAnimator, @NonNull Scene scene) {
            this.sceneAnimator = sceneAnimator;
            this.scene = scene;
        }

        @TargetApi(VERSION_CODES.KITKAT)
        @Override
        public void run() {
            ViewGroup sceneRoot = scene.getSceneRoot();
            View view = sceneRoot.findViewById(R.id.view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            sceneAnimator.sceneTransition(scene);
        }
    }
}
