package deque;

import java.util.Iterator;

public class ArrayDeque <T>{
    //数组大小>=16时，使用率要>=25
    private T[] array;
    private int cap;
    private int size;
    private int nextFirst;
    private int nextLast;
    private int first;
    private int last;


    public ArrayDeque(){
        cap = 8;
        size = 0;
        array = (T[]) new Object[cap];
        nextFirst = 0;
        nextLast = 1;
        first = -1;
        last = -1;
    }

    private void resize(){
        if(cap == size){
            cap = cap * 2;
            T[] tmp = (T[]) new Object[cap];
            for(int i = 0; i < size; ++i){
                tmp[i] = array[i];
            }
            array = tmp;
        }else{
            cap = cap / 4 + 1;
            T[] tmp = (T[]) new Object[cap];
            for(int i = 0; i < size; ++i){
                tmp[i] = array[i];
            }
            array = tmp;
        }
    }

    public void addFirst(T item){
        array[nextFirst] = item;
        first = nextFirst;
        if(size == 0){
            last = first;
        }
        size++;
        nextFirst--;

        if(nextFirst < 0){
            nextFirst = cap-1;
        }

        if(size == cap){
            resize();
        }
    }

    public void addLast(T item){
        array[nextLast] = item;
        last = nextLast;
        if(size == 0){
            first = last;
        }
        size++;
        nextLast++;
        if(nextLast == cap && size != cap){
            nextLast = 0;
        }
        if(size == cap){
            resize();
        }
    }

    public boolean isEmpty(){
        return size == 0;
    }

    public int size(){
        return size;
    }

    public void printDeque(){
        for(int i = 0; i < size; ++i){
            System.out.print(array[i]+" ");
        }
        System.out.println();
    }

    public T removeFirst(){
        if(size == 0) return null;
        size--;
        if(cap >= 16 && (double)size/(double)cap <= 0.25){
            resize();
        }
        return array[first];
    }

    public  T removeLast(){
        if(size == 0) return null;
        size--;
        if(cap >= 16 && (double)size/(double)cap <= 0.25){
            resize();
        }
        return array[last];
    }

    public T get(int index){
        if(index < 0 || index >= size) return null;
        return array[index];
    }

    public Iterator<T> iterator(){
        return new ArrayDequeIterator();
    }

    public boolean equals(Object o){
        if(o instanceof ArrayDeque){
            ArrayDeque p = (ArrayDeque) o;
            for(int i = 0; i < size; ++i){
                if(!p.get(i).equals(array[i])){
                    return false;
                }
            }
        }
        return true;
    }

    private class ArrayDequeIterator implements Iterator{

        private int pSize = 0;

        @Override
        public boolean hasNext() {
            if(pSize < size) return true;
            return false;
        }

        @Override
        public Object next() {
            int tmp = pSize;
            pSize++;
            return get(tmp);
        }
    }
}

