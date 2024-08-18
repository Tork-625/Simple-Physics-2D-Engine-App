package com.example.a2dphysicsengine;

public class vector {

    double x;
    double y;
    public vector v1;

    public double verysmallamount = 0.001;

    public vector(double x,double y) {
        this.x = x;
        this.y = y;
    }

    public vector divide(double p) {
        return new vector(x/p,y/p);
    }

    public vector mul(double p) {
        return new vector(p*x,p*y);
    }

    public vector addition(double p) {
        return new vector(p+x,p+y);
    }

    public vector add(vector v1) {
        return new vector(v1.x+x,v1.y+y);
    }

    public vector sub(vector v1) {
        return new vector(x-v1.x,y-v1.y);
    }

    public vector scale(double s) {
        return new vector(x*s,y*s);
    }

    public vector flip() {
        return new vector(-x,-y);
    }

    public vector normalize() {
        double magnitude = Math.sqrt((x*x) + (y*y));
        double magn = magnitude;
        return new vector(x/magn,y/magn);
    }

    public double dot(vector v1) {
        return ((x*v1.x) + (y*v1.y));
    }

    public double cross(vector v1) {
        return (x*v1.y-(y*v1.x));
    }

    public double mag() {
        return (Math.sqrt((x*x) + (y*y)));
    }

    public boolean nearlyEqual(vector v1) {

        if((Math.abs(x-v1.x)) < verysmallamount && (Math.abs(y-v1.y)) < verysmallamount ) {
            return true;
        }
        else {
            return false;
        }

    }
    public double lengthSq () {
        return ((x*x)+(y*y));
    }
}