package com.nationalgalleryroutefinder.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MyArrayListTest {

    private MyArrayList<String> list;

    @BeforeEach
    void setUp() {
        list = new MyArrayList<>();
    }

    @Test
    void testAddAndGetSize() {
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        list.add("A");
        list.add("B");
        assertEquals(2, list.size());
        assertFalse(list.isEmpty());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(100));
    }

    @Test
    void testAddAll() {
        List<String> other = new ArrayList<>();
        other.add("A");
        other.add("B");
        list.addAll(other);
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
    }

    @Test
    void testAddAll2() {
        MyArrayList<String> other = new MyArrayList<>();
        other.add("A");
        other.add("B");
        list.addAll(other);
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
    }

    @Test
    void testSet() {
        list.add("A");
        list.add("B");
        String old = list.set(1, "C");
        assertEquals("B", old);
        assertEquals("C", list.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.set(-1, "X"));
        assertThrows(IndexOutOfBoundsException.class, () -> list.set(2, "X"));
    }

    @Test
    void testRemove() {
        list.add("A");
        list.add("B");
        list.add("C");
        String removed = list.remove(1);
        assertEquals("B", removed);
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("C", list.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(100));
        assertTrue(list.remove("A"));
        assertEquals(1, list.size());
        assertEquals("C", list.get(0));
        assertFalse(list.remove("X"));
    }

    @Test
    void testClear() {
        list.add("A");
        list.add("B");
        assertEquals(2, list.size());
        list.clear();
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }

    @Test
    void testIterator() {
        list.add("A");
        list.add("B");
        Iterator<String> it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        assertFalse(it.hasNext());
    }
}