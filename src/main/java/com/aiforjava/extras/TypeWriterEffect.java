package com.aiforjava.extras;

public class TypeWriterEffect {
    private String Message;
    private int delayMs;
    private int readDelayMs;
    private int traceBackDelayMs;

    public TypeWriterEffect(){
        this.Message="Welcome";
        this.delayMs=150;
        this.readDelayMs=500;
        this.traceBackDelayMs=25;
    }

    public TypeWriterEffect(String Message, int delayMs, int readDelayMs, int traceBackDelayMs){
        this.Message=Message;
        this.delayMs=delayMs;
        this.readDelayMs=readDelayMs;
        this.traceBackDelayMs=traceBackDelayMs;
    }

    public void generateEffect(){

    }
}
