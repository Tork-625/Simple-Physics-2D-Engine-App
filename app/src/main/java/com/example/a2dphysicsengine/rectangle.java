package com.example.a2dphysicsengine;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;


public class rectangle {

    public Boolean frictionSwitch;
    public Boolean gravitySwitch;
    double centerX;
    double centerY;
    int posX;
    int posY;
    int positionx;
    int positiony;
    int width;
    int height;
    double angle;
    int colour;
    boolean active;
    double mass;
    vector vel;
    vector pos;
    double t ;
    double angularvelocity;
    rectangle bodyA;
    rectangle bodyB;
    vector[] vertices;
    double restitution;
    vector accleration;

    double inertia;
    double inverse_inertia;
    double inverse_mass;

    double StaticFriction;
    double DynamicFriction;

    vector rotatedAxis_x;
    vector rotatedAxis_y;

    public vector center;


    //coordinates:-
    public double coordinate1_x;
    public double coordinate1_y;
    public double coordinate2_x;
    public double coordinate2_y;
    public double coordinate3_x;
    public double coordinate3_y;
    public double coordinate4_x;
    public double coordinate4_y;

    public vector force = new vector(0,0);
    public vector g ; //  Acceleration due to gravity

    int fpsValue;
    double gValue;

    public double rotationFactorValue;

    public rectangle(vector pos, int width, int height, double angle, int colour, boolean active, double mass, vector vel, float restitution) {


        this.frictionSwitch = Settings.sharedPreferences.getBoolean("frictionSwitch", true);
        this.gravitySwitch = Settings.sharedPreferences.getBoolean("gravitySwitch", true);
        this.fpsValue = Settings.sharedPreferences.getInt("fpsValue", 300);
        this.rotationFactorValue = Double.longBitsToDouble(Settings.sharedPreferences.getLong("rotationFactorValue", Double.doubleToLongBits(16)));
        this.gValue = Double.longBitsToDouble(Settings.sharedPreferences.getLong("gValue", Double.doubleToLongBits(9.8 * 70)));


        this.t = 1f/fpsValue;
        //  Acceleration due to gravity

        if(gravitySwitch == false){
            this.g = new vector(0,0);
        }
        else{
            this.g = new vector(0,gValue);
        }

        this.width = width;
        this.height = height;
        this.colour = colour;
        this.active = active;
        this.mass = mass;
        this.vel = vel;
        this.pos = pos;
        this.positionx = (int) pos.x;
        this.positiony = (int) pos.y;
        this.angle = (angle);
        this.restitution = restitution;
        calculateInertia();
        this.inverse_mass = 1.0/mass;
        this.StaticFriction = 0.8;
        this.DynamicFriction = 0.6;
    }

    public void AddForce(vector force) {
        this.force = this.force.add(force);
    }

    public void update() {

        if (active == true) {

            //calculations :-

            //adding the available force
            force = force.add(g); //(adding only gravity as force)

            // linear change Calculations
            accleration = force.divide(mass);
            vel = vel.add(accleration.mul(t));
            pos = pos.add(vel.mul(t));

            //rotational change calculations
            angle -= (float)angularvelocity*rotationFactorValue;

            //System.out.print("velocity x = "+vel.x + " ; velocity y = "+vel.y + " ; angular velocity = " + angularvelocity +"\n");
            //resetting force to zero after every frame
            force = force.mul(0);
            //removing the velocity accumulated due to gravity per frame
            //vel = vel.sub(g.mul(t/mass));

            posX = (int)pos.x;
            posY = (int)pos.y;

        }
        if (active == false) {
            posX = (int) positionx;
            posY = (int) positiony;
        }

    }

    public void draw(Paint paint, Canvas canvas) {

        canvas.save();

        paint.setColor(colour);
        canvas.rotate((float) Math.toRadians(angle),(posX+width/2f),(posY+height/2f));
        canvas.drawRect(posX, posY, posX+width, posY+height,paint);

        //paint.setColor(Color.MAGENTA);
        //canvas.drawRect((int)center.x, (int)center.y, (int)center.x+5, (int)center.y+5,paint);
        //defineCoordinates();
       /* for(int i = 0;i<=3;i++) {
            paint.setColor(Color.RED);
            canvas.drawOval((int)vertices[i].x,(int)vertices[i].y,(int)vertices[i].x+ 3,(int)vertices[i].y+ 3,paint);
        }*/

        canvas.restore();
    }

    public void defineCoordinates() {
        coordinate1_x = pos.x;
        coordinate1_y = pos.y;
        coordinate2_x = (pos.x + width);
        coordinate2_y = pos.y;
        coordinate3_x = (pos.x + width);
        coordinate3_y = (pos.y + height);
        coordinate4_x = pos.x;
        coordinate4_y = (pos.y + height);

        rotatedAxis_x = new vector(Math.cos(Math.toRadians(angle)), Math.sin(Math.toRadians(angle)));
        rotatedAxis_y = new vector(-Math.sin(Math.toRadians(angle)), Math.cos(Math.toRadians(angle)));

        vertices = new vector[4];
        vertices[0] = new vector(coordinate1_x, coordinate1_y);
        vertices[1] = new vector(coordinate2_x, coordinate2_y);
        vertices[2] = new vector(coordinate3_x, coordinate3_y);
        vertices[3] = new vector(coordinate4_x, coordinate4_y);

        // Calculate center of the rectangle
        double centerX = (vertices[0].x + vertices[1].x + vertices[2].x + vertices[3].x) / 4;
        double centerY = (vertices[0].y + vertices[1].y + vertices[2].y + vertices[3].y) / 4;

        // Create a matrix for rotation
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.setRotate((float)Math.toRadians(angle), (float) centerX, (float) centerY);

        // Apply rotation to each vertex
        float[] vertexArray = new float[8];
        for (int i = 0; i < vertices.length; i++) {
            vertexArray[i * 2] = (float) vertices[i].x;
            vertexArray[i * 2 + 1] = (float) vertices[i].y;
        }
        rotationMatrix.mapPoints(vertexArray);

        // Update the vertices with rotated coordinates
        for (int i = 0; i < vertices.length; i++) {
            vertices[i].x = vertexArray[i * 2];
            vertices[i].y = vertexArray[i * 2 + 1];
        }

        // Calculate center of the rotated rectangle
        double centerTransformedX = (vertices[0].x + vertices[1].x + vertices[2].x + vertices[3].x) / 4;
        double centerTransformedY = (vertices[0].y + vertices[1].y + vertices[2].y + vertices[3].y) / 4;

        center = new vector(centerTransformedX, centerTransformedY);
    }

    public void calculateInertia(){
        if(active == true) {
            this.inertia = (1.0/12)*(mass)*((height*height) + (width*width));
            this.inverse_inertia = 1/inertia;
        }
        else {
            this.inverse_inertia = 0;
        }
    }

    public boolean nearlyEqual(double one , double two) {

        if((Math.abs(one) < two)) {
            return true;

        }
        else {
            return false;
        }
    }

}