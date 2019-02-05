package cs.art.ia.model;

import java.util.ArrayList;

public class NonTerminal extends Node {
    public NonTerminal(String data, ArrayList<Node> children, Node parent) {
        super(data, children, parent);
    }
}