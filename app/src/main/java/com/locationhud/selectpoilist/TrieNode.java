package com.locationhud.selectpoilist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

/**
 * Created by Mark on 05/11/2014.
 */
public class TrieNode {
    char d;
    HashMap<Character, TrieNode> next = new HashMap<Character, TrieNode>();
    ArrayList<String> end = new ArrayList<String>();

    public TrieNode(char d) {
        this.d = Character.toLowerCase(d);
    }

    public static TrieNode getCurrentPosition(TrieNode head, String built) {
        TrieNode ptr = head;
        built = built.toLowerCase();
        for (int i = 0; i < built.length(); i++) {
            char d = built.charAt(i);
            if (ptr.next.get(d) != null) {
                ptr = ptr.next.get(d);
            } else {
                return null;
            }
        }
        return ptr;
    }

    public static TrieNode createTrie(ArrayList<String> list) {
        TrieNode head = new TrieNode('\0');
        for (String element : list) {
            insertString(head, element);
        }
        return head;
    }

    public static ArrayList<String> getStringsWithCurrentPrefix(ArrayList<String>list, TrieNode node) {
        list.addAll(node.end);
        for (TrieNode n : getNextList(node)) {
            list = getStringsWithCurrentPrefix(list, n);
        }
        return list;
    }

    private static ArrayList<TrieNode> getNextList(TrieNode n) {
        ArrayList<TrieNode> list = new ArrayList<TrieNode>();
        Collection<TrieNode> values = n.next.values();
        Iterator<TrieNode> iterator = values.iterator();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    public static void insertString(TrieNode head, String element) {
        TrieNode ptr = head;
        for (int i = 0; i < element.length(); i++) {
            char d = Character.toLowerCase(element.charAt(i));
            if (ptr.next.get(d) == null) {
                TrieNode node = new TrieNode(d);
                ptr.next.put(d, node);
            }
            ptr = ptr.next.get(d);
        }
        ptr.end.add(element);
    }

    public static void deleteString(TrieNode head, String element) {
        TrieNode ptr = head;
        Stack<TrieNode> parents = new Stack<TrieNode>();
        for (int i = 0; i < element.length(); i++) {
            parents.push(ptr);
            TrieNode next = ptr.next.get(element.charAt(i));
            if (next != null) {
                ptr = next;
            } else {
                // already deleted
                return;
            }
        }
        int index = ptr.end.indexOf(element);
        ptr.end.remove(index);
        index = element.length() - 1;
        if (ptr.next.size() == 0) {
            TrieNode lastNode = parents.pop();
            while (!parents.isEmpty() && lastNode.next.size() == 1) {
                lastNode = parents.pop();
                index--;
            }
            lastNode.next.remove(element.charAt(index));
        }
    }
}
