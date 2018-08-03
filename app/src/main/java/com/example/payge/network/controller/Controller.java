package com.example.payge.network.controller;

public abstract class Controller {
    protected Object tag;

    protected Controller() {}

    protected Controller(Object tag) {
        this.tag = tag;
    }

}
