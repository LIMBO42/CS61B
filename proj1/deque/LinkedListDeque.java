
package deque;

import java.util.Iterator;
import java.util.LinkedList;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {

    private class LinkedNode{
        T item;
        LinkedNode prev;
        LinkedNode next;
        public LinkedNode(){ }
        public LinkedNode(T _item){
            item = _item;
        }
    }

    private int size;
    private LinkedNode head, tail;

    public LinkedListDeque() {
        size = 0;
        head = new LinkedNode();
        tail = new LinkedNode();
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public void addFirst(T item) {
        LinkedNode firstNode = new LinkedNode(item);
        firstNode.next = head.next;
        head.next.prev = firstNode;
        head.next = firstNode;
        firstNode.prev = head;
        size++;
    }

    @Override
    public void addLast(T item) {
        LinkedNode lastNode = new LinkedNode(item);
        lastNode.next = tail;
        lastNode.prev = tail.prev;
        tail.prev.next = lastNode;
        tail.prev = lastNode;
        size++;
    }


    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        LinkedNode p = head.next;
        while(p != tail){
            System.out.print(p.item+" ");
            p = p.next;
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if(size == 0) return null;
        LinkedNode tmp = head.next;
        tmp.next.prev = head;
        head.next = tmp.next;
        size--;
        return tmp.item;
    }

    @Override
    public  T removeLast() {
        if(size == 0) return null;
        LinkedNode tmp = tail.prev;
        tail.prev = tmp.prev;
        tmp.prev.next = tail;
        size--;
        return tmp.item;
    }

    @Override
    public T get(int index) {
        int num = 0;
        LinkedNode p = head.next;
        while(num < index && num < size){
            p = p.next;
            num++;
        }
        if(num == size)
            return null;
        return p.item;
    }

    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {

        private int pSize = 0;

        @Override
        public boolean hasNext() {
            if(pSize < size) return true;
            return false;
        }

        @Override
        public T next() {
            int tmp = pSize;
            pSize++;
            return get(tmp);
        }
    }

    public boolean equals(Object o) {
        if(o == null) return false;
        if(this == o) return true;
        if(o instanceof LinkedListDeque){
            LinkedListDeque p = (LinkedListDeque) o;
            if(p.size != size) return false;
            LinkedNode head1 = p.head.next;
            LinkedNode head2 = head.next;
            while(head1 != p.tail&&head2 != tail){
                if(!head1.item.equals(head2.item)) return false;
                head1 = head1.next;
                head2 = head2.next;
            }
        }else{
            return false;
        }
        return true;
    }

    public T getRecursive(int index) {
        return get(index);
    }
}

