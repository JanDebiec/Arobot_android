package de.jandrotek.android.arobot.tab;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;

/**
 * Created by jan on 13.04.17.
 */

public class TiltRenderer extends Renderer {

    public  Context mContext;
    private DirectionalLight directionalLight;
    private Sphere earthSphere;
    private Vector3 mRotateValues;

    public TiltRenderer(Context context){
        super(context);
        mContext = context;
        mRotateValues = new Vector3();
    }

    @Override
    protected void initScene() {
        directionalLight = new DirectionalLight(1f, 0.2f, -1.0f);
        directionalLight.setColor(1.0f, 1.0f, 1.0f);
        directionalLight.setPower(2);
        getCurrentScene().addLight(directionalLight);

        Material material = new Material();
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        material.setColor(0);

        Texture earthTexture = new Texture("Earth", R.drawable.earthtruecolor_nasa_big);
        try{
            material.addTexture(earthTexture);
        } catch (ATexture.TextureException error){
            Log.d("Debug", "Texture error");
        }

        earthSphere  = new Sphere(1, 24, 24);
        earthSphere.setMaterial(material);
        getCurrentScene().addChild(earthSphere);
        getCurrentCamera().setZ(4.2f);
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        super.onRender(ellapsedRealtime, deltaTime);
        earthSphere.setRotation(mRotateValues);
//        earthSphere.rotate(Vector3.Axis.Y, 1.0);
    }

    public void setRotateValues(float x, float y, float z){
        mRotateValues.setAll(x, y, z);
    }

}
