package de.jandrotek.android.arobot.tab;

/**
 * Created by jan on 11.07.16.
 */

class SliderDataFilter {
    private static int filterSize;
    private int[] filterKernel;
    private int kernelSumma;

    public float getFilterOut() {
        return filterOut;
    }

    private float filterOut;

    public SliderDataFilter(int size){
        filterSize = size;
        filterKernel = new int[filterSize];
        kernelSumma = 0;
    }

    public float calcFilter(int input){
        for (int i = filterKernel.length - 1; i > 0 ; i--) {
            filterKernel[i] = filterKernel[i-1];
        }
        filterKernel[0] = input;
        kernelSumma += input;
        kernelSumma -= filterKernel[filterSize-1];
        filterOut = kernelSumma / (filterSize - 1);
        return filterOut;
    }
}
