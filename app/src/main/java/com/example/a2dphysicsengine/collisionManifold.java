package com.example.a2dphysicsengine;

public class collisionManifold {

    public rectangle bodyA;
    public rectangle bodyB;
    public double depth;
    public vector normal;
    public vector contact1;
    public vector contact2;
    public int contactCount;
    public vector[] contacts;

    public collisionManifold(rectangle bodyA,rectangle bodyB,double depth,vector normal,vector contact1,vector contact2,
                             int contactCount,vector [] contacts)
    {
        this.bodyA = bodyA;
        this.bodyB = bodyB;
        this.depth = depth;
        this.normal = normal;
        this.contact1 = contact1;
        this.contact2 = contact2;
        this.contactCount = contactCount;
        this.contacts = contacts;
    }



}
