package de.jandrotek.android.arobot.tab;

/**
 * Created by jan on 30.04.2017.
 */


//package com.monyetmabuk.rajawali.tutorials;

        import javax.microedition.khronos.egl.EGLConfig;
        import javax.microedition.khronos.opengles.GL10;

//        import rajawali.BaseObject3D;
//        import rajawali.animation.Animation3D;
//        import rajawali.animation.RotateAnimation3D;
//        import rajawali.animation.RotateAroundAnimation3D;
//        import rajawali.lights.PointLight;
//        import rajawali.math.Number3D;
//        import rajawali.math.Number3D.Axis;
//        import rajawali.parser.ObjParser;
//        import rajawali.parser.AParser.ParsingException;
//        import rajawali.renderer.RajawaliRenderer;

        import org.rajawali3d.Object3D;
        import org.rajawali3d.animation.Animation3D;
        import org.rajawali3d.animation.RotateAnimation3D;
        import org.rajawali3d.lights.DirectionalLight;
        import org.rajawali3d.lights.PointLight;
        import org.rajawali3d.loader.LoaderAWD;
        import org.rajawali3d.loader.LoaderOBJ;
        import org.rajawali3d.loader.ParsingException;
        import org.rajawali3d.materials.Material;
        import org.rajawali3d.materials.methods.DiffuseMethod;
        import org.rajawali3d.materials.textures.CubeMapTexture;
        import org.rajawali3d.math.vector.Vector3;
        import org.rajawali3d.renderer.Renderer;

        import android.content.Context;
        import android.view.MotionEvent;

public class RajawaliLoadModelRenderer extends Renderer {
//    public class RajawaliLoadModelRenderer extends RajawaliRenderer {
//    private PointLight mLight;
//    private Object3D mObjectGroup;
////    private BaseObject3D mObjectGroup;
//    private Animation3D mCameraAnim, mLightAnim;

    private DirectionalLight mLight;
    private Object3D mSteeringWheel;
//    private Object3D mMonkey;
    private Vector3 mAccValues;
    private Vector3 mRotateValues;
    private boolean mSceneOnceCreated = false;


    public RajawaliLoadModelRenderer(Context context) {
        super(context);
        setFrameRate(60);
        mRotateValues = new Vector3();
    }

    protected void initScene() {
        try {
            if(!mSceneOnceCreated) {
                mLight = new DirectionalLight(1f, 0.2f, -1.0f);
                mLight.setColor(1.0f, 1.0f, 1.0f);
                mLight.setPower(5);
                mSceneOnceCreated = true;
            }
            getCurrentScene().addLight(mLight);

                final LoaderOBJ objParser = new LoaderOBJ(mContext.getResources( ),mTextureManager, R.raw.wh3_obj );
//            final LoaderAWD objParser = new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.awd_suzanne);
                objParser.parse();
                mSteeringWheel = objParser.getParsedObject();
            getCurrentScene().addChild(mSteeringWheel);

            getCurrentCamera().setZ(7);

                int[] resourceIds = new int[]{R.drawable.posx, R.drawable.negx,
                        R.drawable.posy, R.drawable.negy, R.drawable.posz,
                        R.drawable.negz};

                Material material = new Material();
                material.enableLighting(true);
                material.setDiffuseMethod(new DiffuseMethod.Lambert());

                CubeMapTexture envMap = new CubeMapTexture("environmentMap",
                        resourceIds);
                envMap.isEnvironmentTexture(true);
                material.addTexture(envMap);
                material.setColorInfluence(0);
                mSteeringWheel.setMaterial(material);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        mSteeringWheel.setRotation(mRotateValues);
//        earthSphere.rotate(Vector3.Axis.Y, 1.0);
    }

    public void setRotateValues(float x, float y, float z){
        mRotateValues.setAll(x, y, z);
    }

    public void setSceneInitialized(boolean flag){
        mSceneInitialized = flag;
    }
}