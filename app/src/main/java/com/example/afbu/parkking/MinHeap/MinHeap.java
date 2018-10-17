package com.example.afbu.parkking.MinHeap;

import java.util.HashMap;
import java.util.Map;

public class MinHeap {
    private Float[] Heap;
    private String[] HeapKeys;
    private int size;
    private int maxsize;

    private static final int FRONT = 1;

    public MinHeap(int maxsize) {
        this.maxsize = maxsize;
        this.size = 0;
        Heap = new Float[this.maxsize + 1];
        HeapKeys = new String[this.maxsize + 1];
        Heap[0] = Float.MIN_VALUE;
        HeapKeys[0] = "-1,-1";
    }

    private int parent(int pos) {
        return pos / 2;
    }


    private int leftChild(int pos) {
        return (2 * pos);
    }


    private int rightChild(int pos) {
        return (2 * pos) + 1;
    }


    private boolean isLeaf(int pos) {
        if (pos >= (size / 2) && pos <= size) {
            return true;
        }

        return false;
    }

    private void swap(int fpos, int spos) {
        Float tmp;

        tmp = Heap[fpos];
        Heap[fpos] = Heap[spos];
        Heap[spos] = tmp;

        String tmpKey;

        tmpKey = HeapKeys[fpos];
        HeapKeys[fpos] = HeapKeys[spos];
        HeapKeys[spos] = tmpKey;
    }

    private void minHeapify(int pos) {
        if (!isLeaf(pos)) {
            if (Heap[pos] > Heap[leftChild(pos)] || Heap[pos] > Heap[rightChild(pos)]) {
                if (Heap[leftChild(pos)] < Heap[rightChild(pos)]) {
                    swap(pos, leftChild(pos));
                    minHeapify(leftChild(pos));
                } else {
                    swap(pos, rightChild(pos));
                    minHeapify(rightChild(pos));
                }
            }
        }
    }

    public void insert(String key, Float element) {
        Heap[++size] = element;
        HeapKeys[size] = key;
        int current = size;

        while (Heap[current] < Heap[parent(current)]) {
            swap(current, parent(current));
            current = parent(current);
        }
    }

    public void print() {
        for (int i = 1; i <= size / 2; i++) {
            System.out.print(" PARENT : " + Heap[i] + " LEFT CHILD : " + Heap[2 * i]
                    + " RIGHT CHILD :" + Heap[2 * i + 1]);
            System.out.println();
        }
    }

    public void minHeap() {
        for (int pos = (size / 2); pos >= 1; pos--) {
            minHeapify(pos);
        }
    }

    public String remove() {
        Float popped = Heap[FRONT];
        String poppedKey = HeapKeys[FRONT];

        Heap[FRONT] = Heap[size];
        HeapKeys[FRONT] = HeapKeys[size];
        size--;
        minHeapify(FRONT);

        return poppedKey;
    }
}