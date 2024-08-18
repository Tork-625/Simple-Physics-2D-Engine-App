package com.example.a2dphysicsengine;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class renderPanel extends View implements Runnable {

    public Boolean frictionSwitch;
    public Boolean gravitySwitch;
    public boolean isTouching = false;
    private vector touchStart;
    private Queue<MotionEvent> touchEventQueue = new LinkedList<>();

    public vector drag_direction;

    ////Note:-
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////--]  PRE-SET VALUES FOR EARTH LIKE SIMULATION :-                        ////
    ////-->  FPS = 600  iteration = 1 (times the collision loop runs per frame) ////
    ////-->  angular velocity divided by 6 or 8 in the com.example.a2dphysicsengine.rectangle class          ////
    ////-->  t in the com.example.a2dphysicsengine.rectangle class should be divided by the FPS value        ////
    ////-->  acceleration due to gravity (g) is multiplied by 50                ////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //// THINGS TO BE FIXED:-                                                                                                     ////
    //// 1) the velocity in y direction keeps on increasing forever when the object is at rest due to acceleration due to gravity.////
    //// 2) the velocity in x direction never becomes zero.                                                                       ////
    //// 3) the angular velocity never becomes zero due to which the object experiences Jittering.                                ////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public boolean drawn;
    public double verysmallamount = 0.001;
    public double mindistanceSqr;

    public Paint paint;
    int objectNo = 11;
    Thread thread1;
    public int forcemag ;
    public int FPS ;
    public int iterations = 1;
    public int screenwidth;
    public int screenheight;
    public float drawInterval;
    public double depth;
    public double axisdepth;
    public vector normal;

    //Object Creation:-// // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    rectangle[] rect;
    public rectangle bodyA;
    public rectangle bodyB;
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    List<collisionManifold> contactList;

    public vector contact1;
    public vector contact2;

    public vector [] contacts;
    public vector rotationVectorA;
    public vector rotationVectorB;
    public vector rotationVectorAnormal;
    public vector rotationVectorBnormal;
    public vector angularLinearVelocityA;
    public vector angularLinearVelocityB;
    public vector impulseVector;
    public vector [] impulseArray;
    public double [] impulses;

    public vector [] frictionvector;

    public int contactCount;
    public vector cp;
    public double distanceSqr;
    public Random random = new Random();
    public vector Tangent;
    private KeyHandler keyH;

    public renderPanel(Context context, AttributeSet attrs) {
        super(context, attrs);


        this.FPS = Settings.sharedPreferences.getInt("fpsValue", 300);
        this.forcemag = (int)Double.longBitsToDouble(Settings.sharedPreferences.getLong("movingForceValue", Double.doubleToLongBits(800)));
        this.frictionSwitch = Settings.sharedPreferences.getBoolean("frictionSwitch", true);
        this.gravitySwitch = Settings.sharedPreferences.getBoolean("gravitySwitch", true);

        paint = new Paint();
        keyH = new KeyHandler();
        setFocusableInTouchMode(true); // Ensure the view can receive key events
        requestFocus(); // Request focus for the view
        setOnKeyListener(keyH); // Attach the KeyHandler to the view


        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenwidth = size.x;
        screenheight = size.y;

        //OBJECT CREATION////////////////////////////////////////////////////////////////////////////////////////////////////
        rect = new rectangle[18];
        rect[0] = new rectangle(new vector(screenwidth/2f,screenheight/4),200,100,0,Color.GREEN,true,0.5f,new vector(0f,0f),0.001f);
        //boundary//////////////////////////////////////////////////////////////////////////////////////////////////////////
        rect[1] = new rectangle(new vector(0f,0f),screenwidth,20,0,Color.GRAY,false,100f,new vector(0f,0f),0.001f);
        rect[2] = new rectangle(new vector(0f,0f),20,screenheight,0,Color.GRAY,false,100f,new vector(0f,0f),0.001f);
        rect[3] = new rectangle(new vector(0f,screenheight-20),screenwidth,20,0,Color.GRAY,false,100f,new vector(0f,0f),0.001f);
        rect[4] = new rectangle(new vector(screenwidth-20,0f),20,screenheight,0,Color.GRAY,false,100f,new vector(0f,0f),0.001f);
        //Ledges///////////////////////////////////////////////////////////////////////////////////////////////////////////
        rect[5] = new rectangle(new vector(screenwidth/8f,screenheight/1.5),(int) (screenwidth-2*screenwidth/8f),75,0,Color.GRAY,false,100f,new vector(0f,0f),0.001f);
        rect[6] = new rectangle(new vector(screenwidth/8f,screenheight/4), 200, 75,0,Color.GRAY,false,100f,new vector(0f,0f),0.001f);
        rect[7] = new rectangle(new vector(screenwidth-screenwidth/8f-200,screenheight/4),200,75,0,Color.GRAY,false,100f,new vector(0f,0f),0.001f);
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Extra Objects
        rect[8] = new rectangle(new vector(600,400),200,100,0, Color.BLUE,true,0.5f,new vector(0f,0f),0.001f);
        rect[9] = new rectangle(new vector(300,200),200,100,0, Color.WHITE,true,0.5f,new vector(0f,0f),0.001f);
        rect[10] = new rectangle(new vector(700,500),200,100,0, Color.CYAN,true,0.5f,new vector(0f,0f),0.001f);
        rect[11] = new rectangle(new vector(500,800),200,100,0, Color.MAGENTA,true,0.5f,new vector(0f,0f),0.001f);

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        contactList = new ArrayList<>();


    }

    // Method to handle touch events
    public void handleTouchEvents() {
        while (!touchEventQueue.isEmpty()) {
            MotionEvent event = touchEventQueue.poll();

            // Get the first touch point in the MotionEvent
            float x = event.getX();
            float y = event.getY();
            int action = event.getActionMasked(); // Get the action for this touch point

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    // Check if touch is within the rectangle
                    if (touch_detection(x, y)) {
                        touchStart = new vector(x, y);
                        isTouching = true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isTouching) {
                        // Calculate the delta movement for this touch point
                        drag_direction = (new vector(x, y).sub(touchStart));
                        rect[0].AddForce(drag_direction.mul(forcemag));
                        //rect[0].pos = (new vector(x,y));
                        touchStart = new vector(x, y);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    // Check if this touch point is the last one to be lifted
                    isTouching = false; // Touch event ended
                    break;
            }
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchEventQueue.offer(event);
        return true;
    }

    public boolean touch_detection(float x, float y){
        vector touch_points = new vector (x,y);
        //edge calculation
        for(int i = 0; i<4 ; i++){
            vector edge = rect[0].vertices[i].sub(rect[0].vertices[(i+1) % 4]);
            vector point_Dis = rect[0].vertices[i].sub(touch_points);
            double liesWithin = edge.cross(point_Dis);
            if (liesWithin < 0){
                return false;
            }
        }
        return true;
    }

    public void startRender() {
        thread1 = new Thread(this);
        thread1.start();
    }

    public void run() {

        double delta = 0;
        double drawInterval = 1000000000/FPS;
        long lastTime = System.nanoTime();
        long currentTime;
        long fpsCounter = 0;
        int drawCount = 0;
        this.drawn = true;

        while(thread1 != null) {
            currentTime = System.nanoTime();
            delta += ((currentTime-lastTime)/drawInterval);
            fpsCounter += currentTime-lastTime;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                handleTouchEvents();
                /*
                if (this.drawn == true){
                    update();
                    drawn = false;
                }*/
                invalidate();
                delta--;
                drawCount++;
            }

            if (fpsCounter >= 1000000000) {
                System.out.print("FPS:"+drawCount+"\n");
                fpsCounter = 0;
                drawCount = 0;
            }


        }
    }

    public void update() {
        ////////////////////////////////////////////////////////////
        if (keyH.uppressed == true) {
            rect[0].AddForce(new vector(0,-forcemag));
        }
        if (keyH.downpressed == true) {
            rect[0].AddForce(new vector(0,forcemag));
        }
        if (keyH.leftpressed == true) {
            rect[0].AddForce(new vector(-forcemag,0));
        }
        if (keyH.rightpressed == true) {
            rect[0].AddForce(new vector(forcemag,0));
        }
        if (keyH.rotate == true) {
            /*if (rect[0].angle >= 360){
               rect[0].angle = 0;
            }*/
            rect[0].angle += 5;
        }
        ////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////
        for (int i = 0; i<=objectNo;i++) {
            rect[i].update();
            rect[i].defineCoordinates();
        }

        for(int j = 1; j <= iterations;j++) {
            contactList.clear();
            HandelCollosions();
        }
        this.drawn = false;
    }


    public void onDraw(Canvas canvas) {
        //Background Color
        canvas.drawColor(Color.BLACK);
        paint.setColor(Color.GREEN);

        for (int i = 0; i<=objectNo;i++) {
            rect[i].draw(paint,canvas);
        }

        this.drawn = true;

    }




    public void HandelCollosions() {


        for(int i = 0; i<objectNo+1;i++) {
            this.bodyA = rect[i];
            bodyA.defineCoordinates();
            for(int j = i+1; j<objectNo+1;j++) {
                try {
                    this.bodyB = rect[j];
                    bodyB.defineCoordinates();
                }catch(Exception ex) {
                    continue;
                }
                boolean collide = collosion(bodyA,bodyB);
                if(collide == true) {
                    if(bodyA.active == true && bodyB.active == true) {
                        rect[i].pos.x += (float)(normal.mul(depth/2)).x;
                        rect[i].pos.y += (float)(normal.mul(depth/2)).y;

                        rect[j].pos.x -= (float)(normal.mul(depth/2)).x;
                        rect[j].pos.y -= (float)(normal.mul(depth/2)).y;
                    }
                    else if(bodyA.active == true && bodyB.active == false) {
                        rect[i].pos.x += (float)(normal.mul(depth)).x;
                        rect[i].pos.y += (float)(normal.mul(depth)).y;
                    }
                    else if(bodyA.active == false && bodyB.active == true){
                        rect[j].pos.x -= (float)(normal.mul(depth)).x;
                        rect[j].pos.y -= (float)(normal.mul(depth)).y;
                    }

                    contacts = new vector [2];
                    findContactPoints(bodyA,bodyB);
                    collisionManifold contact = new collisionManifold(this.bodyA,this.bodyB,this.depth,this.normal,this.contact1,
                            this.contact2,this.contactCount,this.contacts);
                    contactList.add(contact);

                }
            }
        }

        for(int m = 0; m < contactList.size() ; m++) {
            collisionManifold contact = contactList.get(m);

            if(frictionSwitch == true){
                ResolveCollosionWithRotationAndFriction(contact);
            }
            else{
                ResolveCollosionWithRotation(contact);
            }
            //ResolveCollosionRealisticly(contact);
            //ResolveCollosionWithRotation(contact);
            //ResolveCollosionWithRotationAndFriction(contact);

            //drawContacts(contact,paint,new Canvas());
        }

    }

    private void drawContacts(com.example.a2dphysicsengine.collisionManifold contacter,Paint paint,Canvas canvas) {
        for(int i = 0; i < contacter.contactCount ; i++) {
            paint.setColor(Color.BLUE);
            canvas.drawRect((int)contacter.contacts[i].x,(int)contacter.contacts[i].y,(int)contacter.contacts[i].x+3,(int)contacter.contacts[i].y+3,paint);
        }
    }

    public boolean collosion(rectangle bodyA,rectangle bodyB){
        double projection;
        vector []axis;
        depth = Double.MAX_VALUE;

        axis = new vector[8];
        axis[0] = (bodyA.vertices[1].sub(bodyA.vertices[0]));
        axis[0] = new vector(-axis[0].y,axis[0].x).normalize();
        axis[1] = (bodyA.vertices[2].sub(bodyA.vertices[1]));
        axis[1] = new vector(-axis[1].y,axis[1].x).normalize();
        axis[2] = (bodyA.vertices[3].sub(bodyA.vertices[2]));
        axis[2] = new vector(-axis[2].y,axis[2].x).normalize();
        axis[3] = (bodyA.vertices[0].sub(bodyA.vertices[3]));
        axis[3] = new vector(-axis[3].y,axis[3].x).normalize();

        axis[4] = (bodyB.vertices[1].sub(bodyB.vertices[0]));
        axis[4] = new vector(-axis[4].y,axis[4].x).normalize();
        axis[5] = (bodyB.vertices[2].sub(bodyB.vertices[1]));
        axis[5] = new vector(-axis[5].y,axis[5].x).normalize();
        axis[6] = (bodyB.vertices[3].sub(bodyB.vertices[2]));
        axis[6] = new vector(-axis[6].y,axis[6].x).normalize();
        axis[7] = (bodyB.vertices[0].sub(bodyB.vertices[3]));
        axis[7] = new vector(-axis[7].y,axis[7].x).normalize();


        for(int i = 0;i<axis.length;i++) {

            double bodyA_min = axis[i].dot(bodyA.vertices[0]);
            double bodyA_max = axis[i].dot(bodyA.vertices[0]);
            double bodyB_min = axis[i].dot(bodyB.vertices[0]);
            double bodyB_max = axis[i].dot(bodyB.vertices[0]);

            for(int j = 0;j<4;j++) {
                projection = axis[i].dot(bodyA.vertices[j]);
                if(projection > bodyA_max) {
                    bodyA_max = projection;
                }
                if(projection < bodyA_min) {
                    bodyA_min = projection;
                }

            }
            for(int j = 0;j<4;j++) {
                projection = axis[i].dot(bodyB.vertices[j]);
                if(projection > bodyB_max) {
                    bodyB_max = projection;
                }
                if(projection < bodyB_min) {
                    bodyB_min = projection;
                }

            }

            if(bodyA_min >= bodyB_max || bodyB_min >= bodyA_max) {
                return false;
            }

            axisdepth = Math.min(bodyA_max-bodyB_min,bodyB_max-bodyA_min);

            if(axisdepth < depth) {
                depth = axisdepth;
                normal = axis[i];
            }
        }

        depth = depth/normal.mag();
        normal = normal.normalize();

        vector direction = (bodyB.center.sub(bodyA.center));

        if (direction.dot(normal) >= 0) {
            normal = normal.mul(-1);
        }

        return true;
    }

    public void ResolveCollosionRealisticly(collisionManifold contact){

        bodyA = contact.bodyA;
        bodyB = contact.bodyB;
        depth = contact.depth;
        normal = contact.normal;

        double e = Math.min(bodyA.restitution,bodyB.restitution);
        vector relativeVelocity = bodyB.vel.sub(bodyA.vel);

        if (normal.dot(relativeVelocity) < 0) {
            return;
        }

        double impulse = (relativeVelocity.dot(normal))*(-1f-e);
        impulse /= (1/bodyA.mass)+(1/bodyB.mass);

        bodyA.vel = bodyA.vel.sub(normal.mul(impulse/bodyA.mass));
        bodyB.vel = bodyB.vel.add(normal.mul(impulse/bodyB.mass));
    }

    public void ResolveCollosionWithRotation(collisionManifold contact) {
        bodyA = contact.bodyA;
        bodyB = contact.bodyB;
        depth = contact.depth;
        normal = contact.normal;
        contact1 = contact.contact1;
        contact2 = contact.contact2;
        contacts = contact.contacts;
        contactCount = contact.contactCount;
        double e = Math.min(bodyA.restitution,bodyB.restitution);
        impulseArray = new vector[2];
        vector []rotationVectorsA = new vector[2];
        vector []rotationVectorsB = new vector[2];
        impulses = new double[2];

        for(int i = 0; i < contact.contactCount ; i++) {
            rotationVectorA = new vector(0,0);
            rotationVectorB = new vector(0,0);
            angularLinearVelocityA = new vector(0,0);
            angularLinearVelocityB = new vector(0,0);
            rotationVectorAnormal = new vector(0,0);
            rotationVectorBnormal = new vector(0,0);
        }

        // this loop calculates impulses
        for(int i = 0; i < contact.contactCount ; i++) {

            rotationVectorA = bodyA.center.sub(contacts[i]);
            rotationVectorB = bodyB.center.sub(contacts[i]);

            rotationVectorAnormal = new vector(-rotationVectorA.y,rotationVectorA.x);
            rotationVectorBnormal = new vector(-rotationVectorB.y,rotationVectorB.x);

            angularLinearVelocityA = rotationVectorAnormal.mul(bodyA.angularvelocity);
            angularLinearVelocityB = rotationVectorBnormal.mul(bodyB.angularvelocity);

            vector kay = bodyB.vel.add(angularLinearVelocityB);
            vector may = bodyA.vel.add(angularLinearVelocityA);

            vector relativeVelocity = (kay).sub(may);

            rotationVectorsA[i] = rotationVectorA;
            rotationVectorsB[i] = rotationVectorB;

            double ArotationNDotN = rotationVectorAnormal.dot(normal);
            double BrotationNDotN = rotationVectorBnormal.dot(normal);

            double contactNormalMag = normal.dot(relativeVelocity);

            if (contactNormalMag < 0) {
                continue;
            }

            double deno = (bodyA.inverse_mass)+(bodyB.inverse_mass)+(ArotationNDotN*ArotationNDotN)*(bodyA.inverse_inertia)+
                    (BrotationNDotN*BrotationNDotN)*(bodyB.inverse_inertia);

            double impulse = (contactNormalMag)*((-1f-e));


            impulse /= deno;
            impulse /= contactCount;
            impulses[i] = impulse;
            impulseVector = normal.mul(impulse);
            impulseArray[i] = impulseVector;
        }

        // and this loop applies impulses

        for(int j = 0; j < contact.contactCount ; j++) {

            if (impulseArray[j] == null) {
                continue;
            }

            bodyA.vel = bodyA.vel.sub(impulseArray[j].mul(bodyA.inverse_mass));
            bodyA.angularvelocity -= ((rotationVectorsA[j].cross(impulseArray[j]))*(bodyA.inverse_inertia));

            bodyB.vel = bodyB.vel.add(impulseArray[j].mul(bodyB.inverse_mass));
            bodyB.angularvelocity += ((rotationVectorsB[j].cross(impulseArray[j]))*(bodyB.inverse_inertia));
        }
    }

    public void ResolveCollosionWithRotationAndFriction(collisionManifold contact) {
        bodyA = contact.bodyA;
        bodyB = contact.bodyB;
        depth = contact.depth;
        normal = contact.normal;
        contact1 = contact.contact1;
        contact2 = contact.contact2;
        contacts = contact.contacts;
        contactCount = contact.contactCount;
        double e = Math.min(bodyA.restitution,bodyB.restitution);
        impulseArray = new vector[2];
        vector []rotationVectorsA = new vector[2];
        vector []rotationVectorsB = new vector[2];
        impulses = new double[2];
        frictionvector = new vector[2];

        double sf = (bodyA.StaticFriction + bodyB.StaticFriction)*0.5;
        double df = (bodyA.DynamicFriction + bodyB.DynamicFriction)*0.5;

        for(int i = 0; i < contact.contactCount ; i++) {
            rotationVectorA = new vector(0,0);
            rotationVectorB = new vector(0,0);
            angularLinearVelocityA = new vector(0,0);
            angularLinearVelocityB = new vector(0,0);
            rotationVectorAnormal = new vector(0,0);
            rotationVectorBnormal = new vector(0,0);
            frictionvector[i] = new vector(0,0);
            impulses[i] = 0;
            Tangent = new vector(0,0);
        }

        // this loop calculates impulses
        for(int i = 0; i < contact.contactCount ; i++) {

            rotationVectorA = bodyA.center.sub(contacts[i]);
            rotationVectorB = bodyB.center.sub(contacts[i]);

            rotationVectorAnormal = new vector(-rotationVectorA.y,rotationVectorA.x);
            rotationVectorBnormal = new vector(-rotationVectorB.y,rotationVectorB.x);

            angularLinearVelocityA = rotationVectorAnormal.mul(bodyA.angularvelocity);
            angularLinearVelocityB = rotationVectorBnormal.mul(bodyB.angularvelocity);

            vector kay = bodyB.vel.add(angularLinearVelocityB);
            vector may = bodyA.vel.add(angularLinearVelocityA);

            vector relativeVelocity = (kay).sub(may);

            rotationVectorsA[i] = rotationVectorA;
            rotationVectorsB[i] = rotationVectorB;

            double ArotationNDotN = rotationVectorAnormal.dot(normal);
            double BrotationNDotN = rotationVectorBnormal.dot(normal);

            double contactNormalMag = normal.dot(relativeVelocity);

            if (contactNormalMag < 0) {
                continue;
            }

            double deno = (bodyA.inverse_mass)+(bodyB.inverse_mass)+(ArotationNDotN*ArotationNDotN)*(bodyA.inverse_inertia)+
                    (BrotationNDotN*BrotationNDotN)*(bodyB.inverse_inertia);

            double impulse = (contactNormalMag)*((-1f-e));


            impulse /= deno;
            impulse /= contactCount;
            impulses[i] = impulse;
            impulseVector = normal.mul(impulse);
            impulseArray[i] = impulseVector;
        }

        // and this loop applies impulses

        for(int j = 0; j < contact.contactCount ; j++) {

            if (impulseArray[j] == null) {
                continue;
            }

            bodyA.vel = bodyA.vel.sub(impulseArray[j].mul(bodyA.inverse_mass));
            bodyA.angularvelocity -= ((rotationVectorsA[j].cross(impulseArray[j]))*(bodyA.inverse_inertia));

            bodyB.vel = bodyB.vel.add(impulseArray[j].mul(bodyB.inverse_mass));
            bodyB.angularvelocity += ((rotationVectorsB[j].cross(impulseArray[j]))*(bodyB.inverse_inertia));
        }

        // this loop calculates friction impulses
        for(int i = 0; i < contact.contactCount ; i++) {

            rotationVectorA = bodyA.center.sub(contacts[i]);
            rotationVectorB = bodyB.center.sub(contacts[i]);

            rotationVectorAnormal = new vector(-rotationVectorA.y,rotationVectorA.x);
            rotationVectorBnormal = new vector(-rotationVectorB.y,rotationVectorB.x);

            angularLinearVelocityA = rotationVectorAnormal.mul(bodyA.angularvelocity);
            angularLinearVelocityB = rotationVectorBnormal.mul(bodyB.angularvelocity);

            vector kay = bodyB.vel.add(angularLinearVelocityB);
            vector may = bodyA.vel.add(angularLinearVelocityA);

            vector relativeVelocity = (kay).sub(may);

            rotationVectorsA[i] = rotationVectorA;
            rotationVectorsB[i] = rotationVectorB;

            Tangent = relativeVelocity.sub(normal.mul(relativeVelocity.dot(normal)));

            if(Tangent.nearlyEqual(new vector(0.0001,0.0001)) == true) {
                continue;
            }
            else {
                Tangent = Tangent.normalize();
            }

            double ArotationNDotT = rotationVectorAnormal.dot(Tangent);
            double BrotationNDotT = rotationVectorBnormal.dot(Tangent);

            double deno = (bodyA.inverse_mass)+(bodyB.inverse_mass)+(ArotationNDotT*ArotationNDotT)*(bodyA.inverse_inertia)+
                    (BrotationNDotT*BrotationNDotT)*(bodyB.inverse_inertia);

            double impulseT = -(Tangent.dot(relativeVelocity));


            impulseT /= deno;
            impulseT /= contactCount;
            vector FrictionimpulseVector;

            if (Math.abs(impulseT) <= (impulses[i]*sf)) {
                FrictionimpulseVector = Tangent.mul(impulseT);
            }
            else {
                FrictionimpulseVector = Tangent.mul(-impulses[i]*df);
            }

            frictionvector[i] = FrictionimpulseVector;
        }

        // and this loop applies friction impulses

        for(int j = 0; j < contact.contactCount ; j++) {

            if (frictionvector[j] == null) {
                continue;
            }

            bodyA.vel = bodyA.vel.add(frictionvector[j].mul(bodyA.inverse_mass));
            bodyA.angularvelocity += ((rotationVectorsA[j].cross(frictionvector[j]))*(bodyA.inverse_inertia));

            bodyB.vel = bodyB.vel.sub(frictionvector[j].mul(bodyB.inverse_mass));
            bodyB.angularvelocity -= ((rotationVectorsB[j].cross(frictionvector[j]))*(bodyB.inverse_inertia));
        }
    }

    public void findContactPoints(rectangle bodyA,rectangle bodyB) {
        this.bodyA = bodyA;
        this.bodyB = bodyB;

        this.contact1 = new vector(0,0);
        this.contact2 = new vector(0,0);
        this.contacts[0] = new vector(0,0);
        this.contacts[1] = new vector(0,0);
        this.contactCount = 0;
        this.mindistanceSqr = Double.MAX_VALUE;

        for(int i = 0; i <=3;i++) {
            vector A = bodyA.vertices[i];

            for(int j = 0; j<=3;j++) {
                vector B1 = bodyB.vertices[j];
                vector B2 = bodyB.vertices[(j+1) % 4];

                pointSegmentDistance(A,B1,B2);

                if(nearlyEqual(distanceSqr,mindistanceSqr) == true) {
                    if(cp.nearlyEqual(contact1) == false) {
                        this.contact2 = cp;
                        this.contacts[1] = cp;
                        this.contactCount = 2;
                    }
                }

                else if(distanceSqr < mindistanceSqr) {
                    this.mindistanceSqr = distanceSqr;
                    this.contactCount = 1;
                    this.contact1 = cp;
                    this.contacts[0] = cp;
                }
            }
        }

        for(int i = 0; i <=3;i++) {
            vector A = bodyB.vertices[i];

            for(int j = 0; j<=3;j++) {
                vector B1 = bodyA.vertices[j];
                vector B2 = bodyA.vertices[(j+1) % 4];

                pointSegmentDistance(A,B1,B2);

                if(nearlyEqual(distanceSqr,mindistanceSqr) == true) {
                    if(cp.nearlyEqual(contact1) == false) {
                        this.contact2 = cp;
                        this.contacts[1] = cp;
                        this.contactCount = 2;
                    }
                }

                else if(distanceSqr < mindistanceSqr) {
                    mindistanceSqr = distanceSqr;
                    this.contactCount = 1;
                    this.contact1 = cp;
                    this.contacts[0] = cp;
                }
            }
        }
    }

    public void pointSegmentDistance(vector a, vector b1,vector b2){

        vector ab1 = a.sub(b1);
        vector b1b2 = b2.sub(b1);

        double proj = b1b2.dot(ab1);

        double lengthSq = Math.pow(b1b2.mag(),2);

        double normalizedEdgeDistance = proj/lengthSq;

        if (normalizedEdgeDistance >= 1) {
            this.cp = b2;
        }
        else if (normalizedEdgeDistance <= 0) {
            this.cp = b1;
        }
        else {
            this.cp =  b1.add(b1b2.mul(normalizedEdgeDistance));
        }

        this.distanceSqr = Math.pow(((a.sub(cp)).mag()),2);
    }

    public boolean nearlyEqual(double one , double two) {

        if((Math.abs(one-two)) < verysmallamount) {
            return true;

        }

        return false;
    }

}
