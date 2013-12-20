package com.ajjpj.asysmon.servlet.mem;

import java.lang.reflect.Array;
import java.util.Iterator;


/**
 * This is a highly specialized ring buffer implementation custom tailored for holding GC data. It is probably not
 *  fit for use in any other context.
 *
 * @author arno
 */
class RingBuffer<T> implements Iterable<T> {
    /**
     * This is the number of bins allocated in addition to the nominal buffer size to allow room for buffering write
     *  lag when reading.
     */
    private final Class<T> elementClass;
    private final int bufSize;

    private final T[] buffer;

    /**
     * points to the offset with the first (oldest) element, which is also the next one to be overwritten
     */
    private int next = 0;

    private boolean isFull = false;

    RingBuffer(Class<T> cls, int maxSize) {
        elementClass = cls;
        bufSize = maxSize;
        buffer = allocate();
    }

    public synchronized void put(T o) {
        buffer[next] = o;
        next = asBufIndex(next+1);

        if(next == 0) {
            isFull = true;
        }
    }

    @SuppressWarnings("unchecked")
    private T[] allocate() {
        return (T[]) Array.newInstance(elementClass, bufSize);
    }

    private int asBufIndex(int rawIdx) {
        return (rawIdx + bufSize) % bufSize;
    }

    @Override public synchronized Iterator<T> iterator() {
        return new Iterator<T>() {
            final T[] snapshot = allocate();

            final int end = next;
            int curPos;
            boolean hasNext;

            {
                System.arraycopy(buffer, 0, snapshot, 0, bufSize);
                curPos = isFull ? next : 0;
                hasNext = isFull || end != 0;
            }

            @Override public boolean hasNext() {
                return hasNext;
            }

            @Override public T next() {
                if(! hasNext) {
                    throw new IndexOutOfBoundsException();
                }

                final T result = snapshot[curPos];
                curPos = asBufIndex(curPos + 1);
                hasNext = curPos != end;
                return result;
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}