package de.jandrotek.android.arobot.tab;

/**
 * Created by jan on 13.07.16.
 */

public class SliderDatafilterOpt {
    private static int filterSize;
    private static final int BUFFER_SIZE = 8;
    private int head, tail;
    private int[] filterKernel;
    private int kernelSumma;
    private float filterOut;

    public SliderDatafilterOpt(int size) {
        filterSize = size;
        filterKernel = new int[BUFFER_SIZE];
        kernelSumma = 0;
        head = 0;
        tail = BUFFER_SIZE - filterSize;
    }

    public float calcFilter(int input){
        filterKernel[head] = input;
        kernelSumma += input;
        kernelSumma -= filterKernel[tail];
        filterOut = kernelSumma / (filterSize);
        head += 1;
        tail += 1;
        if(head >= BUFFER_SIZE)
            head = 0;
        if(tail >= BUFFER_SIZE)
            tail = 0;
        return filterOut;
    }

}
